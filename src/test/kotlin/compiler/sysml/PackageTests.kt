package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.Package
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class PackageTests {

    @Test
    fun packageTest() = testSession {
        loadSysMLv2("""
            package <openCar> 'Open Car' {
                doc /* test */ 
            }
        """)
        val openCar: Package? = global.resolve("openCar")?.member()
        val openCarLong: Package? = global.resolve("Open Car")?.member()
        assertNotNull(openCar)
        assertNotNull(openCarLong)
        assertNoIssues()
    }
}