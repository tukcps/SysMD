---
name:       MissionProfiles
title:      A Model of Mission Profiles in SysMD
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:    2.12
usage:      ScalarValues, SI
website:    https://cps.cs.uni-kl.de/SysMD
---

## MissionProfiles
MissionProfiles is a small package just to explore how we can model mission profiles.

```KerML
package MissionProfiles {

    class MissionProfile;
    class FunctionalLoad;
    class EnvironmentalLoad;
    
    MissionProfile {
        functionalLoad:     [0 .. 10000] MissionProfiles::FunctionalLoad {
            load: String; 
            number: Integer(0 .. 1000000).
        }
        environmentalLoad:  [0 .. 1000] MissionProfiles::EnvironmentalLoad {}
            load: String; 
            number: Integer(0 .. 1000000); 
            temperature: Real [K]; 
            duration: Real [h]; 
        }   
    }
}
```