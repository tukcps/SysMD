package constraintnettests

import util.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class DiscreteContinuousTests {

    @Test
    fun discreteContinuousIssue1() = testSession("SI", "Ranges") {
        loadKerML(
            catchExceptions = false,
            input = """
                inv x;  
                inv y false;  
                inv z false { x and y }
                package Dependencies { 
                    type component :> Base::Anything { 
                        feature d: SI::Length(10..20) [mm];
                        feature c: SI::Length(2.0 .. 3.0) [m]; 
                        feature b: SI::Length(1.0 .. 2.0) [km]
                        feature a: SI::Volume = b*c*d;
                    }
                }
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        propagate()

        assertEquals(VectorQuantity(builder.real(1.0..2.0), "km"),
            global.resolveVar("Dependencies::component::b")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.real(2.0..3.0), "m"),
            global.resolveVar("Dependencies::component::c")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.real(10.0..20.0), "mm"),
            global.resolveVar("Dependencies::component::d")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.real(20.0..120.0), "m^3"),
            global.resolveVar("Dependencies::component::a")!!.vectorQuantity)
    }
}