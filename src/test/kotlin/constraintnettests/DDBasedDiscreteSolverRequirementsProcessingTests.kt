package constraintnettests

import util.testSession
import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DDBasedDiscreteSolverRequirementsProcessingTests {

    @Test
    fun restrictInteger() = testSession("ScalarValues") {
        loadKerML("""                
            feature weight: ScalarValues::Integer {:>> range = "0..50";}
            inv r { weight <= 30 }
        """)
        propagate()
        val r = global.resolveVar("r")
        assertEquals(r!!.boolSpecs.first(), XBool.True)
    }

    @Test
    fun restrictInteger2() = testSession("ScalarValues") {
        loadKerML("""                
            feature weight: ScalarValues::Integer {:>> range = "0..50";}
            feature r: ScalarValues::Boolean = weight <= 30 {:>> spec = "true";}
        """)
        propagate()
        val r = global.resolveVar("r")
        assertEquals(r!!.boolSpecs.first(), XBool.True)
    }
}