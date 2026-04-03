package compiler.kerml.examples

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
        assertNoIssues()
        val securityRelated: Metaclass? = global.resolve("SecurityRelated")?.member()
        assertNotNull(securityRelated)
        val approvalAnnotation: Metaclass? = global.resolve("ApprovalAnnotation")?.member()
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
        assertNoIssues()
        val approvalAnnotation = global.getOwnedElementOfType<MetadataFeature>()
        assertNotNull(approvalAnnotation)
        val approver: Feature? = approvalAnnotation.resolve("approver")?.member()
        assertNotNull(approver)
        assertEquals(""""John Smith"""", approver.expression)
    }

    @Test
    fun userDefinedKeyword() = testSession("Occurrences") {
        loadKerML("""
            #command behavior Save;
        """)
        assertNoIssues()
    }

}