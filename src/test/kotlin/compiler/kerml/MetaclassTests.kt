package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Metaclass
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MetaclassTests {
    @Test
    fun metaclassIsCreatedTest() = testSession("Occurrences") {
        loadKerML("""
            metaclass c; 
        """)
        assertNoIssues()
        val c = global.resolve("c")?.member<Metaclass>()
        assertEquals("c", c?.declaredName)
        // val objects = global.resolve<Type>("MetaObjects::MetaObject")
        // assertTrue(c?.specializes(objects) == true)
        assertNotNull(c)
    }

    @Test
    fun metaclassHasFeaturesTest() = testSession("Occurrences") {
        loadKerML(""" 
            metaclass c {
                feature f; 
            }
        """)
        assertNoIssues()
        val c = global.resolve("c")?.member<Metaclass>()
        assertNotNull(c)
        val f = global.resolve("c::f")?.member<Feature>()
        assertNotNull(f)
    }
}