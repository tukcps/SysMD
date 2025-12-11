package compiler.kerml.examples

import com.github.tukcps.sysmd.model.kerml.Behavior
import com.github.tukcps.sysmd.model.kerml.Step
import com.github.tukcps.sysmd.model.kerml.Succession
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class SuccessionTests {
    /**
     * Example from KerML $7.4.6.4 on Successions
     */
    @Test
    fun testSuccession7_4_6_4() = testSession("KerMLLibraries"){
        loadKerML("""
            class Shoot; 
            class Focus; // Added 
            behavior TakePicture {
                composite step focus : Focus;
                composite step shoot : Shoot;
                succession focus then shoot;
            }
        """)
        assertNoIssues()
        val focus: Step? = global.resolve("TakePicture::focus")?.member()
        val takePicture: Behavior? = global.resolve("TakePicture")?.member()
        val succession = takePicture?.getOwnedElementOfType<Succession>()
        assertNotNull(succession)
        assertNotNull(focus)
    }
}