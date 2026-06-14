package ui.diagram

import org.junit.jupiter.api.Test
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession

class StateDiagramTest {
    @Test
    fun createsStateDiagram() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            package testPackage {              
                attribute def Command12;                    
                attribute def Command21;                    
                
                part Part1 {                                                                              
                    state Statemachine1 {                                                
                        state state1;                            
                        state state2;
                        entry action initial;  
                        transition 
                            first initial 
                            then state1;   
                        transition                                    
                            first state1                            
                            accept Command12                        
                            then state2;                 
                        transition                                    
                            first state2                            
                            accept Command21       
                            then state1;                            
                    }                                                
                }                                                    
            }                                                        
        """)
        assertNoIssues()
    }
}
