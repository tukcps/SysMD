---
title: LogicalArchitecture
name: LogicalArchitecture
---
# Contents
[toc]

Defines the abstract functional organization of the vehicle system. It structures features and functions into logical units, such as Domains or Zones, independent of the specific hardware implementation.
# Architectures Overview
This package prepares the evaluation of exemplary boardnet architecture scenarios:
- A **Domain Architecture**: Functions are grouped by their domain (Vision, Radar, Ultrasonic).
- A **Zonal Architecture**: Functions are grouped by their physical location in the vehicle (Front, Rear, Center).

Both architectures use the same sensors but structure the logical features and subsequent technical realizations differently.
```SysML::OpenBoardnet::LogicalArchitecture
private import Features::*;
private import Functions::*;
private import Requirements::*;

abstract part def VehicleZone {
    attribute calculatedWeight: MassValue;
    attribute avgTemperature: ThermodynamicTemperatureValue;
}

part def FeatureGroup;

abstract part def ArchitectureVariant {
    // Common features (top-level)
    part cruiseControl : CruiseControlFeature;
    part eba_feature : EmergencyBrakeAssist;

    // Analysis attributes for later evaluations
    attribute totalSystemWeight: MassValue;
    attribute totalSystemCost: AmountOfMoneyValue;
}
```
# Architectures
## Domain Architecture
```SysML::OpenBoardnet::LogicalArchitecture
part def DomainArchitecture :> ArchitectureVariant {
    
    part visionDomain : FeatureGroup {
        part laneKeep : LaneKeepAssist;
    }
    
    part radarDomain : FeatureGroup {
        part acc : AdaptiveCruiseControl;
        part obstacleDet : ObstacleDetection;
    }
    
    part ultrasonicDomain : FeatureGroup {
        part parkAssist : ParkingAssist;
    }
}
```
## Zonal Architecture
```SysML::OpenBoardnet::LogicalArchitecture
part def ZonalArchitecture :> ArchitectureVariant {
    
    part frontZoneFeatures : FeatureGroup {
        // Features logically assigned to front
        part acc : AdaptiveCruiseControl;
        part obstacleDet : ObstacleDetection;
    }
    
    part rearZoneFeatures : FeatureGroup {
        // Features logically assigned to rear
        part parkAssist : ParkingAssist;
    }
    
    part centralFeatures : FeatureGroup {
        // Features that are cross-cutting
        part laneKeep : LaneKeepAssist;
    }
}
```
## Central Architecture
```SysML::OpenBoardnet::LogicalArchitecture
part def CentralArchitecture :> ArchitectureVariant {
    part centralProcessing : FeatureGroup {
        part allFeatures : FeatureGroup;
    }
}
```
