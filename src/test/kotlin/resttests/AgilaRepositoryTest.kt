package resttests
import com.github.tukcps.sysmd.rest.AgilaRepository
import com.github.tukcps.sysmd.rest.AgilaRepository.deleteBranch
import com.github.tukcps.sysmd.rest.AgilaRepository.deleteProject
import com.github.tukcps.sysmd.rest.AgilaRepository.getBranchById
import com.github.tukcps.sysmd.rest.AgilaRepository.getBranches
import com.github.tukcps.sysmd.rest.AgilaRepository.getCommitById
import com.github.tukcps.sysmd.rest.AgilaRepository.getCommits
import com.github.tukcps.sysmd.rest.AgilaRepository.getElements
import com.github.tukcps.sysmd.rest.AgilaRepository.getProjectById
import com.github.tukcps.sysmd.rest.AgilaRepository.getProjectByName
import com.github.tukcps.sysmd.rest.AgilaRepository.getProjects
import com.github.tukcps.sysmd.rest.AgilaRepository.internalSessionId
import com.github.tukcps.sysmd.rest.AgilaRepository.postBranch
import com.github.tukcps.sysmd.rest.AgilaRepository.postCommit
import com.github.tukcps.sysmd.rest.AgilaRepository.postMerge
import com.github.tukcps.sysmd.rest.AgilaRepository.postProject
import com.github.tukcps.sysmd.rest.AgilaRepository.postSession
import com.github.tukcps.sysmd.rest.AgilaRepository.saveElementsLocally
import com.github.tukcps.sysmd.rest.Rest
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmlv2.entities.ElementDAO
import com.github.tukcps.sysmlv2.entities.Project
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

