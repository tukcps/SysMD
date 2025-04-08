package kermltests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolveVars
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedefinitionTests {
    @Test
    fun redefinitionTest()  = testSession("Occurrences") {
        loadKerML("""
            class QuantityPowerFactor {
                feature unit: ScalarValues::String;
                feature exponent: ScalarValues::Integer(-10..10);
            }
            class QuantityDimension {
                feature quantityPowerFactors: QuantityPowerFactor;
            }
            feature lengthPF: QuantityPowerFactor { 
                :>> unit = "m";  
                :>> exponent = 1;
            }
            feature massPF: QuantityPowerFactor { 
                :>> unit = "kg";  
                :>> exponent = 1;  
            }
            feature quantityDimension: QuantityDimension { 
                :>> quantityPowerFactors = lengthPF; 
            }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(1, global.resolveVar("quantityDimension::quantityPowerFactors::exponent")!!.vectorQuantity.idd().getRange().max)
        assertEquals("m", global.resolveVar("quantityDimension::quantityPowerFactors::unit")!!.vectorQuantity.value.asStrDD().toString())
        assertEquals("m", global.resolveVar("lengthPF::unit")!!.vectorQuantity.value.asStrDD().toString())
        assertEquals(1, global.resolveVar("lengthPF::exponent")!!.vectorQuantity.idd().getRange().min)
    }

    //The types are not set as expected in addInheritedFeatures (the function should be right but not correctly called for the elements)
    // , so that in "initialize" the values in the last row can be set. (like in the last test)
    @Test
    fun nestedRedefinitionTest3()  = testSession("ScalarValues") {
        loadKerML("""
            datatype Old {
                feature a: ScalarValues::String = "old";
            }
            datatype OwnsOld {
                feature ownedOld: Old;
            }
            feature redefinedOld: Old { :>> a = "new"; }
            feature ownsOld: OwnsOld { :>> ownedOld = redefinedOld; } 
        """)
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        assertEquals("new", global.resolveVars("ownsOld::ownedOld::a")[0]!!.vectorQuantity.value.asStrDD().toString())
    }
}