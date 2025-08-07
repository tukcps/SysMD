package sysmlv2specificationtests

import util.mockup.loadSysMLv2
import com.github.tukcps.sysmd.model.sysml.PartDefinition
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.testSession
import kotlin.test.*

class UsageTests {

    /**
     * This test checks the creation of a part and its usage in SysML v2.
     * It verifies that a part can be defined and used based on another part definition with no exceptions raised.
     * Refer to Section: 7.11.2 - Part Definitions and Usages - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNameCompartment() = testSession("Parts") {
        loadSysMLv2("""
        part def PartDef1;
        part part1 : PartDef1;
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val partDef1 = global.resolve<PartDefinition>("PartDef1")
        assertNotNull(partDef1)

        val part1 = global.resolve<PartUsage>("part1")
        assertNotNull(part1)
    }

    /**
     * This test checks the use of short names in a part definition in SysML v2.
     * It ensures that parts can be defined with both full and short names.
     * Refer to Section: 7.11.2 - Part Definitions and Usages - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNameCompartmentWithShortName() = testSession("Parts") {
        loadSysMLv2("""
            part def <PD2> PartDef2;
            part <'p#2'> part2 : PartDef2;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val partDef2 = global.resolve<PartDefinition>("PartDef2")
        assertNotNull(partDef2)
    }

    /**
     * This test checks the definition and usage of nested parts in SysML v2.
     * It verifies that parts can have nested compartments with other parts.
     * Refer to Section: 7.11.2 - Part Definitions and Usages - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNestedNameCompartment() = testSession("Parts") {
        loadSysMLv2("""
        part def PartDef3;
        part part1 {
            part part2 {
                part part3 : PartDef3;
            }
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val partDef3 = global.resolve<PartDefinition>("PartDef3")
        assertNotNull(partDef3)

        val part1 = global.resolve<PartUsage>("part1")
        assertNotNull(part1)
    }

    /**
     * This test checks the use of an alias for a part in SysML v2.
     * It verifies that parts can have aliases defined in multiple namespaces.
     * Refer to Section: 7.5 - Namespaces and Packages - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAbstractNameCompartmentWithAlias() = testSession("Parts") {
        loadSysMLv2("""
            part def PartDef1;
            abstract part part1 : PartDef1; 
            package P {
                alias partAlias1 for part1;
            }
            package Q {
                alias partAlias2 for part1;
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val partDef1 = global.resolve<PartDefinition>("PartDef1")
        assertNotNull(partDef1)

        val part1 = global.resolve<PartUsage>("part1")
        assertNotNull(part1)
    }

    /**
     * This test checks the creation of a variation part in SysML v2.
     * It verifies that parts can be defined as variations using the `variation` keyword.
     * Refer to Section: 7.11.2 - Part Definitions and Usages - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Ignore
    @Test
    fun testVariationNameCompartment() = testSession("Parts") {
        loadSysMLv2("""
        part def PartDef1;
        variation part part1 : PartDef1;
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val partDef1 = global.resolve<PartDefinition>("PartDef1")
        assertNotNull(partDef1)
    }

    /**
     * This test checks the use of feature memberships in SysML v2.
     * It verifies that parts can have membership features defined within them.
     * Refer to Section: 7.11.3 - Part Memberships - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testFeatureMembership() = testSession("Parts") {
        loadSysMLv2("""
        part def PartDef1 {
            part def Part2;
            part part2 : Part2 [0..*];
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the nested feature memberships in SysML v2.
     * It ensures that parts can have nested memberships within them.
     * Refer to Section: 7.11.3 - Part Memberships - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNestedFeatureMembership() = testSession("Parts") {
        loadSysMLv2("""
        part def Part1;
        part def Part2;
        part part1 : Part1 [0..1] {
            part part2 : Part2 [0..*];
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the use of part relationships in SysML v2.
     * It verifies that parts can be connected and exhibit relationships.
     * Refer to Section: 7.11.4 - Part Relations and Usages - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testRelationshipsCompartment() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            part def PartDef1;
            part def PartDef2 :> PartDef1;
            part part1 : PartDef1;
            part part2 : PartDef2 :> part1; // 
            connect part2 to part3;
            part part3;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}
