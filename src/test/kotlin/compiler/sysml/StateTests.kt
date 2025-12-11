package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.model.sysml.TransitionUsage
import util.assertNoIssues
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
        assertNoIssues()
        val s = global.resolve("a::s")?.member<StateUsage>()
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
        assertNoIssues()
        val s = global.resolve("a::s")?.member<StateUsage>()
        assertNotNull(s)
        assertEquals("States::StateAction", s.allSupertypes().first().qualifiedName)
        val s1 = s.resolve("s1")?.member<StateUsage>()
        assertNotNull(s1)
        assertEquals("States::StateAction", s1.allSupertypes().first().qualifiedName)
        val s2 = s.resolve("s2")?.member<ActionUsage>()
        assertNotNull(s2)
    }

    @Test
    fun simpleStateWithTransitionTest() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part a {
                state s {
                    state s1; 
                    entry action s2; 
                    transition first s2 then s1; 
                }
            }
        """)
        assertNoIssues()
        val s = global.resolve("a::s")?.member<StateUsage>()
        assertNotNull(s)
        assertTrue("States::StateAction" in s.allSupertypes().map { it.qualifiedName })
        val s1 = s.resolve("s1")?.member<StateUsage>()
        assertNotNull(s1)
        assertTrue("States::StateAction" in s1.allSupertypes().map { it.qualifiedName })
        val s2 = s.resolve("s2")?.member<ActionUsage>()
        assertNotNull(s2)
        val t = s.getOwnedElementOfType<TransitionUsage>()
        assertNotNull(t)
    }

    @Test
    fun stateTest() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            package 'TwoStatemachines' {
                // private import OccurrenceFunctions::isDuring;
                
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
            				accept CommandA21
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
            				accept CommandB21
            				then StateB1;
            		}
            	}    	
            	interface PartA.outPortA to PartB.inPortB;
            }
        """)
        assertNoIssues()
    }
}