package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import kotlin.test.Test
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.assertEquals

class SumTests {

    @Test fun vectorSum() = testSession("ISQ", "Ranges") {
        loadKerML("""  
                feature a: ISQ::CartesianMomentum3dVector {:>> range = "0..6,6..12,4..20";}
                feature b: ISQ::MomentumValue = sum(a);
            """)
        solver.propagate()
        assertNoIssues()
        val b = global.resolveVar("b")!!
        assertEquals(10.0, b.min(), 0.000001)
        assertEquals(38.0, b.max(), 0.000001)
    }

    @Test fun vectorSumInteger() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = "0..6,6..12,4..20";}
                feature b: ScalarValues::Integer = sum(a);
            """)
        solver.propagate()
        assertNoIssues()
        val b = global.resolveVar("b")!!
        assertEquals(10L, b.min())
        assertEquals(38L, b.vectorQuantity.values[0].asIdd().max)
    }

    @Test fun vectorSumIntegerEvalDown() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = "6..6,1..100,10..10";}
                feature b: Ranges::IntegerInRange = sum(a) {:>> range = "20..20";}
            """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        assertEquals(6L, a.min())
        assertEquals(6L, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(4L, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(4L, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(10L, a.vectorQuantity.values[2].asIdd().min)
        assertEquals(10L, a.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSumIntegerEvalDown2() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = "5..10,1..100,20..30";}
                feature b: Ranges::IntegerInRange = sum(a) {:>> range = "60..80";}
            """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        assertEquals(5L, a.min())
        assertEquals(10L, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(20L, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(55L, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(20L, a.vectorQuantity.values[2].asIdd().min)
        assertEquals(30L, a.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSum2() = testSession("ISQ", "Ranges") {
        loadKerML("""  
            feature a: ISQ::CartesianElectricFieldStrength3dVector {:>> range = "5..8,-4..-3,4..5";}
            feature b: ISQ::ElectricFieldStrengthValue = sum(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(5.0, b.min(), 0.000001)
        assertEquals(10.0, b.max(), 0.000001)
    }

    @Test fun vectorSumRealEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianElectricFieldStrength3dVector { :>> range = "6..6, 1..100, 10..10"; }
            feature b: ISQ::ElectricFieldStrengthValue = sum(a) {:>> range = "20..20";}
        """)
        solver.propagate()
        assertNoIssues()
        val a = solver.getVariable("a")!!
        assertEquals(6.0, a.min(), 0.000001)
        assertEquals(6.0, a.max(), 0.000001)
        assertEquals(4.0, a.min(1), 0.000001)
        assertEquals(4.0, a.max(1), 0.000001)
        assertEquals(10.0, a.min(2), 0.000001)
        assertEquals(10.0, a.max(2), 0.000001)
    }

    @Test fun vectorSumRealEvalDown2() = testSession("Ranges") {
        loadKerML("""  
            feature a: ISQ::CartesianElectricFieldStrength3dVector {:>> range = "5..10,1..100,20..30";}
            feature b: ISQ::ElectricFieldStrengthValue = sum(a) {:>> range = "60..80";}
        """)
        solver.propagate()
        assertNoIssues()
        val a = solver.getVariable("a")!!
        assertEquals(5.0, a.min(), 0.000001)
        assertEquals(10.0, a.max(), 0.000001)
        assertEquals(20.0, a.min(1), 0.000001)
        assertEquals(55.0, a.max(1), 0.000001)
        assertEquals(20.0, a.min(2), 0.000001)
        assertEquals(30.0, a.max(2), 0.000001)
    }
}
