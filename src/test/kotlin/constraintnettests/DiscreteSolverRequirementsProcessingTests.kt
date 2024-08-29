package constraintnettests

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DiscreteSolverRequirementsProcessingTests {

    @Test
    fun restrictInteger() = testSession(catchExceptions = false) {
        loadSysMD("""                
                attribute weight: ScalarValues::Integer(0..50);
                inv r { weight <= 30 }
        """.trimIndent()
        )
        //initialize(100)
        propagate()
        val r = global.resolveVar("r")
        //val ast = r!!.ast!!
        assertEquals(r!!.boolSpecs.first(), XBool.True)
    }

    @Test
    fun restrictInteger2() = testSession(catchExceptions = false) {
        loadSysMD("""                
                attribute weight: ScalarValues::Integer(0..50); 
                attribute r: ScalarValues::Boolean(true) = weight <= 30; 
        """.trimIndent()
        )
        //initialize(100)
        propagate()
        val r = global.resolveVar("r")
        // val ast = r!!.ast!!
        assertEquals(r!!.boolSpecs.first(), XBool.True)
    }
}