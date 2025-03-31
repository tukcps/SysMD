package services

import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.loadLibrary
import com.github.tukcps.sysmd.services.session.loadProject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.parallel.Isolated
import org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES
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
    fun projectDataScalarValuesTest() = testSession("ScalarValues") {
        assertEquals("testSession", project?.name)
        assertNotNull(project?.description)
    }

    /**
     * Project import also used projects without side effects;
     * i.e., ISO26262 uses ScalarValues
     */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun projectDataISO26262Test() = testSession("ISO26262") {
        assertTrue(6 <= projectService.getProjects().size)
        assertNotNull(project?.description)
    }

    /**
     * Project import also used projects without side effects;
     * i.e., ISO26262 uses ScalarValues
     */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun projectData3Test() = testSession("Signals") {
        assertTrue(6 <=  projectService.getProjects().size)
        assertNotNull(project?.description)
    }

    /**
     * Repeated loading does not increase the number of elements,
     * Repeated loading does not introduce another builder.
     */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun scalarValuesLoadTest() = testSession {
        projectService.reset()
        loadLibrary("Base")
        loadLibrary("ScalarValues")
        val numElements = get().size
        val bool = global.resolve<TypeImplementation>("ScalarValues::Boolean")
        val builder = bool?.model?.builder
        assertNotNull(bool)
        assertNotNull(builder)

        // LoadProject creates Imports duplicates
        loadLibrary("ScalarValues")
        assertEquals(numElements, get().size)
        loadLibrary("ScalarValues")
        loadLibrary("ScalarValues")
        loadLibrary("ScalarValues")
        assertEquals(numElements, get().size)
        checkConsistencyOfBuilders()

        val bool2 = global.resolve<TypeImplementation>("ScalarValues::Boolean")
        val builder2 = bool2?.model?.builder
        assertNotNull(bool2)
        assertEquals(builder, builder2)
    }

    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun repeatedLoadingTest() = testSession {
        loadProject("Math")
        assertNotNull(global.resolve("Math"))
        val noElements = get().size
        loadProject("Math")
        assertEquals(get().size, noElements)
    }

    /**
     * Tests consistency after re-loading identical projects.
     *
     * TODO: Specializations that are redundant are not consistently cleaned.
     * Same for multiplicities, and more.
     * Not a big issue, but not nice.
     */
    @Ignore
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun repeatedLoadingOfNonStandardProject() = testSession {
        val before = get().filter { it !is Specialization }.size
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        loadProject("ISO26262", initialize = false)
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        loadProject("ISO26262", initialize = false)
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        initialize()
        initialize()
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val noElements = get().filter { it !is Specialization }.size
        val beforeHash = get().associateBy { it.qualifiedName }
        loadProject("ISO26262", initialize = false)
        initialize()
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(noElements, get().filter { it !is Specialization }.size)
        reset()
        assertEquals(before, get().filter { it !is Specialization }.size)
        loadProject("ISO26262", initialize = false)
        initialize()
        val after = get().filter { it !is Specialization }.size
        assertEquals(noElements, after)
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val diff = get().filter { it.qualifiedName !in beforeHash.keys }
        if (diff.isNotEmpty()) {
            println("Added ${diff.size} Elements after reset:")
            diff.forEach {
                println(it.qualifiedName)
            }
        }
        assertTrue(diff.isEmpty())
    }

}
