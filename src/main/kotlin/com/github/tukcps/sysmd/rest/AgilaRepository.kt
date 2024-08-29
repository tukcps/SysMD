package com.github.tukcps.sysmd.rest

import androidx.compose.runtime.mutableStateOf
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.services.repositories.local.ProjectUsageData
import com.github.tukcps.sysmd.rest.entities.BranchImplementation
import com.github.tukcps.sysmd.rest.entities.ProjectImplementation
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.repositories.local.toElementData
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmlv2.api.SysMLv2Services
import com.github.tukcps.sysmlv2.entities.*
import com.github.tukcps.sysmlv2.entities.requestModels.*
import com.github.tukcps.sysmlv2.entities.requestModels.commitData.ElementCommitData
import com.github.tukcps.sysmlv2.entities.responseModels.BranchResponse
import com.github.tukcps.sysmlv2.entities.responseModels.CommitResponse
import com.github.tukcps.sysmlv2.entities.responseModels.ElementResponse
import com.github.tukcps.sysmlv2.entities.responseModels.ProjectResponse
import java.io.File
import java.io.FileWriter
import java.util.*


/**
 * An object that handle communication with the backend project repository via REST.
 */
object AgilaRepository: SysMLv2Services {
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
    private val projectsLoaded= hashMapOf<String, Triple<InterchangeProject, MutableList<ElementDAO>, MutableList<ProjectUsageData>>>()

    override fun createProject(project: Project): Project {
        TODO("Not yet implemented")
    }
    /**
     * Id of the session working with the REST API;
     * Must be set by the Session Manager at client side prior first use of the Agila backend.
     */
    var internalSessionId: UUID = UUID.randomUUID()
    var serverSessionId:UUID? = null

    var projectsState: MutableList<ProjectImplementation> = mutableListOf()

    init {
        objectMapper.registerModule(JavaTimeModule())
        writer = objectMapper.writer().withDefaultPrettyPrinter()
        loadDataFromCache()
    }

