package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import util.assertNoIssues
import util.testSession
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VectorTests {

    @Test
    fun vectorDefineTestReal() = testSession("SI") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange = (0.5, 1.5) kg { :>> range = "0.0..1.0, 1.0..2.0"; }
                feature b: SI::Mass, Ranges::InRange = (0.5, 1.5) kg;
                feature c: SI::Mass, Ranges::InRange  = (-5.0, -1.0, 3.0) kg {:>> range = "-5.0..-1.0, -1.0..2.0, 2.0..4.0"; }
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val a = global.resolve<Feature>("a")!!.variable!!
        val b = global.resolve<Feature>("b")!!.variable!!
        val c = global.resolve<Feature>("c")!!.variable!!
        assertEquals(0.5, a.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(0.5, a.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1.5, a.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(1.5, a.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(0.5, b.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(0.5, b.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1.5, b.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(1.5, b.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-5.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(-5.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-1.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(-1.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(3.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(3.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorDefineTestRealError1() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange = (0.5,1.5) kg {:>> range = "0.0..1.0,1.0..2.0, 3.0..4.0";}
            """)
        propagate()
        assertTrue(status.issues.isNotEmpty() , status.issues.toString())
        assertEquals("Problem with vector size: Vector size of 2 does not match Constraint size of 3", status.issues.first().message)
    }

    @Test fun vectorDefineTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: ScalarValues::Integer, Ranges::InRange = (0,1) {:>> range = "0..1,1..2";}
            feature b: ScalarValues::Integer, Ranges::InRange = (0,1) {:>> range = "0..1,1..2";}
            feature c: ScalarValues::Integer, Ranges::InRange = (-5, -1, 3) {:>> range = "-5..-1,-1..2, 2..4";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val a = global.resolve<Feature>("a")!!.variable!!
        val b = global.resolve<Feature>("b")!!.variable!!
        val c = global.resolve<Feature>("c")!!.variable!!
        assertEquals(0, a.vectorQuantity.values[0].asIdd().min)
        assertEquals(0, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(1, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(0, b.vectorQuantity.values[0].asIdd().min)
        assertEquals(0, b.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, b.vectorQuantity.values[1].asIdd().min)
        assertEquals(1, b.vectorQuantity.values[1].asIdd().max)
        assertEquals(-5, c.vectorQuantity.values[0].asIdd().min)
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
        propagate()
        assertTrue(status.issues.isNotEmpty() , "An error should be reported")
        assertEquals("Problem with vector size: Vector size of 2 does not match Constraint size of 3", status.issues.first().message)
    }

    @Test fun vectorPlusTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::QuantityInRange {:>> range = "0..1,1..2,3..4";}
                feature b: SI::Mass, Ranges::InRange {:>> range = "0..1,1..3,-2..2";}
                feature c: SI::Mass = a + b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(5.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(6.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorPlusTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "0..1,1..2,3..4";} 
                feature b: Ranges::IntegerInRange {:>> range = "0..1,1..3,-2..2";}
                feature c: ScalarValues::Integer  = a + b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(2, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(5, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorMinusTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange {:>> range = "0..1,1..2,3..4";} 
                feature b: SI::Mass, Ranges::InRange {:>> range = "0..1,1..3,-2..2";} 
                feature c: SI::Mass = a - b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-2.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(6.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorMinusTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "0..1,1..2,3..4";}
                feature b: Ranges::IntegerInRange {:>> range = "0..1,1..3,-2..2";}
                feature c: ScalarValues::Integer  = a - b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-1, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(1, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(1, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorScalarMultiplicationTestReal() = testSession("SI", "Ranges") {
        loadKerML("""  
                feature a: SI::Mass {:>> range = "0..6, 6..12, 4..20";}
                feature b: Ranges::RealInRange {:>> range = "-2..3";} 
                feature c: SI::Mass = a * b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-12.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(18.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-24.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(36.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-40.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(60.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorSum() = testSession("SI", "Ranges") {
        loadKerML("""  
                feature a: SI::Mass, Ranges::InRange {:>> range = "0..6,6..12,4..20";}
                feature b: SI::Mass, Ranges::InRange = sum(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val b = global.resolveVar("b")!!
        assertEquals(10.0, b.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(38.0, b.vectorQuantity.values[0].asAadd().max, 0.000001)
    }

    @Test fun vectorSumInteger() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = "0..6,6..12,4..20";}
                feature b: ScalarValues::Integer = sum(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val b = global.resolveVar("b")!!
        assertEquals(10, b.vectorQuantity.values[0].asIdd().min)
        assertEquals(38, b.vectorQuantity.values[0].asIdd().max)
    }

    @Test fun vectorSumIntegerEvalDown() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::IntegerInRange {:>> range = "6..6,1..100,10..10";}
                feature b: Ranges::IntegerInRange = sum(a) {:>> range = "20..20";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val a = global.resolveVar("a")!!
        assertEquals(6, a.vectorQuantity.values[0].asIdd().min)
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
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val a = global.resolveVar("a")!!
        assertEquals(5, a.vectorQuantity.values[0].asIdd().min)
        assertEquals(10, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(20, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(55, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(20, a.vectorQuantity.values[2].asIdd().min)
        assertEquals(30, a.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSum2() = testSession("SI", "Ranges") {
        loadKerML("""  
            feature a: SI::Mass, Ranges::InRange {:>> range = "5..8,-4..-3,4..5";}
            feature b: SI::Mass = sum(a);
        """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val b = global.resolveVar("b")!!
        assertEquals(5.0, b.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(10.0, b.vectorQuantity.values[0].asAadd().max, 0.000001)
    }

    @Test fun vectorSumRealEvalDown() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::RealInRange { :>> range = "6..6, 1..100, 10..10"; }
                feature b: Ranges::RealInRange = sum(a) {:>> range = "20..20";}
            """)
        propagate()
        assertNoIssues()
        val a = global.resolveVar("a")!!
        assertEquals(6.0, a.min(), 0.000001)
        assertEquals(6.0, a.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(4.0, a.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(4.0, a.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(10.0, a.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(10.0, a.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorSumRealEvalDown2() = testSession("Ranges") {
        loadKerML("""  
                feature a: Ranges::RealInRange {:>> range = "5..10,1..100,20..30";}
                feature b: Ranges::RealInRange = sum(a) {:>> range = "60..80";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val a = global.resolveVar("a")!!
        assertEquals(5.0, a.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(10.0, a.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(20.0, a.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(55.0, a.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(20.0, a.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(30.0, a.vectorQuantity.values[2].asAadd().max, 0.000001)
    }



    @Disabled //TODO Eval Down of Power (row wise or scalar operand)
    @Test fun vectorPowerTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: ScalarValues::Real, Ranges::InRange {:>> range = "0..1,1..4,4..9";}
                feature b: ScalarValues::Real, Ranges::InRange {:>> range = "-2..2";}
                feature c: ScalarValues::Real = a ^ b;
        """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(8.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(81.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Disabled //TODO Eval Down of Power (row wise or scalar operand)
    @Test fun vectorPowerTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: ScalarValues::Integer, Ranges::InRange {:>> range = "0..1,1..2,3..4";}
                feature b: ScalarValues::Integer, Ranges::InRange {:>> range = "0..1,1..3,-2..2";}
                feature c: ScalarValues::Integer  = a ^ b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(1, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-8, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(8, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorNegateTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::QuantityInRange {:>> range = "0..1,1..2,3..4";}
                feature c: SI::Mass = -a;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-2.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(-1.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-4.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(-3.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorNegateTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "0..1,1..2,3..4";}
                feature c: ScalarValues::Integer  = -a;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-1, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(0, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(-1, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-4, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(-3, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorAbsTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange {:>> range = "3..3,0..0,4..4";}
                feature c: SI::Mass = abs(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorCityBlockTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange {:>> range = "3..3,0..0,4..4";}
                feature b: SI::Mass, Ranges::InRange {:>> range = "5..5,-3..-3,4..4";}
                feature c: SI::Mass = cityBlockDistance(a,b);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorCityBlockTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "3..3,0..0,4..4";} 
                feature b: Ranges::IntegerInRange {:>> range = "5..5,-3..-3,4..4";}
                feature c: ScalarValues::Integer = cityBlockDistance(a,b); """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(5, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(5, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorAbsTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "3..3,4..4,0..0";}
                feature c: ScalarValues::Integer  = abs(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(5, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(5, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorFloorTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange {:>> range = "0.5..1.5,6.2..9.0,-6.9..7.1";}
                feature c: SI::Mass = floor(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(6.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(9.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-7.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(7.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorCeilTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange {:>> range = "0.5..1.5, 6.2..9.01, -6.9..7.1";}
                feature c: SI::Mass = ceil(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(7.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(10.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-6.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(8.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorSqrtTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Area, Ranges::InRange {:>> range = "1..4,0.64..225,9..81";}
                feature c: SI::Length = sqrt(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!

        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(0.8, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(15.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(3.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(9.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorsSqrtTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..4,64..225,9..81";}
                feature c: ScalarValues::Integer  = sqrt(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(1, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(2, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(8, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(15, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(3, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(9, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSqrTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Length, Ranges::InRange {:>> range = "1..4,0.5..15.0,-3..2";}
                feature c: SI::Area = sqr(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!

        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(16.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(0.25, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(225.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(0.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(9.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorsSqrTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..4,5..15,-3..2";}
                feature c: ScalarValues::Integer  = sqr(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(1, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(16, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(25, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(225, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(0, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(9, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorLogTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1..4,1..10,2.5..4";}
                feature c: ScalarValues::Real = ln(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!

        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(1.3862943611198904, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(0.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(2.3025850929940455, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(0.916290731874155, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(1.3862943611198904, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorPow2TestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1..4,0..5,6..10";}
                feature c: ScalarValues::Real = pow2(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!

        assertEquals(2.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(16.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(32.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(64.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(1024.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorsPow2TestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..4,0..5,6..10";}
                feature c: ScalarValues::Integer  = pow2(a);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(2, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(16, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(32, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(64, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(1024, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorsToString() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..4,0..5,6..10";}
                feature b: Ranges::RealInRange {:>> range = "1..4,0..5,6..10";}
                feature c: Ranges::IntegerInRange = (4,5,6) {:>> range = "1..4,0..5,6..10";}
                feature d: Ranges::RealInRange = (4.0,5.0,6.0) {:>> range = "1..4,0..5,6..10";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        val d = global.resolveVar("d")!!
        assertEquals("(1..4, 0..5, 6..10)", a.vectorQuantity.toString())
        assertEquals("(1..4, 0..5, 6..10)", b.vectorQuantity.toString())
        assertEquals("(4, 5, 6)", c.vectorQuantity.toString())
        assertEquals("(4, 5, 6)", d.vectorQuantity.toString())
    }

    @Test fun vectorsToStringWithUnits() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Quantity, Ranges::QuantityInRange {:>> unit = "kg m / s"; :>> range = "1..4,0..5,6..10";}
                feature b: SI::Force, Ranges::InRange = (4.0,5.0,6.0) N {:>> range = "1..4,0..5,6..10";}
                feature c: SI::Speed, Ranges::QuantityInRange = (1.0,3.0,4.0) [km/h] {:>> unit = "km / h"; :>> range = "1..4,0..5,4..10";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        assertEquals("(1..4, 0..5, 6..10) kg m / s", a.vectorQuantity.toString())
        assertEquals("(4, 5, 6) N", b.vectorQuantity.toString())
        assertEquals("(1, 3, 4) km / h", c.vectorQuantity.toString())
    }

    @Test fun vectorCrossProductTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1..1,5..5,10..10";}
                feature b: Ranges::RealInRange {:>> range = "5..5,1..1,10..10";}
                feature c: ScalarValues::Real = a cross b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(40.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(40.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(40.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(40.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-24.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(-24.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorCrossProductTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..1,5..5,10..10";}
                feature b: Ranges::IntegerInRange {:>> range = "5..5,1..1,10..10";}
                feature c: ScalarValues::Integer  = a cross b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(40, c.vectorQuantity.values[0].asIdd().min)
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
                feature a: ScalarValues::Real, Ranges::InRange {:>> range = "-3..4,5..7,0..1";}
                feature b: ScalarValues::Real, Ranges::InRange {:>> range = "4..5,-2..-1,3..10";}
                feature c: ScalarValues::Real = a cross b.
            """
        )
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(15.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(72.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-40.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(35.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-43.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(-14.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorCrossProductTestInt2() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-3..4,5..7,0..1";}
                feature b: Ranges::IntegerInRange {:>> range = "4..5,-2..-1,3..10";}
                feature c: ScalarValues::Integer  = a cross b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(15, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(72, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-40, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(35, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-43, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(-14, c.vectorQuantity.values[2].asIdd().max)
    }

    @Disabled // Should have the same result as test with Int, but AA gives some strange results for independent values.
    @Test fun vectorDotProductTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: ScalarValues::Real, Ranges::InRange {:>> range = "-3..4,5..7,0..1";}
                feature b: ScalarValues::Real, Ranges::InRange {:>> range = "4..5,-2..-1,3..10";}
                feature c: ScalarValues::Real = a dot b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-29.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(25.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorDotProductTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-3..4,5..7,0..1";}
                feature b: Ranges::IntegerInRange {:>> range = "4..5,-2..-1,3..10";}
                feature c: ScalarValues::Integer  = a dot b;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-29, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(25, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorNormalizeTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "0..0,3..3,4..4";}
                feature b: Ranges::RealInRange {:>> range = "10..10,3..3,2..2";}
                feature c: ScalarValues::Real = norm(a);
                feature d: ScalarValues::Real = norm(b);
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        val d = global.resolveVar("d")!!
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(0.6, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(0.6, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(0.8, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(0.8, c.vectorQuantity.values[2].asAadd().max, 0.000001)
        assertEquals(0.9407208683835953, d.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(0.9407208683835953, d.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(0.28221626051507853, d.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(0.28221626051507853, d.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(0.18814417367671904, d.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(0.18814417367671904, d.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorAngleTestReal() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange {:>> range = "1..1,1..1,0..0";}
                feature b: SI::Mass, Ranges::InRange {:>> range = "1..1,0..0,0..0";}
                feature c: SI::Quantity, Ranges::QuantityInRange = angle(a,b) {:>> unit = "°";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals("45 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal2() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: SI::Mass, Ranges::InRange {:>> range = "1..1, 1..1, 0..0";}
                feature b: SI::Mass, Ranges::InRange {:>> range = "1..1, 1..1, 0..0";}
                feature c: SI::Quantity, Ranges::QuantityInRange = angle(a,b) {:>> unit = "°";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal3() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1..1, 1..1, 0..0";}
                feature b: Ranges::RealInRange {:>> range = "-1..-1, -1..-1, 0..0";}
                feature c: SI::Quantity = angle(a,b) {:>> unit = "°";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals("180 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal4() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1..1,5..5,10..10";}
                feature b: Ranges::RealInRange {:>> range = "5..5,2..2,-1..-1";}
                feature c: SI::Quantity = angle(a,b) {:>> unit = "°";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assertEquals("85.33527 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }


    @Test fun vectorAngleTestInt() = testSession("SI", "Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..1,1..1,0..0";}
                feature b: Ranges::IntegerInRange {:>> range = "1..1,-1..-1,0..0";}
                feature c: SI::Quantity = angle(a,b) {:>> unit = "°";}
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
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
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
    }

    @Test fun vectorPositionAccessTest() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1..1,5..5,10..10";}
                feature b: ScalarValues::Real = a[1];
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("b")!!
        assertEquals("5", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorPositionAccessTest2() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1..1,5..5,10..10";}
                feature b: ScalarValues::Real = a[1..2];
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("b")!!
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().max, 0.00001)
        assertEquals(10.0, c.vectorQuantity.values[1].asAadd().max, 0.0001)
        assertEquals(2,c.vectorQuantity.values.size)
    }

    @Test fun vectorPositionAccessTest3() = testSession("Ranges") {
        loadKerML("""
                feature a: ScalarValues::Real, Ranges::InRange {:>> range = "1..1,5..5,10..10";}
                feature b: ScalarValues::Real, Ranges::InRange {:>> range = "3..3,2..2,7..7";}
                feature c: ScalarValues::Real = a[1]+b[2];
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("c")!!
        assert(12.0 in c.vectorQuantity.values[0].asAadd())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    //Todo implement changing value at given vector position
    @Disabled @Test
    fun vectorPositionAccessTest4() = testSession("Ranges") {
        loadKerML("""
                feature a: ScalarValues::Real, Ranges::InRange {:>> range = "1..1,5..5,10..10";}
                feature a[1]: ScalarValues::Real = 2..2;
            """)
        propagate()
        assertTrue(status.issues.isEmpty() , status.issues.toString())
        val c = global.resolveVar("a")!!
        assert(2.0 in c.vectorQuantity.values[1].asAadd())
        assertEquals(3,c.vectorQuantity.values.size)
    }

}
