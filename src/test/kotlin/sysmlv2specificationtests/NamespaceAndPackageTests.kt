package sysmlv2specificationtests

import com.github.tukcps.sysmd.model.sysml.PartDefinition
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NamespaceAndPackageTests {

    /**
     * This test checks the creation of a simple package in SysML v2.
     * It verifies that packages can be defined using the `package` keyword and that no exceptions are raised.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPackage() = testSession {
        loadSysMLv2("""
            package Package1;
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks the creation of nested packages in SysML v2.
     * It ensures that packages can contain other packages within them.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNestedPackages() = testSession {
        loadSysMLv2("""
        package Package1 {
            package Package2;
        }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks the creation of packages with owned members in SysML v2.
     * It verifies that packages can contain parts and other members defined within them.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPackagesWithOwnedMembers() = testSession("Parts") {
        loadSysMLv2("""
        package Package1 {
            package Package2;
            part def Part2;
            part part2 : Part2;
        }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        val partDef2 = global.resolve<PartDefinition>("Package1::Part2")
        assertNotNull(partDef2)

        val part2 = global.resolve<PartUsage>("Package1::part2")
        assertNotNull(part2)
    }

    /**
     * This test checks the creation of packages with alias members in SysML v2.
     * It verifies that packages can define aliases for other packages.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPackagesWithAliasMembers() = testSession {
        loadSysMLv2("""
        package Package1 {
            package Package2;
            alias Package2Alias for Package2;
        }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks the use of imported packages in SysML v2.
     * It verifies that packages can import other packages, with public and private import distinctions.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPackagesWithImportedPackages() = testSession {
        loadSysMLv2("""
        package Package1 {
            public import Package2::*;
            private import Package3::*;
        }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks the use of imported packages using a fully qualified name in SysML v2.
     * It ensures that packages can import other packages using qualified paths.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testImportedPackages() = testSession {
        loadSysMLv2("""
        package Package2 {
            private import Package0::Package1::**;
        }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks the creation of a package with a compartment in SysML v2.
     * It verifies that packages can have a compartment for specifying members and their relations.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPackageWithCompartment() = testSession {
        loadSysMLv2("""
        package Package1 {
            /* members */
        }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks the creation of a package with members in SysML v2.
     * It ensures that packages can contain parts and define their relationships inside a compartment.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPackageWithMembersCompartment() = testSession("Parts") {
        loadSysMLv2("""
        package Package1 {
            part def PartDef1;
            part def PartDef2;
            part part1 : PartDef1;
            part part2 : PartDef2;
        }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        val partDef2 = global.resolve<PartDefinition>("Package1::PartDef2")
        assertNotNull(partDef2)

        val part1 = global.resolve<PartUsage>("Package1::part2")
        assertNotNull(part1)
    }
}
