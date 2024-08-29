package services

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.compiler.loadLibrary
import com.github.tukcps.sysmd.compiler.loadProject
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.SysMDProjectService
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * Monitoring of the #element that is generated in the pre-defined packages.
 * The number can change -- but it should have a known reason.
 * If not, take care!
 */
class ProjectUsageDataTests {

    @BeforeEach fun loadProjects() {
        SysMDProjectService.reset()
    }

    /**
     * Repeated import of some project does not confuse Global, Any, ...
     * Repeated loading does not change the number of elements.
     */
    private val numKerMLElements = 91
    private val numScalarValuesElements = numKerMLElements + 90
    private val numScalarValuesOwnedElements = 6
    private val numGboElements = numScalarValuesElements+149+21
    private val numGboOwnedElements = numScalarValuesOwnedElements + 2

    @Test
    fun projectDataScalarValuesTest() = testSession("ScalarValues") {
        assertEquals("ScalarValues", project.name)
        assertNotNull(project.version)
    }

    /**
     * Project import also used projects without side effects;
     * i.e., ISO26262 uses ScalarValues
     */
    @Test
    fun projectDataISO26262Test() = testSession("ISO26262", catchExceptions = false) {
        assertEquals("ISO26262", project.name)
        assertEquals(1, SysMDProjectService.getProjects().size)
        assertNotNull(project.version)
    }

    /**
     * Project import also used projects without side effects;
     * i.e., ISO26262 uses ScalarValues
     */
    @Test
    fun projectData3Test() = testSession("Signals") {
        assertEquals("Signals", project.name)
        assertEquals(1, SysMDProjectService.getProjects().size)
        assertNotNull(project.version)
    }

    /**
     * Repeated loading does not increase the number of elements,
     * Repeated loading does not introduce another builder.
     */
    @Test fun scalarValuesLoadTest() = testSession(loadKerML = false) {
        loadLibrary("ScalarValues.md")
        val numElements = get().size
        val bool = global.resolve<TypeImplementation>("ScalarValues::Boolean")
        val builder = bool?.model?.builder
        assertNotNull(bool)
        assertNotNull(builder)

        // LoadProject creates Imports duplicates
        loadLibrary("ScalarValues.md")
        assertEquals(numElements, get().size)
        loadLibrary("ScalarValues.md")
        loadLibrary("ScalarValues.md")
        loadLibrary("ScalarValues.md")
        assertEquals(0, SysMDProjectService.getProjects().size)
        assertEquals(numElements, get().size)
        checkConsistencyOfBuilders()

        val bool2 = global.resolve<TypeImplementation>("ScalarValues::Boolean")
        val builder2 = bool2?.model?.builder
        assertNotNull(bool2)
        assertEquals(builder, builder2)
    }

    @Test
    fun repeatedLoading1() = testSession{
        loadProject("Math")
        val noElements = get().size
        loadProject("Math")
        assertEquals(get().size, noElements)
    }

    /**
     * Tests consistency after re-loading identical projects.
     * TODO: Inconsistency after end features -- due to lack of persisting featureReference??
     */
    @Test
    fun repeatedLoadingOfNonStandardProject() = testSession {
        val before = get().size
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
        val noElements = get().size
        val beforeHash = get().associateBy { it.qualifiedName }
        loadProject("ISO26262", initialize = false)
        initialize()
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(noElements, get().size)
        reset()
        assertEquals(before, get().size)
        loadProject("ISO26262", initialize = false)
        initialize()
        val after = get().size
        assertEquals(noElements, after)
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val diff = get().filter { it.qualifiedName !in beforeHash.keys }
        if (diff.isNotEmpty()) {
            println("Added Elements after reset:")
            diff.forEach {
                println(it.qualifiedName)
            }
        }
        assertTrue(diff.isEmpty())
    }

    @Test fun loadMathTest() =  testSession {
        loadProject("Math")
        assertEquals(1, SysMDProjectService.getProjects().size)
        assertEquals(numScalarValuesElements+28, get().size)
        assertEquals(9, global.ownedElement.size)
    }

    /**
     * Check the number of elements generated by parser.
     * This test is just indicative.
     * Numbers may change even without a bug (but check there is a reason for this!).
     * Also, numbers will change after changing the libraries.
     */
    @Test fun allImport() = testSession {
        settings.catchExceptions = false
        assertEquals (0, SysMDProjectService.getProjects().size)
        assertEquals(numScalarValuesElements, get().size)
        assertEquals(numScalarValuesOwnedElements, global.ownedElement.size)
        loadProject("ISO26262")
        assertEquals(1, SysMDProjectService.getProjects().size)
        assertEquals(numGboElements, get().size)
        assertEquals(numGboOwnedElements, global.ownedElement.size)
        loadProject("Math")
        assertEquals(2, SysMDProjectService.getProjects().size)
        assertEquals(numGboElements + 22+6, get().size)
        assertEquals(numGboOwnedElements+3, global.ownedElement.size)
        loadProject("Context")
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(4, SysMDProjectService.getProjects().size)
        assertEquals(14, global.ownedElement.size)
        assertEquals(numGboElements+43+15, get().size)
    }

    /**
     * It shall be possible to update elements that are existing by the
     * createOrUpdate function.
     */
    @Test fun importAndUpdate() = testSession {
        loadProject("Math")
        initialize()
        val pkg = global.resolve<PackageImplementation>("Math")!!
        val update = FeatureImplementation(declaredName="e")
        create(update, pkg)
        checkConsistency(get(), checkForNoTransients = false)
        assertDoesNotThrow { checkConsistency(get(), checkForNoTransients = false) }
    }

    @Test fun importInModelMath() = testSession("Math") {
        loadSysMD(
            """
                class X {
                    import Math;
                }
        """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val pi = global.resolve<Feature>("Math::pi")
        assertNotNull(pi)
        val x = global.resolve<Classifier>("X")
        assertEquals(1, x!!.getOwnedElementsOfType<Import>().size)
    }

    @Test fun importInModelOtherClass() = testSession(loadKerML = false) {
        loadSysMD("""
            class X;
            class Y;
            X hasA import Y.
            X hasA import Y.
        """.trimIndent())
        Assertions.assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val x = global.resolve<Type>("X")
        assertEquals(1, x?.getOwnedElementsOfType<Import>()?.size)
    }
}
