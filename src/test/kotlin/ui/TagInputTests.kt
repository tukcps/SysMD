package ui

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import util.assertNoIssues
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * During compilation, each created element is tagged with the input that has created or updated it.
 */
class TagInputTests {

    /** Test for KerML */
    @Test @Ignore
    fun testTagInputKerMLUpdated() = testSession {
        val input1 = "package p;"
        import(KerML(this).parse(input1))
        initialize(Runlevel.NAMES_RESOLVED)
        val p = global.resolve("p")?.memberElement
        assertNotNull(p)
        assertEquals(input1, p.input)

        val input2 = "package    p; // edited"
        import(KerML(this).parse(input2))
        initialize(Runlevel.NAMES_RESOLVED)
        assertEquals("package    p;", p.input)
    }

    /** Test for SysML v2 */
    @Test @Ignore
    fun testTagInputSysMLUpdated() = testSession {
        val input1 = "attribute p;"
        val elements = SysMLv2(this).parse(input1)
        import(elements)
        initialize(Runlevel.NAMES_RESOLVED)
        assertNoIssues()
        val p = global.resolve("p")?.memberElement
        assertNotNull(p)
        assertEquals(input1, p.input)

        val input2 = "attribute    p; // updated"
        val elements2 = SysMLv2(this).parse(input2)
        import(elements2)
        initialize(Runlevel.NAMES_RESOLVED)
        assertEquals(input2, p.input)
    }
}