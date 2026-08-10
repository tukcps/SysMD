package constraintnettests

import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

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

        assertEquals(VectorQuantity(builder.real(1.0..2.0), "km"),
            solver.getVariable("Dependencies::component::b")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.real(2.0..3.0), "m"),
            solver.getVariable("Dependencies::component::c")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.real(10.0..20.0), "mm"),
            solver.getVariable("Dependencies::component::d")!!.vectorQuantity)
        assertEquals(VectorQuantity(builder.real(20.0..120.0), "m^3"),
            solver.getVariable("Dependencies::component::a")!!.vectorQuantity)
    }
}