---
name:           Constraints
title:          Implementation of the SysML package Constraints
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Constraints for SysMD notebook
website:        https://cps.cs.uni-kl.de
---
Just added the necessary classes; not yet proper superclasses. 

``` 
standard library package Constraints {
    class ConstraintUsage :> Base::Anything; 
    class AssertConstraintUsage :> ConstraintUsage; 
}
```