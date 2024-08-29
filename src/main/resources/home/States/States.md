---
name:           States
title:          Implementation of the SysML package States
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems; HOOD GmbH
version:        2.12
description:    The definition of SysML v2 States for SysMD notebook
website:        https://cps.cs.uni-kl.de
---

(below is from SysML std. 4/2014, deviations from standard:
relationship to StatePerformance omitted, )
 
```SysMD
standard library package States {
   class StateAction :> Actions::Action;  // The base type of all StateDefinitions
   feature stateActions: StateAction [0 .. *]; // The base feature for all StateUsages
}
```