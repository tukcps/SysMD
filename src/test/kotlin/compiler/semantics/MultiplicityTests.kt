package compiler.semantics

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MultiplicityTests {

    /**
     * A multiplicity is cloned, including its range element.
     */
    @Test
    fun multiplicityTest() = testSession {
        loadKerML("""
            classifier c1 {
                feature f [2 .. 3]; 
            }
            classifier c2 :> c1; 
        """.trimIndent())
        val f1 = global.resolve("c1::f")?.member<Feature>()
        assertNotNull(f1)
        val f1m = global.resolve("c1::f::multiplicity")?.member<Multiplicity>()
        assertNotNull(f1m)

        val f2 = global.resolve("c2::f")?.member<Feature>()
        assertNotNull(f2)
        val f2m = global.resolve("c2::f::multiplicity")?.member<Multiplicity>()
        assertNotNull(f2m)
        val v2mrange = f2m.getOwned<Feature>("range")
        assertNotNull(v2mrange)
        assertEquals("2 .. 3", v2mrange.expression)
    }
}