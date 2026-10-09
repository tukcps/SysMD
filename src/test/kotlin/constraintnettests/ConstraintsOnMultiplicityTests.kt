package constraintnettests

import util.variable
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConstraintsOnMultiplicityTests {
    /**
     * Use of multiplicity directly connected with root
     */
    @Test
    fun multiplicityCanBeRestrictedAsLeafTest() = testSession("ScalarValues") {
        loadKerML("""
            feature  p [0 .. 2];
            feature v: ScalarValues::Integer(1) = p::cardinality;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val p = global.resolve("p")?.member<Feature>()
        val multiplicity = p?.resolveVar("cardinality")
        val v = solver.getVariable("v")
        assertBounds(1L, v!!)
        assertBounds(1L, multiplicity!!)
        assertBounds(1L, multiplicity)
    }

    /**
     * Use of multiplicity where it is not root of AST.
     */
    @Test
    fun multiplicityCanBeRestrictedAsNonRootTest() = testSession("ScalarValues") {
        loadKerML("""
           feature p [0 .. 2];
           feature v: ScalarValues::Integer(2) = p::cardinality*2;
        """)
        assertNoIssues()
        solver.propagate()
        val p = global.resolve("p")!!.member<Feature>()
        val m = p?.resolveVar("cardinality")
        val v = solver.getVariable("v")
        assertBounds(2L, v!!)
        assertBounds(1L..1L, m!!)
    }


    @Test
    fun restrictMultiplicity() = testSession("ScalarValues") {
        loadKerML("""                
            classifier b {
                feature partC: c [0..10]; 
                feature weight: ScalarValues::Real = sumOverParts(j); 
                inv r { weight <= 30.0 }
            } 
            classifier c {
                feature j: ScalarValues::Real = 5.0; 
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val weight = solver.variable("b::weight")
        assertBounds(0L..6L, solver.variable("b::partC::multiplicity"))
    }

    @Test
    fun restrictMultiplicity2() = testSession("ScalarValues") {
        loadKerML("""                
            type b :> Base::Anything {
                feature partC: c[0..10]; 
                feature weight: ScalarValues::Integer = sumOverParts(j);
                inv i { weight <= 30 } // Invariant is not propagated into constraint for weight 
                // Cause? For reals, the translation is done by the LP algorithm that is not part of integers
            }
            type c :> Base::Anything {
                feature j: ScalarValues::Integer(5); 
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val w = solver.variable("b::weight")
        assertBounds(0L .. 6L, solver.variable("b::partC::multiplicity"))
    }
}