package constraintnettests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.XBool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.mockup.loadKerML
import util.testSession

class DDBasedDiscreteSolverRequirementsProcessingTests {

    @Test
    fun restrictInteger() = testSession("ScalarValues") {
        loadKerML("""                
            feature weight: ScalarValues::Integer {:>> range = "0..50";}
            inv r { weight <= 30 }
        """)
        solver.propagate()
        val r = global.resolveVar("r")
        assertEquals(r!!.boolSpecs.first(), XBool.True)
    }

    @Test
    fun restrictInteger2() = testSession("ScalarValues") {
        loadKerML("""                
            feature weight: ScalarValues::Integer {:>> range = "0..50";}
            inv r { weight <= 30 }
        """)
        solver.propagate()
        val r = global.resolveVar("r")
        assertEquals(r!!.boolSpecs.first(), XBool.True)
    }
}