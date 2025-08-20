package compiler.sysml.examples

import util.mockup.loadSysMLv2
import com.github.tukcps.sysmd.model.sysml.ItemDefinition
import com.github.tukcps.sysmd.model.sysml.ItemUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val itemDef1 = global.resolve<ItemDefinition>("ItemDef1")
        assertNotNull(itemDef1)

        val itemDef2 = global.resolve<ItemDefinition>("ItemDef2")
        assertNotNull(itemDef2)
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val itemDef1 = global.resolve<ItemDefinition>("ItemDef1")
        assertNotNull(itemDef1)

        val item1 = global.resolve<ItemUsage>("item1")
        assertNotNull(item1)

        val item2 = global.resolve<ItemUsage>("item2")
        assertNotNull(item2)
    }
}
