package models.kerml

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.implementation.AssociationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.ElementImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementTests {

    @Test
    fun testQualifiedName() = testSession {
        val e1 = NamespaceImplementation(declaredName = "e")
        val e2 = NamespaceImplementation(declaredName = "e")
        val f1 = ElementImplementation()
        val a  = AssociationImplementation(declaredName = "a")
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

        // Check that for f1 with no name a UUID5 is generate
        assertEquals(5, f1.elementId?.version())
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
        val a = global.resolve<Element>("p::a")
        val qn = a?.qualifiedName
    }

    @Test
    fun testPath() = testSession {
        val e1 = NamespaceImplementation(declaredName = "e1")
        val e2 = NamespaceImplementation(declaredName = "e2")
        val f1 = ElementImplementation()
        val f2 = SpecializationImplementation()
        f1.isStandard = true
        addOwnedMember(e1, global)
        addOwnedMember(e2, e1)
        addOwnedMember(f1, e1)
        addOwnedRelationship(f2, e1)
        val e1p = e1.path()
        val e2p = e2.path()
        val f1p = f1.path()
        val f2p = f2.path()
        assertEquals("e1", e1p)
        assertEquals("e1::e2", e2p)
        assertEquals("e1/1/0", f1p)
        assertEquals("e1/2", f2p)

        // Check that for f1 with no name a UUID5 is generate
        assertEquals(5, f1.elementId?.version())
        assertEquals(Generators.nameBasedGenerator().generate(f1.path()), f1.elementId)
    }
}