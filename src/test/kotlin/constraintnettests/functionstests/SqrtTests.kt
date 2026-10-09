package constraintnettests.functionstests

import com.github.tukcps.sysmd.compareTo
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.values.bounds.DoubleBound
import kotlin.test.assertEquals
import util.*
import util.mockup.loadKerML
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertTrue

class SqrtTests {

        /** ConstNet shall compute bottom-up with sqrt in real and model.builder.range */
        @Test
        fun evalUpWithSqrt_real_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = 2.0 .. 9.0;}
                feature b: ScalarValues::Real = sqrt(a);"""

            )
            solver.propagate()
            assertBounds(1.414213562373095..3.0, solver.variable("b"))
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqrt in real and value */
        @Test
        fun propagateWithSqrt_real_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange = 3.0 {:>> range = 2.0 .. 5.0;}
                feature b: ScalarValues::Real = sqrt(a);""")
            solver.propagate()
            assertBounds(sqrt(3.0) .. sqrt(3.0), solver.variable("b"))
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqrt and negative value. This should lead to an error*/
        @Test
        fun propagateWithSqrt_real_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -10.0 .. -5.0;}
                feature b: ScalarValues::Real = sqrt( a );
                """)
            solver.propagate()
            assertEquals(1, status.issues.size, "Issues: ${status.issues}")
            assertEquals(Issue.Kind.WARN_INCONSISTENCY, status.issues.firstOrNull()?.kind)
        }

        /** ConstNet shall compute bottom-up with sqrt in int and model.builder.range */
        @Test
        fun evalUpWithSqrt_int_range() = testSession("Ranges") {
                loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 2 .. 9;}
                feature b: ScalarValues::Integer = sqrt(a);""")
                solver.propagate()
                assertBounds(
                    floor(sqrt(2.0)).toLong() .. ceil(sqrt(9.0)).toLong(),
                    solver.variable("b")
                )
                assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
                assertNoIssues()
        }

        @Test
        fun evalDownWithSqrt_int_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 4 .. 25;}
                feature b: Ranges::IntegerInRange = sqrt(a) {:>> range = 3 .. 3;}""")
            solver.propagate()
            assertBounds(9L .. 9L, solver.variable("a"))
            assertEquals("1", solver.variable("a").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalDownWithSqrt_int() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 4 .. 25;}
                feature b: Ranges::IntegerInRange = min(sqrt(a), 4) {:>> range = 3;}""")
            solver.propagate()
            assertBounds(9L .. 9L, solver.variable("a"))
            assertEquals("1", solver.variable("a").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqrt in int **/
        @Test
        fun evalUpWithSqrt_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange = 3 {:>> range = 2 .. 9;}
                feature b: ScalarValues::Integer = sqrt(a); """
            )
            solver.propagate()
            assertBounds(floor(sqrt(3.0)).toLong() .. ceil(sqrt(3.0)).toLong(), solver.variable("b"))
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithSqrt_mixed_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -4.0 .. 9.0;}
                feature b: ScalarValues::Real = sqrt(a);
            """)
            solver.propagate()
            assertNoIssues()
            // Sqrt of mixed range truncates the negative part
            assertBounds(0.0 .. 3.0, solver.variable("b"))
        }

        /** Sqrt with ISQ unit: sqrt(AreaValue) -> LengthValue, check unit propagation */
        @Test
        fun sqrt_unit1() = testSession("ISQ", "Ranges") {
            loadKerML("""
               feature a: ISQ::AreaValue {:>> range = 4.0..9.0 [m m];}
               feature b: ISQ::LengthValue  = sqrt(a); 
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertEquals("m", solver.variable("b").vectorQuantity.unit.toString())
            assertBounds(2.0 .. 3.0, solver.variable("b"))
        }

        /** Sqrt with Ohm^2 unit, checks string output */
        @Test
        fun sqrt_unit2() = testSession("ISQ", "Ranges") {
            loadKerML("""
                feature a: Quantities::ScalarQuantityValue {:>> range = 4..9 [Ohm^2]; }
                feature b: ISQ::ResistanceValue = sqrt(a); 
            """, Runlevel.ALL)
            assertNoIssues()
            assertEquals("2..3 \u2126", solver.variable("b").vectorQuantity.toString())
        }

        /** Sqrt with compound unit Ohm m */
        @Test
        fun sqrt_unit3() = testSession("ISQ", "Ranges") {
            loadKerML("""
                feature a: Quantities::ScalarQuantityValue {:>> range = 2..9 [Ohm^2 m^2]; }
                feature b: Quantities::ScalarQuantityValue = sqrt(a){:>> range = 2..9 [Ohm m]; }""")
            solver.propagate()
            assertEquals("kg m^3 / A^2 s^3", solver.variable("b").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** Sqrt with complex compound unit */
        @Test
        fun sqrt_unit4() = testSession("ISQ", "Ranges") {
            loadKerML("""
                feature a: Quantities::ScalarQuantityValue {:>> range = 2..9 [Pa^4 J^2 V^8 / A^6 N^2]; }
                feature b: Quantities::ScalarQuantityValue = sqrt(a){:>> range = 2..9 [Pa^2 J V^4 / A^3 N]; }""")
            solver.propagate()
            assertEquals("kg^6 m^7 / A^7 s^16", solver.variable("b").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /**
         * Tests that sqrt is supported in combination with ISQ units.
         * Specifically, sqrt(ElectricPotentialDifferenceValue [V^2]) should yield an ElectricPotentialDifferenceValue [V].
         */
        @Test fun unitsWithOperations() = testSession("ISQ") {
            loadKerML("""
                package unitsWithOperation {
                     feature testV: ISQ::ElectricPotentialDifferenceValue = 5.0 [V];
                     feature testVSquare: Quantities::ScalarQuantityValue(*..* [V^2]) = 49.0 [V^2]; 
                     feature test4: ISQ::ElectricPotentialDifferenceValue = sqrt(testVSquare); 
                    //Property test5: ISQ::ElectricPotentialDifferenceValue = exp(testV)
                    //Property test6: ISQ::ElectricPotentialDifferenceValue = power2(testV)
                }
            """, Runlevel.ALL)
            assertNoIssues()
        }

        @Test
        fun evalUpWithSqrt_int_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = -4 .. 9;}
                feature b: ScalarValues::Integer = sqrt(a);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(0L .. 3L, solver.variable("b"))
        }

        @Test
        fun evalUpWithInverseSqr_real_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = 2.0 .. 9.0;}
                feature b: ScalarValues::Real = inverseSqr(a);
            """)
            solver.propagate()
            assertBounds(-sqrt(9.0)..sqrt(9.0), solver.variable("b"))
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithInverseSqr_int_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = -4 .. 9;}
                feature b: ScalarValues::Integer = inverseSqr(a);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(-3L .. 3L, solver.variable("b"))
        }

        @Test
        fun evalDownWithSqrt_real_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = 0.0 .. 100.0;}
                feature b: Ranges::RealInRange = sqrt(a) {:>> range = -5.0 .. 3.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(0.0 .. 9.0, a)
        }

        @Test
        fun evalDownWithSqrt_real_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = 0.0 .. 100.0;}
                feature b: Ranges::RealInRange = sqrt(a) {:>> range = -5.0 .. -1.0;}
            """)
            solver.propagate()
            val b = solver.variable("b")
            assertEmpty(b)
            assert(status.issues.any { it.kind == Issue.Kind.WARN_INCONSISTENCY })
        }

        @Test
        fun evalDownWithSqrt_int_mixed_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 0 .. 100;}
                feature b: Ranges::IntegerInRange = sqrt(a) {:>> range = -5 .. 3;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(0L .. 9L, a)
        }

        @Test
        fun evalDownWithSqrt_int_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 0 .. 100;}
                feature b: Ranges::IntegerInRange = sqrt(a) {:>> range = -5 .. -1;}
            """)
            solver.propagate()
            val a = solver.variable("a")
            assertEmpty(a)
        }
}
