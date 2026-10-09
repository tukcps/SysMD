---
title: Hardware
name:  Hardware
---
Provides the abstract classification and definitions for physical system components. 
It sums up the base types for Sensors, Network modules, Compute Units (CUs), and Actuators to establish a common taxonomy for the technical architecture.
```SysML::OpenBoardnet
package Hardware {
    
    part def Hardware_Base; 
        //attribute lambdaSPF: FrequencyValue;  // single-point failure rate
        //attribute lambdaRF:  FrequencyValue;  // residual-fault failure rate
        //attribute lambdaMPF: FrequencyValue;  // multiple-point failure rate
        //attribute lambdaS:   FrequencyValue;  // safe-fault failure rate
        //attribute totLambda: FrequencyValue;  // total failure rate
    
    
    part def Sensor_Base :> Hardware_Base;
    part def ControlUnit_Base :> Hardware_Base;
    part def NetworkModule_Base :> Hardware_Base;
    part def Actuator_Base :> Hardware_Base;
}
```