    private fun saveCurrentDataInCache(){
        if(saveElementsLocally.value) {
            for (project in projectsState){
                if(!File(System.getProperty("user.home") + "/SysMD/Cache/Backend",project.name).exists())
                    File(System.getProperty("user.home") + "/SysMD/Cache/Backend",project.name).mkdir()

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

/*
    private fun toMarkdownString(commitId: UUID): String {
        val str = ""
        /*
                if(projectsLoaded[commitId.toString()].isNullOrEmpty())
                    return str

                for (elemDao in projectsLoaded[commitId.toString()]!!) {
                    // The lines of the description section.
                    val e = elemDao.toElementRepositoryDAO()
                    var languageStr = ""
                    if(e.language!=null)
                        languageStr = e.language !!

                    if (languageStr.isNotBlank())
                        str += "```$languageStr\n"
                    str += (e.body?.trimEnd('\n') ?: "") + "\n"
                    if (languageStr.isNotBlank())
                        str += "```\n"
                }*/
        return str
    }
*/

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
     * @param id projectId of the project
     * @return the Project
     */
    override fun getProjectById(id: UUID): ProjectImplementation {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
        val response = Rest.get("/projects/${id}", null)
//            if(serverSessionId!=null)
//                Rest.get("/projects/${projectId.toString()}", serverSessionId.toString())
//            else
//                Rest.get("/projects/${projectId.toString()}", internalSessionId.toString())

        val responseObject = objectMapper.readValue(response.body, ProjectResponse::class.java)
        return ProjectImplementation(responseObject)
    }

    override fun updateProject(project: Project) {
        TODO("Not yet implemented")
    }

    /**
     * Gets a list of projects.
     */
    override fun getProjects(): MutableList<ProjectImplementation> =
        try {
            serverSessionId = postSession()
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val response = Rest.get("/projects", null)

            println(response)

            when (response.statusCode.value()) {
                200 -> { onlineState.value=true }
                201 -> { onlineState.value=true }
                400 -> onlineState.value=false
                403 -> onlineState.value=false
            }

            val projectResponses = objectMapper.readValue<MutableList<ProjectResponse>>(response.body!!)
            projectsState = projectResponses.map { ProjectImplementation(it) }.toMutableList()
            projectsState
        } catch (io: Exception) {
            println("No projects: ${io.message}") // "Exception: " + io.stackTraceToString())
            projectsState
        }


    /**
     * Returns all the commits of a project
     */
    override fun getCommits(projectId: UUID): Collection<CommitImplementation> {
        if (onlineState.value) {
            try {
                Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
                val commitsResponse = Rest.get("/projects/$projectId/commits", null)
                val commits = commitsResponse.body
                val responses = objectMapper.readValue<MutableList<CommitResponse>>(commits!!)
                return responses.map { CommitImplementation(it) }
            } catch (io: Exception) {
                println(io.stackTraceToString())
            }
        } else {
            for (project in projectsState) {
                if (project.id == projectId) {
                    @Suppress("UNCHECKED_CAST")
                    return project.commits as Collection<CommitImplementation>
                }
            }
        }
        return mutableListOf()
    }


    override fun getCommitById(projectId: UUID, commitId: UUID?): CommitImplementation {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

        var commitId2 = commitId
        if (commitId2 == null) {
            val projectResponse = Rest.get("/projects/$projectId", null)
            val project = objectMapper.readValue<ProjectResponse>(projectResponse.body!!)
            val head = getHeadCommit(projectId, project.defaultBranch!!.id)
            commitId2 = head.id
        }
        val commitResponse = Rest.get("/projects/$projectId/commits/$commitId2", internalSessionId.toString())
        val commit = objectMapper.readValue(commitResponse.body, CommitResponse().javaClass)
        return CommitImplementation(commit)
    }


    fun postBranch(projectID:UUID, name: String?, headOfBranch:UUID): Branch? {
        var branch : Branch? = null
        val branchId : String

        try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val branchReq= BranchRequest(name=name, head = Identified(headOfBranch))

            val postProjectResponse = Rest.post("/projects/${projectID}/branches", writer.writeValueAsString(branchReq), null)

            val resultBody: String = postProjectResponse.body ?: throw Exception("empty result body")

            when (postProjectResponse.statusCode.value()) {
                200 -> {
                    branchId = Rest.extractEntityIdFromBody(resultBody).toString()
                    branch = getBranchById(projectID,UUID.fromString(branchId))
                }
                201 -> {
                    branchId = Rest.extractEntityIdFromBody(resultBody).toString()
                    branch = getBranchById(projectID,UUID.fromString(branchId))

                    // Update the projectState variable with a new list of projects
                    val newProjectsState= getProjects()
                    projectsState = newProjectsState
                }
                400 -> println("---" + postProjectResponse.statusCode.value() + "---" + resultBody)
                403 -> println("---" + postProjectResponse.statusCode.value() + "---" + resultBody)
            }
            return branch

        } catch (io: Exception) {
            println(io.stackTraceToString())
            return branch
        }
    }

    /**
     * Get a branch by its id.
     * @param projectId id of the project
     * @param branchId id of the branch; null for default branch
     */
    override fun getBranchById(projectId: UUID, branchId:UUID?) : BranchImplementation {
        if(onlineState.value) {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val branchResponse = Rest.get("/projects/${projectId}/branches/${branchId}", null)
            val branch = objectMapper.readValue(branchResponse.body!!, BranchResponse(id=UUID.randomUUID()).javaClass)
            return BranchImplementation(branch)
        } else {
            for (project in projectsState) {
                if (project.id == projectId){
                    if (branchId == null)
                        return getBranchById(projectId, project.defaultBranchId)
                    for (branch in project.branches) {
                        if (branch.id==branchId)
                            return branch
                    }
                }
            }
        }
        throw Exception("Branch not found")
    }

    override fun deleteBranch(projectId: UUID, branchId: UUID): Boolean {
        var deleted = false

        try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            // TODO(Check the Value of payload?)

            val payload ="""
                {
              "@id": "$branchId"
                }
            """.trimIndent()

            val response = if(serverSessionId!=null)
                Rest.delete("/projects/$projectId/branches/$branchId", payload, serverSessionId.toString())
            else
                Rest.delete("/projects/$projectId/branches/$branchId", payload, internalSessionId.toString())

            when (response.statusCode.value()) {
                200 -> { deleted = true }
                400 -> println("---" + response.statusCode.value() + "---" + response.body)
                401 -> println("---" + response.statusCode.value() + "---" + response.body)
                403 -> println("---" + response.statusCode.value() + "---" + response.body)
                500 -> println("---" + response.statusCode.value() + "---" + response.body)
            }
            return deleted

        } catch (io: Exception) {
            io.printStackTrace()
            throw Exception("No project with the id: $projectId")
        }
    }

