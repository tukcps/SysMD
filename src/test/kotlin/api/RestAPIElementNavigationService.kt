package api

import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.tukcps.sysmd.SysMdRunner
import com.github.tukcps.sysmd.rest.Rest
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.settings
import io.github.tukcps.sysmlv2.api.entities.responseModels.ElementResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import util.mockup.MockupSysMDProjectService
import util.mockup.loadKerML
import util.testSession
import java.util.*
import kotlin.test.assertEquals


@SpringBootTest(
    args= ["headless"],                    // Argument to main / runner to not start the UI
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = [SysMdRunner::class],     // Class that has a run function
    properties = ["server.port=8081"]   // Port at which we test
)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RestAPIElementNavigationService {
    private var jsonMapper: JsonMapper = JsonMapper.builder().findAndAddModules().build()

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
    fun getElementsTest1() = testSession("Base") {

        // request to create a new project
        val response = Rest.get("/projects/${project!!.id}/commits/${UUID.randomUUID()}/elements", null)

        // Response: OK and ID.
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val elements = jsonMapper.readValue(response.body, arrayListOf<ElementData>()::class.java)
        assertEquals(15, elements.size)
    }


    @Test
    fun getRootElementsTest() = testSession("Base") {
        val response = Rest.get("/projects/${project!!.id}/commits/${UUID.randomUUID()}/roots", null)
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val elements = jsonMapper.readValue(response.body, ElementResponseList()::class.java)
        assertEquals(1, elements.size)
        // assertEquals("Base", elements[0].declaredName)
    }

    @Test
    fun getElementByIdTest() = testSession("Base") {
        loadKerML("package test; ")
        val id = global.resolve("test")!!.memberElement.elementId
        val response = Rest.get("/projects/${project!!.id}/commits/${UUID.randomUUID()}/elements/$id", null)
        assertEquals(HttpStatus.OK.value(), response.statusCode.value())
        val element = jsonMapper.readValue(response.body, ElementResponse::class.java)
        assertEquals(id, element.elementId)
        assertEquals("Package", element.type)
        assertEquals("test", element.declaredName)
    }
}
