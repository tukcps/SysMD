---
title: Actuators
name:  Actuators
---
Convert control commands into physical actions (e.g., braking, steering). They operate in conjunction with assigned ECUs and implemented software components.
```SysML::OpenBoardnet
package Actuators { 
    private import Hardware::*;
    
    part def Actuator :> Actuator_Base;    
    
    part def SteeringActuator :> Actuator {
        attribute angle: DimensionOneValue {:>> range = -450..450 [°];}
        attribute responseTime: DurationValue {:>> range = 10..500 [ms];}
    }

    part def BrakeActuator :> Actuator {
        attribute brakeTorque: MomentOfForceValue {:>> range = 0..5000 [Nm];}
        attribute responseTime: DurationValue {:>> range = 5..300 [ms];}
    }

    part def ThrottleActuator :> Actuator {
        attribute throttlePosition: DimensionOneValue {:>> range = 0.0..1.0;}
        attribute responseTime: DurationValue {:>> range = 5..300 [ms];}
    }
}
```
