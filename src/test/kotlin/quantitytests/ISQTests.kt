package quantitytests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ISQTests {

    @Test
    fun basicTestJustLoading() = testSession("SI")  {
        assertNoIssues()
    }

    @Test
    fun basicTestDefinitionOk() = testSession("SI")  {
        loadKerML("""
            feature x: ISQ::LengthValue = 10.0 [km]; 
        """)
        assertNoIssues()
    }

    @Test
    fun basicTestDefinitionWrong() = testSession("SI")  {
        loadKerML("""
            feature x: ISQ::LengthValue = 10.0 [V]; 
        """)
        assertIssue("Unit")
    }

    @Test
    fun derivedUnitTest() = testSession("SI")  {
        loadKerML("""
            feature x: ISQ::VolumeValue = 1000.0 [cm^3]; 
        """)
        assertNoIssues()
        val x = global.resolve<Feature>("x")
        assertNotNull(x)
        assertEquals(0.001, x.variable!!.min(), 0.000000000001)
    }

    @Test
    fun derivedUnitTest2() = testSession("SI", "Occurrences")  {
        loadKerML("""
            class Car {
                feature power: ISQ::PowerValue(10..1000) [kW]; 
            }
            class VW :> Car { 
                :>> power: ISQ::PowerValue(20..100) [kW]; 
            }
        """)
        assertNoIssues()
        val vw = global.resolve<Feature>("VW::power")
        assertNotNull(vw)
        assertEquals(20.0, vw.variable!!.min(), 0.000001)
    }
}