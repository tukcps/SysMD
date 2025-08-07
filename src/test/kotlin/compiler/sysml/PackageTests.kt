package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.services.resolve.resolve
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
        val openCar = global.resolve<Package>("openCar")
        val openCarLong = global.resolve<Package>("Open Car")
        assertNotNull(openCar)
        assertNotNull(openCarLong)
        assertNoIssues()
    }
}