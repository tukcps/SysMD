package compiler.kerml.kernel

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class MetafeatureTest {

    @Test
    fun testMetadataFeatureNoBody() = testSession("Occurrences") {
        loadKerML("""
            metaclass m {
                feature x; 
            }
            metadata d : m ; 
        """)
        assertNoIssues()
        val m = global.resolve("m")?.member<Metaclass>()
        assertNotNull(m)
        val d = global.resolve("d")?.member<MetadataFeature>()
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
        assertNoIssues()
        val m = global.resolve("m")?.member<Metaclass>()
        assertNotNull(m)
        val d = global.resolve("d")?.member<MetadataFeature>()
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
            metaclass mc {
                feature x; 
            }
            metadata md typed by mc {
                x = "test"; 
            }
        """)
        assertNoIssues()
        val m = global.resolve("mc")?.member<Metaclass>()
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
            metaclass mc {
                feature x; 
            }
            @mf { x = "test"; }
        """)
        assertNoIssues()
        val m = global.resolve("mc")?.member<Metaclass>()
        assertNotNull(m)
        val d = global.getOwnedElementsOfType<MetadataFeature>().first()
        assertNotNull(d)
        val dx = d.getOwned<Feature>("x")
        assertNotNull(dx)
    }

}