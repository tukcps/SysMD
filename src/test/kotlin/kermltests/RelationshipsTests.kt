package kermltests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.findRelationshipsFrom
import com.github.tukcps.sysmd.services.findRelationshipsTo
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class RelationshipsTests {

    @Test
    fun dependenciesTestWithName() = testSession("Links") {
        loadKerML("""
                comment a /* a */ 
                comment b /* b */ 
                dependency d from a to b;
            """)
        // assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val d = global.resolve<Dependency>("d")
        assertNotNull(d)
        assertEquals("d", d.name)
        assertEquals("a", d.client.first().ref!!.name)
        assertEquals("b", d.supplier.first().ref!!.name)
    }

    @Test
    fun dependenciesTestWithNoName() = testSession {
        loadKerML("""
                comment a /* a */ 
                comment b /* b */ 
                dependency a to b;
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val d = global.getOwnedElementOfType<Dependency>()
        assertNotNull(d)
        assertEquals("a", d.client.first().ref!!.name)
        assertEquals("b", d.supplier.first().ref!!.name)
    }

    @Test
    fun dependenciesTestWithNoNameInNamespace() = testSession {
        loadKerML("""
                namespace n {
                    comment a /* a */ 
                    comment b /* b */ 
                    dependency a to b;
                }
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val d = global.resolve<Namespace>("n")?.getOwnedElementOfType<Dependency>()
        assertNotNull(d)
        assertEquals("a", d.client.first().ref!!.name)
        assertEquals("b", d.supplier.first().ref!!.name)
    }


    /**
     * Imports are only allowed to Namespace.
     * As Class is a Namespace, the following import works.
     * The check ensures that imports and specialization are correctly created.
     * Both ref and id are set, and both sources and targets are set.
     */
    @Test
    fun importsTest() = testSession {
        loadKerML("""
            namespace A { private import B; }  
            type B :> Base::Anything;
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<Namespace>("A")
        val b = global.resolve<Type>("B")
        val imp = a?.getOwnedElementsOfType<Import>()?.first()
        assertNotNull(imp)
        assertNotNull(b)
        assertEquals(a, imp.source.first().ref)
        assertEquals(a.elementId, imp.source.first().id)
        assertEquals(b, imp.target.first().ref)
        assertEquals(b.elementId, imp.target.first().id)
    }

    /**
     * old SysMD Syntax check
     */
    @Test
    fun relationshipDefinitionTest() = testSession("Occurrences", "Links") {
        loadKerML("""
            feature a;
            feature b;
            assoc rel :> Links::Link; 
            connector rr: rel from a to b; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val rr = global.resolve<Connector>("rr")
        assertNotNull(rr)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<Element>("a")
        assertNotNull(a)
        val rel = global.resolve<Element>("rel") as Type
        val from = findRelationshipsFrom(a, "*", rel)
        assertEquals(1, from.size)
        assertEquals(1, findRelationshipsTo(global.resolve<Element>("b")!!, "*", rel).size)
    }

    /**
     * associations inherit from Link ...
     */
    @Test
    fun relationshipDefinitionInheritsLink() = testSession("ScalarValues", "Links") {
        loadKerML("""
            assoc rel :> Links::Link; 
            """)
        // assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val rel = global.resolve<Element>("rel")
        val binLink = global.resolve<Element>("Links::BinaryLink")
        assertNotNull(rel)
        assertNotNull(binLink)
        val source = global.resolve<Feature>("rel::source")
        assertNotNull(source)
        val target = global.resolve<Feature>("rel::target")
        assertNotNull(target)
    }

    /**
     * We allow also relations on properties.
     */
    @Test fun relationshipDefinitionTest1() = testSession("Links") {
        loadKerML("""
            type A :> Base::Anything;
            type B :> Base::Anything;
            assoc rel {
                end feature a: A [1 .. 2] :>> source; 
                end feature b: B :>> target;
            }""")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val rel = global.resolve<Association>("rel")
        val a = global.resolve<Feature>("rel::a")
        val b = global.resolve<Feature>("rel::b")
        assertNotNull(rel)
        assertNotNull(a)
        assertNotNull(b)
    }


    @Test fun relationshipDefinitionTest2() = testSession("Links") {
        loadKerML("""
            type A :> Base::Anything; 
            type B :> Base::Anything;
            assoc rel :> Links::Link {
                end feature a: A; 
                end feature b: B; 
            }
        """, catchExceptions = false)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val rel = global.resolve<Association>("rel")
        assertNotNull(rel)
    }


    @Test fun relationshipDefinition() = testSession("Occurrences") {
        loadKerML("""
                class A;
                class B;
                feature aa: A; 
                feature bb: B; 
                assoc rel { 
                    end feature b: B; 
                    end feature a: A; 
                }
                connector r from bb to aa;
            """)
        // assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val link = global.resolve<Association>("Links::Link")
        assertNotNull(link)
        val r = global.resolve<Connector>("r")
        assertNotNull(r)
        val source = global.resolve<Element>("bb")
        assertNotNull(source)
        val rel = global.resolve<Association>("rel")
        assertNotNull(rel)
        // assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertNotNull(rel)
        val relFromSource = findRelationshipsFrom(source, "*", rel).toList()
        assertEquals(1, relFromSource[0].target.size)
    }

    @Test
    fun relationshipDefinitionWithMultipleSourcesTargets() = testSession("Occurrences", "Links") {
        loadKerML("""
            class A; 
            class B; 
            feature aa: A;
            feature bb: B; 
            assoc rel {
                end feature b: B;
                end feature a: A;  
            }
            connector r: rel from bb, aa to aa, bb;
        """, catchExceptions = false)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val source = global.resolve<Feature>("bb")
        val rel = global.resolve<Association>("rel")
        val r = global.resolve<Connector>("r")
        assertNotNull(r)
        assertNotNull(rel)
        val relFromSource = findRelationshipsFrom(source!!, "*", rel).toList()
        val relToTarget = findRelationshipsTo(source, "*", rel).toList()
        assertEquals(2, relFromSource.first().source.size)
        assertEquals(2, relToTarget.first().target.size)
        assertEquals(2, r.source.size)
    }

    @Test fun relationshipWithMultiplicity() = testSession("Links") {
        loadKerML("""
            class A :> Base::Anything; 
            class B :> Base::Anything;
            assoc rel :> Links::Link {
                end feature b: B :>> source [2 .. 3];
                end feature a: A redefines target [1..5];
            }
        """)
        val rel = global.resolve<Association>("rel")
        assertNotNull(rel)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}
