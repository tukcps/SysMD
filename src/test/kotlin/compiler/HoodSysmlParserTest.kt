package compiler

import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.model.sysml.TransitionUsage
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class HoodSysmlParserTest {

    @Test
    fun parsesPartThatOwnsState() = testSession("SysMLLibraries") {
        loadSysMLv2("package testPackage{part part1{state state1;}}")

        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val states = part1.getOwnedElementsOfType<StateUsage>()
        val state1 = states[0]
        assertEquals("state1", state1.name)
    }

    @Test
    fun parsesTwoStateNames() = testSession("SysMLLibraries") {
        loadSysMLv2("package testPackage{part part1{state state1;state state2;}}")

        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val states = part1.getOwnedElementsOfType<StateUsage>()
        val state1 = states[0]
        val state2 = states[1]

        assertEquals("state1", state1.name)
        assertEquals("state2", state2.name)
    }

    @Test
    fun parsesTwoStatesOwnershipByPart() = testSession("SysMLLibraries") {
        loadSysMLv2("package testPackage{part part1{state state1;state state2;}}")

        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val states = part1.getOwnedElementsOfType<StateUsage>()
        val state1 = states[0]
        val state2 = states[1]

        assertEquals("part1", state1.owningNamespace?.name)
        assertEquals("part1", state2.owningNamespace?.name)
    }

    @Test
    fun parsesStateOwnershipByState() = testSession("SysMLLibraries") {
        loadSysMLv2("package testPackage{part part1{state state1{state subState;}}}")

        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val states = part1.getOwnedElementsOfType<StateUsage>()
        val state1 = states[0]
        val subState = state1.getOwnedElementsOfType<StateUsage>()[0]

        assertEquals("subState", subState.name)
        assertEquals("state1", subState.owningNamespace?.name)
    }


    @Test
    fun parsesEntryAction() = testSession("SysMLLibraries") {
        loadSysMLv2("""
                package testPackage{
                    part part1{
                        state status{
                            entry action initial;
                        }
                     }
                }
                """)

        assertNoIssues()
        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]
        val status = part1.getOwnedElementsOfType<StateUsage>()[0]
        // val entryAction = parser.getActionUsages(status)[0]

        // assertEquals("initial", entryAction.name)
    }

    @Test
    fun parsesOneTransitionFromEntryActionToState() = testSession("Parts") {
        loadSysMLv2("""
            package testPackage{
                part part1{
                    state status {
                        entry action old;
                        state new;
                        transition first old then new;
                    }
                 }
            }
        """)
        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]
        val status = part1.getOwnedElementsOfType<StateUsage>()[0]
        val transition = status.getOwnedElementsOfType<TransitionUsage>()[0]

        assertEquals("old", transition.source.name)
        assertEquals("new", transition.target.name)
    }

    @Test
    fun parsesOneTransitionFromStateToState() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            package testPackage{
                attribute def TurnOn;
            
                part part1{
                    state status{
                        state state1;
                        state state2;
                        entry action initial;
                        transition t : Base::Anything
                            first state1 
                            accept TurnOn 
                            then state2; 
                    }
                }
            }
        """)
        assertNoIssues()
        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]
        val status = part1.getOwnedElementsOfType<StateUsage>()[0]
        val transition = status.getOwnedElementsOfType<TransitionUsage>()[0]

        assertEquals("state1", transition.source.name)
        assertEquals("state2", transition.target.name)
        // There are now infrastructures for multi-inheritance.
        // In-line with the standard:
        assertEquals("TurnOn", transition.triggerPayloadParameterType?.name)
    }

    @Test
    fun parsesTwoTransitionsFromStateToState() = testSession("SysMLLibraries") {
        loadSysMLv2("""
                package testPackage{
                    part part1{
                        state status{
                            state state1;
                            state state2;
                            transition first state1 then state2;
                            transition first state2 then state1;
                        }
                     }
                }
            """)
        assertNoIssues()
        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]
        val status = part1.getOwnedElementsOfType<StateUsage>()[0]
        val transitions = status.getOwnedElementsOfType<TransitionUsage>()

        val transition1 = transitions[0]
        assertEquals("state1", transition1.source.name)
        assertEquals("state2", transition1.target.name)

        val transition2 = transitions[1]
        assertEquals("state2", transition2.source.name)
        assertEquals("state1", transition2.target.name)
    }

    @Test
    fun parsesOneTransitionFromStateToState_withGuardCondition() = testSession ("SysMLLibraries") {
        loadSysMLv2("""
            package testPackage{
                attribute def TurnOn;
            
                part part1{
                    state status{
                        state state1;
                        state state2;
                        entry action initial;
                        transition 
                          first state1 
                          accept TurnOn
                          if 2 < 5 then state2;
                    }
                 }
            }
        """)
	    assertNoIssues()

        val owningPackage = global.resolve("testPackage")?.member<Package>()
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]
        val status = part1.getOwnedElementsOfType<StateUsage>()[0]
        val transition = status.getOwnedElementsOfType<TransitionUsage>()[0]

        assertEquals("state1", transition.source.name)
        assertEquals("state2", transition.target.name)
        // There are now infrastructures for multi-inheritance.
        // In-line with the standard:
        assertEquals("TurnOn", transition.triggerPayloadParameterType?.name)
        assertEquals("2 < 5", transition.guardCondition?.expression)
    }
}