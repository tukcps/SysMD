---
name: SI
title: SI Units for SysMD
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version: 2.12
usage: 
website: https://cps.cs.uni-kl.de
---

# Quantities and Units

In SysMD, we allow values to be quantities that also have a unit.
As a unit system, the SI units are supported. 

Currently, only explicitly given Units are checked, and the domains are just re-defined Reals. 
Hence, currently we only re-define Real as domain-types for beauty and readability. 
In future work, Units can be added to a Quantity as a parameter to define Domains as Types with units. 

As of now, we have the basic types as proof-of-concept:

```SysMD
standard library package SI {
    datatype Quantity isA ScalarValues::Real;
    datatype Work isA Quantity;
    datatype Mass isA Quantity;
    datatype Time isA Quantity; 
    datatype Power isA Quantity;
    datatype Length isA Quantity;
    datatype Speed isA Quantity; 
    datatype Accelleration isA Quantity; 
    datatype Area isA Quantity; 
    datatype Volume isA Quantity; 
    datatype Current isA Quantity; 
    datatype Currency isA Quantity;
    datatype Temperature isA Quantity; 
    datatype Voltage isA Quantity; 
    datatype Energy isA Quantity;
    datatype Charge isA Quantity;
    datatype Frequency isA Quantity;
}
```