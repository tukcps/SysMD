package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import util.testSession

class MultiplicityTests {
    /**
     * Use of multiplicity directly connected with root
     */
    @Test
    fun multiplicityCanBeRestrictedAsLeafTest() = testSession("ScalarValues") {
        loadKerML("""
                feature  p [0 .. 2];
                feature v: ScalarValues::Integer(1) = p::multiplicity;
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val p = global.resolve<Feature>("p")
        val m = p?.resolve<Multiplicity>("multiplicity")
        val v = global.resolve<Feature>("v")!!.variable
        assertEquals(1L, v?.min())
        assertEquals(1L, m?.variable?.min())
        assertEquals(1L, m?.variable?.min())
    }

    /**
     * Use of multiplicity where it is not root of AST.
     */
    @Test
    fun multiplicityCanBeRestrictedAsNonRootTest() = testSession("ScalarValues") {
        loadKerML("""
           feature p [0 .. 2];
           feature v: ScalarValues::Integer(2) = p::multiplicity*2;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val p = global.resolve<Feature>("p")
        val m = p?.resolve<Multiplicity>("multiplicity")?.variable
        val v = global.resolve<Feature>("v")?.variable
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
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val weight = global.resolveVar("b::weight")
        assertNotNull(weight)
        assertEquals(0L, global.resolve<Feature>("b::partC::multiplicity")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(7, global.resolve<Feature>("b::partC::multiplicity")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    // The same as above with integers does not work
    @Test @Disabled
    fun restrictMultiplicity2() = testSession {
        loadKerML("""                
                class b {
                    feature partC: c[0..10]; 
                    attribute weight: ScalarValues::Integer = sumOverParts(j);
                    inv i { weight <= 30 } // Invariant is not propagated into constraint for weight 
                    // Cause? For reals, the translation is done by the LP algorithm that is not part of integers
                }
                class c {
                    attribute j: ScalarValues::Integer(5); 
                }
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val w = global.resolveVar("b::weight")
        assertNotNull(w)
        assertEquals(0, global.resolveVar("b::partC::multiplicity")!!.vectorQuantity.value.asIdd().min)
        // it is 7, why??
        assertEquals(6, global.resolveVar("b::partC::multiplicity")!!.vectorQuantity.value.asIdd().max)
    }
}