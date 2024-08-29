---
name: Base
title: Implementation of the KerML Package "Base"
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version: 2.12
website: https://cps.cs.uni-kl.de
---

# Base library of KerML

1. The Base library model (see 9.2.2) begins the Specialization hierarchy for all KerML Types,
including the most general Classifier ```Anything``` and the most general Feature ```things```.
It also contains the most general DataType DataValue and its corresponding Feature dataValues.
The Links library model (see 9.2.3) specializes Base to provide the semantics for Associations
between things.

```SysMD
    standard library package Base { 
        // Base::Anything is built-in
        feature things: Base::Anything;
        datatype DataValue specializes Base::Anything;
        feature dataValues: DataValue;
    }
```

The following classes or instances are built-in:

- Natural
- self (it)


```

```