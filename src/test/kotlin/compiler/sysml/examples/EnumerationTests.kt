package sysmlv2specificationtests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class EnumerationTests {

    /**
     * This test checks the definition of enumerations in SysML v2.
     * It verifies that enumerations can be defined using the `enum def` keyword and that no exceptions are raised.
     * Refer to Section: 7.8 - Enumerations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Ignore
    @Test
    fun testEnumerationDefinition() = testSession {
        loadSysMLv2("""
        enum def EnumerationDef1;

        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the creation of enumerations with individual members in SysML v2.
     * It ensures that enumerations can have individual values (enums) and be defined inside a compartment.
     * Refer to Section: 7.8 - Enumerations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Ignore
    @Test
    fun testEnumsCompartment() = testSession {
        loadSysMLv2("""
        enum def EnumerationDef1 {
            enum enum1;
            enum enum2;
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}
