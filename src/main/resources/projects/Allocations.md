---
name:           Allocations
title:          Implementation of the SysML package Allocations
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Allocations for SysMD notebook
website:        https://cps.cs.uni-kl.de
---


(source: SysMLv2, (c) OMG)

# 9.2.8 Allocations
## 9.2.8.1 Allocations Overview
This package defines the base types for allocations and related structural elements in the SysML language.

### 9.2.6.2.1 BinaryConnection
Element: AllocationDefinition

Description: ```Allocation``` is the most general class of allocations, represented as a connection between the source of the
allocation and the target. ```Allocation``` is the base type of all AllocationDefinitions.

General Types: BinaryConnection

Features
- source : Anything [0..*]  { redefines source }
- target : Anything [0..*]  { redefines target }

Constraints: None.

### 9.2.8.2.2 allocations

Element: AllocationUsage

Description:
allocations is the base feature of all ConnectionUsages.

General Types: Allocation binaryConnections

Features: None. 


Constraints: None.

### SysMD implementation 

```SysMD
standard library package Allocations {
    class Allocation :> Links::BinaryLink; 
    feature allocations: Allocation[0..*];
}
```