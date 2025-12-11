package compiler.sysml.examples

import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.model.sysml.AttributeUsage
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class AttributeTests {

    /**
     * This test checks the definition of attributes in SysML v2.
     * It verifies that attributes can be defined using the `attribute def` keyword and that no exceptions are raised.
     * Refer to Section: 7.7 - Attribute
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAttributeDefinition() = testSession("Attributes") {
        loadSysMLv2("""
        attribute def AttributeDef1;
        attribute def AttributeDef2 {
            /* members */
        }
        """)
        assertNoIssues()

        val attributeDef1: AttributeDefinition? = global.resolve("AttributeDef1")?.member()
        assertNotNull(attributeDef1)

        val attributeDef2: AttributeDefinition? = global.resolve("AttributeDef2")?.member()
        assertNotNull(attributeDef2)
    }

    /**
     * This test checks the usage of attributes in SysML v2.
     * It ensures that attributes can be used after being defined and that no exceptions are raised during their usage.
     * Refer to Section: 7.7 - Attributes
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAttributeUsage() = testSession("Attributes") {
        loadSysMLv2("""
            attribute def AttributeDef1;
            attribute attribute1 : AttributeDef1;
        """)
        assertNoIssues()

        val attributeDef1: AttributeDefinition? = global.resolve("AttributeDef1")?.member()
        assertNotNull(attributeDef1)

        val attribute1: AttributeUsage? = global.resolve ("attribute1")?.member()
        assertNotNull(attribute1)

        val attribute1Element = global.resolve("attribute1")?.memberElement
        assertNotNull(attribute1Element)
    }

}
