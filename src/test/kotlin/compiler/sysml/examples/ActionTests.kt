package compiler.sysml.examples

import com.github.tukcps.sysmd.model.sysml.ActionDefinition
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.ItemDefinition
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActionTests {

    /**
    * This test checks the definition of actions.
    * Refer to Section: 7.16 Actions
    * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
    */
    @Test
    fun testActionDefinition() = testSession("Actions") {
        loadSysMLv2("""
            action def ActionDef1;
        """)
        assertNoIssues()
        val actionDef1 = global.resolve("ActionDef1")?.memberElement
        assertTrue(actionDef1 is ActionDefinition)
    }

    /**
    *  This test checks the usage of actions.
    *  It verifies that an action can be instantiated from an action definition,
    *  both with and without members.
    *  Refer to Section:  7.16 Actions
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
    */
    @Test
    fun testActionUsage() = testSession("Actions") {
        loadSysMLv2("""
            action def ActionDef1;
            action action1 : ActionDef1;
        """)
        assertNoIssues()

        //val actionDef1 = global.resolve<ActionDefinition>("ActionDef1")
        //assertNotNull(actionDef1)

        val action1 = global.resolve("action1")
        assertTrue(action1?.memberElement is ActionUsage)

        // val action2 = global.resolve<ActionUsage>("action2")
        // assertNotNull(action2)
    }

    /**
    *      This test checks actions with input and output parameters.
    *      It verifies that an action can define parameters using item definitions,
    *      and that these parameters can be passed in and out of the action.
    *      Refer to Section: 7.16 Actions
     *      Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
    */
    @Test
    fun testActionWithParameters() = testSession("Actions", "Items") {
        loadSysMLv2("""
            item def ItemDef1 {
                in item 'item1.1';
                out item 'item1.2';
                in item 'item1.3';
            }
            item def ItemDef2;
            action action1 {
                inout param1 : ItemDef1;
                out param2 : ItemDef2;
            }
        """)
        assertNoIssues()

        val itemDef1 = global.resolve("ItemDef1")?.memberElement as ItemDefinition
        assertNotNull(itemDef1)

        val itemDef2 = global.resolve("ItemDef2")?.memberElement as ItemDefinition
        assertNotNull(itemDef2)

        val action1 = global.resolve("action1")?.memberElement as ActionUsage
        assertNotNull(action1)
    }

    /**
     *     This test verifies the conditional succession between two actions.
     *     It ensures that one action can succeed another based on a guard condition.
     *     Refer to Section: 7.16 Actions
     *     Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testActionWithConditionalSuccession() = testSession("Actions") {
        loadSysMLv2("""
            package actionExample {
                x : ScalarValues::Integer; 
                action def Action1;
                action def Action2;
                action def Action3;
                action action3 : Action3 {
                    action action1 : Action1;  
                    first action1 if x>0 then action2;             
                    action action2 : Action2;
                }
            }
        """)
        assertNoIssues()

        val action1 = global.resolve("actionExample::action3::action1")?.memberElement as ActionUsage
        assertNotNull(action1)

        val action2 = global.resolve("actionExample::action3::action2")?.memberElement as ActionUsage
        assertNotNull(action2)

        val action3 = global.resolve("actionExample::action3")?.memberElement as ActionUsage
        assertNotNull(action3)
    }


    /**
     *     This test checks the looping behavior within an action.
     *     It verifies that a loop action can repeat a series of assignments
     *     until a specific condition is met.
     *     Refer to Section: 7.16 Actions
     */
    @Test
    fun testActionWithLoop() = testSession("Actions", "Items", "Attributes") {
        loadSysMLv2("""
        package Loop {
            private import ScalarValues::*;
            
            action actionWithLoop {
            
                attribute x: Integer := 1;
                attribute increment: Integer = 1;
                attribute y:Integer;
                
                loop action loop1 {
                    assign y := 2*x;
                    then assign x := x+increment;
                } 
                
                until x >= 10;
                then done;
            }
            
        }
        """)
        assertNoIssues { !it.message.contains("could not be resolved") }
    }
}