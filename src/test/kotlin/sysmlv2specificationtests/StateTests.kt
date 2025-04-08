package sysmlv2specificationtests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class StateTests {

    /**
     * This test checks the definition of a simple state in SysML v2.
     * It verifies that states can be defined using the `state def` keyword and that no exceptions are raised.
     * Refer to Section: 7. 17 States
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testSimpleStateDefinition() = testSession("States") {
        loadSysMLv2("""
        state def StateDef1;
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the definition of a state with a compartment in SysML v2.
     * It ensures that states can define members in a compartment.
     * Refer to Section: 7. 17 States
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testStateWithCompartmentDefinition() = testSession("States", "Parts") {
        loadSysMLv2("""
        state def StateDef1 {
            part def Part1;
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the usage of a simple state in SysML v2.
     * It ensures that states can be instantiated and used with no exceptions raised.
     * Refer to Section: 7. 17 States
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testSimpleStateUsage() = testSession("States", "Parts") {
        loadSysMLv2("""
        state def StateDef1;
        state state1 : StateDef1;
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }


    @Test
    fun testStateWithCompartmentUsage() = testSession("States") {
        loadSysMLv2("""
        state def StateDef1;
        state state1 : StateDef1 {
            /* members */
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the usage of entry, do, and exit actions for a state in SysML v2.
     * It ensures that states can define actions to be performed upon entry, during, and upon exit.
     * Refer to Section: 7.17 States
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Ignore // Cyclic recursion in lookup of action1 / action2
    @Test
    fun testStateWithEntryDoExitActions() = testSession("States", "Actions") {
        loadSysMLv2("""
            action def Action1;
            action action1 : Action1;
            
            action def Action2;
            action action2 : Action2;
            
            action def Action3;
            action action3 : Action3;
            
            state state1 {
                entry action1;
                do action2;
                exit action3;
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }


    /**
     * This test checks the `exhibit` relationship for a state in SysML v2.
     * It verifies that parts can exhibit specific states.
     * Refer to Section: 7.17 States
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Ignore
    @Test
    fun testExhibit() = testSession("States", "Actions", "Parts") {
        loadSysMLv2("""
        part def Part1;
        state def State1;
        state state1 : State1;
        part part1 : Part1 {
            exhibit state1;
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the `exhibit` relationship with a specific state in SysML v2.
     * It ensures that parts can exhibit a particular state.
     * Refer to Section: 7.17 States
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Ignore
    @Test
    fun testExhibitState() = testSession("States", "Actions", "Parts") {
        loadSysMLv2("""
        part def Part1;
        state def State1;
        part part1 : Part1 {
            exhibit state state1 : State1;
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}
