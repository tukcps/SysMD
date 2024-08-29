package parsertests

import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StateTests {
    @Test
    fun stateTest() = testSession("States","Parts", "Ports", "Connections", "Interfaces") {
        + """
            package 'TwoStatemachines' {
                import OccurrenceFunctions::isDuring;
                
                attribute def MessageA1;

            	part PartA{
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
        """.trimIndent()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}