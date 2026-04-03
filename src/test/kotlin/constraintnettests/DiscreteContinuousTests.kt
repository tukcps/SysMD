package constraintnettests

import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class DiscreteContinuousTests {

    @Test
    fun discreteContinuousIssue1() = testSession("ISQ", "Ranges") {
        loadKerML(
            catchExceptions = false,
            input = """
                inv x;  
                inv y false;  
                inv z false { x and y }
                package Dependencies { 
                    type component :> Base::Anything { 
                        feature d: ISQ::LengthValue {:>> unit = "mm"; :>> range = "10.0 .. 20.0";}
                        feature c: ISQ::LengthValue { :>> unit = "m"; :>> range = "2.0 .. 3.0";}
                        feature b: ISQ::LengthValue {:>> unit = "km"; :>> range = "1.0 .. 2.0";}
                        feature a: ISQ::VolumeValue = b*c*d;
                    }
                }
            """)
        assertNoIssues()
        solver.propagate()

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