package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

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
    fun expressionParseTest() = testSession {
        val a = create(FeatureImplementation(declaredName ="a", typeConstraint = mutableListOf("2.0 .. 3.0")), global)
        create(SpecializationImplementation(a, repo.realType!!), a)
        val b = create(FeatureImplementation(declaredName ="b", typeConstraint = mutableListOf("3.0 .. 4.0")), global)
        create(SpecializationImplementation(b, repo.realType!!), b)
        val c =create(FeatureImplementation(declaredName="c", typeConstraint = mutableListOf("1.0..8.0"), expression = "a+b+2.0"), global)
        create(SpecializationImplementation(c, repo.realType!!), c)
        initialize()
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(8.0, global.resolveVar("c")!!.aadd().getRange().max, 0.0001)
        assertEquals(7.0, global.resolveVar("c")!!.aadd().getRange().min, 0.0001)
    }

    /** A property value can become constrained from a dependency value (here: scalar) */
    @Test
    fun evalUpPropertyDirectTest() = testSession {
        +"feature speed: ScalarValues::Real(2.0 .. 22.0) = 5.0+6.0;"
        val speed = global.resolve<Feature>("speed")!!.variable
        assertEquals(11.0, speed!!.min(), 0.000001)
        assertEquals(11.0, speed.max(), 0.000001)
    }


    /** A property can constrain a dependency such that its own constraints can be fulfilled */
    @Test
    fun evalDownPropertyTest() = testSession {
         loadSysMD("""
             feature speed2: ScalarValues::Real(10.0 .. 10000.0);
             feature speed:  ScalarValues::Real(-100.0 ..200.0) = speed2;""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val speed = global.resolve<Feature>("speed")!!.variable!!.aadd().getRange()
        assertEquals(10.0, speed.min, 0.0000001)
        assertEquals(200.0, speed.max, 0.0000001)
    }
}