package models.kerml

import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedType
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession

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
    fun valueConstructorTest() = testSession("ScalarValues") {

        // allowed identifier declaration
        var p = FeatureImplementation(declaredName="Property12_2test")
        addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(p, global.resolve("ScalarValues::Real")!!.memberElement as Type), p)
        initialize()
        assertTrue(solver.getVariable("Property12_2test")!!.vectorQuantity.value is AADD)

        p = FeatureImplementation(declaredName="Property12_2test2")
        addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(p, repo.booleanType!!), p)
        initialize()
        assertTrue(solver.getVariable("Property12_2test2")!!.vectorQuantity.value is BDD)

        p = FeatureImplementation(declaredName="Property12_2test3")
        addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(p, UnresolvedType("ScalarValues::Real")), p)
        initialize()
        assertTrue(solver.getVariable("Property12_2test3")!!.vectorQuantity.value is AADD)

        p = FeatureImplementation(declaredName="Property12_2test4", typeConstraint = mutableListOf( "true" ))
        addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(p, UnresolvedType("ScalarValues::Boolean")), p)
        initialize()
        initialize()
        assertTrue(solver.getVariable("Property12_2test4")!!.vectorQuantity.value is BDD)
    }


    /** The property maintains a root node that has a list of AstLeaves */
    @Test fun leavesListCreationTest() = testSession("ScalarValues") {
        loadKerML("feature a: ScalarValues::Real; feature b :ScalarValues::Real; feature c: ScalarValues::Real; feature x: ScalarValues::Real = a+b+c.")
        assertNoIssues()
        assertEquals(3, global.resolveVar("x")!!.ast!!.leaves.size)
    }
}
