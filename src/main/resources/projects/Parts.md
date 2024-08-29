---
name:           Parts
title:          Implementation of the SysML package Parts
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Parts for SysMD notebook
website:        https://cps.cs.uni-kl.de
usage:          Items
---

(below is from SysML std. 1/2014)

# 9.2.4 Parts
## 9.2.4.1 Parts Overview
This package defines the base types for parts and related structural elements in the SysML language.
- 9.2.4.2 Elements 
### 9.2.4.2.1 Part
Element: PartDefinition
**Description**
Part is the most general class of objects that represent all or a part of a system. 
Part is the base type of all PartDefinitions.

General Types: Item

Features: 
- exhibitedStates: StateAction [0..*] {subsets performedActions} StateActions that are exhibited by this Part.
- ownedActions: Action [0..*] {subsets ownedPerformances} Actions that are owned by this Part. 
  The reference of a ownedAction is always its owning Part.
- ownedPorts: Port [0..*] {subsets timeEnclosedOccurrences} Ports that are owned by this Part.
- ownedStates: StateAction [0..*] {subsets ownedActions} StateActions that are owned by this Part.
- performedActions: Action [0..*] {subsets enactedPerformances} Actions that are performed by this Part.

Constraints: None.

# 9.2.4.2.2 parts

Element: PartUsage

**Description**

parts is the base feature of all PartUsages.

General Types: Part, items

Features: None.

Constraints: None.


```SysMD
standard library package Parts { 
    class Part :> Items::Item;                  // Base class for all part definitions
    feature parts: Part[0 .. *];                // Features that is superset of all part usages 
    // subsets items;                           // iff we support subsetting. 
}
```