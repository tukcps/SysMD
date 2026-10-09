---
title: Features and Functions
name:  FeaturesAndFunctions
---
# Contents
[toc]

Translate requirements into specific system capabilities (features) and operationalize these through executable functional building blocks. They form the logical architecture layer between requirements and technical implementation.
# Features
```SysML::OpenBoardnet
package Features {
    private import BaseTypes::*;

    part def CruiseControlFeature :> Feature {
        attribute isActive: Boolean;
    }

    part def ObstacleDetection :> Feature {
        attribute isObjectDetected: Boolean;
    }
    
    part def EmergencyBrakeAssist :> Feature {
        attribute isBraking: Boolean;
    }
    
    part def LaneKeepAssist :> Feature {
        attribute isSteeringIntervention: Boolean;
    }

    part def AdaptiveCruiseControl :> Feature {
        attribute isFollowing: Boolean;
    }
    
    part def ParkingAssist :> Feature {
        attribute isParking: Boolean;
    }
}
```
# Functions
```SysML::OpenBoardnet
package Functions {
    private import BaseTypes::*;

    part def SpeedAcquisition :> Function {
        out attribute vehicleSpeed: SpeedValue {:>> range = -20..200 [km/h];}
    }
    part def SpeedControl :> Function {
        in attribute speedInput: SpeedValue {:>> range = -20..250 [km/h];}
    }
}
```
