package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Structure
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StructTests {
    @Test
    fun testStruct() = testSession("Occurrences") {
        loadKerML("""
            struct s {
                in feature f1; 
                in feature f2;
                out feature f3;
                out feature f4;
            } 
        """)
        assertNoIssues()
        val s = global.resolve<Structure>("s")
        assertNotNull(s)
        val occurrence = global.resolve<Class>("Occurrences::Occurrence")
        assertEquals(occurrence, s.allSupertypes().firstOrNull())
    }
}