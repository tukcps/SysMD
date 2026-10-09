package constraintnettests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertIssue
import kotlin.test.assertEquals
import kotlin.test.Ignore
import kotlin.test.Test
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.assertTrue

class VectorTests {


    @Test
    fun vectorDefineTestReal() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::VectorQuantityValue = (0.5, 1.5) N { :>> range = (0.0..1.0, 1.0..2.0) [N]; }
            feature b: Quantities::VectorQuantityValue = (0.5, 1.5) N { :>> range = *..* [N]; }
            feature c: ISQ::CartesianForce3dVector  = (-5.0, -1.0, 3.0) N {:>> range = (-5.0..-1.0, -1.0..2.0, 2.0..4.0) [N]; }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        val c = solver.variable("c")
        assertBounds(listOf(0.5 .. 0.5, 1.5 .. 1.5), a)
        assertBounds(listOf(0.5 .. 0.5, 1.5 .. 1.5), b)
        assertBounds(listOf(-5.0 .. -5.0, -1.0 .. -1.0, 3.0 .. 3.0), c)
    }

    @Test fun vectorDefineTestRealError1() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature a: Quantities::VectorQuantityValue = (0.5,1.5) kg {:>> range = (0.0..1.0,1.0..2.0, 3.0..4.0) [kg];}
        """, Runlevel.ALL)
        assertTrue(status.issues.isNotEmpty() , status.issues.toString())
        assertEquals("Problem with vector size: Vector size of 2 does not match Constraint size of 3", status.issues.first().message)
    }

    @Test fun vectorDefineTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange = (0,1) {:>> range = (0..1,1..2);}
            feature b: Ranges::IntegerInRange = (0,1) {:>> range = (0..1,1..2);}
            feature c: Ranges::IntegerInRange = (-5, -1, 3) {:>> range = (-5..-1,-1..2, 2..4);}
            """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        val c = solver.variable("c")
        assertBounds(listOf(
            0L..0L, 1L..1L
        ), a)
        assertBounds(listOf(
            0L..0L, 1L..1L
        ), b)
        assertBounds(listOf(
            -5L..-5L, -1L..-1L, 3L..3L
        ), c)
    }

    @Test fun vectorDefineTestIntegerError1() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::IntegerInRange = (0, 1)  { :>> range = (0..1, 1..2, 0..3); }
        """, Runlevel.ALL)
        assertTrue(status.issues.isNotEmpty() , "An error should be reported")
        assertIssue("Problem with vector size: Vector size of 2 does not match Constraint size of 3", status.issues.first().message)
    }

    @Test fun vectorPlusTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianPosition3dVector {:>> range = (0..1, 1..2, 3..4) [m]; }
            feature b: ISQ::CartesianPosition3dVector {:>> range = (0..1, 1..3, -2..2) [m]; }
            feature c: ISQ::CartesianPosition3dVector = a + b;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(0.0 .. 2.0, 2.0 .. 5.0, 1.0 .. 6.0), c)
    }

    @Test fun vectorPlusTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = (0..1 ,1..2, 3..4);} 
            feature b: Ranges::IntegerInRange {:>> range = (0..1, 1..3, -2..2);}
            feature c: ScalarValues::Integer  = a + b;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(0L..2L, 2L..5L, 1L..6L), c)
    }

    @Test fun vectorMinusTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianVelocity3dVector  = (0.0..1.0,1.0..2.0,3.0..4.0) [m/s]; 
            feature b: ISQ::CartesianVelocity3dVector { :>> range = (0..1, 1..3, -2..2) [m/s]; } 
            feature c: ISQ::CartesianVelocity3dVector = a - b;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(-1.0 .. 1.0, -2.0 .. 1.0, 1.0 .. 6.0), c)
    }

    @Test fun vectorMinusTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange { :>> range = (0..1,1..2,3..4);}
            feature b: Ranges::IntegerInRange { :>> range = (0..1,1..3,-2..2);}
            feature c: ScalarValues::Integer  = a - b;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(
            -1L..1L, -2L..1L, 1L..6L
        ), c)
    }

    @Test fun vectorScalarMultiplicationTestReal() = testSession("Ranges") {
        loadKerML("""  
                feature a: ISQ::CartesianAcceleration3dVector { :>> range = (0..6, 6..12, 4..20) [m/s^2];}
                feature b: ISQ::DimensionOneValue { :>> range = -2..3;} 
                feature c: ISQ::CartesianAcceleration3dVector = a * b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(-12.0 .. 18.0, -24.0 .. 36.0, -40.0 .. 60.0), c)
    }


    @Test
    fun vectorTestInheritanceMixedDefintions() = testSession("ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            part def InstallationSpace {
                attribute positionOfSpace: CartesianPosition3dVector { :>> range = (-1.5..6.0, -1.25..1.25, -0.5..4.0) [m];} 
            }
            part def FrontSpace :> InstallationSpace{
                :>> positionOfSpace: CartesianPosition3dVector = (-0.9, 0.0, 0.2) [m];
            }
            part def Location {
                part space : InstallationSpace;
                attribute relativePosition : CartesianPosition3dVector {:>> range= (0.0..4.0, -1.25..1.25, -0.5..4.0) [m];}
                attribute position: CartesianPosition3dVector = space::positionOfSpace + relativePosition;
            }
            part def TopViewLeftLoc :> Location {
                :>> relativePosition = (0.4, -1.0, 0.3) [m];
                part space : FrontSpace;
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
    }

    @Test fun vectorPowerTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::RealInRange {:>> range = (0..1, 1..4, 4..9);}
                feature b: Ranges::RealInRange {:>> range = -2..2;}
                feature c: Ranges::RealInRange = a ^ b;
        """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(Double.NEGATIVE_INFINITY .. Double.POSITIVE_INFINITY, (1.0 / 16.0) .. 16.0, (1.0 / 81.0) .. 81.0), c)
    }


    @Test fun vectorNegateTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianPosition3dVector {:>> range = (0..1, 1..2, 3..4) [m];}
            feature c: ISQ::CartesianPosition3dVector = -a;
        """, Runlevel.ALL)
        assertNoIssues()
        val c = solver.variable("c")
        assertEquals("(-1..0, -2..-1, -4..-3) m", c.vectorQuantity.toString())
    }

    @Test fun vectorNegateTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = (0..1,1..2,3..4);}
                feature c: ScalarValues::Integer  = -a;
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(
            -1L..0L, -2L..-1L, -4L..-3L
        ), c)
    }

    @Test fun vectorAbsTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianVelocity3dVector { :>> range = (3..3, 0..0, 4..4) [m/s];}
            feature c: ISQ::SpeedValue = abs(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(5.0 .. 5.0, c)
    }

    @Test fun vectorLenTestReal() = testSession("ISQ") {
        loadKerML("""
                feature a: ISQ::CartesianAcceleration3dVector {:>> range = (3..3,0..0,4..4);}
                feature c: ScalarValues::Integer = size(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(3L..3L, c)
    }

    @Test fun vectorCityBlockTestReal() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::CartesianPosition3dVector {:>> range = (3..3,0..0,4..4) [m];}
            feature b: ISQ::CartesianPosition3dVector {:>> range = (5..5,-3..-3,4..4) [m];}
            feature c: ISQ::LengthValue = cityBlockDistance(a, b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(5.0 .. 5.0, c)
        assertEquals(1, c.vectorQuantity.values.size)
    }

    @Test fun vectorCityBlockTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = (3..3,0..0,4..4);} 
            feature b: Ranges::IntegerInRange {:>> range = (5..5,-3..-3,4..4);}
            feature c: ScalarValues::Integer = cityBlockDistance(a,b); 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(5L .. 5L, c)
    }

    @Test fun vectorAbsTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = (3..3,4..4,0..0);}
            feature c: ScalarValues::Integer  = abs(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(5L .. 5L, c)
    }

    @Test fun vectorFloorTestReal() = testSession("ISQ") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = (0.5..1.5,6.2..9.0,-6.9..7.1) [N];}
                feature c: ISQ::CartesianForce3dVector = floor(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(0.0 .. 1.0, 6.0 .. 9.0, -7.0 .. 7.0), c)
    }

    @Test fun vectorCeilTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = (0.5..1.5, 6.2..9.01, -6.9..7.1) [N];}
                feature c: ISQ::CartesianForce3dVector = ceil(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(1.0 .. 2.0, 7.0 .. 10.0, -6.0 .. 8.0), c)
    }

    @Test fun vectorSqrtTestReal() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::VectorQuantityValue {:>> range = (1..4,0.64..225,9..81);}
            feature c: Ranges::RealInRange = sqrt(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")

        assertBounds(listOf(1.0 .. 2.0, 0.8 .. 15.0, 3.0 .. 9.0), c)
    }

    @Test fun vectorsSqrtTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = (1..4, 64..225, 9..81);}
            feature c: ScalarValues::Integer  = sqrt(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(1L..2L, 8L..15L, 3L..9L), c)
    }

    @Test fun vectorSqrTestReal() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::VectorQuantityValue { :>> range = (1..4, 0.5..15.0, -3..2); }
            feature c: Quantities::VectorQuantityValue = sqr(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(1.0 .. 16.0, 0.25 .. 225.0, 0.0 .. 9.0), c)
    }

    @Test fun vectorsSqrTestInt() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = (1..4, 5..15, -3..2);}
            feature c: Ranges::IntegerInRange  = sqr(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(1L..16L, 25L..225L, 0L..9L), c)
    }

    @Test fun vectorLogTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue {:>> range = (1..4,1..10,2.5..4);}
                feature c: ScalarValues::Real = ln(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")

        assertBounds(listOf(
            0.0 .. 1.3862943611198904,
            0.0 .. 2.3025850929940455,
            0.916290731874155 .. 1.3862943611198904
        ), c)
    }

    @Test fun vectorPow2TestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: Quantities::VectorQuantityValue {:>> range = (1..4, 0..5, 6..10);}
                feature c: ScalarValues::Real = pow2(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")

        assertBounds(listOf(
            2.0 .. 16.0, 1.0 .. 32.0, 64.0 .. 1024.0
         ), c)
    }

    @Test
    fun vectorsPow2TestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = (1..4, 0..5, 6..10);}
                feature c: ScalarValues::Integer  = pow2(a);
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(
            2L..16L, 1L..32L, 64L..1024L
        ), c)
    }

    @Test
    fun vectorsToString() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = (1..4,0..5,6..10);}
            feature b: Quantities::VectorQuantityValue {:>> range = (1..4,0..5,6..10);}
            feature c: Ranges::IntegerInRange = (4,5,6) {:>> range = (1..4,0..5,6..10);}
            feature d: Quantities::VectorQuantityValue = (4.0,5.0,6.0) {:>> range = (1..4,0..5,6..10);}
        """, Runlevel.ALL)
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        val c = solver.variable("c")
        val d = solver.variable("d")
        assertEquals("(1..4, 0..5, 6..10)", a.vectorQuantity.toString())
        assertEquals("(1..4, 0..5, 6..10)", b.vectorQuantity.toString())
        assertEquals("(4, 5, 6)", c.vectorQuantity.toString())
        assertEquals("(4, 5, 6)", d.vectorQuantity.toString())
    }

    @Test fun vectorsToStringWithUnits() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature a: Quantities::VectorQuantityValue { :>> range = (1..4,0..5,6..10) [kg m / s];}
            feature b: ISQ::CartesianForce3dVector = (4.0,5.0,6.0) [N] { :>> range = (1..4, 0..5, 6..10) [N];}
            feature c: ISQ::CartesianVelocity3dVector = (1.0,3.0,4.0) [km/h] { :>> range = (1..4,0..5,4..10) [km / h];}
        """, Runlevel.ALL)
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        val c = solver.variable("c")
        assertEquals("(1..4, 0..5, 6..10) kg m / s", a.vectorQuantity.toString())
        assertEquals("(4, 5, 6) N", b.vectorQuantity.toString())
        assertEquals("(1, 3, 4) km / h", c.vectorQuantity.toString())
    }

    @Test fun vectorCrossProductTestReal() = testSession("Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianForce3dVector {:>> range = (1..1, 5..5, 10..10) [N];}
            feature b: ISQ::CartesianPosition3dVector {:>> range = (5..5, 1..1, 10..10) [m];}
            feature c: ISQ::CartesianMomentOfForce3dVector = a cross b;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(40.0 .. 40.0, 40.0 .. 40.0, -24.0 .. -24.0), c)
    }

    @Test fun vectorCrossProductTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = (1..1,5..5,10..10);}
                feature b: Ranges::IntegerInRange {:>> range = (5..5,1..1,10..10);}
                feature c: ScalarValues::Integer  = a cross b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(
            40L..40L, 40L..40L, -24L..-24L
        ), c)
    }

    @Ignore // Should have the same result as test with Int, but AA gives some strange results for independent values.
    @Test fun vectorCrossProductTestReal2() = testSession("Ranges") {
        loadKerML(
            """
                feature a: ISQ::CartesianForce3dVector {:>> range = (-3..4,5..7,0..1) [N];}
                feature b: ISQ::CartesianPosition3dVector {:>> range = (4..5,-2..-1,3..10) [m];}
                feature c: ISQ::CartesianMomentOfForce3dVector = a cross b.
            """
        )
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(
            15.0 .. 72.0,
            -40.0 .. 35.0,
            -43.0 .. -14.0
        ), c)
    }

    @Test fun vectorCrossProductTestInt2() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = (-3..4,5..7,0..1);}
                feature b: Ranges::IntegerInRange {:>> range = (4..5,-2..-1,3..10);}
                feature c: ScalarValues::Integer  = a cross b;
            """)
        solver.propagate()
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(listOf(15L..72L, -40L..35L, -43L..-14L), c)
    }

    @Ignore // Should have the same result as test with Int, but AA gives some strange results for independent values.
    @Test fun vectorDotProductTestReal() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = "-3..4,5..7,0..1";}
                feature b: ISQ::CartesianPosition3dVector {:>> range = "4..5,-2..-1,3..10";}
                feature c: ISQ::CartesianMomentOfForce3dVector = a dot b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(-29.0 .. 25.0, c)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorDotProductTestInt() = testSession("Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = (-3..4,5..7,0..1);}
                feature b: Ranges::IntegerInRange {:>> range = (4..5,-2..-1,3..10);}
                feature c: ScalarValues::Integer  = a dot b;
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(-29L .. 25L, c)
    }

    @Test fun vectorNormalizeTestReal() = testSession("Ranges") {
        loadKerML("""
            feature a: Quantities::VectorQuantityValue {:>> range = (0..0,3..3,4..4);}
            feature b: Quantities::VectorQuantityValue {:>> range = (10..10,3..3,2..2);}
            feature c: ScalarValues::Real = norm(a);
            feature d: ScalarValues::Real = norm(b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        val d = solver.variable("d")
        assertBounds(listOf(
            0.0 .. 0.0, 0.6 .. 0.6, 0.8 .. 0.8
        ), c)
        assertBounds(listOf(
            0.9407208683835973,
            0.2822162605150792,
            0.18814417367671946
        ).map { it .. it }, d)
    }

    @Test fun vectorAngleTestReal() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianMomentOfForce3dVector { :>> range = (1..1,1..1,0..0);}
            feature b: ISQ::CartesianMomentOfForce3dVector { :>> range = (1..1,0..0,0..0);}
            feature c: ISQ::DimensionOneValue = angle(a,b) { :>> range = -1000 .. 1000 [°];}
        """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertEquals("45 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal2() = testSession("ISQ") {
        loadKerML("""
                feature a: ISQ::CartesianForce3dVector {:>> range = (1..1, 1..1, 0..0);}
                feature b: ISQ::CartesianForce3dVector {:>> range = (1..1, 1..1, 0..0);}
                feature c: ISQ::DimensionOneValue = angle(a,b) {:>> range = (*..*) [°];}
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(0.0, c)
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal3() = testSession("ISQ") {
        loadKerML("""
                feature a: ISQ::CartesianMomentum3dVector {:>> range = (1..1, 1..1, 0..0);}
                feature b: ISQ::CartesianMomentum3dVector {:>> range = (-1..-1, -1..-1, 0..0);}
                feature c: ISQ::DimensionOneValue = angle(a,b) {:>> range = *..* [°];}
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertEquals("180 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }

    @Test fun vectorAngleTestReal4() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::CartesianForce3dVector { :>> range = (1..1, 5..5, 10..10) [N];}
            feature b: ISQ::CartesianForce3dVector { :>> range = (5..5, 2..2, -1..-1) [N];}
            feature c: ISQ::DimensionOneValue = angle(a,b) {:>> range = *..* [°];}
        """, Runlevel.ALL)
        assertNoIssues()
        val c = solver.variable("c")
        assertEquals("85.335 °", c.vectorQuantity.toString())
        assertEquals(1,c.vectorQuantity.values.size)
    }


    @Test fun vectorAngleTestInt() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange { :>> range = (1..1, 1..1, 0..0); }
            feature b: Ranges::IntegerInRange { :>> range = (1..1, -1..-1, 0..0); }
            feature c: ISQ::DimensionOneValue = angle(a,b) { :>> range = *..* [°]; }
        """, Runlevel.ALL)
        assertNoIssues()
        val c = solver.variable("c")
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

    @Test fun vectorPositionAccessTest() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::CartesianPosition3dVector { :>> range = (1..1, 5..5, 10..10) [m]; }
            feature b: ISQ::LengthValue = a[1];
        """, Runlevel.ALL)
        assertNoIssues()
        val b = solver.variable("b")
        assertEquals("5 m", b.vectorQuantity.toString())
        assertEquals(1, b.vectorQuantity.values.size)
    }

    @Test fun vectorPositionAccessTest2() = testSession("Ranges") {
        loadKerML("""
                feature a: ISQ::CartesianPosition3dVector {:>> range = (1..1,5..5,10..10) [m];}
                feature b: Quantities::VectorQuantityValue = a[1..2]  {:>> range = (*..*) [m];}
            """)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("b")
        assertBounds(listOf(5.0..5.0, 10.0..10.0), c)
    }

    @Test fun vectorPositionAccessTest3() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::CartesianPosition3dVector {:>> range = (1..1,5..5,10..10) [m];}
            feature b: ISQ::CartesianPosition3dVector {:>> range = (3..3,2..2,7..7) [m];}
            feature c: ISQ::LengthValue = a[1]+b[2];
        """, Runlevel.ALL)
        assertNoIssues()
        val c = solver.variable("c")
        assert(12.0 in c.vectorQuantity.values[0].asAadd())
        solver.propagate()
        assertNoIssues()
        assertEquals(1,c.vectorQuantity.values.size)
    }

    //Todo implement changing value at given vector position
    @Ignore @Test
    fun vectorPositionAccessTest4() = testSession("Ranges") {
        loadKerML("""
            feature a: ISQ::CartesianPosition3dVector {:>> range = (1..1,5..5,10..10) [m];}
            feature a[1]: ISQ::LengthValue = 2..2;
        """, Runlevel.ALL)
        val c = solver.variable("a")
        assert(2.0 in c.vectorQuantity.values[1].asAadd())
        assertEquals(3,c.vectorQuantity.values.size)
    }

}
