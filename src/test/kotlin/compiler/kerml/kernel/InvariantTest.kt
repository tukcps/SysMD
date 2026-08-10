package compiler.kerml.kernel

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.DataType
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InvariantTest {
    @Test
    fun simpleInvariant() = testSession("ScalarValues") {
        loadKerML("inv i;")
        val i = global.resolve("i")?.member<Invariant>()
        assertNotNull(i)
        assertTrue(!i.isNegated)
        assertEquals(repo.booleanType, i.generalization.first() as DataType)
    }

    @Test
    fun invariantNegated() = testSession("ScalarValues") {
        loadKerML("inv false i;")
        val i = global.resolve("i")?.member<Invariant>()
        assertNotNull(i)
        assertTrue(i.isNegated)
    }

    @Test
    fun invariantWithValue() = testSession("ScalarValues") {
        loadKerML("inv i = true;")
        val i = global.resolve("i")?.member<Invariant>()
        assertNotNull(i)
        assertTrue(!i.isNegated)
        assertEquals(i.expression, "true")
    }
}