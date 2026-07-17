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

class SqrTests {

        @Test
        fun propagateWithSqr_int_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-10.0 .. -5.0";}
                feature b: ScalarValues::Real = sqr(a);
                """)
            solver.propagate()
            assertNoIssues()
            assertEquals(25.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(100.0, solver.getVariable("b")!!.max(), 0.00001)
        }

        @Test
        fun evalUpWithSqr_zero() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "0 .. 0";} 
                feature b: ScalarValues::Integer = sqr(a);
                """)
            solver.propagate()
            assertEquals(0, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
            assertEquals(0, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqr in real and model.builder.range */
        @Test
        fun evalUpWithSqr_real_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "2.0 .. 9.0";} 
                feature b: ScalarValues::Real = sqr(a); """
            )
            solver.propagate()
            assertEquals(4.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(81.0, solver.getVariable("b")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithSqr_real_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-3.0 .. -1.0";} 
                feature b: ScalarValues::Real = sqr(a); """
            )
            solver.propagate()
            assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(9.0, solver.getVariable("b")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqr in real and value */
        @Test
        fun evalUpWithSqr_real_value() = testSession("Ranges") {
            loadKerML("""
                feature a: ScalarValues::Real(1..5) = 3.0;  
                feature b: ScalarValues::Real = sqr(a);"""
            )
            solver.propagate()
            assertEquals(9.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(9.0, solver.getVariable("b")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqr in int and model.builder.range */
        @Test
        fun evalUpWithSqr_int_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "2 .. 9";} 
                feature b: ScalarValues::Integer = sqr(a);"""
            )
            solver.propagate()
            assertEquals(4, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
            assertEquals(81, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithSqr_int_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-3 .. -2";} 
                feature b: ScalarValues::Integer = sqr(a);"""
            )
            solver.propagate()
            assertEquals(4, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
            assertEquals(9, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with sqr in int and value */
        @Test
        fun evalUpWithSqr_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange = 3 {:>> range = "2 .. 9";}  
                feature b: ScalarValues::Integer = sqr(a); """
            )
            assertNoIssues()
            solver.propagate()
            assertNoIssues()
            assertEquals(9, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
            assertEquals(9, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        }

        @Test
        fun evalDownWithSqr_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "2 .. 9";}  
                feature b: Ranges::IntegerInRange = sqr(a) {:>> range = "8 .. 23";}  """
            )
            solver.propagate()
            assertNoIssues()
            assertEquals(2, (solver.getVariable("a")!!.idd() as IDD.Leaf).value.min)
            assertEquals(5, (solver.getVariable("a")!!.idd() as IDD.Leaf).value.max)
            assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        }

        /** Sqr with no unit, large numbers */
        @Test
        fun sqr_unit() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange { :>> range = "2.0E16 .. 9.0E16";}
                feature b: ScalarValues::Real = sqr(a);""")
            solver.propagate()
            assertEquals("400e30..8.1e33", solver.getVariable("b")!!.vectorQuantity.toString())
            assertNoIssues()
        }

        /** Sqr with ISQ LengthValue -> AreaValue, check unit */
        @Test
        fun sqr_unit1() = testSession("ISQ", "Ranges") {
            loadKerML("""
                feature a: ISQ::LengthValue{:>> range = "2.0 .. 9.0";}
                feature b: ISQ::AreaValue = sqr(a);""")
            solver.propagate()
            assertEquals("m^2", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** Sqr with Ohm unit */
        @Test
        fun sqr_unit2() = testSession("ISQ") {
            loadKerML("""
                feature a: ISQ::ResistanceValue{ :>> range = "2.0 .. 9.0";}
                feature b: Quantities::ScalarQuantityValue = sqr(a){:>> unit = "Ohm^2";}
            """)
            solver.propagate()
            assertEquals("kg^2 m^4 / A^4 s^6", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertEquals("Resistance", solver.getVariable("a")!!.vectorQuantity.getDomain())
            assertNoIssues()
        }

        /** Sqr with compound unit Ohm m */
        @Test
        fun sqr_unit3() = testSession("ISQ", "Ranges") {
            loadKerML("""
                feature a: Quantities::ScalarQuantityValue {:>> unit = "Ohm m"; :>> range = "2.0 .. 9.0";}
                feature b: Quantities::ScalarQuantityValue = sqr(a){:>> unit = "Ohm^2 m^2"; :>> range = "2.0 .. 9.0";}""")
            solver.propagate()
            assertEquals("kg^2 m^6 / A^4 s^6", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** Sqr with complex compound unit */
        @Test
        fun sqr_unit4() = testSession("ISQ") {
            loadKerML("""
                feature a: Quantities::ScalarQuantityValue { :>> unit = "Pa^2 J V^4 / A^3 N"; :>> range = "2.0 .. 9.0";}
                feature b: Quantities::ScalarQuantityValue = sqr(a) { :>> unit = "Pa^4 J^2 V^8 / A^6 N^2"; :>> range = "4.0 .. 81.0";} 
            """)
            solver.propagate()
            assertEquals("kg^12 m^14 / A^14 s^32", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithSqr_real_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-3.0 .. 3.0";}
                feature b: ScalarValues::Real = sqr(a);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(0.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(9.0, solver.getVariable("b")!!.max(), 0.00001)
        }

        @Test
        fun evalUpWithSqr_int_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-3 .. 3";}
                feature b: ScalarValues::Integer = sqr(a);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(0L, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
            assertEquals(9L, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
        }
}
