package kermltests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.kerml.getOwned
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MetafeatureTest {

    @Test
    fun testMetadataFeatureNoBody() = testSession("Occurrences") {
        loadKerML("""
            metaclass m {
                feature x; 
            }
            metadata d : m ; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val m = global.resolve<Metaclass>("m")
        assertNotNull(m)
        val d = global.resolve<MetadataFeature>("d")
        assertNotNull(d)
    }

    @Test
    fun testMetaDataFeatureWithBody() = testSession("Occurrences") {
        loadKerML("""
            metaclass m {
                feature x; 
            }
            metadata d : m {
                x = "test"; 
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val m = global.resolve<Metaclass>("m")
        assertNotNull(m)
        val d = global.resolve<MetadataFeature>("d")
        assertNotNull(d)
        val dx = d.getOwned<Feature>("x")
        assertNotNull(dx)
    }


    /**
     * Metadata without name
     */
    @Test
    fun testMetadataFeatureWithBodyNoName() = testSession("Occurrences") {
        loadKerML("""
            metaclass m {
                feature x; 
            }
            metadata m {
                x = "test"; 
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val m = global.resolve<Metaclass>("m")
        assertNotNull(m)
        val d = global.getOwnedElementsOfType<MetadataFeature>().first()
        assertNotNull(d)
        val dx = d.getOwned<Feature>("x")
        assertNotNull(dx)
    }


    /**
     * Metadata without name and @
     */
    @Test
    fun testMetadataFeatureWithBodyNoNameAndAt() = testSession("Occurrences") {
        loadKerML("""
            metaclass m {
                feature x; 
            }
            @m { x = "test"; }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val m = global.resolve<Metaclass>("m")
        assertNotNull(m)
        val d = global.getOwnedElementsOfType<MetadataFeature>().first()
        assertNotNull(d)
        val dx = d.getOwned<Feature>("x")
        assertNotNull(dx)
    }

}