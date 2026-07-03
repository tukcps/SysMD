@file:Suppress("JvmTaintAnalysis")

package api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tukcps.sysmd.SysMdRunner
import com.github.tukcps.sysmd.model.sysml.AttributeUsage
import com.github.tukcps.sysmd.rest.Rest
import com.github.tukcps.sysmd.rest.entities.requests.CodeRequest
import com.github.tukcps.sysmd.rest.entities.requests.IndexEntry
import com.github.tukcps.sysmd.rest.entities.requests.ProjectMetaRequest
import com.github.tukcps.sysmd.rest.entities.response.SessionResponse
import com.github.tukcps.sysmd.rest.entities.response.SessionStatusResponse
import com.github.tukcps.sysmd.rest.entities.response.VariablesResponse
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.readBytes
import io.github.tukcps.sysmlv2.api.entities.responseModels.ElementResponse
import kotlinx.io.files.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import util.mockup.MockupSysMDProjectService
import util.mockup.loadSysMLv2
import util.testProjectSession
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@SpringBootTest(
    args= ["headless"],                    // Argument to main / runner to not start the UI
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = [SysMdRunner::class],     // Class that has a run function
    properties = ["server.port=8081"]   // Port at which we test
)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RestAPISessionServiceTest {
    private var jsonMapper = jacksonObjectMapper().configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false
    )

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
    fun getSessionsTest() = testProjectSession("Base") {

        class SessionResponseList: ArrayList<SessionResponse>()

        // request to create a new project
        val response = Rest.get("/session", null)

        // Response: OK and ID.
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val sessions: List<SessionResponse> = jsonMapper.readValue(response.body, SessionResponseList()::class.java)
        assertNotNull(sessions.find { it.projectName != null })
    }


    @Test
    fun getSessionFilesTest() = testProjectSession("Base") {
        class StringList : ArrayList<String>()
        val response = Rest.get("/session/files", id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val fileNames = jsonMapper.readValue(response.body, StringList()::class.java)
        assertEquals(1, fileNames.size)
        assertEquals("icon.png", fileNames[0])
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun getSessionFileDataTest() = testProjectSession("Base") {
        val response = Rest.get("/session/files/icon.png", id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val original = Path(project.directory!!, "Files", "icon.png").readBytes()
        val body = response.body!!
        // assertTrue(body.contentEquals(original))
    }

    @Test
    fun startSessionTest() {
        projectService.createProject("startSessionTest")
        val projects = projectService.getProjects()
        val response = Rest.postText("/session", projects.first().name?:"", null)
        assertEquals(HttpStatus.CREATED.value(), response.statusCode.value())
    }

    @Test
    fun deleteSessionTest() {

    }

    @Test
    fun getAllElementsOfSessionTest() = testProjectSession("Base") {
        val response = Rest.get("/session/elements", id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val elements = jsonMapper.readValue(response.body, Array<ElementResponse>::class.java)
        assertTrue(elements.isNotEmpty())
    }

    @Test
    fun compileInSessionTest() {
        val project = projectService.createProject("compileInSessionTest") as ProjectData
        val codeRequest = CodeRequest(
            language = Language.SYS_ML.toString(),
            namespace = "",
            runlevel = Runlevel.MODEL.toString(),
            body = "package test;",
        )
        val codeRequestJson = jsonMapper.writeValueAsString(codeRequest)
        val session = SessionManager.createSession(project)
        val response = Rest.put("/session/model", codeRequestJson, sessionId = session.id.toString())
        val test = session.global.resolve("test")?.memberElement
        assertNotNull(test)
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
    }


    /**
     * This test checks whether constraint propagation starts and the status reports computed values.
     */
    @Test
    fun compileInSessionTest2() {
        val project = projectService.createProject("compileInSessionTest") as ProjectData
        val codeRequest = CodeRequest(
            language = Language.SYS_ML.toString(),
            runlevel = Runlevel.ALL.toString(),
            body = "attribute test: ScalarValues::Real = 2.0;",
        )
        val codeRequestJson = jsonMapper.writeValueAsString(codeRequest)
        val session = SessionManager.createSession(project)
        val response = Rest.put("/session/model", codeRequestJson, sessionId = session.id.toString())
        val test = session.global.resolve("test")?.member<AttributeUsage>()
        assertNotNull(test)
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
    }

    @Test
    fun compileInSessionTestWithIssue() {
        val project = projectService.createProject("compileInSessionTest") as ProjectData
        val codeRequest = CodeRequest(
            language = Language.KerML.toString(),
            runlevel = Runlevel.NAMES_RESOLVED.toString(),
            body = "package test; +error+",
        )
        val codeRequestJson = jsonMapper.writeValueAsString(codeRequest)
        val session = SessionManager.createSession(project)
        val response = Rest.put("/session/model", codeRequestJson, sessionId = session.id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val responseObject = jsonMapper.readValue(response.body, SessionStatusResponse::class.java)
        assertTrue(responseObject.issues.isNotEmpty())
    }

    @Test @Ignore
    fun putSessionIndexTest() = testProjectSession("Base") {
        val indexEntry1 = IndexEntry("file2.md", content = "Hello World!")
        val request = ProjectMetaRequest(
            project.id,
            project.name!!,
            null,
            null,
            mutableListOf(indexEntry1)
        )
        val asJson = jsonMapper.writeValueAsString(request)
        val response = Rest.put("/session/index", asJson, sessionId = id.toString())
        assertEquals(HttpStatus.CREATED.value(), response.statusCode.value())
    }

    @Test
    fun getVariablesTest() = testProjectSession("ScalarValues", runlevel = Runlevel.VARIANCE_CHECKED) {
        loadSysMLv2("""
            attribute x: ScalarValues::Real = 2.0;
        """)
        val response = Rest.get("/session/variables", id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val variables = jsonMapper.readValue(response.body, VariablesResponse::class.java)
        assertTrue(variables.variables.isNotEmpty())
        assertTrue(variables.variables.any { it.qualifiedName=="x" && it.value == "2" && it.unit == "1"})
    }

    @Test fun getSubtypesTest() = testProjectSession("ScalarValues") {
        val response = Rest.get("/session/elements/${anything.elementId}/subtypes", id.toString())
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val subtypes = jsonMapper.readValue(response.body, Array<ElementResponse>::class.java)
        assertTrue(subtypes.isNotEmpty())
    }
}
