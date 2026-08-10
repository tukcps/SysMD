package models.kerml

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.implementation.AssociationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.ElementImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementTests {

    @Test
    fun testQualifiedName() = testSession {
        val e1 = NamespaceImplementation(this, declaredName = "e")
        val e2 = NamespaceImplementation(this, declaredName = "e")
        val f1 = ElementImplementation(this)
        val a  = AssociationImplementation(this, declaredName = "a")
        f1.isStandard = true
        addOwnedMember(e1, global)
        addOwnedMember(e2, e1)
        addOwnedMember(f1, e1)
        addOwnedMember(a, global)
        val e1p = e1.qualifiedName
        val e2p = e2.qualifiedName
        val f1p = f1.qualifiedName
        val aName = a.qualifiedName
        assertEquals("e", e1p)
        assertEquals("e::e", e2p)
        assertEquals(null, f1p)
        assertEquals("a", aName)

        // Check that for f1 with no name a UUID5 is generated
        // assertEquals(5, f1.elementId?.version())
    }


    /**
     * Qualified name of an association that is both relationship and namespace
     */
    @Test
    fun testQualifiedName2() = testSession {
        loadKerML("""
            package p {
                assoc a; 
            }
        """)
        val a = global.resolve("p::a")?.memberElement  as Association
        assertEquals("p::a", a.qualifiedName)
    }
}