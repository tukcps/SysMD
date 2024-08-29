---
name: Context
title: Definition of variables that model the context of a GENIAL! roadmap
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version: 2.12
usage: ScalarValues
website: https://cps.cs.uni-kl.de
---
In the package context we model classes and instances that each describe a particular context.
This includes time and other time-dependent properties.

```
package Context;
```

The concrete context in which an element is used physically after development.

- the time at which the final performances are agreed in a contract (time of procurement)
- the time at which the element is delivered and integrated (time of delivery)

```SysMD::Context
    attribute timeOfProcurement: ScalarValues::Real; 
    attribute timeOfDelivery: ScalarValues::Real.
```
