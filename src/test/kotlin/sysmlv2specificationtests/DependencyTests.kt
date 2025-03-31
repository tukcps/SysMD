package sysmlv2specificationtests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class DependencyTests {

    /**
     * This test checks the creation of a simple dependency in SysML v2.
     * It verifies that dependencies can be defined between packages using the `dependency` keyword.
     * Refer to Section: 7.3 - Dependency Definitions and Usages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNormalDependency() = testSession  {
        loadSysMLv2("""
        dependency Package2 to Package1;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks the creation of crossed dependencies in SysML v2.
     * It verifies that multiple packages can be involved in dependencies to multiple other packages.
     * Refer to Section: 7.3 - Dependency Definitions and Usages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testCrossedDependencies() = testSession {
        loadSysMLv2("""
        dependency Package1, Package2 to Package3, Package4;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks the creation of a dependency with descriptive names in SysML v2.
     * It verifies that named layers (such as service and data layers) can be used in the context of dependencies.
     * Refer to Section: 7.3 - Dependency Definitions and Usages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testDependencyExample() = testSession  {
        loadSysMLv2("""
        dependency 'Service Layer' to 'Data Layer', 'External Interface Layer' {
            /* 'Service Layer' is the client of this dependency,
            * not its name. */
        }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}
