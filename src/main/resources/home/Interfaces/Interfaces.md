---
name:           Interfaces
title:          Implementation of the SysML package Interfaces
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Interfaces for SysMD notebook
website:        https://cps.cs.uni-kl.de
usages:         Connections
---

# 9.2.7 Interfaces
## 9.2.7.1 Interfaces Overview
This package defines the base types for interfaces and related structural elements in the SysML language.
## 9.2.7.2 Elements
### 9.2.7.2.1 BinaryInterface
Element 
- InterfaceDefinition

Description
- BinaryInterface is the most general class of links between two PortUsages within some containing structure.
- BinaryInterface is the base Type of all InterfaceDefinitions with exactly two ends.

General Types
- BinaryConnection
- Interface

Features
- source : Port [0..*] {redefines source}
- target : Port [0..*] {redefines target}

Constraints
- None.

### 9.2.7.2.2 binaryInterfaces
Element
- InterfaceUsage

Description
- binaryInterfaces is the base feature of all binary InterfaceUsages.

General Types
- interfaces
- BinaryInterface
- binaryConnections

Features
- [no name] : Port
- [no name] : Port

Constraints
- None.

### 9.2.7.2.3 Interface
Element
- InterfaceDefinition

Description
- Interface is the most general class of links between PortUsages within some containing structure. 
- Interface is the base Type of all InterfaceDefinitions.

General Types
- Connection

Features
- None.

Constraints
- None.

### 9.2.7.2.4 interfaces
Element
- InterfaceUsage

Description
- interfaces is the base feature of all InterfaceUsages.

General Types
- connections
- Interface

Features
- None.

Constraints
- None.

```SysMD
standard library package Interfaces {
    class Interface :> Connections::Connection; 
    feature interfaces: Interface [0 .. *]; 
}
```