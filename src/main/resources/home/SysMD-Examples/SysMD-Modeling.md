---
name: Modeling Automotive Architecture
title: Modeling Automotive Architecture
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version: 2.12
usage: SI, ISO26262
website: https://cps.cs.uni-kl.de
---
# SysMD Example: Logical and Technical Architecture

The example shows a very small example for the modeling of 

- Definition of requirements, 
- Breakdown of requirements into *functions* in a logical architecture, and
- Implementation of functions by *components* in a technical architecture.

The package ISO26262 of SysMD provides (among other) the following classes and relationships: 

- Function
- Component
- implements, a Relationship from Components to Functions. 

The example uses the packages ISO26262, ScalarValues and SI.

In this simple tutorial we assume that we have the following items: 

- A requirement *enoughPower* for a vehicle that shall have a drive with enough power.
- A function *Drive* that shall satisfy this requirement.
- A component *Engine* that shall implement the function by a suitable choice between
    - An ElectricDrive, or
    - A Combustion engine. 
  
### Requirements

We model the overall system and the above-given setup. 
We begin with the definition of Vehicle, Drive, Engine and its variants and
create one instance of the System (```archExample hasA Component vehicle```) as follows: 

```SysMD::Global
attribute year: Integer = 2040; 

package archExample {
    // Definitions
    part def Vehicle :> Part;
    part def Drive :> Function; 
    part def Engine :> Part;
    part def ElectricDrive :> Engine;
    part def CombustionEngine :> Engine; 

    // Usages and requirements 
    part vehicle: Vehicle; 
    Function drive: Drive; 
    constraint enoughPower { drive::power > 100.0 kW } 
}
```

### Logical architecture

The above example does not yet show how the Function drive of the vehicle will be 
implemented, or how it looks like.
For this purpose, we must give more details of ```Drive```: 
```SysMD::archExample
Drive hasA 
    attribute power: SI::Power [kW] = byImplements(power).
```

### Technical architecture

For implementation, we give a usage of the defined parts and map it to the functions via
```implements```: 

```SysMD::Global
archExample::Vehicle hasA
    part engine: [1..2] Engine;
    attribute power: SI::Power [kW] = engine::power * Real(engine::multiplicity). 

archExample::Engine hasA
    attribute power: SI::Power [kW] = bySubclasses(power); 
    connector r = Vehicle implements Drive. 

archExample::ElectricDrive hasA
    attribute power: SI::Power[kW] = if year > 2030 ?  200.0 kW else 100.0 kW.
    
archExample::CombustionEngine hasA 
    attribute power: SI::Power(50..100) [kW]. 
```

### Dependencies and Imports

In the last section, we document dependencies and imports: 

```
import ScalarValues::*;
import ISO26262::*; 
```