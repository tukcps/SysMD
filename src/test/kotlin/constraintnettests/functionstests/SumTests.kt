package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class SumTests {

    @Test fun vectorSum() = testSession("ISQ", "Ranges") {
        loadKerML("""  
            feature a: ISQ::CartesianMomentum3dVector { :>> range = (0..6, 6..12, 4..20) [kg m/s];}
            feature b: ISQ::MomentumValue = sum(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(10.0 .. 38.0, b)
    }

    @Test fun vectorSumInteger() = testSession("Ranges") {
        loadKerML("""  
            feature a: Ranges::IntegerInRange {:>> range = (0..6,6..12,4..20);}
            feature b: ScalarValues::Integer = sum(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(10L .. 38L, b)
    }

    @Test fun vectorSumIntegerEvalDown() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = (6..6, 1..100, 10..10);}
                feature b: Ranges::IntegerInRange = sum(a) {:>> range = 20;}
            """)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(listOf(
                6L..6L,
                4L..4L,
                10L..10L
            ), a)
    }

    @Test fun vectorSumIntegerEvalDown2() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = (5..10,1..100,20..30);}
                feature b: Ranges::IntegerInRange = sum(a) {:>> range = 60..80;}
            """)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(listOf(
                5L..10L,
                20L..55L,
                20L..30L
            ), a)
    }

    @Test fun vectorSum2() = testSession("ISQ", "Ranges") {
        loadKerML("""  
            feature a: ISQ::CartesianElectricFieldStrength3dVector {:>> range = (5..8,-4..-3,4..5) [N/C];}
            feature b: ISQ::ElectricFieldStrengthValue = sum(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(5.0 .. 10.0, b)
    }

    @Test fun vectorSumRealEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianElectricFieldStrength3dVector { :>> range = (6..6, 1..100, 10..10); }
            feature b: ISQ::ElectricFieldStrengthValue = sum(a) {:>> range = 20;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(listOf(6.0 .. 6.0, 4.0 .. 4.0, 10.0 .. 10.0), a)
    }

    @Test fun vectorSumRealEvalDown2() = testSession("Ranges") {
        loadKerML("""  
            feature a: ISQ::CartesianElectricFieldStrength3dVector {:>> range = (5..10,1..100,20..30);}
            feature b: ISQ::ElectricFieldStrengthValue = sum(a) {:>> range = 60..80;}
        """)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(listOf(5.0 .. 10.0, 20.0 .. 55.0, 20.0 .. 30.0), a)
    }
}
