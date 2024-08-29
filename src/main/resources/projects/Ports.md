---
name:           Ports
title:          Implementation of the SysML package Ports
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Ports for SysMD notebook
website:        https://cps.cs.uni-kl.de
---

(below is from SysML std. 1/2014)

# 9.2.5 Ports
## 9.2.5.1 Ports Overview
This package defines the base types for ports and related structural elements in the SysML language.

### 9.2.5.2 Elements
#### 9.2.5.2.1 Port
Element: PortDefinition

**Description** 
Port is the most general class of objects that represent connection points for interacting with a Part. 
Port is the base type of all PortDefinitions.

General Types: Object

Features:
- subports: Port [0..*] {subsets timeEnclosedOccurrences} 

Constraints: None.

#### 9.2.5.2.2 ports
Element: PortUsage

**Description:**  
ports is the base feature of all PortUsages.

General Types
- Port
- objects

Features
- None.

Constraints
- None.

```SysMD
standard library package Ports { 
    class Port :> Objects::Object;            // The base class of all Ports
    feature ports: Port [1 .. *];           // The superset of all features typed by Port
}
```