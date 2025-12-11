package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Structure
import com.github.tukcps.sysmd.model.kerml.Type
import junit.framework.TestCase.assertTrue
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class StructTests {
    @Test
    fun testStruct() = testSession( "Objects") {
        loadKerML("""
            struct s {
                in feature f1; 
                in feature f2;
                out feature f3;
                out feature f4;
            } 
        """)
        assertNoIssues()
        val s = global.resolve("s")?.member<Structure>()
        assertNotNull(s)
        val objects = global.resolve("Objects::Object")?.member<Type>()
        assertTrue(objects in s.allSupertypes())
    }
}