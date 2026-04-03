package compiler.kerml.examples

import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class DatatypeTests {

    /**
     * Tests datatype declaration with features `sensorId` and `value`.
     * Ref: Section 7.4.2 - Datatypes
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testDatatype() = testSession("ScalarValues") {
        loadKerML("""
            datatype IdNumber specializes ScalarValues::Integer;
            datatype Reading { // Subtypes Base::DataValue by default
                feature sensorId : IdNumber; // Subsets Base::dataValues by default.
                feature value : ScalarValues::Real;
            }
        """)
        assertNoIssues()
    }
}
