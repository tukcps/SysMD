package api

import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.tukcps.sysmd.SysMdRunner
import com.github.tukcps.sysmd.rest.Rest
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.ProjectResponse
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.util.JsonSupport
import com.github.tukcps.sysmd.settings
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import util.mockup.MockupSysMDProjectService
import java.util.*
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid


/**
 * The project controller provides an api for simple version management.
 * It handles the projects following the SysML v2 REST API endpoints:
 * - POST /projects: JSON → ID
 * - GET /projects: → Array of Project-Infos (Name, branches, …)
 * - GET /projects/ID: → Project
 * - PUT /projects/ID: JSON w/ ID → Status
 * - DEL /projects/id: ID → Status
 */
@SpringBootTest(
    args= ["headless"],                    // Argument to main / runner to not start the UI
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = [SysMdRunner::class],     // Class that has a run function
    properties = ["server.port=8081"]   // Port at which we test
)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RestAPIProjectServiceTests {
    private var jsonMapper: JsonMapper =  JsonMapper.builder().findAndAddModules().build()

    init {
        settings.rest.port = "8081"
        settings.rest.entryURI = ""
        projectService = MockupSysMDProjectService()
    }

    /**
     * POST /projects
     * creates a new project, and returns its id.
     * The id can be used in a session and is exclusive for its owner.
     */
    @Test
    fun postProjectCreatesProject() {
        // The payload
        val payload = """ 
            {
              "name": "postProjectTest", 
              "description": "Description of Document, optional",
              "default": "Dummy"
            }
            """

        // request to create a new project
        val response = Rest.post("/projects", payload, null)

        // Response: OK and ID.
        assertEquals(HttpStatus.CREATED.value(), response.statusCode.value())
        assertNotNull(Rest.extractKeyFromBody("@id", response.body))

        // Check if a project is correct in the SysMD folder resp. internal buffer.
        val project = projectService.getProjectById(Uuid.parse(Rest.extractKeyFromBody("@id", response.body)!!))
        assertNotNull(project)
        with (project){
            assertEquals("postProjectTest", name)
            assertEquals("Description of Document, optional", description)
            assertEquals(Rest.extractKeyFromBody("@id", response.body), id.toString())
        }
    }


    /**
     * If we post an incomplete request, response shall be BAD_REQUEST
     * (ISSUE: Exception is not processed correctly)
     */
    @Test @Ignore
    fun postProjectWithoutNameReturnsError() {
        // ==Arrange==
        val payloadWithoutName = """
            {
              "description": "Description of Document, optional",
              "default": "Dummy"
            }
            """

        // ==Act==
        val res = Rest.post("/projects", payloadWithoutName, null)

        // ==Assert==
        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
        assertNotNull(res.body)
    }

    /**
     * GET /projects
     * @return a list with all Document IDs, tags, names, descriptions
     */
    @Test
    fun getProjectsReturnsAllProjectsTest() {
        // ==Arrange==
        projectService = MockupSysMDProjectService()
        projectService.createProject(name="getProjectsTest1")
        projectService.createProject(name="getProjectsTest2")
        val projectsNumber = projectService.getProjects().count()

        // ==Act==
        // get all projects
        val response = Rest.get("/projects", null)

        // ==Assert==
        assertEquals(HttpStatus.OK, response.statusCode)
        val projects:  List<ProjectResponse>  = JsonSupport.json.decodeFromString(response.body!!)
        // id must be somewhere in documents.
        // val found = projects.find { it.id == id }
        // assertNotNull(found)

        // must be the same amount as in the repository
        val actualCount = projects.size
        assertEquals(projectsNumber, actualCount)
    }


    /**
     * GET /projects/id
     *
     * Fails in the gitlab pipeline only
     */
    @Test
    fun getProjectByIdShouldReturnProjectTest() {
        // ==Arrange==
        val actualProject = projectService.createProject(name="getProjectTest", description = "description")

        // ==Act==
        val response = Rest.get("/projects/${actualProject.id}", null)

        // ==Assert==
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val project = JsonSupport.json.decodeFromString<ProjectResponse>(response.body!!)
        assertEquals(actualProject.id, project.id)
        assertEquals(actualProject.name, project.name)
        assertEquals(actualProject.description, project.description)
    }

    /**
     * GET /projects/id with not-existing id
     * ISSUE: returns wrong response code
     */
    @Test @Ignore
    fun getProjectWithNonExistingIdShouldReturnErrorTest() {
        // ==Arrange==
        val id = UUID.randomUUID()

        // ==Act==
        val response = Rest.get("/projects/$id", null)

        // ==Assert==
        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode.value())
        assertNotNull(response.body)
        assert(response.body!!.contains("ot found"))
    }

    @Test
    fun deleteProjectTest() {
        // The payload
        val payload = """ 
            {
              "name": "postProjectTest", 
              "description": "Description of Document, optional",
              "default": "Dummy"
            }
            """

        // request to create a new project
        val response = Rest.post("/projects", payload, null)

        // Response: OK and ID.
        assertEquals(HttpStatus.CREATED.value(), response.statusCode.value())
        assertNotNull(Rest.extractKeyFromBody("@id", response.body))

        val projects = projectService.getProjects()
        val id: String
        // Check if a project is correct in the SysMD folder resp. internal buffer.
        with(projectService.getProjects().first { it.name == "postProjectTest" }) {
            id = this.id.toString()
            assertEquals("postProjectTest", name)
            assertEquals("Description of Document, optional", description)
            assertEquals(Rest.extractKeyFromBody("@id", response.body), id)
        }

        val responseDel = Rest.delete("/projects/${id}", "", null)
        assertEquals(HttpStatus.OK.value(), responseDel.statusCode.value())

    }

    @Test
    fun getElements() {

    }

}