package quantitytests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ISQTests {

    @Test
    fun basicTestJustLoading() = testSession("ISQ")  {
        assertNoIssues()
    }

    @Test
    fun basicTestDefinitionOk() = testSession("ISQ")  {
        loadKerML("""
            feature x: ISQ::LengthValue = 10.0 [km]; 
        """)
        assertNoIssues()
    }

    @Test
    fun basicTestDefinitionWrong() = testSession("ISQ")  {
        loadKerML("""
            feature x: ISQ::LengthValue = 10.0 [V]; 
        """, Runlevel.VARIABLES)
        assertIssue("Unit")
    }

    @Test
    fun derivedUnitTest() = testSession("ISQ")  {
        loadKerML("""
            feature x: ISQ::VolumeValue = 1000.0 [cm^3]; 
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val x = solver.getVariable("x")
        assertNotNull(x)
        assertEquals(0.001, x.min(), 0.000000000001)
    }

    @Test
    fun derivedUnitTest2() = testSession("ISQ", "Occurrences")  {
        loadKerML("""
            class Car {
                feature power: ISQ::PowerValue(10..1000 [kW]); 
            }
            class VW :> Car { 
                :>> power: ISQ::PowerValue(20..100 [kW]) ; 
            }
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val vw = solver.getVariable("VW::power")
        assertNotNull(vw)
        assertEquals(20.0, vw.min(), 0.000001)
    }
}