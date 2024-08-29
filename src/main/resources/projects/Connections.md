---
name:           Connections
title:          Implementation of the SysML package Connections
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Connections for SysMD notebook
website:        https://cps.cs.uni-kl.de
---

[toc]

# 9.2.6 Connections
## 9.2.6.1 Connections Overview
This package defines the base types for connections and related structural elements in the SysML language. 9.2.6.2 Elements
### 9.2.6.2.1 BinaryConnection
Element: ConnectionDefinition

Description: BinaryConnection is the most general class of binary links between two things within some containing structure. BinaryConnection is the base type of all ConnectionDefinitions with exactly two ends.

General Types: Connection BinaryLinkObject

Features
- source : Anything [0..*] 
- target : Anything [0..*] 

Constraints: None.

### 9.2.6.2.2 binaryConnections

Element: ConnectionUsage

Description: 
binaryConnections is the base feature of all binary ConnectionUsages. 

General Types: binaryLinkObjects connections BinaryConnection

Features
- [no name] : Anything 
- [no name] : Anything 

Constraints: None.

### 9.2.6.2.3 Connection
Element: ConnectionDefinition

Description: Connection is the most general class of links between things within some containing structure. Connection is the base type of all ConnectionDefinitions.

General Types: LinkObject Part

Features
- None.

Constraints
- None.

### 9.2.6.2.4 connections

Element: ConcernUsage

Description: 
connections is the base feature of all ConnectionUsages.

General Types: parts linkObjects

Features: 
- None.

Constraints
- None.

### 9.2.6.2.5 FlowConnection
Element: ConnectionDefinition

FlowConnectionDefinition: 
Description
FlowConnection is the class of binary connections that represent a transfer of objects or values between two occurrences. It is the base type of all FlowConnectionUsages.

General Types
- Transfer 
- BinaryConnection 
- Action

Features
- source : Occurrence [0..*] {redefines source, target} 
- target : Occurrence [0..*] {redefines target, target} 

Constraints
- None.

### 9.2.6.2.6 flowConnections
Element: 
FlowConnectionUsage

ConcernUsage

Description: 
flowConnections is the base feature of all FlowConnectionUsages. General Types
transfers binaryConnections actions FlowConnection

Features
- source : Occurrence [0..*] {redefines source} 
- target : Occurrence [0..*] {redefines target} 

Constraints
- None.

### 9.2.6.2.7 SuccessionFlowConnection
Element: FlowConnectionDefinition

ConnectionDefinition

Description: 
SuccessionFlowConnection is the subclass of flow connections that represent temporally ordered transfers. It is the base type of all SuccessionFlowConnectionUsages.

General Types: 
TransferBefore FlowConnection 

Features
- source : Occurrence [0..*] {redefines source, source} 
- target : Occurrence [0..*] {redefines target, target} 

Constraints
- None.

### 9.2.6.2.8 successionFlowConnections

Element
ConcernUsage
FlowConnectionUsage

Description
successionFlowConnections is the base feature of all SuccessionFlowConnectionUsages. General Types
transfersBefore flowConnections SuccessionFlowConnection

Features
- source : Occurrence [0..*] {redefines source} 
- target : Occurrence [0..*] {redefines target} 

Constraints
- None.

```SysMD
standard library package Connections {
    class BinaryConnection :> Links::BinaryLink; 
    feature binaryConnections: BinaryConnection[0..*];
    class Connection :> Links::Link; 
    feature connections: Connection[0..*];
}
```