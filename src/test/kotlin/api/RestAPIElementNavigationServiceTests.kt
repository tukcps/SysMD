package api

import com.github.tukcps.sysmd.SysMdRunner
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.rest.Rest
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.ElementResponse
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.util.JsonSupport
import com.github.tukcps.sysmd.settings
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import util.mockup.MockupSysMDProjectService
import util.mockup.loadKerML
import util.testProjectSession
import kotlin.test.assertEquals
import kotlin.uuid.Uuid


@SpringBootTest(
    args= ["headless"],                    // Argument to main / runner to not start the UI
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = [SysMdRunner::class],     // Class that has a run function
    properties = ["server.port=8081"]   // Port at which we test
)
@DirtiesContext
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RestAPIElementNavigationServiceTests {

    @Serializable
    class ElementResponseList : ArrayList<ElementResponse>()

    init {
        settings.rest.port = "8081"
        settings.rest.entryURI = ""
        projectService = MockupSysMDProjectService()
    }

    /**
     * GET /projects/ID/commits/id/elements
     * returns all elements from the current session with session id == project id
     */
    @Test
    fun getElementsTest1() = testProjectSession("Base") {
        // request to create a new project
        val response = Rest.get("/projects/${project.id}/commits/${Uuid.random()}/elements", null)

        // Response: OK and ID.
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val elements: List<ElementData> = JsonSupport.json.decodeFromString(response.body!!)
        assertEquals(15, elements.size)
    }

    @Test
    fun getRootElementsTest() = testProjectSession("Base") {
        val response = Rest.get("/projects/${project.id}/commits/${Uuid.random()}/roots", null)
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val elements: List<ElementResponse> = JsonSupport.json.decodeFromString(response.body!!)
        assertEquals(1, elements.size)
        assertEquals("Base", elements[0].declaredName)
    }

    @Test
    fun getElementByIdTest() = testProjectSession("Base") {
        loadKerML("package test; ")
        val id = global.resolve("test")!!.memberElement.elementId
        val response = Rest.get("/projects/${project.id}/commits/${Uuid.random()}/elements/$id", null)
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val element = JsonSupport.json.decodeFromString<ElementResponse>(response.body!!)
        assertEquals(id, element.elementId)
        assertEquals(ElementType.Package, element.type)
        assertEquals("test", element.declaredName)
    }
}
