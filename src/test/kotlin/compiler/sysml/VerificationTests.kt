package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.sysml.VerificationCaseDefinition
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VerificationTests {

    @Test
    fun verificationCaseUsageTest() = testSession("Attributes", "Parts", "Requirements") {
        loadSysMLv2("""   
            part p {
                attribute w : ScalarValues::Real = 100.0;   
            } 
              
            requirement volumeRequirementUsage  {
                subject box references p; 
                require constraint r { box.w >= 10.0 }
            }
            
            verification testVolume {
                subject references p; 
                objective testVolume {
                    doc /* description of test */ 
                    verify volumeRequirementUsage; 
                }
            }   
        """)
        assertNoIssues()
        val verification = global.resolve("testVolume")
        assertNotNull(verification)
    }

    @Test
    fun verificationCaseDefTest() = testSession("Attributes", "Parts", "Requirements") {
        loadSysMLv2("""   
            part p {
                attribute w : ScalarValues::Real = 100.0;   
            } 
              
            requirement volumeRequirementUsage  {
                subject box references p; 
                require constraint r { box.w >= 10.0 }
            }
            
            verification def testVolume {
                subject references p; 
                objective testVolume {
                    doc /* description of test */ 
                    verify volumeRequirementUsage; 
                }
            }
        """)
        assertNoIssues()
        val verification = global.resolve("testVolume")?.member<Element>()
        assertTrue(verification is VerificationCaseDefinition)
    }
}