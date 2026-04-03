package sysmdtests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.Range
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class BuiltInFunctionsTests {

    /**
     *  The function getLeaves returns the leaves of the ast of a dependency expression.
     */
    @Test
    fun getLeavesTest() = testSession("ScalarValues") {
        loadKerML("""
            feature b: ScalarValues::Real = 2.0;
            feature c: ScalarValues::Real = 3.0;
            feature d: ScalarValues::Real = 4.0;
            feature a: ScalarValues::Real = b + c * d;
        """)
        solver.propagate()
        assertNoIssues()
        val a = global.resolveVar("a")
        assertEquals(3, a!!.ast!!.getLeaves().size)
    }

    @Test fun hasAFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
            namespace a;
            feature b: ScalarValues::Boolean = owns(Global, a);
        """)
        solver.propagate()
        // owns ist hier Funktionsaufruf und Global ein parameter, der kein Feature ist. 
        // Wird versucht zu Feature zu Casten (Feature-Expression) --> Exception.
        // (Gibt es etwas gleichartiges in Standard? Wo wir ownership feststellen können?)
        assertNoIssues()
        val b = global.resolveVar("b")!!
        assertEquals(builder.True, b.vectorQuantity.value)
    }

    @Test fun hasAFunctionTest2() = testSession("ScalarValues") {
        loadKerML("""
            namespace a;
            feature b: ScalarValues::Boolean = owns(Global, c); 
        """)
        solver.propagate()
        assertNoIssues()
        val b = global.resolveVar("b")!!
        assertEquals(builder.False, b.vectorQuantity.value)
    }

    @Test fun hasTypeOperationTest() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real = 2.0;
            feature b: ScalarValues::Boolean = a hastype ScalarValues::Real;
        """)
        solver.propagate()
        assertNoIssues()
        val b = global.resolveVar("b") !!
        assertEquals(builder.True, b.vectorQuantity.value)
    }

    @Test fun hastypeOperationTest2() = testSession("ScalarValues") {
        loadKerML(input = """
                feature a: ScalarValues::Boolean;
                feature b: ScalarValues::Boolean = a hastype ScalarValues::Real;
                """)
        solver.propagate()
        assertNoIssues()
        val b = global.resolveVar("b") !!
        assertEquals(builder.False, b.vectorQuantity.value)
    }

    @Test fun hasTypeOperationTest3() = testSession("ScalarValues") {
        loadKerML(input = """
                feature a: ScalarValues::Integer = 2;
                feature b: ScalarValues::Boolean = a hastype ScalarValues::ScalarValue;
                """)
        solver.propagate()
        assertNoIssues()
        val b = global.resolveVar("b") !!
        assertEquals(builder.True, b.vectorQuantity.value)
    }

    @Test
    fun oneOfOperationTest3() = testSession("ScalarValues") {
        loadKerML("feature r: ScalarValues::Real = oneOf(1.0 .. 2.0);")
        solver.propagate()
        assertNoIssues()
        val r = global.resolveVar("r") !!
        assertEquals(Range(1.0 .. 2.0), r.vectorQuantity.value.asAadd().getRange())
    }


    // TODO: Fix semantics of function all Of !
    @Test @Ignore
    fun allOfOperationTest4() = testSession("ScalarValues") {
        loadKerML("feature r: ScalarValues::Real(1.5) = allOf(1.0 .. 2.0);")
        solver.propagate()
        assertNoIssues()
        val r = global.resolveVar("r")
        assertEquals(Range(1.0 .. 2.0), r!!.vectorQuantity.value.asAadd().getRange())
    }


    @Test
    fun oneOfOperationTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("feature r: Ranges::RealInRange = oneOf(1.0 .. 2.0) {:>> range = \"1.0\";}")
        solver.propagate()
        assertNoIssues()
        val r = global.resolveVar("r") !!
        assertEquals(Range(1.0 .. 1.0), r.vectorQuantity.value.asAadd().getRange())
    }

    @Test @Ignore //TODO: Fix semantics of anyOf!
    fun anyOfOperationTest() = testSession("ScalarValues") {
        loadKerML("feature r: ScalarValues::Real = anyOf(1.0 .. 2.0);")
        solver.propagate()
        assertNoIssues()
        val r = global.resolveVar("r") !!
        assertEquals(Range(1.0 .. 2.0), r.vectorQuantity.value.asAadd().getRange())
    }
}
