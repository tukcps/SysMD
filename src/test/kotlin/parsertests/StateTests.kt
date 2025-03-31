package parsertests

import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.model.sysml.TransitionUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StateTests {

    @Test
    fun simpleStateTest() = testSession("Parts", "States") {
        loadSysMLv2("""
            part a {
                state s;  
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val s = global.resolve<StateUsage>("a::s")
        assertNotNull(s)
    }

    @Test
    fun simpleStateWithEntryTest() = testSession("Parts", "States") {
        loadSysMLv2("""
            part a {
                state s {
                    state s1; 
                    entry action s2; 
                }
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val s = global.resolve<StateUsage>("a::s")
        assertNotNull(s)
        assertEquals("States::StateAction", s.allSupertypes().first().qualifiedName)
        val s1 = s.resolve<StateUsage>("s1")
        assertNotNull(s1)
        assertEquals("States::StateAction", s1.allSupertypes().first().qualifiedName)
        val s2 = s.resolve<ActionUsage>("s2")
        assertNotNull(s2)
    }

    @Test
    fun simpleStateWithTransitionTest() = testSession("Parts", "States", "Connections") {
        loadSysMLv2("""
            part a {
                state s {
                    state s1; 
                    entry action s2; 
                    transition first s2 then s1; 
                }
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val s = global.resolve<StateUsage>("a::s")
        assertNotNull(s)
        assertTrue("States::StateAction" in s.allSupertypes().map { it.qualifiedName })
        val s1 = s.resolve<StateUsage>("s1")
        assertNotNull(s1)
        assertTrue("States::StateAction" in s1.allSupertypes().map{ it.qualifiedName })
        val s2 = s.resolve<ActionUsage>("s2")
        assertNotNull(s2)
        val t = s.getOwnedElementOfType<TransitionUsage>()
        assertNotNull(t)
    }

    @Test
    fun stateTest() = testSession("States", "Parts", "Ports", "Connections", "Interfaces") {
        loadSysMLv2("""
            package 'TwoStatemachines' {
                private import OccurrenceFunctions::isDuring;
                
                attribute def MessageA1;

            	part PartA {
            		attribute def CommandA12;
            		attribute def CommandA21;
            		out port outPortA;
            	
            		state StatemachineA{			
            			state StateA1;
            			state StateA2;
            			
            			entry action Initial; 
            			
            			transition 
            				first Initial 
            				then StateA1;
            			transition 
            				first StateA1 
            				// accept CommandA12
            				// do send MessageA1() via outPortA
            				then StateA2;
            			transition 
            				first StateA2 
            				// accept CommandA21
            				then StateA1;
            		}
            	}
            	
            	part PartB{
            		attribute def CommandB21;
            		
            		in port inPortB;
            	
            		state StatemachineB{			
            			state StateB1;
            			state StateB2;
            			
            			entry action Initial; 
            			
            			transition 
            				first Initial 
            				then StateB1;
            			transition 
            				first StateB1 
            				// accept MessageA1 via inPortB
            				then StateB2;
            			transition 
            				first StateB2
            				// accept CommandB21
            				then StateB1;
            		}
            	}
            	
            	// interface PartA.outPortA to PartB.inPortB;
                // "." not yet supported ... 
                interface i connect PartA::outPortA to PartB::inPortB;
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}