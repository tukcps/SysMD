package models.kerml

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.implementation.ElementImplementation
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementTests {

    @Test
    fun testQualifiedName() = testSession {
        val e1 = ElementImplementation(declaredName = "e")
        val e2 = ElementImplementation(declaredName = "e")
        val f1 = ElementImplementation()
        f1.isStandard = true
        create(e1, global)
        create(e2, e1)
        create(f1, e1)
        val e1p = e1.path()
        val e2p = e2.path()
        val f1p = f1.path()
        assertEquals("e", e1p)
        assertEquals("e::e", e2p)
        assertEquals("e::1", f1p)

        // Check that for f1 with no name a UUID5 is generate
        assertEquals(5, f1.elementId?.version())
        assertEquals(Generators.nameBasedGenerator().generate("e::1"), f1.elementId)
    }
}