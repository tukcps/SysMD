package constraintnettests

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class DiscreteContinuousTests {

    @Test
    fun discreteContinuousIssue1() = SessionManager.testSession {
        loadSysMD(
            catchExceptions = false,
            input = """
                import ScalarValues; 
                attribute x: Boolean(true).
                attribute y: Boolean(false).
                attribute z: Boolean(false) = x and y. 
                package Dependencies { class component; } 
                Dependencies::component hasA
                    attribute d: Real(10.0 .. 20.0)[mm];
                    attribute c: Real(2.0 .. 3.0)[m];
                    attribute b: Real(1.0 .. 2.0)[km];
                    attribute a: Real = b*c*d. 
            """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()

        assertEquals(VectorQuantity(builder.range(1.0..2.0), "km"),
            global.resolveVar("Dependencies::component::b")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.range(2.0..3.0), "m"),
            global.resolveVar("Dependencies::component::c")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.range(10.0..20.0), "mm"),
            global.resolveVar("Dependencies::component::d")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.range(20.0..120.0), "m^3"),
            global.resolveVar("Dependencies::component::a")!!.vectorQuantity)
    }
}