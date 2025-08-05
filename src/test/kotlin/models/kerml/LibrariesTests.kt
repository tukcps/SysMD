package models.kerml

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.loadLibrary
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LibrariesTests {

    /**
     * Check whether the Base library is loaded correctly.
     */
    @Test
    fun initBaseTest() = testSession("Base") {
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val dataValue = global.resolve<Type>("Base::DataValue")
        assertNotNull(dataValue)
        val dataValues = global.resolve<Type>("Base::dataValues")
        assertNotNull(dataValues)
        val things = global.resolve<Type>("Base::things")
        assertNotNull(things)
    }

    /**
     * Initialization test
     */
    @Test
    fun initScalarValuesTest() = testSession("ScalarValues", initialize = false) {
        initialize()
        assertEquals(0, status.issues.size, status.issues.toString())
        val real = global.resolve<Element>("ScalarValues::Real")
        assertNotNull(real)
    }

    @Test
    fun initScalarValuesModelOK() = testSession("Base") {
        loadKerML("""
                    package ScalarValues {
                        datatype ScalarValue;
                        datatype String specializes ScalarValue;
                        datatype Number specializes ScalarValue;
                        datatype Real specializes Number;
                        datatype Integer specializes Number;
                        datatype Natural specializes Integer; // TODO: (0 .. *)  
                        datatype Boolean specializes Number;
                        datatype Requirement specializes Boolean;
                        datatype Performance specializes Real;
                        datatype Quality specializes Real; 
                    }
            """)
        assertNoIssues()
        val real = global.resolve<Element>("ScalarValues::Real")
        assertNotNull(real)
    }

    /**
     * loadLibrary
     * - loads a library from a file;
     * - standard libraries get UUID v5, not v4 and is the same for all qualified names.
     */
    @Test
    fun loadLibrary() = testSession {
        loadLibrary("ScalarValues")
        initialize()
        val real = global.resolve<DataType>("ScalarValues::Real")
        val sv = global.resolve<Package>("ScalarValues")
        assertEquals(5, real?.elementId?.version())
        assertEquals(5, sv?.elementId?.version())
        assertTrue(sv!!.isLibraryElement)
        println (" sv path ${sv.path()}")
        assertEquals(Generators.nameBasedGenerator().generate(sv.path() ), sv.elementId)
        assertEquals(Generators.nameBasedGenerator().generate("ScalarValues::Real"), real!!.elementId)
    }
}