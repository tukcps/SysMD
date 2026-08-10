package services

import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.loadProject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.parallel.Isolated
import org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES
import util.loadLibraryArrangement
import util.testProjectSession
import util.testSession
import kotlin.test.*


/**
 * Monitoring of the #element that is generated in the pre-defined packages.
 * The number can change -- but it should have a known reason.
 * If not, take care!
 */
@Isolated @Ignore
class ProjectUsageDataTests {

    @BeforeEach
    fun loadProjects() {
        projectService.reset()
    }


    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun projectDataScalarValuesTest() = testProjectSession("ScalarValues") {
        assertEquals("testSession", project.name)
        assertNotNull(project.description)
    }

    /**
     * Project import also used projects without side effects;
     * i.e., ISO26262 uses ScalarValues
     */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun projectDataISO26262Test() = testProjectSession("ISO26262") {
        assertTrue(6 <= projectService.getProjects().size)
        assertNotNull(project.description)
    }

    /**
     * Project import also used projects without side effects;
     * i.e., ISO26262 uses ScalarValues
     */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun projectData3Test() = testProjectSession("Signals") {
        assertTrue(6 <=  projectService.getProjects().size)
        assertNotNull(project.description)
    }

    /**
     * Repeated loading does not increase the number of elements,
     * Repeated loading does not introduce another builder.
     */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun scalarValuesLoadTest() = testSession {
        projectService.reset()
        loadLibraryArrangement("Base")
        loadLibraryArrangement("ScalarValues")
        val numElements = get().size
        val bool = global.resolve("ScalarValues::Boolean")
        val builder = bool?.model?.builder
        assertNotNull(bool)
        assertNotNull(builder)

        // LoadLibrary must not create Imports duplicates?
        loadLibraryArrangement("ScalarValues")
        assertEquals(numElements, get().size)
        loadLibraryArrangement("ScalarValues")
        loadLibraryArrangement("ScalarValues")
        loadLibraryArrangement("ScalarValues")
        assertEquals(numElements, get().size)
        checkConsistencyOfBuilders()

        val bool2 = global.resolve("ScalarValues::Boolean")
        val builder2 = bool2?.model?.builder
        assertNotNull(bool2)
        assertEquals(builder, builder2)
    }

    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun repeatedLoadingTest() = testProjectSession {
        loadProject("Math")
        assertNotNull(global.resolve("Math"))
        val noElements = get().size
        loadProject("Math")
        assertEquals(get().size, noElements)
    }
}
