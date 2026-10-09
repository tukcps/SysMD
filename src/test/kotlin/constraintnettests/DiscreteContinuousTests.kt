package constraintnettests

import util.variable
import util.assertBounds
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiscreteContinuousTests {

    @Test
    fun discreteContinuousIssue1() = testSession("ISQ") {
        loadKerML("""
            inv x;  
            inv false y;  
            inv false z { x and y }
            package Dependencies { 
                type component :> Base::Anything { 
                    feature d: ISQ::LengthValue { :>> range = 10.0 .. 20.0 [mm];}
                    feature c: ISQ::LengthValue { :>> range = 2.0 .. 3.0 [m];}
                    feature b: ISQ::LengthValue { :>> range = 1.0 .. 2.0 [km];}
                    feature a: ISQ::VolumeValue = b*c*d;
                }
            }
        """, Runlevel.ALL)
        assertNoIssues()

        assertBounds(1.0..2.0, solver.variable("Dependencies::component::b"), unit = "km")
        assertBounds(2.0..3.0, solver.variable("Dependencies::component::c"), unit = "m")
        assertBounds(10.0..20.0, solver.variable("Dependencies::component::d"), unit = "mm")
        // Interval arithmetic can widen the computed bounds by rounding error.
        assertTrue(VectorQuantity(builder.real(20.0..120.0), "m^3").isApproximatelyEqualTo(
            solver.variable("Dependencies::component::a").vectorQuantity))
    }
}
