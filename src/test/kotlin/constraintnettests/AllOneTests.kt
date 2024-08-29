package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AllOnePropagationTests {


    @Test fun allOnePropagationTestReal() = testSession(loadKerML = false) {
        loadSysMD("""
                // a is a Real from 1..2, and is assigned a value from 1.2 to 2.5
                package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; datatype Real :> ScalarValue; }
                feature a: all ScalarValues::Real(1 .. 2) = [1.5 .. 2.5];
                feature b: ScalarValues::Real(1 .. 2) = [1.5 .. 2.5];
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isNotEmpty(), "an error shall be reported as the constraints cannot be satisfied all")
        val a = global.resolve<Feature>("a")!!.variable!!
        val b = global.resolve<Feature>("b")!!.variable!!
        assertEquals(1.0, a.min(), 0.000001)
        assertEquals(2.0, a.max(), 0.000001)
        assertEquals(1.5, b.min(), 0.000001)
        assertEquals(2.0, b.max(), 0.000001)
        assertEquals(1, status.exceptions.size, status.exceptions.toString())
    }

    @Test fun allOnePropagationTestInt() = testSession {
        loadSysMD("""
                feature a: all ScalarValues::Integer(1 .. 10) = [5 .. 15];
                feature b: ScalarValues::Integer(1 .. 10) = [5 .. 15].
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isNotEmpty(), status.exceptions.toString())
        val a = global.resolve<Feature>("a")!!.variable!!
        val b = global.resolve<Feature>("b")!!.variable!!
        assertEquals(1.0, a.min(), 0.000001)
        assertEquals(10.0, a.max(), 0.000001)
        assertEquals(5.0, b.min(), 0.000001)
        assertEquals(10.0, b.max(), 0.000001)
        assertEquals(1, status.exceptions.size)
    }
}