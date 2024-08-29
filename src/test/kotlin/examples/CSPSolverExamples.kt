package examples

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.SysMDInconsistency
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class CSPSolverExamples {

    /**
     * Multiplicity of subclasses must be a subset of multiplicity of superclass;
     * otherwise, an error shall be reported.
     */
    @Test
    fun automotiveExample() = testSession {
        loadSysMD("""
            package ExampleDesign { 
                import ScalarValues;  
                class Chassis;
                class Axis;
                class Engine;
                class Wheels; 
            }
                
            ExampleDesign::Chassis hasA
                feature wheels: ExampleDesign::Wheels[2..6] ; 
                feature numAxis: Integer = wheels::multiplicity/2.
                                
            package ExampleDesignExtension {
                import ExampleDesign; 
                class SportsChassis isA Chassis;
                class TruckChassis isA Chassis;
                class FaultyChassis isA Chassis;
            }
            
            // Feature classes
            ExampleDesignExtension::SportsChassis hasA 
                feature wheels: ExampleDesign::Wheels [2..4].
            ExampleDesignExtension::TruckChassis hasA 
                feature wheels: ExampleDesign::Wheels [6..6].
            ExampleDesignExtension::FaultyChassis hasA 
                feature wheels: ExampleDesign::Wheels [1..3].
            """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.first() is SysMDInconsistency)
    }
}