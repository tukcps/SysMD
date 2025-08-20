package compiler.kerml.examples

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Examples from KerML sec. 7.4.13
 */
class MetaFeatureTests {
    @Test
    fun metadataFeatureImplementationTest() = testSession("ScalarValues") {

        loadKerML("""
            public import ScalarValues::*; 
            metaclass SecurityRelated;
            metaclass ApprovalAnnotation {
                feature approved[1] : Boolean;
                feature approver[1] : String;
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val securityRelated = global.resolve<Metaclass>("SecurityRelated")
        assertNotNull(securityRelated)
        val approvalAnnotation = global.resolve<Metaclass>("ApprovalAnnotation")
        assertNotNull(approvalAnnotation)
    }

    @Test
    fun metadataFeatureImplementationTest2() = testSession("ScalarValues") {
        loadKerML("""
            private import ScalarValues::*; 
            metaclass ApprovalAnnotation {
                feature approved[1] : Boolean;
                feature approver[1] : String;
            }
            metadata ApprovalAnnotation about Design {
                feature redefines approved = true;
                feature redefines approver = "John Smith";
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val approvalAnnotation = global.getOwnedElementOfType<MetadataFeature>()
        assertNotNull(approvalAnnotation)
        val approver = approvalAnnotation.resolve<Feature>("approver")
        assertNotNull(approver)
        assertEquals(""""John Smith"""", approver.expression)
    }

    @Test
    fun userDefinedKeyword() = testSession("Occurrences") {
        loadKerML("""
            #command behavior Save;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

}