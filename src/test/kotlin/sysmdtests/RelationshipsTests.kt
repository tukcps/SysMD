package sysmdtests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.findRelationshipsFrom
import com.github.tukcps.sysmd.services.findRelationshipsTo
import com.github.tukcps.sysmd.services.resolve.resolve
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RelationshipsTests {

    @Test
    fun dependenciesTestWithName() = testSession(loadKerML = false) {
        loadSysMD("""
            comment a /* a */ 
            comment b /* b */ 
            dependency d from a to b; 
        """.trimIndent())
       // assertEquals(0, status.errors.size, status.errors.toString())
        val dep = global.resolve<Dependency>("d")
        assertNotNull(dep)
        assertEquals("d", dep!!.name)
        assertEquals("a", dep.client.first().ref!!.name)
        assertEquals("b", dep.supplier.first().ref!!.name)
    }

    @Test
    fun dependenciesTestWithNoName() = testSession(loadKerML = false) {
        loadSysMD("""
            comment a /* a */ 
            comment b /* b */ 
            dependency a to b; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * Imports are only allowed to Namespace.
     * As Class is a Namespace, the following import works.
     * The check ensures that imports and specialization are correctly created.
     * Both ref and id are set, and both sources and targets are set.
     */
    @Test
    fun importsTest() = testSession(loadKerML = false) {
        loadSysMD("""
            class A;  // Specialization is created in A
            class B;
            A hasA import B. // Import is created in A 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<ClassImplementation>("A")
        val b = global.resolve<ClassImplementation>("B")
        val imp = a?.getOwnedElementsOfType<Import>()?.first()
        assertEquals(a, imp!!.source.first().ref)
        assertEquals(a.elementId, imp.source.first().id)
        assertEquals(b, imp.target.first().ref)
        assertEquals(b!!.elementId, imp.target.first().id)

        val spec = a.getOwnedElementsOfType<Specialization>().first()
        assertEquals(a, spec.source.first().ref)
        assertEquals(a.elementId, spec.source.first().id)
        assertEquals(any, spec.target.first().ref)
        assertEquals(any.elementId, spec.target.first().id)
    }

    /**
     * old SysMD Syntax check
     */
    @Test
    fun relationshipDefinitionTest() = testSession {
        loadSysMD("""
            feature a;
            feature b;
            assoc rel :> Links::Link; 
            connector rr: rel from a to b; 
        """.trimIndent())
        val rr = global.resolve<Connector>("rr")
        assertNotNull(rr)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<Element>("a")
        assertNotNull(a)
        val rel = global.resolve<Element>("rel") as Type
        val from = findRelationshipsFrom(a!!, "*", rel)
        assertEquals(1, from.size)
        assertEquals(1, findRelationshipsTo(global.resolve<Element>("b")!!, "*", rel).size)
    }

    /**
     * associations inherit from Link ...
     */
    @Test
    fun relationshipDefinitionInheritsLink() = testSession {
        loadSysMD("""
            assoc rel :> Links::Link; 
            """, catchExceptions = false)
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
    @Test fun relationshipDefinitionTest1() = testSession {
        loadSysMD("""
            class A;
            class B;
            assoc rel {
                end feature a: A; 
                end feature b: B;
            }""")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val rel = global.resolve<Element>("rel")
        assertNotNull(rel)
    }


    @Test fun relationshipDefinitionTest2() = testSession {
        loadSysMD("""
            class A :> ScalarValues::Real; 
            class B :> ScalarValues::Real;
            assoc rel isA Links::Link {
                end feature a: A; 
                end feature b: B; 
            }
        """, catchExceptions = false)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val rel = global.resolve<Association>("rel")
        assertNotNull(rel)
    }


    @Test fun relationshipDefinition() = testSession {
        loadSysMD("""
            class A;
            class B;
            feature aa: A; 
            feature bb: B; 
            assoc rel { 
                end feature b: B; 
                end feature a: A; 
            }
            connector r = bb rel aa.           // usage
        """, catchExceptions = false)
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
        val relFromSource = findRelationshipsFrom(source!!, "*", rel).toList()
        assertEquals(1, relFromSource[0].target.size)
    }

    @Test
    fun relationshipDefinitionWithMultipleSourcesTargets() = testSession {
        loadSysMD("""
            class A; 
            class B; 
            feature aa: A;
            feature bb: B; 
            assoc rel {
                end feature b: B;
                end feature a: A;  
            }
            connector r = bb,aa rel aa,bb.          // usage
        """, catchExceptions = false)
        // assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val source = global.resolve<Element>("bb")
        val rel = global.resolve<Association>("rel")
        val r = global.resolve<Connector>("r")
        assertNotNull(r)
        assertNotNull(rel)
        val relFromSource = findRelationshipsFrom(source!!, "*", rel).toList()
        val relToTarget = findRelationshipsTo(source, "*", rel).toList()
        assertEquals(2, relFromSource.first().source.size)
        assertEquals(2, relToTarget.first().target.size)
    }

    @Test fun relationshipWithMultiplicity() = testSession(catchExceptions = false) {
        loadSysMD("""
            class A :> Base::Anything; 
            class B :> Base::Anything;
            assoc rel isA Links::Link {
                end feature b: B :>> source [2 .. 3];
                end feature a: A redefines target [1..5];
            }
        """, catchExceptions = false)
        val rel = global.resolve<Association>("rel")
        assertNotNull(rel)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}
