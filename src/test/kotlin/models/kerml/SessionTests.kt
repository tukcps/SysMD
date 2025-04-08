package models.kerml

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SessionTests {


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
        assertEquals(0, status.issues.size, status.issues.toString())
        val real = global.resolve<Element>("ScalarValues::Real")
        assertNotNull(real)
    }

}