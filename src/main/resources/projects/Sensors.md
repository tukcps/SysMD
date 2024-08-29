# Sensors Basic Package

Key properties of sensors and sensor architecture are: 

- Overall architecture central or distributed? Zones? On/Offboard? 

Associated with the architecture are: 

- Limited bandwidth
- Latency
- Limited power
- Accuracy


```
Sensors isA Package. 

Sensors defines 
    SensorNode isA Component. 

Sensors::SensorNode hasA 
    sensedQuantity: Quantity,
    transmitDataRate: Datarate = latency * power * T,
    latency: Time, 
    compressor: HwSwSystemForCompression, 
    powerConsumption: Power.

channel hasA 
    latency

```
