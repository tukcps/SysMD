package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.PartDefinition
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import com.github.tukcps.sysmd.model.sysml.PortUsage
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PartAndPortTests {

    /**
     * A port usage generates a feature of class "Port".
     */
    @Test
    fun portTest1() = testSession("Ports") {
        loadSysMLv2("""
            port p; 
        """)
        assertNoIssues()
        val p = global.resolve("p")?.memberElement as PortUsage?
        assertNotNull(p)
        val type = p.type
        assertTrue(global.resolve("Ports::Port")?.memberElement as Type in type)
    }

    /**
     * A port usage generates a feature of class "Port".
     */
    @Test
    fun portTestDirection() = testSession("Ports") {
        loadSysMLv2("""
            out port p; 
        """)
        assertNoIssues()
        val p = global.resolve("p")?.memberElement
        assertTrue(p is PortUsage)
        assertNotNull(p)
        assertEquals(p.direction, Feature.FeatureDirectionKind.OUT)
        assertTrue { global.resolve("Ports::Port")?.memberElement as Type in p.type }
    }

    /**
     * A port usage generates a feature of class "Port".
     */
    @Test
    fun portDefTest() = testSession("Ports") {
        loadSysMLv2("""
            port def <short> p; 
        """)
        assertNoIssues()
        val p = global.resolve("p")?.memberElement
        assertTrue(p is PortDefinition)
        assertNotNull(p)
        assertEquals(p.allSupertypes().first().qualifiedName, "Ports::Port")
    }

    /**
     * A port usage generates a feature of class "Port".
     * Here, a port that "features" a Real value and has the direction out.
     */
    @Test
    fun portDefTestWithSpecialization() = testSession("Ports") {
        loadSysMLv2("""
            port def p1 {
                attribute value: ScalarValues::Real; 
            }
            port def p2 :> p1 {
                :>> value; 
            }
            out port p3 : p2; 
        """)
        assertNoIssues()
        val p2 = global.resolve("p2")?.memberElement as PortDefinition
        assertNotNull(p2)
        assertEquals(p2.allSupertypes().first().qualifiedName, "p1")
        val p3 = global.resolve("p3")?.memberElement as PortUsage
        assertEquals(Feature.FeatureDirectionKind.OUT, p3.direction)
    }

    /**
     * A port usage generates a feature of class "Parts::Part".
     */
    @Test
    fun partTest1() = testSession("Parts") {
        loadSysMLv2("""
            part p; 
        """)
        assertNoIssues()
        val p = global.resolve("p")?.memberElement
        assertTrue(p is PartUsage)
        val type = p.type
        assertEquals(type.first().qualifiedName, "Parts::Part")
    }

    /**
     * A port usage generates a feature of class "Port".
     */
    @Test
    fun partDefTestWithSpecialization() = testSession("Parts") {
        loadSysMLv2("""
            part def p1; 
            part def p2 :> p1; 
        """)
        assertNoIssues()
        val p2 = global.resolve("p2")?.memberElement
        assertTrue(p2 is PartDefinition)
        assertEquals(p2.allSupertypes().first().qualifiedName, "p1")
    }

}