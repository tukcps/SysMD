package api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tukcps.sysmd.SysMdRunner
import com.github.tukcps.sysmd.rest.Rest
import com.github.tukcps.sysmd.rest.entities.requests.IndexEntry
import com.github.tukcps.sysmd.rest.entities.requests.SessionIndexRequest
import com.github.tukcps.sysmd.settings
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.Ignore
import kotlin.test.assertEquals


@SpringBootTest(
    args= ["headless"],                    // Argument to main / runner to not start the UI
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = [SysMdRunner::class],     // Class that has a run function
    properties = ["server.port=8081"]   // Port at which we test
)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class HoodRestAPISessionServiceTest {
    private var jsonMapper = jacksonObjectMapper()

    init {
        settings.rest.port = "8081"
        settings.rest.entryURI = ""
        //projectService = MockupSysMDProjectService()
    }

    @Test
    @Ignore
    fun createProjectAndSessionTest() {
        val expectedProjectName = "TestingProject"
        val expectedProjectDescription = "This is for testing purposes."

        val projectResponse: ProjectResponse = createProjectViaRest(name = expectedProjectName, description = expectedProjectDescription)
        assertEquals(expectedProjectName, projectResponse.name)
        assertEquals(expectedProjectDescription, projectResponse.description)

        val sessionResponse = createSessionViaRest(projectName = expectedProjectName)
    }

    private fun createProjectViaRest(name : String, description: String): ProjectResponse {
        val request = ProjectRequest(name = name, description = description)
        val response = Rest.post(endpoint = "/projects", payload = toJson(request), sessionId = null)
        assertCreated(response)
        return fromJson(response.body!!)
    }

    private fun createSessionViaRest(projectName : String): String {
        val quotedProjectName = "{\"$projectName\"}"
        val response = Rest.post(endpoint = "/session", payload = projectName, sessionId = null)
        assertCreated(response)
        return response.body!!
    }

    private fun createIndexViaRest(sessionId: String, filename : String, content: String): ProjectResponse {
        val indexEntry = IndexEntry(filename, content = content)
        val request = SessionIndexRequest(mutableListOf(indexEntry))

        val response = Rest.post(endpoint = "/session/index", payload = toJson(request), sessionId = sessionId)
        assertCreated(response)
        return fromJson(response.body!!)
    }

    private fun assertCreated(response: ResponseEntity<String>) {
        assertEquals(HttpStatus.CREATED.value(), response.statusCode.value())
    }

    private fun toJson(obj: Any): String = jsonMapper.writeValueAsString(obj)

    private inline fun <reified T> fromJson(json: String): T {
        return jsonMapper.readValue(json, object : TypeReference<T>() {})
    }


    data class ProjectRequest (
        val name: String,
        val description: String,
        val defaultBranchName: String = "Main"
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ProjectResponse (
        val name: String,
        val description: String,
        val defaultBranchName: String = "Main"
    )
}
