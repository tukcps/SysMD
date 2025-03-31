package kermltests

import com.github.tukcps.sysmd.model.kerml.Structure
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StructTests {
    @Test
    fun testStruct() = testSession("Occurrences", "Links") {
        loadKerML("""
                struct s; 
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val s = global.resolve<Structure>("s")
        assertNotNull(s)
        assertEquals("Occurrence", s.ownedSpecialization.firstOrNull()?.target?.firstOrNull()?.ref?.declaredName )
    }
}