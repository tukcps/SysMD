package sysmlv2tests

import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*

class AssertAndConstraintsTests {

    @Test
    fun assertTestSyntax1() = testSession("ScalarValues", "Constraints") {
        loadSysMLv2("""
            assert { true or false } 
        """)
        assertNoIssues()
    }

    @Test
    fun assertTestSyntax1b() = testSession("ScalarValues", "Constraints") {
        loadSysMLv2("""
            assert constraint { true or false } 
        """)
        assertNoIssues()
    }

    @Test
    fun assertTestSyntax3() = testSession("ScalarValues", "Constraints") {
        loadSysMLv2("""
            assert not { true and false } 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun assertTest() = testSession("ScalarValues", "Constraints") {
        loadSysMLv2("""
            attribute v1: ScalarValues::Real = 1.0; 
            attribute v2: ScalarValues::Real = 3.0; 
            assert constraint c { v1 < v2 }
        """)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val current = global.resolveVar("c")
        assertEquals(XBool.True, current!!.vectorQuantity.value as XBool)
    }

    @Test
    fun assertTest2() = testSession("ScalarValues") {
        loadSysMLv2("""
            assert constraint test { (3 >= 3) and (3 <= 3) }
            assert constraint test2 { (3 == 3) }
        """)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val testr = global.resolveVar("test")
        assertEquals(builder.True, testr!!.vectorQuantity.value)
        val testr2 = global.resolveVar("test2")
        assertEquals(builder.True, testr2!!.vectorQuantity.value)
    }

    @Test
    fun assertTestEQWithVariable() = testSession("Calculations", "SI") {
        loadSysMLv2("""
            attribute a: ScalarValues::Integer(0..4); 
            attribute ASIlFromReliability: ScalarValues::Integer(0..4); 
            attribute ASILCalculated: ScalarValues::Integer = 1;
            assert constraint ASIL { ASIlFromReliability == ASILCalculated }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("ASILCalculated")
        assertEquals(1, test2!!.vectorQuantity.value.asIdd().min)
        val testr = global.resolveVar("ASIlFromReliability")
        assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
    }

    @Test
    fun assertTestEQWithVariable2() = testSession("Calculations", "SI") {
        loadSysMLv2(
            """
            attribute a: ScalarValues::Integer(0..4); 
            attribute ASIlFromReliability: ScalarValues::Integer(0..4) = a; 
            attribute ASILCalculated: ScalarValues::Integer(1) = ASIlFromReliability;
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("ASILCalculated")
        assertEquals(1, test2!!.vectorQuantity.value.asIdd().min)
        val testr = global.resolveVar("ASIlFromReliability")
        assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
    }

    @Test
    fun assertTestEQ() = testSession("Calculations", "SI") {
        loadSysMLv2(
            """
            attribute ASIlFromReliability: ScalarValues::Integer(0..4); 
            attribute ASILCalculated: ScalarValues::Integer = 1;
            assert constraint ASIL { ASIlFromReliability == ASILCalculated }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("ASILCalculated")
        assertEquals(1, test2!!.vectorQuantity.value.asIdd().min)
        val testr = global.resolveVar("ASIlFromReliability")
        assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
    }
}