    override fun getTags(projectId: UUID): Collection<Tag> {
        TODO("Not yet implemented")
    }

    override fun getTagById(projectId: UUID, tagId: UUID): Tag {
        TODO("Not yet implemented")
    }

    override fun getExternalRelationships() {
        TODO("Not yet implemented")
    }

    override fun getProjectUsage(projectId: UUID, commitId: UUID): Collection<ProjectUsage> {
        TODO("Not yet implemented")
    }

    override fun deleteProjectUsage(projectId: UUID, commitId: UUID?): Boolean {
        TODO("Not yet implemented")
    }


    override fun getBranches(projectId: UUID): Collection<BranchImplementation> {
        if(onlineState.value) {
            return try {
                Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
                val branchResponse = Rest.get("/projects/${projectId}/branches", null)
                val branchList = objectMapper.readValue<List<BranchResponse>>(branchResponse.body!!)
                return branchList.map { BranchImplementation(it) }
            } catch (io: Exception) {
                mutableListOf()
            }
        }else {
            projectsState.forEach{
                if(it.id==projectId)
                    return it.branches
            }
            return mutableListOf()
        }
    }

    override fun getElements(projectId: UUID, commitId: UUID?): MutableList<ElementDAO> {
        if (onlineState.value) {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

            val elementResponse = Rest.get("/projects/$projectId/commits/$commitId/elements", null)

            println(elementResponse)

            if (elementResponse.statusCode.value() == 200) {
                val elements = objectMapper.readValue<MutableList<ElementResponse>>(elementResponse.body!!)
                val returnValue = mutableListOf<ElementDAO>()

                for (element in elements)
                    returnValue.add(element.toElementData())

                // TODO: Get list of usages!!
                commitId?.let { projectsLoaded.put(it.toString(), Triple(InterchangeProject(id=it, name=""), returnValue, mutableListOf())) }

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
                    println("Loading: ${searchedProject.name} Commit: $commitId")
                    val model = AgilaSessionImpl()
                    val projectCache = File(System.getProperty("user.home") + "/SysMD/Cache/Backend/${searchedProject.name}/${commitId}.md")
                    try {
                        model.loadDataFromFile(projectCache)
                    } catch (error: Exception) {
                        model.report(error)
                    }
                    if (model.status.errors.size > 0)
                        println(model.status.errors)

                    projectsLoaded[commitId.toString()] = model.export()

                    return projectsLoaded[commitId.toString()]!!
                }
            }*/
        }
        return mutableListOf()
    }

    override fun getRelationshipsByRelatedElement(
        projectId: UUID,
        commitId: UUID,
        relatedElementId: UUID,
        direction: String
    ): List<ElementDAO> {
        TODO("Not yet implemented")
    }

    override fun getRootElements(projectId: UUID, commitId: UUID?): List<ElementDAO> {
        TODO("Not yet implemented")
    }

    override fun getHeadCommit(projectId: UUID, branchId: UUID?): Commit {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
        val branchResponse = Rest.get("/projects/${projectId}/branches", null)
        val branchList = objectMapper.readValue<List<BranchResponse>>(branchResponse.body!!)
        val branch = branchList.first { it.id == branchId }
        val commit = getCommitById(projectId, branch.head!!.id)
        return commit
    }

    override fun getProjectUsage(projectName: String): MutableList<ProjectUsageData> {
        TODO("Not yet implemented")
        // Get Project Usage elements from respective project commit
    }

    override fun getProjectByName(projectName: String): Project {
        val projects = getProjects().filter { it.name == projectName }
        if (projects.size > 1) {
            println("Warning: more than one project with name '$projectName' in repository. Using first one.")
        }
        if (projects.isNotEmpty())
            return projects.first()
        else
            throw SysMDError("Project '$projectName' not found")
    }

    override fun createCommit(projectId: UUID, commit: Commit): Commit {
        TODO("Not yet implemented")
    }

    override fun getCommitChange() {
        TODO("Not yet implemented")
    }

    override fun getCommitChangeById() {
        TODO("Not yet implemented")
    }

    override fun getDefaultBranch(projectId: UUID): UUID {
        TODO("Not yet implemented")
    }

    override fun setDefaultBranch(projectId: UUID, branchID: UUID) {
        TODO("Not yet implemented")
    }

    override fun createBranch(projectId: UUID, branch: Branch) {
        TODO("Not yet implemented")
    }

    /** For later use ?
     * @param projectId ID of the project
     * @param commitId ID of the commit
     * @param elementId ID of the element
     * @return An element by project, commit and its Id
     */
    override fun getElementById(projectId: UUID, commitId: UUID, elementId: UUID): ElementDAO {
        if(onlineState.value) {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)
            val elementResponse = Rest.get("/projects/$projectId/commits/$commitId/elements/$elementId", null)

            val element = objectMapper.readValue<ElementResponse>(elementResponse.body?:"")
            return element.toElementData()
        } else {
            if((saveElementsLocally.value)&&(projectsLoaded[commitId.toString()]!=null)) {
                for(element in projectsLoaded[commitId.toString()]!!.second) {
                    if(element.elementId == elementId)
                        return element
                }
            }
        }
        return ElementData(elementId = UUID.randomUUID(), type = "t.b.d.")
    }


    fun getElementsForUi2(projectId: UUID?, commitId: UUID? = null): MutableList<UUID> {

        val cellList : MutableList<UUID> = mutableListOf()
        val elementsDAO = getElements(projectId!!,commitId!!)

        for (elementDAO in elementsDAO){
            if (elementDAO.type == "TextualRepresentation"){
                cellList.add(elementDAO.elementId)
                for (elementUUID in elementDAO.ownedElements) {
                    cellList.add(elementUUID.id!!)
                }
            }
        }
        return cellList
    }

    @Suppress("unused", "unused_parameter")
    fun getRelationships(projectId: UUID, commitId: UUID? = null): MutableList<Relationship> {
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
            val projectReq = ProjectRequest(name=projectName,description = description,defaultBranchName = defaultBranchName)

            val postProjectResponse = Rest.post("/projects/", writer.writeValueAsString(projectReq), null)

            val resultBody: String = postProjectResponse.body ?: throw Exception("empty result body")

            when (postProjectResponse.statusCode.value()) {
                200 -> {
//                    println("--- " + postProjectResponse.statusCode.value() + " ---" + resultBody)
                    projectId = Rest.extractEntityIdFromBody(resultBody).toString()
                    project = getProjectById(UUID.fromString(projectId))
                }
                201 -> {
//                    println("---" + postProjectResponse.statusCode.value() + " ---" + resultBody)
                    projectId = Rest.extractEntityIdFromBody(resultBody).toString()
                    project = getProjectById(UUID.fromString(projectId))

                    // Update the projectState variable with a new list of projects
                    val newProjectsState= getProjects()
                    projectsState = newProjectsState
                }
                400 -> println("---" + postProjectResponse.statusCode.value() + "---" + resultBody)
                403 -> println("---" + postProjectResponse.statusCode.value() + "---" + resultBody)
            }
            return project

        } catch (io: Exception) {
            throw Exception("No project: $projectName")
        }
    }

    /** For the moment just Used in Test to deleted posted projects in test
     * @param id
     * @return true or false, if the deletion was successful or not
     */
    override fun deleteProject(id: UUID): Boolean {
        var deleted = false

        try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

            val payload ="""
                {
              "@id": "$id"
                }
            """.trimIndent()

            val response = Rest.delete("/projects/$id", payload, null)

            when (response.statusCode.value()) {
                200 -> {
//                    println("--- " + response.statusCode.value() + " ---" + response.body)
                    deleted = true
                }
                400 -> println("---" + response.statusCode.value() + "---" + response.body)
                401 -> println("---" + response.statusCode.value() + "---" + response.body)
                403 -> println("---" + response.statusCode.value() + "---" + response.body)
                500 -> println("---" + response.statusCode.value() + "---" + response.body)
            }
            return deleted

        } catch (io: Exception) {
            throw Exception("No project with the id: $id")
        }
    }

    /**
     * @param commitName name of the commit
     * @param commitDescription description of the commit
     * @param  projectId
     * @param  elementsDAOList MutableList<ElementDAO>?
     * @return the created commit or null
     */
    fun postCommit(
        commitName: String,
        commitDescription: String,
        projectId: UUID,
        elementsDAOList: MutableList<ElementDAO>? = mutableListOf(),
        branchId: UUID?
    ): Commit {

        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

        val changeList = arrayListOf<DataVersionRequest>()

        if (elementsDAOList != null) {
            if (elementsDAOList.isNotEmpty()) {
                for (element in elementsDAOList) {
                    val commitData = ElementCommitData(
                        id = element.elementId,
                        type = element.type ,
                        name = element.name,
                        shortName = element.shortName,
                        ownedElement = element.ownedElements.map { Identified(id=it.id) }.toMutableList(),
                        owner = Identified(id = element.owner?.id),
                        language = element.language,
                        textualRepresentation = element.textualRepresentation!!
                    )
                    commitData.body = element.body
                    val dataVersionRequest = DataVersionRequest(payload = commitData)
                    changeList.add(dataVersionRequest)
                }
            }
        }

        val commitRequest = CommitRequest(description = commitDescription, change = changeList)

        val dataString = writer.writeValueAsString(commitRequest)

//        println(dataString)

        val postCommitResponse = if(branchId!=null) Rest.post("/projects/$projectId/commits?branchId=$branchId", dataString, sessionId = null) else Rest.post("/projects/$projectId/commits", dataString, sessionId = null)

        val resultBody: String = postCommitResponse.body ?: throw Exception("empty result body")
        val commitId : UUID?
        var commit : Commit? = null

        when (postCommitResponse.statusCode.value()) {
            200 -> {
//                println("--- " + postCommitResponse.statusCode.value() + " ---" + resultBody)
                commitId = UUID.fromString(Rest.extractEntityIdFromBody(resultBody))
                commit = this.getCommitById(projectId, commitId)
            }
            201 -> {
//                println("---" + postCommitResponse.statusCode.value() + " ---" + resultBody)
                commitId = UUID.fromString(Rest.extractEntityIdFromBody(resultBody))
                commit = this.getCommitById(projectId, commitId)

            }
            400 -> println("---" + postCommitResponse.statusCode.value() + "---" + resultBody)
            403 -> println("---" + postCommitResponse.statusCode.value() + "---" + resultBody)
        }

        if (commit == null){
            throw Exception("Commit Unsuccessful: $commitName")
        }

        return commit
    }

    /**
     * Implements method from interface
     */
    override fun getElements(projectName: String): List<ElementDAO> {
        return try {
            val project = projectsState.first { it.name == projectName }
            val projectId = project.id
            val branch = getBranchById(projectId, project.defaultBranchId)
            getElements(projectId, branch.referencedCommitId())
        } catch (notLoaded: Exception) {
            //            println("Could not connect to backend via REST.")
            emptyList()
        }
    }


    fun postSession() : UUID? {
        Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

        val postCommitResponse = Rest.post("/sessions", null, null)
        val resultBody: String = postCommitResponse.body ?: throw Exception("empty result body")

        print(resultBody)

        when (postCommitResponse.statusCode.value()) {
            200 -> {
//                println("--- " + postCommitResponse.statusCode.value() + " ---" + resultBody)
//                commitId = UUID.fromString(Rest.extractEntityIdFromBody(resultBody))
//                commit = getCommit(projectId, commitId)
            }
            201 -> {
//                println("---" + postCommitResponse.statusCode.value() + " ---" + resultBody)
                val uuidString = Rest.extractEntityUUIDFromBody(resultBody)
                if(uuidString!=null)
                    return UUID.fromString(uuidString)


//                commitId = UUID.fromString(Rest.extractEntityIdFromBody(resultBody))
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
            400 -> println("---" + postCommitResponse.statusCode.value() + "---" + resultBody)
            403 -> println("---" + postCommitResponse.statusCode.value() + "---" + resultBody)
        }
        return null
    }


    override fun commit(
        owningProject: UUID,
        name: String?,
        description: String?,
        payload: List<CommitDataObject>,
        previousCommit: UUID?
    ): UUID {
        TODO("Not yet implemented")
    }

    @Suppress("UNUSED_PARAMETER")
    fun branch(projectId: UUID, branchName: String, headCommit: Commit): UUID {
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
    fun postMerge(projectId: UUID,mainBranch:UUID,mutableList: MutableList<UUID>){

    }

    fun postDigitalTwin(projectId: UUID, request: DigitalTwinRequest): DigitalTwin?{
        var dt:DigitalTwin? = null
        try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

            print(writer.writeValueAsString(request))

            val postProjectResponse = if (serverSessionId != null)
                Rest.post(
                    "/projects/$projectId/digital-twin",
                    writer.writeValueAsString(request),
                    serverSessionId.toString()
                )
            else
                Rest.post(
                    "/projects/$projectId/digital-twin",
                    writer.writeValueAsString(request),
                    internalSessionId.toString()
                )
            val resultBody: String = postProjectResponse.body ?: throw Exception("empty result body")

            when (postProjectResponse.statusCode.value()) {
                200 -> {
                    println("--- " + postProjectResponse.statusCode.value() + " ---" + resultBody)
                    dt = objectMapper.readValue<DigitalTwin>(resultBody)
                }

                201 -> {
                    println("---" + postProjectResponse.statusCode.value() + " ---" + resultBody)
                    dt = objectMapper.readValue<DigitalTwin>(resultBody)

                    // Update the projectState variable with a new list of projects
                }

                400 -> println("---" + postProjectResponse.statusCode.value() + "---" + resultBody)
                403 -> println("---" + postProjectResponse.statusCode.value() + "---" + resultBody)
            }
        } catch (ex:Exception){
            ex.printStackTrace()
        }
        return dt
    }
/*
    fun getDigitalTwinsFromProject(projectId: UUID) : MutableList<DigitalTwin> {
        return try {
            Rest.login("/users/login", USER_KEY, username, PASSWORD_KEY, password)

            val branchResponse = if(serverSessionId!=null)
                Rest.get("/projects/${projectId}/digital-twin", serverSessionId.toString())
            else
                Rest.get("/projects/${projectId}/digital-twin", internalSessionId.toString())

            print(branchResponse.body)

            objectMapper.readValue(branchResponse.body!!)
        } catch (io: Exception) {
            mutableListOf()
        }
    }
 */
}
