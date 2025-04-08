package sysmlv2specificationtests

import util.mockup.loadSysMLv2
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.model.sysml.AttributeUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val attributeDef1 = global.resolve<AttributeDefinition>("AttributeDef1")
        assertNotNull(attributeDef1)

        val attributeDef2 = global.resolve<AttributeDefinition>("AttributeDef2")
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

        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val attributeDef1 = global.resolve<AttributeDefinition>("AttributeDef1")
        assertNotNull(attributeDef1)

        val attribute1 = global.resolve<AttributeUsage> ("attribute1")
        assertNotNull(attribute1)

        val attribute1Element = global.resolve<Element>("attribute1")
        assertNotNull(attribute1Element)
    }

}
