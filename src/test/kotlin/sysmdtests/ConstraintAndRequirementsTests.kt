package sysmdtests

import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*

class ConstraintAndRequirementsTests {
    @Test
    fun requirementTest() = testSession("Parts", "SI") {
        loadSysMLv2(
            """
            package Filter {
                part testComponent {
                    attribute value1: SI::Length(0 .. 10) [m] = 4.0 m; 
                    attribute value2: SI::Length(0 .. 10) [m] = 2.0 m; 
                    assert current { value1 > value2 }
                }
            }
        """
        )
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val current = global.resolveVar("Filter::testComponent::current")
        assertEquals(XBool.True, current!!.vectorQuantity.value as XBool)
    }

    @Test
    fun requirementTest2() = testSession("ScalarValues") {
        loadSysMLv2(
            """
            assert test { (3 >= 3) and (3 <= 3) }
            assert test2 { (3 == 3) }
        """
        )
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val testr = global.resolveVar("test")
        assertEquals(builder.True, testr!!.vectorQuantity.value)
        val testr2 = global.resolveVar("test2")
        assertEquals(builder.True, testr2!!.vectorQuantity.value)
    }

    @Test
    fun assertTestEQWithVariable() = testSession("Calculations", "SI") {
        loadSysMLv2(
            """
            attribute a: ScalarValues::Integer {:>> range = "0..4";}
            attribute ASIlFromReliability: ScalarValues::Integer = a {:>> range = "0..4";}
            attribute ASILCalculated: ScalarValues::Integer = 1;
            assert ASIL { ASIlFromReliability == ASILCalculated }
           
        """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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
        """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val test2 = global.resolveVar("ASILCalculated")
        assertEquals(1, test2!!.vectorQuantity.value.asIdd().min)
        val testr = global.resolveVar("ASIlFromReliability")
        assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
    }

    @Test
    fun assertTestEQ() = testSession("Calculations", "SI") {
        loadSysMLv2(
            """
            attribute ASIlFromReliability: ScalarValues::Integer  {:>> range = "0..4";}
            attribute ASILCalculated: ScalarValues::Integer = 1;
            assert ASIL {ASIlFromReliability == ASILCalculated}
           
        """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val test2 = global.resolveVar("ASILCalculated")
        assertEquals(1, test2!!.vectorQuantity.value.asIdd().min)
        val testr = global.resolveVar("ASIlFromReliability")
        assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
    }






}