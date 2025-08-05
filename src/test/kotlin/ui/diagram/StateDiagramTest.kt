package ui.diagram

import com.github.tukcps.sysmd.compiler.HoodSysmlParser
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.ui.diagram.StateDiagram
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StateDiagramTest {
    @Test
    fun createsStateDiagram() {
        val sysml = """
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
        """

        val model = HoodSysmlParser().parseString(sysml)
        val statemachine = model.global.resolve<StateUsage>("testPackage::Part1::Statemachine1")
        val stateDiagram = StateDiagram(statemachine!!)

        val nodes = stateDiagram.nodes()
        assertEquals(3, nodes.size)
        assertEquals(STATE1, nodes[0].text)
        assertEquals(STATE2, nodes[1].text)
        assertEquals("initial", nodes[2].text)

        val edges = stateDiagram.edges()
        assertEquals(3, edges.size)

        val edge0 = edges[0]
        assertEquals("initial", edge0.from.text)
        assertEquals(STATE1, edge0.to.text)
        assertEquals("", edge0.text)

        val edge1 = edges[1]
        assertEquals(STATE1, edge1.from.text)
        assertEquals(STATE2, edge1.to.text)
        assertEquals("Command12", edge1.text)

        val edge2 = edges[2]
        assertEquals(STATE2, edge2.from.text)
        assertEquals(STATE1, edge2.to.text)
        assertEquals("Command21", edge2.text)
    }

    companion object {
        private const val STATE1 = "state1"
        private const val STATE2 = "state2"
    }
}
