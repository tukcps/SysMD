---
name:       MissionProfiles
title:      A Model of Mission Profiles in SysMD
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:    2.12
usage:      ScalarValues, SI
website:    https://cps.cs.uni-kl.de/SysMD
---
```
## MissionProfiles
MissionProfiles is a small package just to explore how we can model mission profiles.
```
package MissionProfiles {

MissionProfiles defines
    class MissionProfile isA Anything;
    class FunctionalLoad isA Anything;
    class EnvironmentalLoad isA Anything;
}

MissionProfiles::MissionProfile hasA
    functionalLoad:     [0 .. 10000] MissionProfiles::FunctionalLoad; 
    environmentalLoad:  [0 .. 1000] MissionProfiles::EnvironmentalLoad.

MissionProfiles::FunctionalLoad hasA
    load: String; 
    number: Integer(0 .. 1000000).

MissionProfiles::EnvironmentalLoad hasA
    load: String; 
    number: Integer(0 .. 1000000); 
    temperature: Real [K]; 
    duration: Real [h].
