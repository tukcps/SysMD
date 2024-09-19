---
name: Signals
title: Signal types for export to SystemC
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version: 2.12
usage: 
website: https://cps.cs.uni-kl.de
---
The Document Signals defines different types of interactions between elements: 

- EffectChain; an effect that propagates from a cause to other elements. 
- Signal; a dedicated element to transport information from a source to a sink. 

```SysMD
package Signals {
    assoc EffectChain :> Links::Link {
        end attribute source: ScalarValues::Real;
        end attribute target: ScalarValues::Real;
        inv inoutIsEqual { source == target } 
    }
    assoc Signal :> Links::Link {
        end attribute source: ScalarValues::Real; 
        end attribute source: calarValues::Real; 
    }
}
```
