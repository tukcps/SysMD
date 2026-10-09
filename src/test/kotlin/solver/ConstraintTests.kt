package solver

import util.assertBounds
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.bounds.LongBound
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*

class ConstraintTests {

    @Test
    fun assertTest() = testSession("Constraints", runlevel = Runlevel.ALL) {
        loadSysMLv2("""
            attribute v1: ScalarValues::Real = 1.0; 
            attribute v2: ScalarValues::Real = 3.0; 
            assert constraint c { v1 < v2 }
        """, Runlevel.ALL)
        assertNoIssues()
        val current = solver.getVariable("c")
        assertEquals(XBool.True, current!!.vectorQuantity.value.asBdd().value)
    }

    @Test
    fun assertTest2() = testSession("Constraints", runlevel = Runlevel.ALL) {
        loadSysMLv2("""
            assert constraint test { (3 >= 3) and (3 <= 3) }
            assert constraint test2 { (3 == 3) }
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val testr = solver.getVariable("test")
        assertEquals(builder.Bool.True, testr!!.vectorQuantity.value)
        val testr2 = solver.getVariable("test2")
        assertEquals(builder.Bool.True, testr2!!.vectorQuantity.value)
    }

    @Test
    fun assertTestEQWithVariable() = testSession("Calculations", "ISQ") {
        loadSysMLv2("""
            attribute a: ScalarValues::Integer(0..4); 
            attribute ASIlFromReliability: ScalarValues::Integer(0..4); 
            attribute ASILCalculated: ScalarValues::Integer = 1;
            assert constraint ASIL { ASIlFromReliability == ASILCalculated }
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("ASILCalculated")
        assertBounds(1L, test2!!)
        val testr = solver.getVariable("ASIlFromReliability")
        assertBounds(1L, testr!!)
    }

    @Test
    fun assertTestEQWithVariable2() = testSession("Calculations", "ISQ") {
        loadSysMLv2("""
            attribute a: ScalarValues::Integer(0..4); 
            attribute ASIlFromReliability: ScalarValues::Integer(0..4) = a; 
            attribute ASILCalculated: ScalarValues::Integer(1) = ASIlFromReliability;
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("ASILCalculated")
        assertBounds(1L, test2!!)
        val testr = solver.getVariable("ASIlFromReliability")
        assertBounds(1L, testr!!)
    }

    @Test
    fun assertTestEQ() = testSession("Calculations", "ISQ") {
        loadSysMLv2("""
            attribute ASIlFromReliability: ScalarValues::Integer(0..4); 
            attribute ASILCalculated: ScalarValues::Integer = 1;
            assert constraint ASIL { ASIlFromReliability == ASILCalculated }
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("ASILCalculated")
        assertBounds(1L, test2!!)
        val testr = solver.getVariable("ASIlFromReliability")
        assertBounds(1L, testr!!)
    }

    @Test
    fun testRequirement4Bool() = testSession("Parts", "Requirements") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Boolean = false; 
            }
            
            requirement test {
                subject f references p;
                assume constraint r { f::a == false }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val test = global.resolve("test")?.member<Feature>()
        assertNotNull(test)
        val f = global.resolve("test::f::a")
        assertNotNull(f)

        val testR = global.resolve("test::r")?.member<Feature>()
        assertNotNull(testR)
        val testRVar = solver.getVariable("test::r")
        assertEquals(builder.Bool.True, testRVar!!.bool())
    }
}