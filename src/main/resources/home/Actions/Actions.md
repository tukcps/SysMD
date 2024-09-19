---
name:           Actions
title:          Implementation of the SysML package Actions
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems; HOOD GmbH 
version:        2.12
description:    The definition of SysML v2 Actions for SysMD notebook
website:        https://cps.cs.uni-kl.de
---

(below is from SysML std. 4/2014, deviation from standard: used Occurrence instead of Performance)

```SysMD
standard library package Actions {
   class Action :> Occurrences::Occurrence;      // The base type of all ActionDefinitions
   feature actions: Action [0 .. *]; // The base feature for all ActionUsages
}
```