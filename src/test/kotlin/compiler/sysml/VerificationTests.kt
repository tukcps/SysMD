package compiler.sysml

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class VerificationTests {

    @Test
    fun simpleExampleTest() = testSession("Attributes", "Parts", "Requirements") {
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
}