package com.github.tukcps.sysmd.rest

import androidx.compose.runtime.mutableStateOf
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.datamodel.IdentifiedImplementation
import com.github.tukcps.sysmd.model.datamodel.createFrom
import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.rest.entities.api.entities.*
import com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.BranchRequest
import com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.CommitRequest
import com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.DataVersionRequest
import com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.ProjectRequest
import com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.commitData.ElementCommitData
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.BranchResponse
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.CommitResponse
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.ElementResponse
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.ProjectResponse
import com.github.tukcps.sysmd.rest.entities.api.services.ChangeType
import com.github.tukcps.sysmd.rest.entities.api.services.SysMLv2Services
import com.github.tukcps.sysmd.rest.entities.interchange.InterchangeProject
import com.github.tukcps.sysmd.services.repositories.local.ProjectUsageData
import com.github.tukcps.sysmd.settings
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileWriter
import kotlin.uuid.Uuid


/**
 * An object that handles communication with the backend project repository via REST.
 */
object RESTRepository: SysMLv2Services {
    private val objectMapper = ObjectMapper()
    private var writer:ObjectWriter

    private val password
        get() = settings.rest.password
    private val username
        get() = settings.rest.username

    private const val USER_KEY = "email"
    private const val PASSWORD_KEY = "password"

    val onlineState = mutableStateOf(false)
    private val filesAreAvailable = mutableStateOf(false)
    val saveElementsLocally = mutableStateOf(false)
    private val projectsLoaded= hashMapOf<String, Triple<InterchangeProject, MutableList<ElementDataIF>, MutableList<ProjectUsageData>>>()
    override fun createBranch(project: Project, branchName: String, head: Commit): Branch {
        TODO("Not yet implemented")
    }

    override fun createCommit(
        change: Collection<DataVersion>,
        branch: Branch?,
        previousCommits: Collection<Commit>,
        project: Project
    ): Commit {
        TODO("Not yet implemented")
    }

    override fun createProject(name: String?, description: String?, defaultBranch: Branch?): Project {
        TODO("Not yet implemented")
    }

    /**
     * ID of the session working with the REST API;
     * Must be set by the Session Manager at client side prior first use of the backend.
     */
    var internalSessionId: Uuid = Uuid.random()
    var serverSessionId:Uuid? = null

    private var projectsState: MutableList<ProjectImplementation> = mutableListOf()

    init {
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(com.github.tukcps.sysmd.configuration.JacksonKotlinUuidConfig.createModule())
        writer = objectMapper.writer().withDefaultPrettyPrinter()
        loadDataFromCache()
    }

    private fun saveCurrentDataInCache(){
        if(saveElementsLocally.value) {
            for (project in projectsState){
                if(!File(System.getProperty("user.home") + "/SysMD/Cache/Backend", project.name!!).exists())
                    File(System.getProperty("user.home") + "/SysMD/Cache/Backend", project.name!!).mkdir()

                val data = File(System.getProperty("user.home") + "/SysMD/Cache/Backend/${project.name}", project.name+".json")
                data.createNewFile()
                val myWriter = FileWriter(data)
                myWriter.write(writer.writeValueAsString(project))
                myWriter.close()

                for(commit in project.commitsIdList()) {
                    val commitFile = File(
                        System.getProperty("user.home") + "/SysMD/Cache/Backend/${project.name}",
                        "$commit.md"
                    )
                    commitFile.createNewFile()
                    val myCommitWriter = FileWriter(commitFile)
                    //myCommitWriter.write(toMarkdownString(commit))
                    myCommitWriter.close()
                }
            }
        }
    }

