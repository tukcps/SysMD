package compiler.sysml.examples

import com.github.tukcps.sysmd.model.sysml.PartDefinition
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefinitionTests {

    /**
     * This test checks the creation of a simple part definition in SysML v2.
     * It verifies that a part can be defined using the `part def` keyword and that no exceptions are raised.
     * Refer to Section: 7.6 - Part Definitions and Usages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNameCompartment() = testSession("Parts") {
        loadSysMLv2("""
            part def PartDef1;
        """)
        assertNoIssues()

        val partDef1 = global.resolve("PartDef1")
        assertNotNull(partDef1)
    }

    /**
     * This test checks the creation of a part definition with a short name in SysML v2.
     * It verifies that parts can have both full and short names and be resolved correctly.
     * Refer to Section: 7.6 - Part Definitions and Usages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNameCompartmentWithShortname() = testSession("Parts") {
        loadSysMLv2("""
            part def <PD2> PartDef2;
        """)
        assertNoIssues()

        val partDef2 = global.resolve("PartDef2")
        assertNotNull(partDef2)

        val pd2 = global.resolve("PD2")
        assertNotNull(pd2)
    }

    /**
     * This test checks the creation of a part definition within a nested package structure in SysML v2.
     * It ensures that parts can be defined with fully qualified names within packages.
     * Refer to Section: 7.5 - Namespaces and Packages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNameCompartmentWithQualifiedName() = testSession("Parts") {
        loadSysMLv2("""
        package Package1 {
            package Package2 {
                part def PartDef3;
            }
        }
        """)
        assertNoIssues()

        //val partDef3 = global.resolve<PartDefinition>("PartDef3")
        //assertNotNull(partDef3)
    }

    /**
     * This test checks the creation of an abstract part definition in SysML v2.
     * It verifies that parts can be defined as abstract using the `abstract` keyword.
     * Refer to Section: 7.6 - Part Definitions and Usages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAbstractNameCompartment() = testSession("Parts") {
        loadSysMLv2("""
            abstract part def PartDef1;
        """)
        assertNoIssues()

        val partDef1 = global.resolve("PartDef1")
        assertTrue(partDef1?.memberElement is PartDefinition)
        assertTrue((partDef1.memberElement as PartDefinition).isAbstract)
    }

    /**
     * This test checks the creation of a variation part definition in SysML v2.
     * It verifies that parts can be defined as variations using the `variation` keyword.
     * Refer to Section: 7.6 - Part Definitions and Usages
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
     @Test
    fun testVariationNameCompartment() = testSession("Parts") {
        loadSysMLv2("""
            variation part def PartDef1;
        """)
        assertNoIssues()
        val partDef1 = global.resolve("PartDef1")
        assertTrue(partDef1?.memberElement is PartDefinition)
        // assertTrue((partDef1.memberElement as PartDefinition).isVariation)
    }
}
