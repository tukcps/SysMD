package api

import com.github.tukcps.sysmd.rest.entities.interchange.Meta
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.SessionManager.elementNavigationService
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import io.github.tukcps.sysmlv2.api.entities.CommitImplementation
import kotlinx.datetime.Instant
import util.testProjectSession
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PimApiServicesTests {

    /**
     * getProjects gets all projects
     */
    @Test
    fun getProjectsTest()  = testProjectSession {
        val testProject = projectService.getProjects().firstOrNull { it.name == "testSession" }
        assertNotNull(testProject?.id)
        assertEquals("testSession", testProject.name)
        assertEquals("Description", testProject.description)
    }

    /**
     * testSession creates a mockup project 'project' ...
     */
    @Test
    fun getProjectByIdTest()  = testProjectSession {
        project.description = "Description"
        val testProject = projectService.getProjectById(project.id)
        assertNotNull(testProject)
        assertEquals(project.id, testProject.id)
        assertEquals("testSession", testProject.name)
        assertEquals("Description", testProject.description)
    }

    @Test
    fun getElements() = testProjectSession("Base") {
        val project = projectService.getProjects().first { it.name == "testSession" }
        assertNotNull(project)
        val elements = elementNavigationService.getElements(project, CommitImplementation(id= UUID.randomUUID()))
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())
    }

    @Test
    fun getElementByIdTest()  = testProjectSession("Base") {
        val project = projectService.getProjects().first { it.name == "testSession" }
        assertNotNull(project)
        val element = elementNavigationService.getElementById(project, CommitImplementation(id=UUID.randomUUID()) , UUID.fromString("077fe9c5-4ed5-5d26-ba54-7f4ded3ef9a9"))
        assertNotNull(element)
        assertEquals("Base", element.declaredName)
    }

    @Test
    fun getRootElements() = testProjectSession {
        val project = projectService.getProjects().first { it.name == "testSession" }
        assertNotNull(project)
        val elements = elementNavigationService.getRootElements(project, CommitImplementation(id=UUID.randomUUID()))
        assertNotNull(elements)
        assertEquals(1, elements.size)
        // assertEquals("Base", elements.first().name)
    }

    // sessionService
    @Test
    fun getAllSessions() = testProjectSession {
        val sessions = sessionService.getAllSessions()
        assertNotNull(sessions)
        assertTrue(sessions.isNotEmpty()) // At least, testSession -- maybe more
    }

    @Test
    fun getSessionByIdTest() = testProjectSession {
        val project = projectService.createProject("getSessionByIdTest", "description", null)
        val session = sessionService.createSession(project as ProjectData)
        assertNotNull(session)
        assertEquals("getSessionByIdTest", session.project.name)
        assertEquals("description", session.project.description)
        val id = session.id
        assertNotNull(id)
        val result = sessionService.getSession(id)
        assertNotNull(result)
        assertEquals(session, result)
    }

    @Test
    fun getMetaTest() = testProjectSession {
        val meta = sessionService.getMeta(id)
        assertNotNull(meta)
        assertEquals(2, meta.index.size)
    }

    @Test
    fun setMetaTest() {
        val project = projectService.createProject("setMetaTest", "description", null)
        val session = sessionService.createSession(project as ProjectData)
        val meta = Meta(
            index = linkedMapOf("a" to "A", "b" to "B"),
            created = Instant.parse("2021-09-09T00:00:00.00Z"),
        )
        sessionService.putMeta(session.id, meta)
        assertEquals(meta, project.meta)
    }

    @Test
    fun getCellsTest1() = testProjectSession {
        val cells = sessionService.getCells(this.id)
        assertEquals(2, cells?.size)
    }

    @Test
    fun getCellsTest2() = testProjectSession {
        val meta = sessionService.getMeta(id)
        val file = meta?.index["a"]
        val cells = file?.let {  sessionService.getCells(this.id, file) }
        assertEquals(3, cells?.size)
    }

    @Test
    fun setCellsTest()  {
        val project = projectService.createProject("setCellsTest", "description", null)
        val session = sessionService.createSession(project as ProjectData)
        val meta = Meta(
            index = linkedMapOf("a" to "A", "b" to "B"),
            created = Instant.parse("2021-09-09T00:00:00.00Z"),
        )
        sessionService.putMeta(session.id, meta)
        sessionService.putCells(
            session.id,
            "a",
            listOf(ElementData(
                UUID.randomUUID(),
                type = "TextualRepresentation",
                body = "content",
                language = "SysML"
            ))
        )
    }
}