package models.kerml

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
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
        assertNoIssues()
        val dataValue = global.resolve("Base::DataValue")?.memberElement
        assertNotNull(dataValue as? Type)
        val dataValues = global.resolve("Base::dataValues")?.memberElement
        assertNotNull(dataValues as? Feature)
        val things = global.resolve("Base::things")?.memberElement
        assertNotNull(things as? Feature)
    }

    /**
     * Initialization test
     */
    @Test
    fun initScalarValuesTest() = testSession("ScalarValues", runlevel = Runlevel.NONE) {
        initialize(Runlevel.ALL)
        assertNoIssues()
        val real = global.resolve("ScalarValues::Real")?.memberElement
        assertNotNull(real as? DataType)
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
        val real = global.resolve("ScalarValues::Real")?.memberElement
        assertNotNull(real as? DataType)
    }

    /**
     * loadLibrary
     * - loads a library from a file;
     * - standard libraries get UUID v5, not v4 and is the same for all qualified names.
     */
    @Test
    fun loadLibrary() = testSession {
        loadLibrary("ScalarValues")
        initialize(Runlevel.MODEL)
        val real = global.resolve("ScalarValues::Real")?.memberElement
        val sv = global.resolve("ScalarValues")?.memberElement
        assertEquals(5, real?.elementId?.version())
        assertEquals(5, sv?.elementId?.version())
        assertTrue(sv!!.isLibraryElement)
        // println (" sv path ${sv.path()}")
        assertEquals(Generators.nameBasedGenerator().generate(sv.path() ), sv.elementId)
        assertEquals(Generators.nameBasedGenerator().generate("ScalarValues::Real"), real!!.elementId)
    }
}