    fun loadDataFromCache(){
        if(saveElementsLocally.value) {
            if (!File(System.getProperty("user.home") + "/SysMD/Cache/Backend").exists()) {
                if (!File(System.getProperty("user.home") + "/SysMD/Cache").exists())
                    !File(System.getProperty("user.home") + "/SysMD/", "Cache").mkdir()
                File(System.getProperty("user.home") + "/SysMD/Cache/", "Backend").mkdir()
            }else {
                if(!File(System.getProperty("user.home") + "/SysMD/Cache/Backend").listFiles().isNullOrEmpty()) {
                    filesAreAvailable.value = false
                    val fileList = File(System.getProperty("user.home") + "/SysMD/Cache/Backend").listFiles()
                    if(!fileList.isNullOrEmpty()) {
                        for (file in File(System.getProperty("user.home") + "/SysMD/Cache/Backend").listFiles()!!) {
                            filesAreAvailable.value =
                                filesAreAvailable.value || (file.isDirectory && (!file.listFiles().isNullOrEmpty()))
                            projectsState.add(objectMapper.readValue<ProjectImplementation>(File(file.toString(),file.name + ".json")))
                        }

                    }
                }
            }
        }
    }

    /**
     * @param projectId projectId of the project
     * @return the Project
     */
    override fun getProjectById(projectId: Uuid): ProjectImplementation {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
        val response = Rest.get("/projects/${projectId}", null)
        val responseObject = objectMapper.readValue(response.body, ProjectResponse::class.java)
        return ProjectImplementation(responseObject)
    }

    override fun updateProject(projectId: Uuid, name: String?, description: String?, defaultBranch: Branch?): Project { TODO("Not yet implemented") }

    /**
     * Gets a list of projects.
     */
    override fun getProjects(): MutableList<ProjectImplementation> =
        try {
            serverSessionId = postSession()
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val response = Rest.get("/libraries", null)

            logger.info(response)

            when (response.statusCode.value()) {
                200 -> { onlineState.value=true }
                201 -> { onlineState.value=true }
                400 -> onlineState.value=false
                403 -> onlineState.value=false
            }

            val projectResponses = objectMapper.readValue<MutableList<ProjectResponse>>(response.body!!)
            projectsState = projectResponses.map { ProjectImplementation(it) }.toMutableList()
            projectsState.forEach {
                it.branches = getBranches(it).toMutableList()
                it.commits = getCommit(it).toMutableList()
            }
            projectsState
        } catch (io: Exception) {
            logger.info("No projects from web: ${io.message}")
            projectsState
        }

    override fun getRelationshipsByRelatedElement(
        project: Project,
        commit: Commit,
        elementId: Uuid,
        direction: String
    ): Collection<ElementDataIF> {
        TODO("Not yet implemented")
    }


    override fun getCommit(project: Project): Collection<Commit> {
        if (onlineState.value) {
            try {
                Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
                val commitsResponse = Rest.get("/projects/${project.id}/commits", null)
                val commits = commitsResponse.body

                logger.info(commits)

                val responses = objectMapper.readValue<MutableList<CommitResponse>>(commits!!)
                return responses.map { CommitImplementation(it) }
            } catch (io: Exception) {
                logger.error(io.stackTraceToString())
            }
        } else {
            for (it in projectsState) {
                if (it.id == project.id) {
                    @Suppress("UNCHECKED_CAST")
                    return it.commits as Collection<CommitImplementation>
                }
            }
        }
        return mutableListOf()    }


    override fun getCommitById(project: Project, commitId: Uuid): CommitImplementation {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
        val commitResponse = Rest.get("/projects/${project.id}/commits/$commitId", internalSessionId.toString())
        val commit = objectMapper.readValue(commitResponse.body, CommitResponse().javaClass)
        return CommitImplementation(commit)
    }

    override fun getCommitChange(
        project: Project,
        commit: Commit,
        changeTypes: Collection<ChangeType>
    ): Collection<DataVersion> {
        TODO("Not yet implemented")
    }

    override fun getCommitChangeById(project: Project, commit: Commit, changeId: Uuid): DataVersion {
        TODO("Not yet implemented")
    }

    override fun getDefaultBranch(project: Project): Branch {
        TODO("Not yet implemented")
    }


