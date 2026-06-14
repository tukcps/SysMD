package constraintnettests

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * In these tests, the interaction between the symbol table, the selected property and the parser is tested.
 * The property is selected from the symbol table, passed to the parser in the field property and updated when the
 * methods evalUp, evalDown, solve are called.
 * The constraints rangeSpec or boolSpec are considered.
 */
class VariableInteractionTest {

    /**
     * A property consists of the left-hand data (target variable, type and subtype of it),
     * and the dependency that is in the property field dependency and that can be
     * analyzed separately.
     */
    @Test
    fun expressionParseTest() = testSession("ScalarValues") {
        initialize(Runlevel.NAMES_RESOLVED)
        val a = addOwnedMember(FeatureImplementation(declaredName ="a", typeConstraint = mutableListOf("2.0 .. 3.0")), global)
        addOwnedRelationship(SpecializationImplementation(a, repo.realType!!), a)
        val b = addOwnedMember(FeatureImplementation(declaredName ="b", typeConstraint = mutableListOf("3.0 .. 4.0")), global)
        addOwnedRelationship(SpecializationImplementation(b, repo.realType!!), b)
        val c =addOwnedMember(FeatureImplementation(declaredName="c", typeConstraint = mutableListOf("1.0..8.0"), expression = "a+b+2.0"), global)
        addOwnedRelationship(SpecializationImplementation(c, repo.realType!!), c)
        initialize(Runlevel.ALL)
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(8.0, global.resolveVar("c")!!.aadd().getRange().max, 0.0001)
        assertEquals(7.0, global.resolveVar("c")!!.aadd().getRange().min, 0.0001)
    }

    /** A property value can become constrained from a dependency value (here: scalar) */
    @Test
    fun evalUpPropertyDirectTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("feature speed: Ranges::RealInRange = 5.0+6.0 {:>> range = \"2.0 .. 22.0\";}", Runlevel.ALL)
        val speed = global.resolveVar("speed")
        assertEquals(11.0, speed!!.min(), 0.000001)
        assertEquals(11.0, speed.max(), 0.000001)
    }


    /** A property can constrain a dependency such that its own constraints can be fulfilled */
    @Test
    fun evalDownPropertyTest() = testSession("ScalarValues", "Ranges") {
         loadKerML("""
             feature speed2: Ranges::RealInRange {:>> range = "10.0 .. 10000.0";}
             feature speed:  Ranges::RealInRange = speed2 {:>> range = "-100.0 ..200.0";}""")
        solver.propagate()
        assertNoIssues()
        val speed = global.resolveVar("speed")!!.aadd().getRange()
        assertEquals(10.0, speed.min, 0.0000001)
        assertEquals(200.0, speed.max, 0.0000001)
    }
}