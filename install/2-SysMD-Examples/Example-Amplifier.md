---
title:        SysMD Example - Amplifier
author:   TU Kaiserslautern, Lehrstuhl CPS
---

# Requirements of Operational Amplifiers

## Slew Rate

- The slew rate specifies the rate with which the output signal can change in [V/mS]
   under the input of a unit step.

- It is verified by a unit step and computing the time needed to go from -90% to +90% of the output range



```SysML
package OpAmpExample {
    part def OpAmp {
        attribute slewRate: SI::Quantity [V/ms];
    } 
    requirement def SlewRateRequirement {
        subject dut: OpAmp;
        attribute minSlewRate: SI::Quantity [V/ms];
        constraint minSlewRateConstraint { dut::slewRate > minSlewRate }   
    }
}
```
Application example: 
```SysML::OpAmpExample
    part myOpAmp: OpAmp { // the design
        attribute redefines slewRate = 5.0 [V/ms]; 
    }
     
    requirement slewRate: SlewRateRequirement {
        subject dut references myOpAmp;
        attribute redefines minSlewRate: SI::Quantity [V/ms] = 10.0 [V/ms]; 
    }
```

# Example 2: Amplifier with gain

Assume we are developing an amplifier. Let the system specification define three stages in a series.

![Example-Amplifier.png](Files/Example-Amplifier.png){width=500 height=200}

Furthermore:
- gain = 30 dB
- as we know, the gain is the product of all gains and hence ```productOverParts(gain)```. 

Note that we could also just write the product manually as: 
```lna::gain * stage2::gain * driver::gain```. 
```SysML
package AmplifierExample {
    private import ScalarValues::*; 
    private import SI::*; 
    // Library instances --> SystemC classes
    part def Amplifier {
        attribute gain: Quantity [dB] = [0.0 .. 100.0] dB;     
    }
    // Concrete model --> SystemC instances of library classes
    part myAmplifier {
        part lna:    Amplifier {
            attribute gain: Quantity [dB] = oneOf(15.0 .. 20.0 [dB]);  
        }
        part stage2: Amplifier {
            attribute gain: Quantity [dB] = oneOf(5.0 .. 20.0 [dB]);  
        }
        part driver: Amplifier {
            attribute gain: Quantity [dB] = oneOf(5.0 .. 20.0 [dB]);  
        }
        attribute gain: Quantity(20 .. 30) [dB] = productOverParts(gain); 
    }
    // TODO: Test specification
}

/*
package demo2 {
    import ScalarValues::*; 
    import SI::*; 
    // Library instances --> SystemC classes
    part def Amplifier isA Base::Anything {
        attribute gain: Quantity [dB] = [0.0 .. 100.0] dB;     
    }
    // Concrete model --> SystemC instances of library classes
    part myAmplifier {
        part lna:    Amplifier {
            attribute gain: Quantity [dB] = characterizedResult(
                oneOf(15.0 .. 20.0 [dB]), "Amplifier/importResultsLna");             ;  
        }
        part stage2: Amplifier {
            attribute gain: Quantity [dB] = characterizedResult(
                oneOf(5.0 .. 20.0 [dB]), "Amplifier/importResultsStage2");  
        }
        part driver: Amplifier {
            attribute gain: Quantity [dB] = characterizedResult(
                oneOf(0.0 .. 3.0 [dB]), "Amplifier/importResultsDriver");  
        }
        attribute gain: Quantity(20 .. 30) [dB] = productOverParts(gain); 
    }
}*/ 
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