    fun postBranch(project: Project, name: String?, headOfBranch:Uuid): Branch? {
        var branch : Branch? = null
        val branchId : String

        try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val branchReq= BranchRequest(name = name, head = IdentifiedImplementation(headOfBranch))

            val postProjectResponse = Rest.post("/projects/${project.id}/branches", writer.writeValueAsString(branchReq), null)

            val resultBody: String = postProjectResponse.body ?: throw Exception("empty result body")

            when (postProjectResponse.statusCode.value()) {
                200 -> {
                    branchId = Rest.extractEntityIdFromBody(resultBody).toString()
                    branch = getBranchById(project, Uuid.parse(branchId))
                }
                201 -> {
                    branchId = Rest.extractEntityIdFromBody(resultBody).toString()
                    branch = getBranchById(project, Uuid.parse(branchId))

                    // Update the projectState variable with a new list of projects
                    val newProjectsState= getProjects()
                    projectsState = newProjectsState
                }
                400 -> logger.error(postProjectResponse.statusCode.value().toString() + "---" + resultBody)
                403 -> logger.error(postProjectResponse.statusCode.value().toString() + "---" + resultBody)
            }
            return branch

        } catch (io: Exception) {
            logger.error(io.stackTraceToString())
            return branch
        }
    }

    /**
     * Get a branch by its id.
     * @param project the project
     * @param branchId id of the branch
     */
    override fun getBranchById(project: Project, branchId: Uuid) : Branch? {
        if(onlineState.value) {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val branchResponse = Rest.get("/projects/${project.id}/branches/${branchId}", null)
            val branch = objectMapper.readValue(branchResponse.body!!, BranchResponse(id = Uuid.random()).javaClass)
            return BranchImplementation(branch)
        } else {
            for (local in projectsState) {
                if (local.id == project.id){
                    for (branch in local.branches) {
                        if (branch.id==branchId)
                            return branch
                    }
                }
            }
        }
        return null
    }

    override fun deleteBranch(project: Project, branchId: Uuid): Branch? {
        try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            // TODO(Check the Value of payload?)

            val payload ="""
                {
              "@id": "$branchId"
                }
            """.trimIndent()

            val response = if(serverSessionId!=null)
                Rest.delete("/projects/${project.id}/branches/$branchId", payload, serverSessionId.toString())
            else
                Rest.delete("/projects/${project.id}/branches/$branchId", payload, internalSessionId.toString())

            when (response.statusCode.value()) {
                200 -> {  }
                400 -> logger.info("---" + response.statusCode.value() + "---" + response.body)
                401 -> logger.info("---" + response.statusCode.value() + "---" + response.body)
                403 -> logger.error("---" + response.statusCode.value() + "---" + response.body)
                500 -> logger.error("---" + response.statusCode.value() + "---" + response.body)
            }
            return null

        } catch (io: Exception) {
            logger.error(io.stackTraceToString())
            throw Exception("No project with the id: $project")
        }
    }

    override fun getProjectUsage(project: Project, commit: Commit): Collection<ProjectUsage> {
        TODO("Not yet implemented")
    }

    override fun deleteProjectUsage(project: Project, branch: Branch?, projectUsageId: Uuid): Commit {
        TODO("Not yet implemented")
    }

    override fun deleteTag(project: Project, tagId: Uuid): Tag? {
        TODO("Not yet implemented")
    }


    override fun getBranches(project: Project): Collection<BranchImplementation> {
        if(onlineState.value) {
            return try {
                Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
                val branchResponse = Rest.get("/projects/${project.id}/branches", null)
                val branchList = objectMapper.readValue<List<BranchResponse>>(branchResponse.body!!)
                return branchList.map { BranchImplementation(it) }
            } catch (io: Exception) {
                mutableListOf()
            }
        }else {
            projectsState.forEach{
                if(it.id==project.id)
                    return it.branches
            }
            return mutableListOf()
        }
    }

    override fun getElements(project: Project, commit: Commit?): MutableList<ElementDataIF> {
        if (onlineState.value) {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

            val elementResponse = Rest.get("/projects/${project.id}/commits/${commit?.id}/elements", null)

            logger.info(elementResponse)

            if (elementResponse.statusCode.value() == 200) {
                val elements = objectMapper.readValue<MutableList<ElementResponse>>(elementResponse.body!!)
                val returnValue = mutableListOf<ElementDataIF>()

                for (element in elements)
                    returnValue.add(element.createFrom())

                // TODO: Get list of usages!!
                commit?.id?.let { projectsLoaded.put(it.toString(), Triple(InterchangeProject(name=""), returnValue, mutableListOf())) }

                return returnValue
            } else
                throw Exception("Elements could not be downloaded")
        } else if (filesAreAvailable.value){
            /*
            if(projectsLoaded[commitId.toString()]!=null) {
                return projectsLoaded[commitId.toString()] !!
            }

            var searchedProject:Project?=null
            for(project in projectsState)
                if(project.id==projectId)
                    searchedProject = project

            if(searchedProject!=null) {
                var commitIsAvailable = false

                for(commit in searchedProject.commits)
                    if(commit.id == commitId)
                        commitIsAvailable=true

                if(commitIsAvailable) {
                    logger.error("Loading: ${searchedProject.name} Commit: $commitId")
                    val model = RESTSessionImpl()
                    val projectCache = File(System.getProperty("user.home") + "/SysMD/Cache/Backend/${searchedProject.name}/${commitId}.md")
                    try {
                        model.loadDataFromFile(projectCache)
                    } catch (error: Exception) {
                        model.report(error)
                    }
                    if (model.status.errors.size > 0)
                        logger.error(model.status.errors)

                    projectsLoaded[commitId.toString()] = model.export()

                    return projectsLoaded[commitId.toString()]!!
                }
            }*/
        }
        return mutableListOf()
    }

    override fun getRootElements(project: Project, commit: Commit): Collection<ElementDataIF> { TODO("Not yet implemented") }
    override fun getTagById(project: Project, tagId: Uuid): Tag { TODO("Not yet implemented") }
    override fun getTaggedCommit(project: Project, tag: Tag): Commit { TODO("Not yet implemented") }
    override fun getTags(project: Project): Collection<Tag> { TODO("Not yet implemented") }
    override fun setDefaultBranch(project: Project, branchId: Uuid): Project { TODO("Not yet implemented") }

    override fun getHeadCommit(project: Project, branch: Branch?): Commit {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
        val branchResponse = Rest.get("/projects/${project.id}/branches", null)
        val branchList = objectMapper.readValue<List<BranchResponse>>(branchResponse.body!!)
        val br = branchList.first { it.id == branch?.id }
        val commit = getCommitById(project, br.head!!.id!!)
        return commit
    }


    override fun createProjectUsage(project: Project, branch: Branch?, projectUsage: ProjectUsage): ProjectUsage { TODO("Not yet implemented") }
    override fun createTag(project: Project, tagName: String, taggedCommit: Commit): Tag { TODO("Not yet implemented") }


    /** For later use ?
     * @param project project
     * @param commit the commit
     * @param elementId ID of the element
     * @return An element by project, commit and its ID
     */
    override fun getElementById(project: Project, commit: Commit?, elementId: Uuid): ElementDataIF {
        if(onlineState.value) {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val elementResponse = Rest.get("/projects/${project.id}/commits/${commit!!.id}/elements/$elementId", null)

            val element = objectMapper.readValue<ElementResponse>(elementResponse.body?:"")
            return element.createFrom()
        } else {
            if((saveElementsLocally.value)&&(projectsLoaded[commit?.id.toString()]!=null)) {
                for(element in projectsLoaded[commit?.id?.toString()]!!.second) {
                    if(element.elementId == elementId)
                        return element
                }
            }
        }
        return ElementData(elementId = Uuid.random(), type = ElementType.Element)
    }

    fun getCellsForUi(project: Project, commit: Commit): MutableList<Uuid> {

        val cellList : MutableList<Uuid> = mutableListOf()
        val elementsDAO = getElements(project, commit)

        for (elementDAO in elementsDAO){
            if (elementDAO.type == ElementType.TextualRepresentation){
                cellList.add(elementDAO.elementId)
                for (elementUuid in elementDAO.ownedElement) {
                    cellList.add(elementUuid.id!!)
                }
            }
        }
        return cellList
    }

    @Suppress("unused", "unused_parameter")
    fun getRelationships(projectId: Uuid, commitId: Uuid? = null): MutableList<Relationship> {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
        TODO()
    }

    /**
     * @param projectName name of the project
     * @param description description of the project
     * @return The project created
     */
    fun postProject(projectName: String, description: String?, defaultBranchName:String = "Main"): ProjectImplementation? {
        var project : ProjectImplementation? = null
        val projectId : String

        try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val projectReq =
                ProjectRequest(name = projectName, description = description, defaultBranchName = defaultBranchName)

            val postProjectResponse = Rest.post("/libraries/", writer.writeValueAsString(projectReq), null)

            val resultBody: String = postProjectResponse.body ?: throw Exception("empty result body")

            when (postProjectResponse.statusCode.value()) {
                200 -> {
//                    logger.error("--- " + postProjectResponse.statusCode.value() + " ---" + resultBody)
                    projectId = Rest.extractEntityIdFromBody(resultBody).toString()
                    project = getProjectById(Uuid.parse(projectId))
                }
                201 -> {
//                    logger.error("---" + postProjectResponse.statusCode.value() + " ---" + resultBody)
                    projectId = Rest.extractEntityIdFromBody(resultBody).toString()
                    project = getProjectById(Uuid.parse(projectId))

                    // Update the projectState variable with a new list of projects
                    val newProjectsState= getProjects()
                    projectsState = newProjectsState
                }
                400 -> logger.info("---" + postProjectResponse.statusCode.value() + "---" + resultBody)
                403 -> logger.error("---" + postProjectResponse.statusCode.value() + "---" + resultBody)
            }
            return project

        } catch (io: Exception) {
            throw Exception("No project: $projectName")
        }
    }

    /** For the moment just Used in Test to deleted posted projects in test
     * @param projectId
     * @return true or false, if the deletion was successful or not
     */
    override fun deleteProject(projectId: Uuid): Project? {
        try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

            val payload ="""
                {
              "@id": "$projectId"
                }
            """.trimIndent()

            val response = Rest.delete("/projects/$projectId", payload, null)

            when (response.statusCode.value()) {
                200 -> { }
                400 -> logger.error("" + response.statusCode.value() + "---" + response.body)
                401 -> logger.error("" + response.statusCode.value() + "---" + response.body)
                403 -> logger.error("" + response.statusCode.value() + "---" + response.body)
                500 -> logger.error("" + response.statusCode.value() + "---" + response.body)
            }
            return null // Returns deleted project - fix

        } catch (io: Exception) {
            throw Exception("No project with the id: $projectId")
        }
    }

    /**
     * @param commitName name of the commit
     * @param commitDescription description of the commit
     * @param  project
     * @param  elementsDAOList MutableList<ElementDAO>?
     * @return the created commit or null
     */
    fun postCommit(
        commitName: String,
        commitDescription: String,
        project: Project,
        elementsDAOList: MutableList<ElementDataIF>? = mutableListOf(),
        branchId: Uuid?
    ): Commit {

        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

        val changeList = arrayListOf<DataVersionRequest>()

        if (elementsDAOList != null) {
            if (elementsDAOList.isNotEmpty()) {
                for (element in elementsDAOList) {
                    val commitData = ElementCommitData(
                        id = element.elementId,
                        type = element.type,
                        declaredName = element.declaredName,
                        declaredShortName = element.declaredShortName,
                        ownedElement = element.ownedElement.map { IdentifiedImplementation(id = it.id) }.toMutableList(),
                        owner = IdentifiedImplementation(id = element.owner?.id),
                        language = element.language,
                        aliasIds = TODO(),
                        elementId = TODO(),
                        isImpliedIncluded = TODO(),
                        ownedAnnotation = TODO(),
                        ownedRelationship = TODO(),
                        owningMembership = TODO(),
                        owningNamespace = TODO(),
                        owningRelationship = TODO(),
                        qualifiedName = TODO()
                    )
                    commitData.body = element.body
                    val dataVersionRequest = DataVersionRequest(payload = commitData)
                    changeList.add(dataVersionRequest)
                }
            }
        }

        val commitRequest = CommitRequest(description = commitDescription, change = changeList)

        val dataString = writer.writeValueAsString(commitRequest)

        val postCommitResponse = if(branchId!=null) Rest.post("/projects/${project.id}/commits?branchId=$branchId", dataString, sessionId = null)
                                 else Rest.post("/projects/${project.id}/commits", dataString, sessionId = null)

        val resultBody: String = postCommitResponse.body ?: throw Exception("empty result body")
        val commitId : Uuid?
        var commit : Commit? = null

        when (postCommitResponse.statusCode.value()) {
            200 -> {
                commitId = Uuid.parse(Rest.extractEntityIdFromBody(resultBody)!!)
                commit = this.getCommitById(project, commitId)
            }
            201 -> {
                commitId = Uuid.parse(Rest.extractEntityIdFromBody(resultBody)!!)
                commit = this.getCommitById(project, commitId)

            }
            400 -> logger.error("---" + postCommitResponse.statusCode.value() + "---" + resultBody)
            403 -> logger.error("---" + postCommitResponse.statusCode.value() + "---" + resultBody)
        }

        if (commit == null){
            throw Exception("Commit Unsuccessful: $commitName")
        }

        return commit
    }

    /**
     * Implements method from interface
     */
    fun getElements(projectName: String): List<ElementDataIF> { TODO() }


    fun postSession() : Uuid? {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

        val postCommitResponse = Rest.post("/sessions", null, null)
        val resultBody: String = postCommitResponse.body ?: throw Exception("empty result body")

        print(resultBody)

        when (postCommitResponse.statusCode.value()) {
            200 -> {
//                logger.error("--- " + postCommitResponse.statusCode.value() + " ---" + resultBody)
//                commitId = Uuid.fromString(Rest.extractEntityIdFromBody(resultBody))
//                commit = getCommit(projectId, commitId)
            }
            201 -> {
//              logger.error("---" + postCommitResponse.statusCode.value() + " ---" + resultBody)
                val uuidString = Rest.extractEntityUuidFromBody(resultBody)
                if(uuidString!=null) return Uuid.parse(uuidString)


//                commitId = Uuid.fromString(Rest.extractEntityIdFromBody(resultBody))
//                commit = getCommit(projectId, commitId)
//
//                if (elementsDAOList != null) {
//                    for(elementDAO in elementsDAOList){
//                        commit.change.add(DataVersion( payload = elementDAO ))
//                    }
//                }
//                //add the new commit to the ProjectCommitIds List
//                getProjectById(projectId).commitIDs.add(commitId)
            }
            400 -> logger.error("---" + postCommitResponse.statusCode.value() + "---" + resultBody)
            403 -> logger.error("---" + postCommitResponse.statusCode.value() + "---" + resultBody)
        }
        return null
    }


    @Suppress("UNUSED_PARAMETER")
    fun branch(projectId: Uuid, branchName: String, headCommit: Commit): Uuid {
        TODO("Not yet implemented")
    }


    fun deleteSession() {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

        val postCommitResponse = Rest.delete("/sessions", "", serverSessionId.toString())
        val resultBody: String = postCommitResponse.body ?: throw Exception("empty result body")

        print(resultBody)
        saveCurrentDataInCache()
    }

    @Suppress("UNUSED_PARAMETER")
    fun postMerge(projectId: Uuid,mainBranch:Uuid,mutableList: MutableList<Uuid>){

    }

    private val logger = LogManager.getLogger(RESTRepository::class.java)
}
