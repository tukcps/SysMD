package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.SysMDInfo
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VectorTests {

    @Test fun vectorDefineTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0.0..1.0,1.0..2.0) [kg] = (0.5,1.5) kg;
                feature b: ScalarValues::Real [kg] = (0.5,1.5) kg;
                feature c: ScalarValues::Real(-5.0..-1.0,-1.0..2.0, 2.0..4.0) [kg] = (-5.0, -1.0, 3.0) kg.
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
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

    @Test fun vectorDefineTestRealError1() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0.0..1.0,1.0..2.0, 3.0..4.0) [kg] = (0.5,1.5) kg;
            """)
        propagate()
        assertTrue(status.exceptions.isNotEmpty() , status.exceptions.toString())
        assertEquals("Internal error: Problem with vector size: Vector size of 2 does not match Constraint size of 3", status.exceptions.first().message)
    }

    @Test fun vectorDefineTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(0..1,1..2) = (0,1);
                feature b: ScalarValues::Integer(0..1,1..2) = (0,1);
                feature c: ScalarValues::Integer(-5..-1,-1..2, 2..4) = (-5, -1, 3).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
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

    @Test fun vectorDefineTestIntegerError1() = testSession {
        loadSysMD("""
                feature b: ScalarValues::Integer(0..1,1..2,0..3) = (0,1).
            """)
        propagate()
        assertTrue(status.exceptions.isNotEmpty() , "An error should be reported")
        assertEquals("Internal error: Problem with vector size: Vector size of 2 does not match Constraint size of 3", status.exceptions.first().message)
    }

    @Test fun vectorPlusTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0..1,1..2,3..4) [kg];
                feature b: ScalarValues::Real(0..1,1..3,-2..2) [kg];
                feature c: ScalarValues::Real [kg] = a + b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(5.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(6.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorPlusTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(0..1,1..2,3..4);
                feature b: ScalarValues::Integer(0..1,1..3,-2..2);
                feature c: ScalarValues::Integer  = a + b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(2, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(5, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorMinusTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0..1,1..2,3..4) [kg];
                feature b: ScalarValues::Real(0..1,1..3,-2..2) [kg];
                feature c: ScalarValues::Real [kg] = a - b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-2.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(6.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorMinusTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(0..1,1..2,3..4);
                feature b: ScalarValues::Integer(0..1,1..3,-2..2);
                feature c: ScalarValues::Integer  = a - b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-1, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(1, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(1, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorScalarMultiplicationTestReal() = testSession {
        loadSysMD("""  
                feature a: ScalarValues::Real(0..6,6..12,4..20) [kg];
                feature b: ScalarValues::Real(-2..3); 
                feature c: ScalarValues::Real [kg] = a * b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-12.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(18.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-24.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(36.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-40.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(60.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorSum() = testSession {
        loadSysMD("""  
                attribute a: ScalarValues::Real(0..6,6..12,4..20) [kg];
                attribute b: ScalarValues::Real [kg] = sum(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val b = global.resolveVar("b")!!
        assertEquals(10.0, b.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(38.0, b.vectorQuantity.values[0].asAadd().max, 0.000001)
    }

    @Test fun vectorSumInteger() = testSession {
        loadSysMD("""  
                attribute a: ScalarValues::Integer(0..6,6..12,4..20);
                attribute b: ScalarValues::Integer = sum(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val b = global.resolveVar("b")!!
        assertEquals(10, b.vectorQuantity.values[0].asIdd().min)
        assertEquals(38, b.vectorQuantity.values[0].asIdd().max)
    }

    @Test fun vectorSumIntegerEvalDown() = testSession {
        loadSysMD("""  
                attribute a: ScalarValues::Integer(6..6,1..100,10..10);
                attribute b: ScalarValues::Integer(20..20) = sum(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val a = global.resolveVar("a")!!
        assertEquals(6, a.vectorQuantity.values[0].asIdd().min)
        assertEquals(6, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(4, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(4, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(10, a.vectorQuantity.values[2].asIdd().min)
        assertEquals(10, a.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSumIntegerEvalDown2() = testSession {
        loadSysMD("""  
                attribute a: ScalarValues::Integer(5..10,1..100,20..30);
                attribute b: ScalarValues::Integer(60..80) = sum(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val a = global.resolveVar("a")!!
        assertEquals(5, a.vectorQuantity.values[0].asIdd().min)
        assertEquals(10, a.vectorQuantity.values[0].asIdd().max)
        assertEquals(20, a.vectorQuantity.values[1].asIdd().min)
        assertEquals(55, a.vectorQuantity.values[1].asIdd().max)
        assertEquals(20, a.vectorQuantity.values[2].asIdd().min)
        assertEquals(30, a.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSum2() = testSession {
        loadSysMD("""  
                attribute a: ScalarValues::Real(5..8,-4..-3,4..5) [kg];
                attribute b: ScalarValues::Real [kg] = sum(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val b = global.resolveVar("b")!!
        assertEquals(5.0, b.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(10.0, b.vectorQuantity.values[0].asAadd().max, 0.000001)
    }

    @Test fun vectorSumRealEvalDown() = testSession {
        loadSysMD("""  
                attribute a: ScalarValues::Real(6..6,1..100,10..10);
                attribute b: ScalarValues::Real(20..20) = sum(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val a = global.resolveVar("a")!!
        assertEquals(6.0, a.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(6.0, a.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(4.0, a.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(4.0, a.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(10.0, a.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(10.0, a.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorSumRealEvalDown2() = testSession {
        loadSysMD("""  
                attribute a: ScalarValues::Real(5..10,1..100,20..30);
                attribute b: ScalarValues::Real(60..80) = sum(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val a = global.resolveVar("a")!!
        assertEquals(5.0, a.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(10.0, a.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(20.0, a.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(55.0, a.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(20.0, a.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(30.0, a.vectorQuantity.values[2].asAadd().max, 0.000001)
    }



    @Disabled //TODO Eval Down of Power (row wise or scalar operand)
    @Test fun vectorPowerTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0..1,1..4,4..9);
                feature b: ScalarValues::Real(-2..2);
                feature c: ScalarValues::Real = a ^ b;
                """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(8.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(81.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Disabled //TODO Eval Down of Power (row wise or scalar operand)
    @Test fun vectorPowerTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(0..1,1..2,3..4);
                feature b: ScalarValues::Integer(0..1,1..3,-2..2);
                feature c: ScalarValues::Integer  = a ^ b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(1, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(6, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-8, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(8, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorNegateTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0..1,1..2,3..4) [kg];
                feature c: ScalarValues::Real [kg] = -a;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-2.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(-1.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-4.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(-3.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorNegateTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(0..1,1..2,3..4);
                feature c: ScalarValues::Integer  = -a;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-1, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(0, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-2, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(-1, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-4, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(-3, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorAbsTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(3..3,0..0,4..4) [kg];
                feature c: ScalarValues::Real [kg] = abs(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorCityBlockTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(3..3,0..0,4..4) [kg];
                feature b: ScalarValues::Real(5..5,-3..-3,4..4) [kg];
                feature c: ScalarValues::Real [kg] = cityBlockDistance(a,b);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(5.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorCityBlockTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(3..3,0..0,4..4); 
                feature b: ScalarValues::Integer(5..5,-3..-3,4..4); 
                feature c: ScalarValues::Integer = cityBlockDistance(a,b); """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(5, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(5, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorAbsTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(3..3,4..4,0..0);
                feature c: ScalarValues::Integer  = abs(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(5, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(5, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorFloorTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0.5..1.5,6.2..9.0,-6.9..7.1) [kg];
                feature c: ScalarValues::Real [kg] = floor(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(6.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(9.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-7.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(7.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorCeilTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0.5..1.5,6.2..9.0,-6.9..7.1) [kg];
                feature c: ScalarValues::Real [kg] = ceil(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(7.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(10.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-6.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(8.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorSqrtTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..4,0.64..225,9..81) [m^2];
                feature c: ScalarValues::Real [m] = sqrt(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!

        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(2.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(0.8, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(15.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(3.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(9.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorsSqrtTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(1..4,64..225,9..81);
                feature c: ScalarValues::Integer  = sqrt(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(1, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(2, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(8, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(15, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(3, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(9, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorSqrTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..4,0.5..15.0,-3..2) [m];
                feature c: ScalarValues::Real [m^2] = sqr(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!

        assertEquals(1.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(16.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(0.25, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(225.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(0.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(9.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorsSqrTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(1..4,5..15,-3..2);
                feature c: ScalarValues::Integer  = sqr(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(1, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(16, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(25, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(225, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(0, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(9, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorLogTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..4,1..10,2.5..4);
                feature c: ScalarValues::Real = ln(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!

        assertEquals(0.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(1.3862943611198904, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(0.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(2.3025850929940455, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(0.916290731874155, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(1.3862943611198904, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorPow2TestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..4,0..5,6..10);
                feature c: ScalarValues::Real = pow2(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!

        assertEquals(2.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(16.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(32.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(64.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(1024.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorsPow2TestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(1..4,0..5,6..10);
                feature c: ScalarValues::Integer  = pow2(a);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(2, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(16, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(32, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(64, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(1024, c.vectorQuantity.values[2].asIdd().max)
    }

    @Test fun vectorsToString() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(1..4,0..5,6..10);
                feature b: ScalarValues::Real(1..4,0..5,6..10);
                feature c: ScalarValues::Integer(1..4,0..5,6..10) = (4,5,6);
                feature d: ScalarValues::Real(1..4,0..5,6..10) = (4.0,5.0,6.0);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        val d = global.resolveVar("d")!!
        assertEquals("(1..4, 0..5, 6..10)", a.vectorQuantity.toString())
        assertEquals("(1..4, 0..5, 6..10)", b.vectorQuantity.toString())
        assertEquals("(4, 5, 6)", c.vectorQuantity.toString())
        assertEquals("(4, 5, 6)", d.vectorQuantity.toString())
    }

    @Test fun vectorsToStringWithUnits() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..4,0..5,6..10) [kg m / s];
                feature b: ScalarValues::Real(1..4,0..5,6..10)  [N]= (4.0,5.0,6.0) N;
                feature c: ScalarValues::Real(1..4,0..5,4..10)  [km/h] = (1.0,3.0,4.0) [km/h];
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        assertEquals("(1..4, 0..5, 6..10) kg m / s", a.vectorQuantity.toString())
        assertEquals("(4, 5, 6) N", b.vectorQuantity.toString())
        assertEquals("(1, 3, 4) km / h", c.vectorQuantity.toString())
    }

    @Test fun vectorCrossProductTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,5..5,10..10);
                feature b: ScalarValues::Real(5..5,1..1,10..10);
                feature c: ScalarValues::Real = a cross b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(40.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(40.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(40.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(40.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-24.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(-24.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorCrossProductTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(1..1,5..5,10..10);
                feature b: ScalarValues::Integer(5..5,1..1,10..10);
                feature c: ScalarValues::Integer  = a cross b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(40, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(40, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(40, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(40, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-24, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(-24, c.vectorQuantity.values[2].asIdd().max)
    }

    @Disabled // Should have the same result as test with Int, but AA gives some strange results for independent values.
    @Test fun vectorCrossProductTestReal2() = testSession {
        loadSysMD(
            """
                feature a: ScalarValues::Real(-3..4,5..7,0..1);
                feature b: ScalarValues::Real(4..5,-2..-1,3..10);
                feature c: ScalarValues::Real = a cross b.
            """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(15.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(72.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(-40.0, c.vectorQuantity.values[1].asAadd().min, 0.000001)
        assertEquals(35.0, c.vectorQuantity.values[1].asAadd().max, 0.000001)
        assertEquals(-43.0, c.vectorQuantity.values[2].asAadd().min, 0.000001)
        assertEquals(-14.0, c.vectorQuantity.values[2].asAadd().max, 0.000001)
    }

    @Test fun vectorCrossProductTestInt2() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(-3..4,5..7,0..1);
                feature b: ScalarValues::Integer(4..5,-2..-1,3..10);
                feature c: ScalarValues::Integer  = a cross b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(15, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(72, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(-40, c.vectorQuantity.values[1].asIdd().min)
        assertEquals(35, c.vectorQuantity.values[1].asIdd().max)
        assertEquals(-43, c.vectorQuantity.values[2].asIdd().min)
        assertEquals(-14, c.vectorQuantity.values[2].asIdd().max)
    }

    @Disabled // Should have the same result as test with Int, but AA gives some strange results for independent values.
    @Test fun vectorDotProductTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(-3..4,5..7,0..1),
                feature b: ScalarValues::Real(4..5,-2..-1,3..10),
                feature c: ScalarValues::Real = a dot b.
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-29.0, c.vectorQuantity.values[0].asAadd().min, 0.000001)
        assertEquals(25.0, c.vectorQuantity.values[0].asAadd().max, 0.000001)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorDotProductTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(-3..4,5..7,0..1);
                feature b: ScalarValues::Integer(4..5,-2..-1,3..10);
                feature c: ScalarValues::Integer  = a dot b;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals(-29, c.vectorQuantity.values[0].asIdd().min)
        assertEquals(25, c.vectorQuantity.values[0].asIdd().max)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorNormalizeTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(0..0,3..3,4..4);
                feature b: ScalarValues::Real(10..10,3..3,2..2);
                feature c: ScalarValues::Real = norm(a);
                feature d: ScalarValues::Real = norm(b);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
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

    @Test fun vectorAngleTestReal() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,1..1,0..0) [kg];
                feature b: ScalarValues::Real(1..1,0..0,0..0) [kg];
                feature c: ScalarValues::Real [°] = angle(a,b).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals("45 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal2() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,1..1,0..0) [kg];
                feature b: ScalarValues::Real(1..1,1..1,0..0) [kg];
                feature c: ScalarValues::Real [°] = angle(a,b).
            """)
        propagate()
        assertTrue(status.exceptions.any {it !is SysMDInfo} , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals("7.09198e-6 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal3() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,1..1,0..0);
                feature b: ScalarValues::Real(-1..-1,-1..-1,0..0);
                feature c: ScalarValues::Real [°] = angle(a,b);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals("180 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal4() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,5..5,10..10);
                feature b: ScalarValues::Real(5..5,2..2,-1..-1);
                feature c: ScalarValues::Real [°] = angle(a,b).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals("85.33527 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }


    @Test fun vectorAngleTestInt() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Integer(1..1,1..1,0..0);
                feature b: ScalarValues::Integer(-1..-1,-1..-1,0..0);
                feature c: ScalarValues::Real [°] = angle(a,b);
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assertEquals("90 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun inheritanceOfVectorTest() = testSession {
        loadSysMD("""
            package InstallationSpaces {
                class InstallationSpace;
                class EngineCompartment isA InstallationSpace; 
            }

            InstallationSpaces::InstallationSpace hasA 
                feature origin: ScalarValues::Real(*..*, *..*,*..*);
                feature vertex1: ScalarValues::Real = origin.
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
    }

    @Test fun vectorPositionAccessTest() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,5..5,10..10);
                feature b: ScalarValues::Real = a[1].
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("b")!!
        assertEquals("5", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorPositionAccessTest2() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,5..5,10..10);
                feature b: ScalarValues::Real = a[1..2].
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("b")!!
        assertEquals("5", c.vectorQuantity.values[0].toString())
        assertEquals("10", c.vectorQuantity.values[1].toString())
        assertEquals(2,c.vectorQuantity.values.size)
    }

    @Test fun vectorPositionAccessTest3() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,5..5,10..10);
                feature b: ScalarValues::Real(3..3,2..2,7..7);
                feature c: ScalarValues::Real = a[1]+b[2].
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("c")!!
        assert(12.0 in c.vectorQuantity.values[0].asAadd())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    //Todo implement changing value at given vector position
    @Disabled @Test fun vectorPositionAccessTest4() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Real(1..1,5..5,10..10);
                feature a[1]: ScalarValues::Real = 2..2.
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty() , status.exceptions.toString())
        val c = global.resolveVar("a")!!
        assert(2.0 in c.vectorQuantity.values[1].asAadd())
        assertEquals(3,c.vectorQuantity.values.size)
    }

}
