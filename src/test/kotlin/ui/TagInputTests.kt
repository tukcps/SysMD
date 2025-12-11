package ui

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.services.initialize
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * During compilation, each created element is tagged with the input that has created or updated it.
 */
class TagInputTests {

    /** Test for KerML */
    @Test
    fun testTagInputKerML() = testSession {
        val input1 = "package p;"
        KerML(this).parse(input1)
        initialize(1)
        val p = global.resolve("p")?.memberElement
        assertNotNull(p)
        assertEquals(input1, p.input)

        val input2 = "package    p; // edited"
        KerML(this).parse(input2)
        initialize(1)
        assertEquals(input2, p.input)
    }

    /** Test for SysML v2 */
    @Test
    fun testTagInputSysML() = testSession {
        val input1 = "attribute p;"
        SysMLv2(this).parse(input1)
        initialize(1)
        val p = global.resolve("p")?.memberElement
        assertNotNull(p)
        assertEquals(input1, p.input)

        val input2 = "attribute    p; // updated"
        SysMLv2(this).parse(input2)
        initialize(1)
        assertEquals(input2, p.input)
    }
}