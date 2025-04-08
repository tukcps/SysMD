package api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tukcps.sysmd.SysMdRunner
import com.github.tukcps.sysmd.rest.Rest
import com.github.tukcps.sysmd.rest.entities.requests.IndexEntry
import com.github.tukcps.sysmd.rest.entities.requests.SessionIndexRequest
import com.github.tukcps.sysmd.rest.entities.response.SessionResponse
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.settings
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import util.mockup.MockupSysMDProjectService
import util.testSession
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@SpringBootTest(
    args= ["headless"],                    // Argument to main / runner to not start the UI
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = [SysMdRunner::class],     // Class that has a run function
    properties = ["server.port=8081"]   // Port at which we test
)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RestAPISessionServiceTest {
    private var jsonMapper = jacksonObjectMapper()

    init {
        settings.rest.port = "8081"
        settings.rest.entryURI = ""
        projectService = MockupSysMDProjectService()
    }

    /**
     * GET /session
     * returns all elements from the current session with session id == project id
     */
    @Test
    fun getSessionsTest() = testSession("Base") {

        class SessionResponseList: ArrayList<SessionResponse>()

        // request to create a new project
        val response = Rest.get("/session", null)

        // Response: OK and ID.
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val sessions: List<SessionResponse> = jsonMapper.readValue(response.body, SessionResponseList()::class.java)
        assertNotNull(sessions.find { it.projectName != null })
    }


    @Test
    fun getSessionFilesTest() = testSession("Base") {
        class StringList : ArrayList<String>()
        val response = Rest.get("/session/files", id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val fileNames = jsonMapper.readValue(response.body, StringList()::class.java)
        assertEquals(1, fileNames.size)
        assertEquals("icon.png", fileNames[0])
    }

    @Test
    fun getSessionFileDataTest() = testSession("Base") {
        val response = Rest.get("/session/files/icon.png", id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        // val original = project!!.directory!!.resolve("Files").resolve("icon.png").toFile().readBytes()
        // val body = response.body?.toByteArray()
        // assertTrue(body.contentEquals(original))
    }

    @Test
    fun startSessionTest() {
        projectService.createProject("startSessionTest")
        val projects = projectService.getProjects()
        val response = Rest.post("/session", projects.first().name, null)
    }

    @Test
    fun deleteSessionTest() {

    }

    @Test
    fun getElementsOfSessionTest() = testSession("Base") {
        val response = Rest.get("/session/elements", id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
    }

    @Test
    fun compileInSessionTest() {
        val project = projectService.createProject("compileInSessionTest") as ProjectData
        val session = SessionManager.startSession(project)
        val response = Rest.put("/session/code", """{ "body": "package test;" }""", sessionId = session.id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
    }

    @Test @Ignore
    fun putSessionIndexTest() = testSession("Base") {
        val indexEntry1 = IndexEntry("file2.md", content = "Hello World!")
        val request = SessionIndexRequest(mutableListOf(indexEntry1))
        val asJson = jsonMapper.writeValueAsString(request)
        val response = Rest.put("/session/index", asJson, sessionId = id.toString())
        assertEquals(HttpStatus.CREATED.value(), response.statusCode.value())
    }
}
