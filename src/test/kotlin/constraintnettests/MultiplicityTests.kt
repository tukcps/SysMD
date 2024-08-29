package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MultiplicityTests {
    /**
     * Use of multiplicity directly connected with root
     */
    @Test
    fun multiplicityCanBeRestrictedAsLeafTest() = testSession("ScalarValues") {
        loadSysMD("""
                feature  p [0 .. 2];
                feature v: ScalarValues::Integer(1) = p::multiplicity;
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val p = global.resolve<Feature>("p")
        val m = p?.resolve<Multiplicity>("multiplicity")
        val v = global.resolve<Feature>("v")!!.variable
        assertEquals(1.0, v?.min())
        assertEquals(1.0, m?.variable?.vectorQuantity?.getMinAsDouble())
        assertEquals(1.0, m?.variable?.vectorQuantity?.getMaxAsDouble())
    }

    /**
     * Use of multiplicity where it is not root of AST.
     */
    @Test
    fun multiplicityCanBeRestrictedAsNonRootTest() = testSession {
        loadSysMD("""
                feature p [0 .. 2];
                feature v: ScalarValues::Integer(2) = p::multiplicity*2;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val p = global.resolve<Feature>("p")
        val m = p?.resolve<Multiplicity>("multiplicity")?.variable
        val v = global.resolve<Feature>("v")?.variable
        assertEquals(2.0, v?.min())
        assertEquals(1.0, m?.vectorQuantity?.getMinAsDouble())
        assertEquals(1.0, m?.vectorQuantity?.getMaxAsDouble())
    }


    @Test
    fun restrictMultiplicity() = testSession(catchExceptions = false) {
        loadSysMD("""                
            class b {
                feature partC: c [0..10]; 
                feature weight: ScalarValues::Real = sumOverParts(j); 
                inv r { weight <= 30.0 }
            } 
            class c {
                feature j: ScalarValues::Real = 5.0; 
            }
        """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val weight = global.resolveVar("b::weight")
        assertNotNull(weight)
        assertEquals(0, global.resolve<Feature>("b::partC::multiplicity")!!.variable!!.vectorQuantity.value.asIdd().min)
        // it is 7, why??
        assertEquals(7, global.resolve<Feature>("b::partC::multiplicity")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    // The same as above with integers does not work
    @Test @Disabled
    fun restrictMultiplicity2() = testSession(catchExceptions = false) {
        loadSysMD("""                
            class b {
                feature partC: c[0..10]; 
                attribute weight: ScalarValues::Integer = sumOverParts(j);
                inv i { weight <= 30 } // Invariant is not propagated into constraint for weight 
                // Cause? For reals, the translation is done by the LP algorithm that is not part of integers
            }
            class c {
                attribute j: ScalarValues::Integer(5); 
            }

        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val w = global.resolveVar("b::weight")
        assertNotNull(w)
        assertEquals(0, global.resolveVar("b::partC::multiplicity")!!.vectorQuantity.value.asIdd().min)
        // it is 7, why??
        assertEquals(6, global.resolveVar("b::partC::multiplicity")!!.vectorQuantity.value.asIdd().max)
    }
}