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
    fun discreteContinuousIssue1() = testSession("SI") {
        loadKerML(
            catchExceptions = false,
            input = """
                feature x: ScalarValues::Boolean{:>> spec = "true";}
                feature y: ScalarValues::Boolean{:>> spec = "false";}
                feature z: ScalarValues::Boolean = x and y {:>> spec = "false";}
                package Dependencies { 
                    type component :> Base::Anything { 
                        feature d: SI::Length {:>> unit = "mm"; :>> range = "10.0 .. 20.0";}
                        feature c: SI::Length { :>> range = "2.0 .. 3.0";}
                        feature b: SI::Length {:>> unit = "km"; :>> range = "1.0 .. 2.0";}
                        feature a: SI::Volume = b*c*d;
                    }
                }
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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