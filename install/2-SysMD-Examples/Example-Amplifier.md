---
subtitle: SysMD Example - Amplifier
author:   University of Kaiserslautern-Landau, Chair of Cyber-Physical Systems
---

# Requirements of Operational Amplifiers

## Slew Rate

- The slew rate specifies the rate with which the output signal can change in [V/mS]
   under the input of a unit step.

- It is verified by a unit step and computing the time needed to go from -90% to +90% of the output range


```SysML
package OpAmpExample {
    part def OpAmp {
        attribute slewRate: Quantities::ScalarQuantityValue {:>> unit = "V/ms";}
    } 
    requirement def SlewRateRequirement {
        subject dut: OpAmp;
        attribute minSlewRate: Quantities::ScalarQuantityValue {:>> unit = "V/ms";}
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
        attribute redefines minSlewRate: Quantities::ScalarQuantityValue = 10.0 [V/ms]; 
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
    private import ISQ::*; 
    // Library instances --> SystemC classes
    part def Amplifier {
        attribute gain: DimensionOneValue = [0.0 .. 100.0] dB {:>> unit = "dB";}        
    }
    // Concrete model --> SystemC instances of library classes
    part myAmplifier {
        part lna:    Amplifier {
            attribute gain: DimensionOneValue = oneOf(15.0 .. 20.0 [dB]) {:>> unit = "dB";}
        }
        part stage2: Amplifier {
            attribute gain: DimensionOneValue = oneOf(5.0 .. 20.0 [dB]) {:>> unit = "dB";}
        }
        part driver: Amplifier {
            attribute gain: DimensionOneValue = oneOf(5.0 .. 20.0 [dB]) {:>> unit = "dB";}  
        }
        attribute gain: DimensionOneValue = productOverParts(gain) {:>> unit = "dB"; :>> range = "20..30";} 
    }
    // TODO: Test specification
}

/*
package demo2 {
    import ScalarValues::*; 
    import ISQ::*; 
    // Library instances --> SystemC classes
    part def Amplifier isA Base::Anything {
        attribute gain: DimensionOneValue = [0.0 .. 100.0] dB {:>> unit = "dB";}    
    }
    // Concrete model --> SystemC instances of library classes
    part myAmplifier {
        part lna:    Amplifier {
            attribute gain: DimensionOneValue = characterizedResult(
                oneOf(15.0 .. 20.0 [dB]), "Amplifier/importResultsLna") {:>> unit = "dB";}   
        }
        part stage2: Amplifier {
            attribute gain: DimensionOneValue = characterizedResult(
                oneOf(5.0 .. 20.0 [dB]), "Amplifier/importResultsStage2") {:>> unit = "dB";}    
        }
        part driver: Amplifier {
            attribute gain: DimensionOneValue = characterizedResult(
                oneOf(0.0 .. 3.0 [dB]), "Amplifier/importResultsDriver") {:>> unit = "dB";}     
        }
        attribute gain: DimensionOneValue = productOverParts(gain)  {:>> unit = "dB"; :>> value = "20..30";}    
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

