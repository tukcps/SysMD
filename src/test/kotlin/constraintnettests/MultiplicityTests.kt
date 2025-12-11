package constraintnettests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MultiplicityTests {
    /**
     * Use of multiplicity directly connected with root
     */
    @Test
    fun multiplicityCanBeRestrictedAsLeafTest() = testSession("ScalarValues") {
        loadKerML("""
            feature  p [0 .. 2];
            feature v: ScalarValues::Integer(1) = p::cardinality;
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolve("p")?.member<Feature>()
        val multiplicity = p?.resolveVar("cardinality")
        val v = global.resolveVar("v")
        assertEquals(1L, v?.min())
        assertEquals(1L, multiplicity?.min())
        assertEquals(1L, multiplicity?.min())
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
        val v = global.resolveVar("v")
        assertEquals(2L, v?.min())
        assertEquals(1L, m?.min())
        assertEquals(1L, m?.max())
    }


    @Test
    fun restrictMultiplicity() = testSession("ScalarValues") {
        loadKerML("""                
            type b :> Base::Anything {
                feature partC: c [0..10]; 
                feature weight: ScalarValues::Real = sumOverParts(j); 
                inv r { weight <= 30.0 }
            } 
            type c :> Base::Anything {
                feature j: ScalarValues::Real = 5.0; 
            }
        """)
        solver.propagate()
        assertNoIssues()
        val weight = global.resolveVar("b::weight")
        assertNotNull(weight)
        assertEquals(0L, global.resolveVar("b::partC::cardinality")!!.min())
        assertEquals(7L, global.resolveVar("b::partC::cardinality")!!.max())
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
        """)
        solver.propagate()
        assertNoIssues()
        val w = global.resolveVar("b::weight")
        assertNotNull(w)
        assertEquals(0L, global.resolveVar("b::partC::cardinality")!!.min())
        // it is 7, why??
        assertEquals(6L, global.resolveVar("b::partC::cardinality")!!.max())
    }
}