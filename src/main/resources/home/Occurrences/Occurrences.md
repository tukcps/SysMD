---
name: Occurrences
description: The SysML Occurrences package 
title: KerML Semantic Library - Occurrences
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
logo: Files/logo.png
version: 2.12
website: https://cps.cs.uni-kl.de
---

[toc]

# Occurrences
(KerML, Section 9.2.4)
>This library adds a time and space model, starting with Occurrence, the most general Class (see Clause ), which
classifies Anything that takes up time and space, and occurrences, the most general Feature typed by Classes.
Occurrences can take up the same or overlapping time and space when they represent different things happening or
existing in it. For example, the time and space taken by a room might have air moving in it, as well as light, radio
waves, and so on.
Occurrences divide into Objects and Performances (see 9.2.5.1 and 9.2.6.1, respectively), corresponding to Classes
dividing into Structures and Behaviors (see Clause and Clause , respectively). This subclause covers what is in
common between Objects and Performances.

See the KerML documentation for detailed semantics!

NOTE: Occurrence is the base class for many SysML v2 classes. 

```SysML
standard library package Occurrences { 
     
    class Occurrence :> Base::Anything; 
    
    assoc HappensLink :> Links::BinaryLink {
        end feature happensSource: Occurrence[0..*];
        end feature happensTarget: Occurrence[0..*];  
    }
    
    assoc HappensBefore :> HappensLink {
        end feature earlierOccurrence: Occurrence [1] redefines happensSource;
        end feature laterOccurrence: Occurrence [1] redefines happensTarget; 
    }
    
    assoc HappensJustBefore :> HappensBefore;
   
    
    assoc HappensDuring :> Links::BinaryLink {
    ; 
    }; 
    
    assoc OutsideOf :> Links::BinaryLink {
    ; 
    }; 
    
    assoc JustOutsideOf :> Links::BinaryLink {
    ; 
    }; 
    
    assoc InsideOf :> Links::BinaryLink {
    ; 
    }; 
}
```
