package constraintnettests.functionstests

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.IDD
import kotlin.test.Test
import kotlin.test.Ignore
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.math.*
import kotlin.test.*

class SqrtTests {

        /** ConstNet shall compute bottom-up with sqrt in real and model.builder.range */
        @Test
        fun evalUpWithSqrt_real_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "2.0 .. 9.0";}
                feature b: ScalarValues::Real = sqrt(a);"""

            )
            solver.propagate()
            assertEquals(sqrt(2.0), solver.getVariable("b")!!.min(), 0.00001)
            //AADD returns bigger result for upper border
            assert(sqrt(9.0) <= solver.getVariable("b")!!.max<Double>())
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqrt in real and value */
        @Test
        fun propagateWithSqrt_real_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange = 3.0 {:>> range = "2.0 .. 5.0";}
                feature b: ScalarValues::Real = sqrt(a);""")
            solver.propagate()
            assertEquals(sqrt(3.0), solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(sqrt(3.0), solver.getVariable("b")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqrt and negative value. This should lead to an error*/
        @Test
        fun propagateWithSqrt_real_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-10.0 .. -5.0";}
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
                feature a: Ranges::IntegerInRange {:>> range = "2 .. 9";}
                feature b: ScalarValues::Integer = sqrt(a);""")
                solver.propagate()
                assertEquals(floor(sqrt(2.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
                assertEquals(ceil(sqrt(9.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
                assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
                assertNoIssues()
        }

        @Test
        fun evalDownWithSqrt_int_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "4 .. 25";}
                feature b: Ranges::IntegerInRange = sqrt(a) {:>> range = "3 .. 3";}""")
            solver.propagate()
            assertEquals(9, solver.getVariable("a")!!.idd().getRange().min)
            assertEquals(9, solver.getVariable("a")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalDownWithSqrt_int() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "4 .. 25";}
                feature b: Ranges::IntegerInRange = min(sqrt(a), 4) {:>> range = "3 .. 3";}""")
            solver.propagate()
            assertEquals(9, solver.getVariable("a")!!.idd().getRange().min)
            assertEquals(9, solver.getVariable("a")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqrt in int **/
        @Test
        fun evalUpWithSqrt_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange = 3 {:>> range = "2 .. 9";}
                feature b: ScalarValues::Integer = sqrt(a); """
            )
            solver.propagate()
            assertEquals(floor(sqrt(3.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(ceil(sqrt(3.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithSqrt_mixed_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-4.0 .. 9.0";}
                feature b: ScalarValues::Real = sqrt(a);
            """)
            solver.propagate()
            // Sqrt of mixed range truncates the negative part
            assertEquals(0.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(3.0, solver.getVariable("b")!!.max(), 0.00001)
        }

        /** Sqrt with ISQ unit: sqrt(AreaValue) -> LengthValue, check unit propagation */
        @Test
        fun sqrt_unit1() = testSession("ISQ", "Ranges") {
            loadKerML("""
               feature a: ISQ::AreaValue {:>> range = "4.0..9.0";}
               feature b: ISQ::LengthValue  = sqrt(a); 
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals("m", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertEquals(2.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(3.0, solver.getVariable("b")!!.max(), 0.00001)
        }

        /** Sqrt with Ohm^2 unit, checks string output */
        @Test
        fun sqrt_unit2() = testSession("ISQ", "Ranges") {
            loadKerML("""
                feature a: Quantities::ScalarQuantityValue {:>> unit = "Ohm^2"; :>> range = "4.0 .. 9.0";}
                feature b: ISQ::ResistanceValue = sqrt(a); """)
            solver.propagate()
            assertNoIssues()
            assertEquals("2..3 \u2126", solver.getVariable("b")!!.vectorQuantity.toString())
        }

        /** Sqrt with compound unit Ohm m */
        @Test
        fun sqrt_unit3() = testSession("ISQ", "Ranges") {
            loadKerML("""
                feature a: Quantities::ScalarQuantityValue {:>> unit = "Ohm^2 m^2"; :>> range = "2.0 .. 9.0";}
                feature b: Quantities::ScalarQuantityValue = sqrt(a){:>> unit = "Ohm m"; :>> range = "2.0 .. 9.0";}""")
            solver.propagate()
            assertEquals("kg m^3 / A^2 s^3", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** Sqrt with complex compound unit */
        @Test
        fun sqrt_unit4() = testSession("ISQ", "Ranges") {
            loadKerML("""
                feature a: Quantities::ScalarQuantityValue {:>> unit = "Pa^4 J^2 V^8 / A^6 N^2"; :>> range = "2.0 .. 9.0";}
                feature b: Quantities::ScalarQuantityValue = sqrt(a){:>> unit = "Pa^2 J V^4 / A^3 N"; :>> range = "2.0 .. 9.0";}""")
            solver.propagate()
            assertEquals("kg^6 m^7 / A^7 s^16", solver.getVariable("b")!!.vectorQuantity.unit.toString())
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
                     feature testVSquare: Quantities::ScalarQuantityValue [V^2] = 49.0 [V^2]; 
                     feature test4: ISQ::ElectricPotentialDifferenceValue = sqrt(testVSquare); 
                    //Property test5: ISQ::ElectricPotentialDifferenceValue = exp(testV)
                    //Property test6: ISQ::ElectricPotentialDifferenceValue = power2(testV)
                }
                """
            )
            assertNoIssues()
        }

        @Test
        fun evalUpWithSqrt_int_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-4 .. 9";}
                feature b: ScalarValues::Integer = sqrt(a);
            """)
            solver.propagate()
            assertEquals(0L, solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(3L, solver.getVariable("b")!!.idd().getRange().max)
        }
}
