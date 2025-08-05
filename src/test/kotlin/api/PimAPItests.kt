package api

import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.elementNavigationService
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.settings
import io.github.tukcps.sysmlv2.api.entities.CommitImplementation
import util.testSession
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
    fun getProjectsTest()  = testSession {
        project?.description = "Description"
        val testProject = projectService.getProjects().first { it.name == "testSession" }
        assertNotNull(testProject.id)
        assertEquals("testSession", testProject.name)
        assertEquals("Description", testProject.description)

    }

    /**
     * testSession creates a mockup project 'project' ...
     */
    @Test
    fun getProjectByIdTest()  = testSession {
        project?.description = "Description"
        val testProject = projectService.getProjectById(project!!.id)
        assertNotNull(testProject)
        assertEquals(project!!.id, testProject.id)
        assertEquals("testSession", testProject.name)
        assertEquals("Description", testProject.description)
    }

    @Test
    fun getElements() = testSession("Base") {
        val project = projectService.getProjects().first { it.name == "testSession" }
        assertNotNull(project)
        val elements = elementNavigationService.getElements(project, CommitImplementation(id= UUID.randomUUID()))
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())
    }

    @Test
    fun getElementByIdTest()  = testSession("Base") {
        val project = projectService.getProjects().first { it.name == "testSession" }
        assertNotNull(project)
        val element = elementNavigationService.getElementById(project, CommitImplementation(id=UUID.randomUUID()) , UUID.fromString("077fe9c5-4ed5-5d26-ba54-7f4ded3ef9a9"))
        assertNotNull(element)
        assertEquals("Base", element.declaredName)
    }

    @Test
    fun getRootElements() = testSession {
        val project = projectService.getProjects().first { it.name == "testSession" }
        assertNotNull(project)
        val elements = elementNavigationService.getRootElements(project, CommitImplementation(id=UUID.randomUUID()))
        assertNotNull(elements)
        assertEquals(1, elements.size)
        assertEquals("Base", elements.first().name)
    }
}