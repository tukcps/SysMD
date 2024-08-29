package models.kerml

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The checks of the Expression class
 */
class VariableTests {

    /**
     * Check that the constructor of property works with the kind-string and creates appropriate quantities
     * after initialization. Note that initialization requires a unique id for the condition / indexes (BDD)
     * and noise symbols.
     */
    @Test
    fun valueConstructorTest() = testSession {

        // allowed identifier declaration
        var p = FeatureImplementation(declaredName="Property12_2test")
        create(p, global)
        create(SpecializationImplementation(p, repo.realType!!), p)
        initialize()
        assertTrue(p.variable!!.vectorQuantity.value is AADD)

        p = FeatureImplementation(declaredName="Property12_2test2")
        create(p, global)
        create(SpecializationImplementation(p, repo.booleanType!!), p)
        initialize()
        assertTrue(p.variable!!.vectorQuantity.value is BDD)

        p = FeatureImplementation(declaredName="Property12_2test3")
        create(p, global)
        create(SpecializationImplementation(p, "ScalarValues::Real"), p)
        initialize()
        assertTrue(p.variable!!.vectorQuantity.value is AADD)

        p = FeatureImplementation(declaredName="Property12_2test4", typeConstraint = mutableListOf( "true" ))
        create(p, global)
        create(SpecializationImplementation(p, "ScalarValues::Boolean"), p)
        initialize()
        initialize()
        assertTrue(p.variable!!.vectorQuantity.value is BDD)
    }


    /** The property maintains a root node that has a list of AstLeaves */
    @Test fun leavesListCreationTest() = testSession {
        loadSysMD("Value a: ScalarValues::Real; Value b :ScalarValues::Real; Value c: ScalarValues::Real; Value x: ScalarValues::Real = a+b+c.")
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(3, global.resolveVar("x")!!.ast!!.leaves.size)
    }
}
