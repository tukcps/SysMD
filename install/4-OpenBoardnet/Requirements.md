---
title: Requirements
name:  Requirements
---
Define mandatory system requirements as the starting point of the development process. They specify functional and non-functional conditions that must be fulfilled by features and functions.
```SysML::OpenBoardnet
package Requirements {

    // --- Cruise Control Subject & Req ---
    part def CruiseControl {
        attribute actualSpeed: SpeedValue {:>> range = -20..200 [km/h];}
        attribute targetSpeed: SpeedValue {:>> range = 0..200 [km/h];}
    }

    requirement def SpeedAccuracyRequirement {
        subject cc: CruiseControl;
        attribute maxDeviation: SpeedValue {:>> range = 0..5 [km/h];}
        constraint deviationOK {
            abs(cc::actualSpeed - cc::targetSpeed) <= maxDeviation
        }
    }

    // --- Global Vehicle Subject & Reqs ---
    abstract part def Vehicle {
        attribute totalLength: LengthValue {:>> unit = "m";}
        attribute totalWeight: MassValue {:>> unit = "kg";}
        attribute totalLengthofCable: LengthValue {:>> unit = "m";}
        attribute totalCosts: AmountOfMoneyValue; 
    }

    requirement def VehicleWeightRequirement {
        subject v: Vehicle;
        attribute maxWeight: MassValue {:>> range = 1000..3500 [kg];}
        constraint { v::totalWeight <= maxWeight }
    }
    
    requirement def VehicleLengthRequirement {
        subject v: Vehicle;
        attribute maxLength: LengthValue {:>> range = 3.0..6.0 [m];}
        constraint { v::totalLength <= maxLength }
    }
    
    requirement def VehicleCostRequirement {
        subject v: Vehicle;
        attribute maxCost: AmountOfMoneyValue {:>> range = 10000..100000 [EUR];}
        constraint { v::totalCosts <= maxCost }
    }
    
    requirement def VehicleCableLengthRequirement {
        subject v: Vehicle;
        attribute maxCableLength: LengthValue {:>> range = 100..5000 [m];}
        constraint { v::totalLengthofCable <= maxCableLength }
    }

    // --- EBA Subject & Req ---
    part def EBA_System {
        attribute minDetectionDistance: LengthValue {:>> unit = "m";}
        attribute timeToCollision: TimeValue {:>> unit = "s";}
    }
    
    requirement def EmergencyBrakeAssistRequirement {
        subject eba: EBA_System;
        attribute requiredDistance: LengthValue {:>> range = 1..200 [m];}
        constraint { eba::minDetectionDistance >= requiredDistance }
    }
}
```
