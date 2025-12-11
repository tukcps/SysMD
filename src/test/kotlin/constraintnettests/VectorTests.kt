package constraintnettests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VectorTests {

    @Test
    fun vectorDefineTestReal() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::VectorQuantityValue = (0.5, 1.5) N { :>> range = "0.0..1.0, 1.0..2.0"; :>> unit = "N";}
            feature b: Quantities::VectorQuantityValue = (0.5, 1.5) N { :>> unit = "N";}
            feature c: ISQ::CartesianForce3dVector  = (-5.0, -1.0, 3.0) N {:>> range = "-5.0..-1.0, -1.0..2.0, 2.0..4.0";}
        """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        assertEquals(0.5, a.min(), 0.000001)
        assertEquals(0.5, a.max(), 0.000001)
        assertEquals(1.5, a.min(1), 0.000001)
        assertEquals(1.5, a.max(1), 0.000001)
        assertEquals(0.5, b.min(), 0.000001)
        assertEquals(0.5, b.max(), 0.000001)
        assertEquals(1.5, b.min(1), 0.000001)
        assertEquals(1.5, b.max(1), 0.000001)
        assertEquals(-5.0, c.min(), 0.000001)
        assertEquals(-5.0, c.max(), 0.000001)
        assertEquals(-1.0, c.min(1), 0.000001)
        assertEquals(-1.0, c.max(1), 0.000001)
        assertEquals(3.0, c.min(2), 0.000001)
        assertEquals(3.0, c.max(2), 0.000001)
    }

    @Test fun vectorDefineTestRealError1() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue = (0.5,1.5) kg {:>> range = "0.0..1.0,1.0..2.0, 3.0..4.0"; :>>unit ="kg";}
            """)
        solver.propagate()
        assertTrue(status.issues.isNotEmpty() , status.issues.toString())
        assertEquals("Problem with vector size: Vector size of 2 does not match Constraint size of 3", status.issues.first().message)
    }

    @Test fun vectorDefineTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange = (0,1) {:>> range = "0..1,1..2";}
            feature b: Ranges::IntegerInRange = (0,1) {:>> range = "0..1,1..2";}
            feature c: Ranges::IntegerInRange = (-5, -1, 3) {:>> range = "-5..-1,-1..2, 2..4";}
            """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        assertEquals(0L, a.min())
        assertEquals(0, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(1, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(0L, b.min())
        assertEquals(0, b.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, b.vectorQuantity.values[1].asIdd().min)
        assertEquals(1, b.vectorQuantity.values[1].asIdd().max)
        assertEquals(-5L, c.min())
        assertEquals(-5, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-1, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(-1, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(3, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(3, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorDefineTestIntegerError1() = testSession("Ranges") {
        loadKerML("""
                feature b: Ranges::IntegerInRange = (0,1)  {:>> range = "0..1,1..2,0..3";}
            """)
        solver.propagate()
        assertTrue(status.issues.isNotEmpty() , "An error should be reported")
        assertEquals("Problem with vector size: Vector size of 2 does not match Constraint size of 3", status.issues.first().message)
    }

    @Test fun vectorPlusTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianPosition3dVector {:>> range = "0..1,1..2,3..4";}
                feature b: ISQ::CartesianPosition3dVector {:>> range = "0..1,1..3,-2..2";}
                feature c: ISQ::CartesianPosition3dVector = a + b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.min(), 0.000001)
        assertEquals(2.0, c.max(), 0.000001)
        assertEquals(2.0, c.min(1), 0.000001)
        assertEquals(5.0, c.max(1), 0.000001)
        assertEquals(1.0, c.min(2), 0.000001)
        assertEquals(6.0, c.max(2), 0.000001)
    }

    @Test fun vectorPlusTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "0..1,1..2,3..4";} 
                feature b: Ranges::IntegerInRange {:>> range = "0..1,1..3,-2..2";}
                feature c: ScalarValues::Integer  = a + b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(0L, c.min())
        assertEquals(2, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(5, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorMinusTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianVelocity3dVector {:>> range = "0..1,1..2,3..4";} 
                feature b: ISQ::CartesianVelocity3dVector {:>> range = "0..1,1..3,-2..2";} 
                feature c: ISQ::CartesianVelocity3dVector = a - b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(-1.0, c.min(), 0.000001)
        assertEquals(1.0, c.max(), 0.000001)
        assertEquals(-2.0, c.min(1), 0.000001)
        assertEquals(1.0, c.max(1), 0.000001)
        assertEquals(1.0, c.min(2), 0.000001)
        assertEquals(6.0, c.max(2), 0.000001)
    }

    @Test fun vectorMinusTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "0..1,1..2,3..4";}
                feature b: Ranges::IntegerInRange {:>> range = "0..1,1..3,-2..2";}
                feature c: ScalarValues::Integer  = a - b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(-1, c.min())
        assertEquals(1, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(1, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorScalarMultiplicationTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""  
                feature a: ISQ::CartesianAcceleration3dVector {:>> range = "0..6, 6..12, 4..20";}
                feature b: ISQ::DimensionOneValue {:>> range = "-2..3";} 
                feature c: ISQ::CartesianAcceleration3dVector = a * b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(-12.0, c.min(), 0.000001)
        assertEquals(18.0, c.max(), 0.000001)
        assertEquals(-24.0, c.min(1), 0.000001)
        assertEquals(36.0, c.max(1), 0.000001)
        assertEquals(-40.0, c.min(2), 0.000001)
        assertEquals(60.0, c.max(2), 0.000001)
    }

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
        assertEquals(10, b.min())
        assertEquals(38, b.vectorQuantity.values[0].asIdd().max)
    }

    @Test fun vectorSumIntegerEvalDown() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = "6..6,1..100,10..10";}
                feature b: Ranges::IntegerInRange = sum(a) {:>> range = "20..20";}
            """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        assertEquals(6, a.min())
        assertEquals(6, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(4, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(4, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(10, a.vectorQuantity.values[2].asIdd().min)
        assertEquals(10, a.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSumIntegerEvalDown2() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = "5..10,1..100,20..30";}
                feature b: Ranges::IntegerInRange = sum(a) {:>> range = "60..80";}
            """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        assertEquals(5, a.min())
        assertEquals(10, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(20, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(55, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(20, a.vectorQuantity.values[2].asIdd().min)
        assertEquals(30, a.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSum2() = testSession("ISQ", "Ranges") {
        loadKerML("""  
            feature a: ISQ::CartesianElectricFieldStrength3dVector {:>> range = "5..8,-4..-3,4..5";}
            feature b: ISQ::ElectricFieldStrengthValue = sum(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = global.resolveVar("b")!!
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
        val a = global.resolveVar("a")!!
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
        val a = global.resolveVar("a")!!
        assertEquals(5.0, a.min(), 0.000001)
        assertEquals(10.0, a.max(), 0.000001)
        assertEquals(20.0, a.min(1), 0.000001)
        assertEquals(55.0, a.max(1), 0.000001)
        assertEquals(20.0, a.min(2), 0.000001)
        assertEquals(30.0, a.max(2), 0.000001)
    }



    @Disabled //TODO Eval Down of Power (row wise or scalar operand)
    @Test fun vectorPowerTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = "0..1, 1..4, 4..9";}
                feature b: ISQ::CartesianForce3dVector {:>> range = "-2..2";}
                feature c: ISQ::CartesianForce3dVector = a ^ b;
        """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.min(), 0.000001)
        assertEquals(1.0, c.max(), 0.000001)
        assertEquals(1.0, c.min(1), 0.000001)
        assertEquals(8.0, c.max(1), 0.000001)
        assertEquals(2.0, c.min(2), 0.000001)
        assertEquals(81.0, c.max(2), 0.000001)
    }

    @Disabled //TODO Eval Down of Power (row wise or scalar operand)
    @Test fun vectorPowerTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "0..1,1..2,3..4";}
                feature b: Ranges::IntegerInRange {:>> range = "0..1,1..3,-2..2";}
                feature c: Ranges::IntegerInRange  = a ^ b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(0L, c.min())
        assertEquals(1L, c.max())
        assertEquals(1, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-8, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(8, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorNegateTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianPosition3dVector {:>> range = "0..1,1..2,3..4";}
                feature c: ISQ::CartesianPosition3dVector = -a;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(-1.0, c.min(), 0.000001)
        assertEquals(0.0, c.max(), 0.000001)
        assertEquals(-2.0, c.min(1), 0.000001)
        assertEquals(-1.0, c.max(1), 0.000001)
        assertEquals(-4.0, c.min(2), 0.000001)
        assertEquals(-3.0, c.max(2), 0.000001)
    }

    @Test fun vectorNegateTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "0..1,1..2,3..4";}
                feature c: ScalarValues::Integer  = -a;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(-1, c.min())
        assertEquals(0, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(-1, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-4, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(-3, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorAbsTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianVelocity3dVector {:>> range = "3..3,0..0,4..4";}
                feature c: ISQ::SpeedValue = abs(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(5.0, c.min(), 0.000001)
        assertEquals(5.0, c.max(), 0.000001)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorLenTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianAcceleration3dVector {:>> range = "3..3,0..0,4..4";}
                feature c: ScalarValues::Integer = size(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(3, c.min())
        assertEquals(3, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorCityBlockTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianPosition3dVector {:>> range = "3..3,0..0,4..4";}
                feature b: ISQ::CartesianPosition3dVector {:>> range = "5..5,-3..-3,4..4";}
                feature c: ISQ::LengthValue = cityBlockDistance(a,b);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(5.0, c.min(), 0.000001)
        assertEquals(5.0, c.max(), 0.000001)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorCityBlockTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "3..3,0..0,4..4";} 
                feature b: Ranges::IntegerInRange {:>> range = "5..5,-3..-3,4..4";}
                feature c: ScalarValues::Integer = cityBlockDistance(a,b); """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(5, c.min())
        assertEquals(5, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorAbsTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "3..3,4..4,0..0";}
                feature c: ScalarValues::Integer  = abs(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(5, c.min())
        assertEquals(5, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorFloorTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = "0.5..1.5,6.2..9.0,-6.9..7.1";}
                feature c: ISQ::CartesianForce3dVector = floor(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.min(), 0.000001)
        assertEquals(1.0, c.max(), 0.000001)
        assertEquals(6.0, c.min(1), 0.000001)
        assertEquals(9.0, c.max(1), 0.000001)
        assertEquals(-7.0, c.min(2), 0.000001)
        assertEquals(7.0, c.max(2), 0.000001)
    }

    @Test fun vectorCeilTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = "0.5..1.5, 6.2..9.01, -6.9..7.1";}
                feature c: ISQ::CartesianForce3dVector = ceil(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(1.0, c.min(), 0.000001)
        assertEquals(2.0, c.max(), 0.000001)
        assertEquals(7.0, c.min(1), 0.000001)
        assertEquals(10.0, c.max(1), 0.000001)
        assertEquals(-6.0, c.min(2), 0.000001)
        assertEquals(8.0, c.max(2), 0.000001)
    }

    @Test fun vectorSqrtTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue {:>> range = "1..4,0.64..225,9..81";}
                feature c: Ranges::RealInRange = sqrt(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!

        assertEquals(1.0, c.min(), 0.000001)
        assertEquals(2.0, c.max(), 0.000001)
        assertEquals(0.8, c.min(1), 0.000001)
        assertEquals(15.0, c.max(1), 0.000001)
        assertEquals(3.0, c.min(2), 0.000001)
        assertEquals(9.0, c.max(2), 0.000001)
    }

    @Test fun vectorsSqrtTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..4,64..225,9..81";}
                feature c: ScalarValues::Integer  = sqrt(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(1, c.min())
        assertEquals(2, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(8, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(15, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(3, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(9, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSqrTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue {:>> range = "1..4,0.5..15.0,-3..2";}
                feature c: Quantities::VectorQuantityValue = sqr(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!

        assertEquals(1.0, c.min(), 0.000001)
        assertEquals(16.0, c.max(), 0.000001)
        assertEquals(0.25, c.min(1), 0.000001)
        assertEquals(225.0, c.max(1), 0.000001)
        assertEquals(0.0, c.min(2), 0.000001)
        assertEquals(9.0, c.max(2), 0.000001)
    }

    @Test fun vectorsSqrTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..4,5..15,-3..2";}
                feature c: Ranges::IntegerInRange  = sqr(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(1, c.min())
        assertEquals(16, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(25, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(225, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(0, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(9, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorLogTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue {:>> range = "1..4,1..10,2.5..4";}
                feature c: ScalarValues::Real = ln(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!

        assertEquals(0.0, c.min(), 0.000001)
        assertEquals(1.3862943611198904, c.max(), 0.000001)
        assertEquals(0.0, c.min(1), 0.000001)
        assertEquals(2.3025850929940455, c.max(1), 0.000001)
        assertEquals(0.916290731874155, c.min(2), 0.000001)
        assertEquals(1.3862943611198904, c.max(2), 0.000001)
    }

    @Test fun vectorPow2TestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue {:>> range = "1..4,0..5,6..10";}
                feature c: ScalarValues::Real = pow2(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!

        assertEquals(2.0, c.min(), 0.000001)
        assertEquals(16.0, c.max(), 0.000001)
        assertEquals(1.0, c.min(1), 0.000001)
        assertEquals(32.0, c.max(1), 0.000001)
        assertEquals(64.0, c.min(2), 0.000001)
        assertEquals(1024.0, c.max(2), 0.000001)
    }

    @Test fun vectorsPow2TestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..4,0..5,6..10";}
                feature c: ScalarValues::Integer  = pow2(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(2, c.min())
        assertEquals(16, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(32, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(64, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(1024, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorsToString() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..4,0..5,6..10";}
                feature b: Quantities::VectorQuantityValue {:>> range = "1..4,0..5,6..10";}
                feature c: Ranges::IntegerInRange = (4,5,6) {:>> range = "1..4,0..5,6..10";}
                feature d: Quantities::VectorQuantityValue = (4.0,5.0,6.0) {:>> range = "1..4,0..5,6..10";}
            """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        val d = global.resolveVar("d")!!
        assertEquals("(1..4, 0..5, 6..10)", a.vectorQuantity.toString())
        assertEquals("(1..4, 0..5, 6..10)", b.vectorQuantity.toString())
        assertEquals("(4, 5, 6)", c.vectorQuantity.toString())
        assertEquals("(4, 5, 6)", d.vectorQuantity.toString())
    }

    @Test fun vectorsToStringWithUnits() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue {:>> unit = "kg m / s"; :>> range = "1..4,0..5,6..10";}
                feature b: ISQ::CartesianForce3dVector = (4.0,5.0,6.0) N {:>> range = "1..4,0..5,6..10";}
                feature c: ISQ::CartesianVelocity3dVector = (1.0,3.0,4.0) [km/h] {:>> unit = "km / h"; :>> range = "1..4,0..5,4..10";}
            """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        assertEquals("(1..4, 0..5, 6..10) kg m / s", a.vectorQuantity.toString())
        assertEquals("(4, 5, 6) N", b.vectorQuantity.toString())
        assertEquals("(1, 3, 4) km / h", c.vectorQuantity.toString())
    }

    @Test fun vectorCrossProductTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = "1..1,5..5,10..10";}
                feature b: ISQ::CartesianPosition3dVector {:>> range = "5..5,1..1,10..10";}
                feature c: ISQ::CartesianMomentOfForce3dVector = a cross b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(40.0, c.min(), 0.000001)
        assertEquals(40.0, c.max(), 0.000001)
        assertEquals(40.0, c.min(1), 0.000001)
        assertEquals(40.0, c.max(1), 0.000001)
        assertEquals(-24.0, c.min(2), 0.000001)
        assertEquals(-24.0, c.max(2), 0.000001)
    }

    @Test fun vectorCrossProductTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..1,5..5,10..10";}
                feature b: Ranges::IntegerInRange {:>> range = "5..5,1..1,10..10";}
                feature c: ScalarValues::Integer  = a cross b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(40, c.min())
        assertEquals(40, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(40, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(40, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-24, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(-24, c.vectorQuantity.values[2].asIdd().max)
    }

    @Disabled // Should have the same result as test with Int, but AA gives some strange results for independent values.
    @Test fun vectorCrossProductTestReal2() = testSession("Ranges") {
        loadKerML(
            """
                feature a: ISQ::CartesianForce3dVector {:>> range = "-3..4,5..7,0..1";}
                feature b: ISQ::CartesianPosition3dVector {:>> range = "4..5,-2..-1,3..10";}
                feature c: ISQ::CartesianMomentOfForce3dVector = a cross b.
            """
        )
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(15.0, c.min(), 0.000001)
        assertEquals(72.0, c.max(), 0.000001)
        assertEquals(-40.0, c.min(1), 0.000001)
        assertEquals(35.0, c.max(1), 0.000001)
        assertEquals(-43.0, c.min(2), 0.000001)
        assertEquals(-14.0, c.max(2), 0.000001)
    }

    @Test fun vectorCrossProductTestInt2() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-3..4,5..7,0..1";}
                feature b: Ranges::IntegerInRange {:>> range = "4..5,-2..-1,3..10";}
                feature c: ScalarValues::Integer  = a cross b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(15, c.min())
        assertEquals(72, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-40, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(35, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-43, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(-14, c.vectorQuantity.values[2].asIdd().max)
    }

    @Disabled // Should have the same result as test with Int, but AA gives some strange results for independent values.
    @Test fun vectorDotProductTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = "-3..4,5..7,0..1";}
                feature b: ISQ::CartesianPosition3dVector {:>> range = "4..5,-2..-1,3..10";}
                feature c: ISQ::CartesianMomentOfForce3dVector = a dot b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(-29.0, c.min(), 0.000001)
        assertEquals(25.0, c.max(), 0.000001)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorDotProductTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-3..4,5..7,0..1";}
                feature b: Ranges::IntegerInRange {:>> range = "4..5,-2..-1,3..10";}
                feature c: ScalarValues::Integer  = a dot b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(-29, c.min())
        assertEquals(25, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorNormalizeTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue {:>> range = "0..0,3..3,4..4";}
                feature b: Quantities::VectorQuantityValue {:>> range = "10..10,3..3,2..2";}
                feature c: ScalarValues::Real = norm(a);
                feature d: ScalarValues::Real = norm(b);
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        val d = global.resolveVar("d")!!
        assertEquals(0.0, c.min(), 0.000001)
        assertEquals(0.0, c.max(), 0.000001)
        assertEquals(0.6, c.min(1), 0.000001)
        assertEquals(0.6, c.max(1), 0.000001)
        assertEquals(0.8, c.min(2), 0.000001)
        assertEquals(0.8, c.max(2), 0.000001)
        assertEquals(0.9407208683835953, d.min(), 0.000001)
        assertEquals(0.9407208683835953, d.max(), 0.000001)
        assertEquals(0.28221626051507853, d.min(1), 0.000001)
        assertEquals(0.28221626051507853, d.max(1), 0.000001)
        assertEquals(0.18814417367671904, d.min(2), 0.000001)
        assertEquals(0.18814417367671904, d.max(2), 0.000001)
    }

    @Test fun vectorAngleTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianMomentOfForce3dVector {:>> range = "1..1,1..1,0..0";}
                feature b: ISQ::CartesianMomentOfForce3dVector {:>> range = "1..1,0..0,0..0";}
                feature c: ISQ::DimensionOneValue = angle(a,b) {:>> unit = "°";}
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals("45 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal2() = testSession("ISQ") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = "1..1, 1..1, 0..0";}
                feature b: ISQ::CartesianForce3dVector {:>> range = "1..1, 1..1, 0..0";}
                feature c: ISQ::DimensionOneValue = angle(a,b) {:>> unit = "°";}
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.min(), 0.00001)
        assertEquals(0.0, c.max(), 0.00001)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal3() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianMomentum3dVector {:>> range = "1..1, 1..1, 0..0";}
                feature b: ISQ::CartesianMomentum3dVector {:>> range = "-1..-1, -1..-1, 0..0";}
                feature c: ISQ::DimensionOneValue = angle(a,b) {:>> unit = "°";}
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals("179.99999 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal4() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = "1..1,5..5,10..10";}
                feature b: ISQ::CartesianForce3dVector {:>> range = "5..5,2..2,-1..-1";}
                feature c: ISQ::DimensionOneValue = angle(a,b) {:>> unit = "°";}
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals("85.33527 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }


    @Test fun vectorAngleTestInt() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..1,1..1,0..0";}
                feature b: Ranges::IntegerInRange {:>> range = "1..1,-1..-1,0..0";}
                feature c: ISQ::DimensionOneValue = angle(a,b) {:>> unit = "°";}
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assertEquals("90 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun inheritanceOfVectorTest() = testSession("Ranges") {
        loadKerML("""
            package InstallationSpaces {
                type InstallationSpace :> Base::Anything {
                    feature origin: ScalarValues::Real;
                    feature vertex1: ScalarValues::Real = origin;
                }
                type EngineCompartment :> InstallationSpace; 
            }
            """)
        solver.propagate()
        assertNoIssues()
    }

    @Test fun vectorPositionAccessTest() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianPosition3dVector {:>> range = "1..1,5..5,10..10";}
                feature b: ISQ::LengthValue = a[1];
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("b")!!
        assertEquals("5 m", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorPositionAccessTest2() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianPosition3dVector {:>> range = "1..1,5..5,10..10";}
                feature b: Quantities::VectorQuantityValue = a[1..2]  {:>> unit = "m";}
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("b")!!
        assertEquals(5.0, c.max(), 0.00001)
        assertEquals(10.0, c.max(1), 0.0001)
        assertEquals(2,c.vectorQuantity.values.size)
    }

    @Test fun vectorPositionAccessTest3() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianPosition3dVector {:>> range = "1..1,5..5,10..10";}
                feature b: ISQ::CartesianPosition3dVector {:>> range = "3..3,2..2,7..7";}
                feature c: ISQ::LengthValue = a[1]+b[2];
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("c")!!
        assert(12.0 in c.vectorQuantity.values[0].asAadd())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    //Todo implement changing value at given vector position
    @Disabled @Test
    fun vectorPositionAccessTest4() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianPosition3dVector {:>> range = "1..1,5..5,10..10";}
                feature a[1]: ISQ::LengthValue = 2..2;
            """)
        solver.propagate()
        assertNoIssues()
        val c = global.resolveVar("a")!!
        assert(2.0 in c.vectorQuantity.values[1].asAadd())
        assertEquals(3,c.vectorQuantity.values.size)
    }

}
