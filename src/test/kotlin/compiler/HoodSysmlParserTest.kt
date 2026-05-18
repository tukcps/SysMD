package compiler

import com.github.tukcps.sysmd.compiler.HoodSysmlParser
import com.github.tukcps.sysmd.model.kerml.Element
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
import kotlin.test.assertTrue

class HoodSysmlParserTest {
    val parser : HoodSysmlParser = HoodSysmlParser()


    @Test
    fun parsesOnePackage() {
        val model = parser.parseString("package firstPackage;")
        val pkt = model.global.resolve("firstPackage")?.member<Package>()
        assertEquals("firstPackage", pkt!!.name)
    }

    @Test
    fun parsesTwoPackages() {
        val model = parser.parseString("package firstPackage;\npackage secondPackage;")
        assertEquals("firstPackage", model.global.resolve("firstPackage")!!.memberName)
        assertEquals("secondPackage", model.global.resolve("secondPackage")!!.memberName)
    }

    @Test
    fun doesNotParseNestedPackages() {
        val model = parser.parseString("package firstPackage { package subPackage; }")
        val packageList = model.global.resolve("firstPackage")?.member<Package>()
        assertEquals("firstPackage", packageList?.name)
    }


    @Test
    fun parsesTopLevelPackage() {
        val model = parser.parseString("package testPackage;")
        val topLevelPackage = parser.getTopLevelPackage(model, "testPackage")

        assertEquals("testPackage", topLevelPackage?.name)
    }

    @Test
    fun parsesTwoParts() {
        val model = parser.parseString("package testPackage{part part1;part part2;}")

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val partNames = parts.map(Element::name).toList()
        assertTrue(partNames.contains("part1"))
        assertTrue(partNames.contains("part2"))

        val part1 = parts[0]
        val part2 = parts[1]
        assertEquals("testPackage", parser.getOwner(part1)?.name)
        assertEquals("testPackage", parser.getOwner(part2)?.name)
    }

    @Test
    fun parsesPartThatOwnsAttributeDefinition() {
        val model = parser.parseString("package testPackage{part part1{attribute def EventType1;}}")

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val eventTypes = parser.getAttributeDefinitions(part1)
        val eventType1 = eventTypes[0]
        assertEquals("EventType1", eventType1.name)
    }

    @Test
    fun parsesTwoAttributeDefinitions() {
        val model =
            parser.parseString("package testPackage{part part1{attribute def EventType1;attribute def EventType2;}}")

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val eventTypes = parser.getAttributeDefinitions(part1)
        val eventType1 = eventTypes[0]
        val eventType2 = eventTypes[1]

        assertEquals("EventType1", eventType1.name)
        assertEquals("EventType2", eventType2.name)
    }

    @Test
    fun parsesPartThatOwnsState() {
        val model = parser.parseString("package testPackage{part part1{state state1;}}")

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val states = part1.getOwnedElementsOfType<StateUsage>()
        val state1 = states[0]
        assertEquals("state1", state1.name)
    }

    @Test
    fun parsesTwoStateNames() {
        val model = parser.parseString("package testPackage{part part1{state state1;state state2;}}")

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val states = part1.getOwnedElementsOfType<StateUsage>()
        val state1 = states[0]
        val state2 = states[1]

        assertEquals("state1", state1.name)
        assertEquals("state2", state2.name)
    }

    @Test
    fun parsesTwoStatesOwnershipByPart() {
        val model = parser.parseString("package testPackage{part part1{state state1;state state2;}}")

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val states = part1.getOwnedElementsOfType<StateUsage>()
        val state1 = states[0]
        val state2 = states[1]

        assertEquals("part1", parser.getOwner(state1)?.name)
        assertEquals("part1", parser.getOwner(state2)?.name)
    }

    @Test
    fun parsesStateOwnershipByState() {
        val model = parser.parseString("package testPackage{part part1{state state1{state subState;}}}")

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]

        val states = part1.getOwnedElementsOfType<StateUsage>()
        val state1 = states[0]
        val subState = state1.getOwnedElementsOfType<StateUsage>()[0]

        assertEquals("subState", subState.name)
        assertEquals("state1", parser.getOwner(subState)?.name)
    }


    @Test
    fun parsesEntryAction() {
        val model = parser.parseString("""
                package testPackage{
                    part part1{
                        state status{
                            entry action initial;
                        }
                     }
                }
                """)

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]
        val status = part1.getOwnedElementsOfType<StateUsage>()[0]
        val entryAction = parser.getActionUsages(status)[0]

        assertEquals("initial", entryAction.name)
    }

    @Test
    fun parsesEntryActionAndState() {
        val model = parser.parseString("""
                package testPackage{
                    part part1{
                        state status{
                            entry action Initial;
                            state state1;
                        }
                     }
                }
                """)

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
        val parts = owningPackage!!.getOwnedElementsOfType<PartUsage>()
        val part1 = parts[0]
        val status = part1.getOwnedElementsOfType<StateUsage>()[0]
        val actionUsages = parser.getActionUsages(status)

        val entryAction = actionUsages[0]
        val state1 = actionUsages[1]

        assertEquals("Initial", entryAction.name)
        assertEquals("state1", state1.name)
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
        val owningPackage = parser.getTopLevelPackage(this, "testPackage")
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
    fun parsesTwoTransitionsFromStateToState() {
        val model = parser.parseString("""
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
        model.assertNoIssues()
        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
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
    fun parsesOneTransitionFromStateToState_withGuardCondition() {
        val model = parser.parseString("""
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
	    model.assertNoIssues()

        val owningPackage = parser.getTopLevelPackage(model, "testPackage")
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