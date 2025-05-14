package sysmlv2specificationtests

import com.github.tukcps.sysmd.model.sysml.ActionDefinition
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.ItemDefinition
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val actionDef1 = global.resolve<ActionDefinition>("ActionDef1")
        assertNotNull(actionDef1)
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        //val actionDef1 = global.resolve<ActionDefinition>("ActionDef1")
        //assertNotNull(actionDef1)

        val action1 = global.resolve<ActionUsage>("action1")
        assertNotNull(action1)

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
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val itemDef1 = global.resolve<ItemDefinition>("ItemDef1")
        assertNotNull(itemDef1)

        val itemDef2 = global.resolve<ItemDefinition>("ItemDef2")
        assertNotNull(itemDef2)

        val action1 = global.resolve<ActionUsage>("action1")
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
                action action3 : Action3{
                    action action1 : Action1;  
                    first action1 if x>0 then action2;             
                    action action2 : Action2;
                }
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val action1 = global.resolve<ActionUsage>("actionExample::action3::action1")
        assertNotNull(action1)

        val action2 = global.resolve<ActionUsage>("actionExample::action3::action2")
        assertNotNull(action2)
    }


    /**
     *     This test checks the looping behavior within an action.
     *     It verifies that a loop action can repeat a series of assignments
     *     until a specific condition is met.
     *     Refer to Section: 7.16 Actions
     */
    @Ignore
    @Test
    fun testActionWithLoop() = testSession("Actions", "Items", "Attributes") {
        loadSysMLv2("""
        package Loop {
            private  import ScalarValues::*;
            
            action actionWithLoop {
            
                attribute x:Integer := 1;
                attribute increment:Integer = 1;
                attribute y:Integer;
                
                loop action loop1 {
                    assign y := 2*x;
                    then assign x := x+increment;
                } 
                
                until x >= 10;
                then done;
            }
            
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

}