---
title: Allocations
name:  Allocations
---
# Contents
[toc]

In the OpenBoardnet Framework, allocations define mapping relationships between system elements. 
This file serves as the central integration layer that bridges the Logical and Technical architectures. 
It ensures that behaviors, resources, and constraints are properly assigned by mapping logical features to technical hardware 
and verifying that system requirements are satisfied by the combined architecture.
```SysML::OpenBoardnet::Allocations
    private import LogicalArchitecture::*;
    private import Requirements::*;
    private import LocationsAndSpaces::*;
    private import Software::*;
    private import Sensors::*;
    private import CU::*;
    
    part zonalLogic : ZonalArchitecture;                  
    part domainLogic : DomainArchitecture;
    part req_Speed : SpeedAccuracyRequirement;
    part req_Brake : EmergencyBrakeAssistRequirement;

    // Zonal Software Instances
    part zonal_acc_sw : Software::SpeedControlSW;
    part zonal_obstacle_sw : Software::ObstacleAvoidanceSW;
    part zonal_laneKeep_sw : Software::NeuralNetworkInferenceSW;
    part zonal_park_sw : Software::ObstacleAvoidanceSW;

    // Domain Software Instances
    part domain_acc_sw : Software::SpeedControlSW;
    part domain_obstacle_sw : Software::ObstacleAvoidanceSW;
    part domain_laneKeep_sw : Software::NeuralNetworkInferenceSW;
    part domain_park_sw : Software::ObstacleAvoidanceSW;
```
# Requirement -> Feature Allocations (satisfiedBy)
## Zonal Architecture Requirements
```SysML::OpenBoardnet::Allocations
    allocation alloc_sat_1 allocate req_Speed to zonalLogic.frontZoneFeatures.acc; 
    allocation alloc_sat_2 allocate req_Brake to zonalLogic.frontZoneFeatures.obstacleDet;
```
## Domain Architecture Requirements
```SysML::OpenBoardnet::Allocations
    allocation alloc_sat_3 allocate req_Speed to domainLogic.radarDomain.acc;
    allocation alloc_sat_4 allocate req_Brake to domainLogic.radarDomain.obstacleDet;
```
# Feature -> Software Component Allocations (implementedBy)
## Zonal Feature -> Zonal Software
```SysML::OpenBoardnet::Allocations
    allocation alloc_zonal_impl_1 allocate zonalLogic.frontZoneFeatures.acc to zonal_acc_sw;
    allocation alloc_zonal_impl_2 allocate zonalLogic.frontZoneFeatures.obstacleDet to zonal_obstacle_sw;
    allocation alloc_zonal_impl_3 allocate zonalLogic.centralFeatures.laneKeep to zonal_laneKeep_sw;
    allocation alloc_zonal_impl_4 allocate zonalLogic.rearZoneFeatures.parkAssist to zonal_park_sw;
```
## Domain Feature -> Domain Software
```SysML::OpenBoardnet::Allocations
    allocation alloc_domain_impl_1 allocate domainLogic.radarDomain.acc to domain_acc_sw;
    allocation alloc_domain_impl_2 allocate domainLogic.radarDomain.obstacleDet to domain_obstacle_sw;
    allocation alloc_domain_impl_3 allocate domainLogic.visionDomain.laneKeep to domain_laneKeep_sw;
    allocation alloc_domain_impl_4 allocate domainLogic.ultrasonicDomain.parkAssist to domain_park_sw;
```
# Software Component -> Controller Allocations (executedBy)
## Zonal Software -> Zonal Controllers
```SysML::OpenBoardnet::Allocations
    allocation alloc_zonal_exec_1 allocate zonal_acc_sw to CU::zonalControllerFront;
    allocation alloc_zonal_exec_2 allocate zonal_obstacle_sw to CU::zonalControllerFront;
    allocation alloc_zonal_exec_3 allocate zonal_laneKeep_sw to CU::centralController;
    allocation alloc_zonal_exec_4 allocate zonal_park_sw to CU::zonalControllerRear;
```
## Domain Software -> Domain Controllers
```SysML::OpenBoardnet::Allocations
    allocation alloc_domain_exec_1 allocate domain_laneKeep_sw to CU::cameraController;
    allocation alloc_domain_exec_2 allocate domain_acc_sw to CU::radarAndLidarController;
    allocation alloc_domain_exec_3 allocate domain_obstacle_sw to CU::radarAndLidarController;
    allocation alloc_domain_exec_4 allocate domain_park_sw to CU::ultrasonicController;
```
# Hardware -> Location allocations
## Sensors -> Locations
```SysML::OpenBoardnet::Allocations
    allocation alloc_topViewLeft_loc allocate Sensors::topViewLeft to LocationsAndSpaces::topViewLeftLoc;
    allocation alloc_topViewRight_loc allocate Sensors::topViewRight to LocationsAndSpaces::topViewRightLoc;
    allocation alloc_topViewFront_loc allocate Sensors::topViewFront to LocationsAndSpaces::topViewFrontLoc;
    allocation alloc_topViewRear_loc allocate Sensors::topViewRear to LocationsAndSpaces::topViewRearLoc;
    allocation alloc_roofCamera_loc allocate Sensors::roofCamera to LocationsAndSpaces::roofCameraLoc;
    allocation alloc_lidar_loc allocate Sensors::lidar to LocationsAndSpaces::lidarLoc;
    allocation alloc_midRangeRadar_loc allocate Sensors::midRangeRadar to LocationsAndSpaces::midRangeRadarLoc;
    allocation alloc_ultrasonicsensorFront_loc allocate Sensors::ultrasonicsensorFront to LocationsAndSpaces::ultrasonicsensorFrontLoc;
    allocation alloc_ultrasonicsensorRear_loc allocate Sensors::ultrasonicsensorRear to LocationsAndSpaces::ultrasonicsensorRearLoc;
```
## Controllers -> Locations
### Zonal Controllers -> Locations
```SysML::OpenBoardnet::Allocations
    allocation alloc_centralController_loc allocate CU::centralController to LocationsAndSpaces::centralControllerLoc;
    allocation alloc_zonalControllerFront_loc allocate CU::zonalControllerFront to LocationsAndSpaces::zonalControllerFrontLoc;
    allocation alloc_zonalControllerRear_loc allocate CU::zonalControllerRear to LocationsAndSpaces::zonalControllerRearLoc;
```
### Domain Controllers -> Locations
```SysML::OpenBoardnet::Allocations
    allocation alloc_gateway_loc allocate CU::gateway to LocationsAndSpaces::gatewayLoc;
    allocation alloc_cameraController_loc allocate CU::cameraController to LocationsAndSpaces::cameraControllerLoc;
    allocation alloc_ultrasonicController_loc allocate CU::ultrasonicController to LocationsAndSpaces::ultrasonicControllerLoc;
    allocation alloc_radarAndLidarController_loc allocate CU::radarAndLidarController to LocationsAndSpaces::radarAndLidarControllerLoc;
```
