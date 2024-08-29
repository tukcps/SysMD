---
name:           Items
title:          Implementation of the SysML package Items
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Items for SysMD notebook
website:        https://cps.cs.uni-kl.de
usage:          Occurrences
---

# Items

```SysMD
    standard library package Items {
        class Item :> Occurrences::Occurrence; // From KerML 
        feature items: Item [0 .. *];
    }
```