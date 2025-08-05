package compiler.kerml

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
    fun testStruct() = testSession("Occurrences", "Links") {
        loadKerML("""
            struct s; 
        """)
        assertNoIssues()
        val s = global.resolve<Structure>("s")
        assertNotNull(s)
        assertEquals("Occurrence", s.ownedSpecialization.firstOrNull()?.target?.firstOrNull()?.declaredName)
    }
}