package kermltests

import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import util.testSession

/**
 * Here we test whether the properties on some paths or via inheritance are found correctly.
 */
class CreateFindTests {

    @Test fun createFindPackage() = testSession {
        loadKerML("package x;")
        assertNotNull(global.resolve<Package>("x"))
    }

    @Test
    fun createFindElement() = testSession("Occurrences") {
        loadKerML("""
                    package x { class y; } 
            """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val x1 = global.resolve<Package>("x")!!
        assertNotNull(x1)
        assertNotNull(x1.resolve<Class>("y"))
    }

    @Test fun createFindHasAElement() = testSession("ScalarValues") {
       loadKerML("""
                    package x {
                        class y; 
                        feature z; 
                    } 
        """.trimIndent())
       val x = global.resolve<Package>("x")!!
       assertNotNull(x)
       val y = global.resolve<Class>("x::y")
       val y2 = x.resolve<Class>("y")
       assertNotNull(y)
       assertNotNull(y2)
       val z = global.resolve<Feature>("x::z")
       val z2 = x.resolve<Feature>("z")
       assertNotNull(z)
       assertNotNull(z2)
    }

    /**
     * Properties can be found.
     */
    @Test
    fun createFindHasAProperty() = testSession("ScalarValues") {
        loadKerML("""
            package x {
                class y { 
                    feature z;  // Shall be visible as x::y::z from root namespace. 
                }
                feature z;     // Shall be visible in x via x::z
            }
            """.trimIndent())
        val xyz = global.resolve<Feature>("x::y::z")
        assertNotNull(xyz)
        val xz = global.resolve<Feature>("x::z")
        assertNotNull(xz)
        val x = global.resolve<Package>("x")
        assertNotNull(x)
        assertNotNull(x!!.resolve<Class>("y"))
        assertNotNull(x.resolve<Class>("y")) // Occurrence in hasA
    }
}