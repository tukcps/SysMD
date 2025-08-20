package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ImportTests {
    @Test
    fun importCreatedTest() = testSession {
        loadKerML("""
            namespace a {
                namespace b;
            }
            private import a::b::*; 
        """)
        assertNoIssues()
        val imp = global.getOwnedElementOfType<Import>()
        assertNotNull(imp)
    }

    @Test
    fun importCreatedTest2() = testSession {
        loadKerML("""
            namespace a {
                public import Base::*; 
            }
        """)
        assertNoIssues()
        val a = global.resolve<Namespace>("a")
        assertNotNull(a)
        val imp = a.getOwnedElementOfType<Import>()
        assertNotNull(imp)
    }

    /**
     * Imports are only allowed to Namespace.
     * The check ensures that imports and specialization are correctly created.
     * Both ref and id are set, and both sources and targets are set.
     */
    @Test
    fun importsTest() = testSession {
        loadKerML("""
            namespace A { private import B; }  
            type B :> Base::Anything;
        """)
        assertNoIssues()
        val a = global.resolve<Namespace>("A")
        val b = global.resolve<Type>("B")
        val imp = a?.getOwnedElementsOfType<Import>()?.first()
        assertNotNull(imp)
        assertNotNull(b)
        assertEquals(a, imp.source.first())
        assertEquals(a.elementId, imp.source.first().elementId)
        assertEquals(b, imp.target.first())
        assertEquals(b.elementId, imp.target.first().elementId)
    }


    /**
     * According to spec possible, but proof-of-concept does not generate abstract
     * representation.
     */
    @Ignore
    @Test
    fun importBodyIsAddedTest() = testSession {
        loadKerML("""
            public import Base {
                classifier d; 
            }
        """)
        assertNoIssues()
        val imp = global.getOwnedElementOfType<Import>()
        assertNotNull(imp)
        val d = imp.getOwnedElement("d")
        assertNotNull(d)
    }

    @Test
    fun importNonRecursiveTest() = testSession {
        loadKerML("""
            private import Base::*;  
        """)
        assertNoIssues()
        val imp = global.getOwnedElementOfType<Import>()
        assertEquals(false, imp?.isRecursive)
        assertEquals(false, imp?.isImportAll)
    }


    /**
     * Recursive import of Namespace
     */
    @Test
    fun importRecursiveTest() = testSession {
        loadKerML("""
            package test {
                public import Base::Anything; 
            }
            public import test::*::**;
        """)
        assertNoIssues()
        val imp = global.getOwnedElementOfType<Import>()
        assertEquals(true, imp?.isRecursive)
        assertEquals(false, imp?.isImportAll)
        val any = global.resolve<Type>("Anything")
        assertNotNull(any)
    }
}