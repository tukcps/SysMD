package compiler.sysml.examples

import com.github.tukcps.sysmd.model.sysml.ItemDefinition
import com.github.tukcps.sysmd.model.sysml.ItemUsage
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class ItemTests {

    /**
     * This test checks the definition of items in SysML v2.
     * It verifies that items can be defined using the `item def` keyword and that no exceptions are raised.
     * Refer to Section: 7.10 Items
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testItemDefinition() = testSession("Parts") {
        loadSysMLv2("""
            item def ItemDef1;
            item def ItemDef2 {
                /* members */
            }
        """)
        assertNoIssues()

        val itemDef1 = global.resolve("ItemDef1")
        assertTrue(itemDef1?.memberElement is ItemDefinition)

        val itemDef2 = global.resolve("ItemDef2")
        assertTrue(itemDef2?.memberElement is ItemDefinition)
    }

    /**
     * This test checks the usage of items in SysML v2.
     * It ensures that items can be instantiated from a definition and used with no exceptions raised.
     * Refer to Section: 7.10 Items
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testItemUsage() = testSession("Parts") {
        loadSysMLv2("""
            item def ItemDef1;
            item item1 : ItemDef1;
            item item2 : ItemDef1 {
                /* members */
            }
        """)
        assertNoIssues()

        val itemDef1 = global.resolve("ItemDef1")
        assertTrue(itemDef1?.memberElement is ItemDefinition)

        val item1 = global.resolve("item1")
        assertTrue(item1?.memberElement is ItemUsage)

        val item2 = global.resolve("item2")
        assertTrue(item2?.memberElement is ItemUsage)
    }
}