/**
 * These tests require a running Agila Backend.
 * They should be enabled manually to see if the data is transferred to the database.
 */
 class AgilaRepositoryTest {

    private val user = "admin@cps.de"
    private val password = "admin"
    private var online = true

    private fun onlyOnline(f: () -> Unit ) {
        if (online) f()
    }

    @BeforeEach
    fun setUp() {
        saveElementsLocally.value=false
        internalSessionId=UUID.randomUUID()
        try {
            if (online) {
                settings.rest.baseURI = "localhost"
                settings.rest.entryURI = "/agila-server"
                settings.rest.port = "8080"
                Rest.login("/users/login", "email", user, "password", password)
                AgilaRepository.serverSessionId = postSession()
                println("   Agila SysMD: Login as $user succeeded.")
            }
        } catch (exception: Exception) {
            println("Login to backend failed. Check if backend is online and login credentials are ok.")
            println(" --> Will disable REST tests and not report errors.")
            online = false
        }
    }


    private fun createProject(name: String, description: String? = null): Project? {
        return postProject(name, description)
    }

    @Test
    fun testProjectList() = onlyOnline {
        val projects = getProjects()
        assertTrue(projects.isNotEmpty())
        // assertEquals(projects.first().name,"ScalarValues")
    }

    @Test
    fun deleteProjectTest() = onlyOnline {
        val project = postProject("TestDeleteProject","Description")

        val deleted = deleteProject(project?.id!!)
        assertFalse(getProjects().contains(project))
        assertEquals(true, deleted)
    }


    @Test
    fun createAndDeleteBranchesTest() = onlyOnline {
        val project = getProjectByName("ISO26262")
        val branchesOfProject = getBranches(project.id)
        val branch = postBranch(project.id,"TestDeleteBranch", branchesOfProject.first().referencedCommitId!!)
        val newBranchesListOfProject = getBranches(project.id)
        assertTrue(newBranchesListOfProject.size > branchesOfProject.size)

        val deleted = deleteBranch(project.id, branch?.id ?: UUID.randomUUID())
//        assertFalse(getProjects().contains(project))
        assertEquals(true, deleted)
    }


    @Test
    fun postProjectTest() = onlyOnline {
        val projectsSizeBefore = getProjects().size
        val project = postProject("TestDeleteProject","Description")

        val projects=getProjects()

        assertTrue(projects.size > projectsSizeBefore)
        assertEquals(projects.last().name,"TestDeleteProject")

        // deletes the project in The Data Base
        assertTrue(deleteProject(project?.id!!))
    }

    @Test
    fun getProjectsTest() = onlyOnline {
        val oldSize = getProjects().count()
        val project1 = postProject("TestProject1", "1")
        val project2 = postProject("TestProject2", "2")
        val projects = getProjects()
        assertTrue(projects.isNotEmpty())
        assertTrue(projects.toList().size >= oldSize +2)

        // deletes the project in The Data Base
        assertTrue(deleteProject(project1?.id!!))
        assertTrue(deleteProject(project2?.id!!))
    }

    @Test
    fun getProjectByIdTest() = onlyOnline {
        val project = postProject("TestGetProjectById","Description")

        val projectResponse = getProjectById(project?.id!!)
        assertNotNull(projectResponse)
        assertEquals("TestGetProjectById", projectResponse.name)
        assertEquals("Description", projectResponse.description)

        // deletes the project in The Data Base
        assertTrue(deleteProject(project.id))
    }

    /**
     * For a new project without commits
     */
    @Test
    fun getCommitsTest() = onlyOnline {
        val project = createProject("TestPostCommit", "Description")
        val projectId = project?.id
//        val commit : MutableList<Commit> = getCommits(projectId)
//        assertTrue(commit.isEmpty())

        assertTrue(deleteProject(projectId!!))
    }

    /**
     * For an existing project with commits
     * also tests getCommit(projectId,commitId)
     */
    @Test
    fun getCommitsTest2() = onlyOnline {
        val projects = getProjects()
        val mathProject = projects.find { it.name == "Math" } !!
        val mathCommits = getCommits(mathProject.id)
        assertTrue(mathCommits.isNotEmpty())
        assertEquals(1,mathCommits.size)
        assertEquals(mathProject.id,mathCommits.first().owningProject)
    }

    /**
     * Precondition : Empty ElementIdsList
     * Creates a project with a commit without elements: initial commit -> commitIDS
     */
    @Test
    fun postCommitTest() = onlyOnline {
        val project = createProject("TestProjectCommit", "Description")
        val projectId = project?.id
        // empty List
        val elementsDAOList : MutableList<ElementDAO> = mutableListOf()
        elementsDAOList.add(
            ElementData(
                elementId = UUID.randomUUID(),
                type = "TextualRepresentation",
                name = "Initial Commit",
                shortName = "string",
                language = "Markdown",
                body = "")
        )

        val commit = postCommit("TestPostCommit.md","Description", projectId!!,elementsDAOList, null)
        assertNotNull(commit)
        assertNotNull(commit.id)
        assertTrue(getCommits(projectId).isNotEmpty())

        // ElementsList in payload is Empty
        // val commitElements =
        getElements(projectId, commit.id)
        // assertTrue(commitElements.isEmpty())

        assertTrue(deleteProject(projectId))
    }

    /**
     * Precondition : The project had No previousCommit and no previousElements
     * creates a project with a commit having two elements in payload
     */
    @Test
    fun newPostCommitWithOneElementInPayloadTest() = onlyOnline {
        val project = createProject("TestProjectCommit", "Description")
        val projectId = project?.id

        // creates an ElementDAOList
        val elementsDAOList: MutableList<ElementDAO> = mutableListOf()
        val elementDAO = ElementData(
            elementId = UUID.randomUUID(),
            type = "TextualRepresentation",
            name = "First Element",
            shortName = "string",
            language = "SysMD",
            body = "Hello World")
        elementsDAOList.add(elementDAO)

        // make the commit
        val commit = postCommit("TestPostWithPayloadCommit.md","Description", projectId!!, elementsDAOList, null)
        // tests if the commit was successfully  created
        assertNotNull(commit)
        assertNotNull(commit.id)
        //Also tests getCommit after a commit have more Commits than before
        assertTrue(getCommits(projectId).size == 1)
        //Also tests getCommitPayload after a Commit
        val allElements = getElements(projectId, commit.id)
        assertEquals(1, allElements.size)

        assertTrue(deleteProject(projectId))
    }

    /**
     * Precondition : The project had previousCommits and  previousElements
     * Creates a project with two commits. The first commit has one element and the second commit has two elements (the first element is from the  previous element)
     * note: doesn't check if there were modifications in the previous commit. just does dummy commit
     */
    @Test
    fun postCommitWithDataTest2() = onlyOnline {
        val project = createProject("TestProject", "Description")
        val projectId = project?.id
        val elementsDAOList : MutableList<ElementDAO> = mutableListOf()

        val elementDAO1 = ElementData(
            elementId = UUID.randomUUID(),
            type = "TextualRepresentation",
            language = "Markdown",
            name= "element 1",
            shortName = "shortName",
            body ="## SysMD Tutorial")
        elementsDAOList.add(elementDAO1)

        val commit1= postCommit("TestCommit1.md","Description1", projectId!!, elementsDAOList, null) // just has one element

        assertEquals(1, getElements(projectId, commit1.id).size)

        val elementDAO2 = ElementData(
            elementId = UUID.randomUUID(),
            type = "TextualRepresentation",
            language = "Markdown",
            name= "element 2",
            shortName = "shortName",
            body = "SysMD does not want to and does not replace SysMLv2 textual.")

        elementsDAOList.add(elementDAO2)
        val commit2 = postCommit("TestCommit2.md","Description2", projectId, elementsDAOList, null)

        assertEquals(2, getElements(projectId, commit2.id).size)
        assertTrue(deleteProject(projectId))
    }

    @Test
    fun getElementsTest() = onlyOnline {
        val projects = getProjects()
        val project = projects.find { it.name == "Math" }!!
        val defaultBranch = getBranchById(projectId = project.id, branchId = project.defaultBranchId)

        val commits = getCommits(project.id)
        assertEquals(1, commits.size)

        val headId = defaultBranch.referencedCommitId
        val headCommit = getCommitById(project.id, headId)
        assertTrue(headCommit.description.isNotEmpty())
        assertEquals(headCommit.owningProject, project.id)

        val elements = getElements(project.id, headCommit.id)
        assertTrue(elements.size>0)
    }

    @Test
    fun testCreationOfNewSession() = onlyOnline {
        val session = postSession()
        assertNotNull(session)
    }

    @Test
    fun testGetBranches() = onlyOnline {
        val projects = getProjects()
        val branches = getBranches(projects.first().id)
        assertTrue(branches.isNotEmpty())
        assertEquals(branches.first().name,"Main")
    }

    @Test
    fun testMergeTwoBranches() = onlyOnline {
        val project = createProject("TestMergeBranchProject","Tests the merge for the Projects")
        val elementsDAOList : MutableList<ElementDAO> = mutableListOf()

        val elementDAO1 = ElementData(
            elementId = UUID.randomUUID(),
            type = "TextualRepresentation",
            language = "Markdown",
            name= "element 1",
            shortName = "shortName",
            body ="## SysMD Tutorial")
        elementsDAOList.add(elementDAO1)

        assertTrue(project!=null)

        val projectId = project?.id !!

        val commit = postCommit("TestCommit2.md","Description2", projectId, elementsDAOList, null)

        val testBranch = postBranch(projectId, "TestBranch", commit.id)

        val branchesList = getBranches(projectId)

        assertEquals(2,branchesList.size)

        val elementDAO2 = ElementData(
            elementId = UUID.randomUUID(),
            type = "TextualRepresentation",
            language = "Markdown",
            name= "element 2",
            shortName = "shortName",
            body = "SysMD does not want to and does not replace SysMLv2 textual.")

        elementsDAOList.add(elementDAO2)

        val commit2 = postCommit("TestCommit2.md","Description2", projectId, elementsDAOList, testBranch?.id)

        postMerge(projectId,branchesList.toList()[0].id, mutableListOf(commit.id,commit2.id))

        deleteProject(projectId)
    }

    fun getElementByIdTest(){
    }

    fun getPropertiesTest() {
    }

    fun getRelationshipsTest() {
    }
}
