package sysmdtests

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.compiler.loadLibrary
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.createKerMLIntrospectionLibrary
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * Test of the basic libraries that come with SysMD.
 * They are loaded and names are resolved before any operations start.
 * Also, any UUID given in these libraries must be a UUID v5 UUID to ensure unique mapping
 * of these elements to the instances in all kind of KerML models.
 */
class LibrariesTest {

    @Test
    fun kerMLLibraryTest() = testSession(loadKerML = false)  {
        createKerMLIntrospectionLibrary()
        assertEquals(Generators.nameBasedGenerator().generate("Base::Anything"), any.elementId )
        assertEquals(Generators.nameBasedGenerator().generate("Global"), global.elementId )
        val dataType = global.resolve<DataType>("KerML::Kernel::DataType")            // Key for ScalarValues etc.
        assertNotNull(dataType)
        assertEquals(Generators.nameBasedGenerator().generate("KerML::Kernel::DataType"), dataType.elementId)

        checkConsistency(repo.elements.values, checkForNoTransients = false)
    }

    @Test
    fun kerMLLibraryExportImportTest() = testSession(loadKerML = false) {
        createKerMLIntrospectionLibrary()
        val e = export().getElements()
        import(e)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun kerMLLibraryInitializeTest() = testSession(loadKerML = false) {
        settings.catchExceptions = false
        createKerMLIntrospectionLibrary()
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun baseLibraryTest() = testSession(loadKerML = false) {
        createKerMLIntrospectionLibrary()
        loadLibrary("Base.md")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertNotNull(global.resolve<Classifier>("Base::Anything"))
        assertNotNull(global.resolve<Element>("Base::DataValue"))
    }

    @Test
    fun linksTest() = testSession(loadKerML = false, catchExceptions = false) {
        createKerMLIntrospectionLibrary()
        loadLibrary("Base.md")
        loadLibrary("Links.md")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertNotNull(global.resolve<Association>("Links::Link"))
    }

    @Test
    fun scalarValuesTest() = testSession(loadKerML = false) {
        createKerMLIntrospectionLibrary()
        loadLibrary("Base.md")
        loadLibrary("Links.md")
        loadLibrary("ScalarValues.md")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertNotNull(global.resolve<DataType>("ScalarValues::Natural"))
        assertNotNull(global.resolve<DataType>("ScalarValues::Real"))
        assertNotNull(global.resolve<DataType>("ScalarValues::Integer"))
    }

    @Test
    fun objectsTest() = testSession(loadKerML = false) {
        createKerMLIntrospectionLibrary()
        loadLibrary("Base.md", false)
        loadLibrary("Links.md", false)
        loadLibrary("Occurrences.md", false)
        loadLibrary("Objects.md", false)
        assertTrue(status.exceptions.isEmpty(),  status.exceptions.toString())
    }

    @Test
    fun resetTest() = testSession(loadKerML = false) {
        createKerMLIntrospectionLibrary()
        reset()
        createKerMLIntrospectionLibrary()
    }
}