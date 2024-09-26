---
title:        SysMD Example - Amplifier
name:         Example-Amplifier
maintainer:   TU Kaiserslautern, Lehrstuhl CPS
description:  An example about the top-down design of AMS systems
logo:         Files/SysMD-Icon.png
version:      2.12
website:      none
license:      Apache 2.0
---
# Example: Amplifier with gain

Assume we are developing an amplifier. Let the system specification define three stages in a series.

![Example-Amplifier.png](Files/Example-Amplifier.png){width=500 height=200}

Furthermore:
- gain = 30 dB
- as we know, the gain is the product of all gains and hence ```productOverParts(gain)```. 

Note that we could also just write the product manually as: 
```lna::gain * stage2::gain * driver::gain```. 
```SysMD::Global
package demo {
    import ScalarValues::*; 
    import SI::*; 
    // Library instances --> SystemC classes
    part def Amplifier isA Base::Anything {
        attribute gain: Real [dB] = [0.0 .. 100.0] dB;     
    }
    // Concrete model --> SystemC instances of library classes
    part myAmplifier {
        part lna:    Amplifier {
            attribute gain: Real [dB] = oneOf(15.0 .. 20.0 [dB]);  
        }
        part stage2: Amplifier {
            attribute gain: Real [dB] = oneOf(5.0 .. 20.0 [dB]);  
        }
        part driver: Amplifier {
            attribute gain: Real [dB] = oneOf(5.0 .. 20.0 [dB]);  
        }
        attribute gain: Real(20 .. 30) [dB] = productOverParts(gain); 
    }
    // TODO: Test specification
}
```
# SystemC Roundtrip
## Generation of SystemC templates
Click on a part or class  generates 

- respective SystemC code frameworks for each element.
- CMake makefiles, and 
- for specified performance properties for each a test and infrastructures for coverage monitoring. 

## Feedback from characterization

Characterization from SystemC sets the (uncertain) values of the initial performance budgets to values
that are concrete, i.e., confidence intervals for three sigma (or more). 

In the example, play with the above values and check its impact on other blocks!*

