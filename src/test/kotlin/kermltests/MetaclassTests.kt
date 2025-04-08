package kermltests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MetaclassTests {
    @Test
    fun metaclassIsCreatedTest() = testSession("Occurrences") {
        loadKerML("""
            metaclass c; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.resolve<Metaclass>("c")
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.resolve<Metaclass>("c")
        assertNotNull(c)
        val f = global.resolve<Feature>("c::f")
        assertNotNull(f)
    }
}