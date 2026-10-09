---
title: Sensors
name: Sensors
---
# Contents
[toc]

Capture physical quantities (e.g. speed, temperature) as input data for control systems. Examples include radar sensors or cameras, 
which are linked to software components like SpeedMeasureSW. 
```SysML::OpenBoardnet::Sensors
private import Hardware::*;      

part def Sensor :> Sensor_Base {
    attribute measuredQuantityType: String;
    attribute dataLoad: StorageCapacityValue {:>> range = 0..100 [kB];} 
}

part def Camera :> Sensor;
part def Radar :> Sensor;
part def Ultrasonicsensor :> Sensor;
```
# Sensor definitions
## Radar and Lidar Sensors
```SysML::OpenBoardnet::Sensors
part midRangeRadar: Radar;
part longRangeRadar: Radar;
part lidar: Sensor;
```
## Ultrasonic Sensors
```SysML::OpenBoardnet::Sensors
part ultrasonicsensorFront: Ultrasonicsensor;
part ultrasonicsensorRear: Ultrasonicsensor;
```
## Camera Systems
```SysML::OpenBoardnet::Sensors
part topViewLeft: Camera;
part topViewRight: Camera;
part topViewFront: Camera;
part topViewRear: Camera;
    
part frontCamera: Camera;
    
part roofCamera: Camera {
    attribute hasInternalDataProcessing: Boolean; 
}
```
## Additional Sensors
```SysML::OpenBoardnet::Sensors
part wheelSpeedSensor: Sensor {
    out attribute wheelSpeed: SpeedValue {:>> range = -20..200 [km/h];}
}
```
