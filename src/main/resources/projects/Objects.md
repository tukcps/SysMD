---
name: Objects
title: KerML Semantic Library - Objects
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version: 2.12
website: https://cps.cs.uni-kl.de
---

[toc]

(from: KerML, OMG Standard)

# 8.5 Objects

## 8.5.1 Objects Overview
Objects are Occurrences that take up a single region of time and space, even though they might be in multiple places
over time. Object is the most general Structure, while objects is the most general Feature typed by Structures (see
7.4.3 and compare to Performances in 8.6.1). Objects and Performances do not overlap, but Performances can
Involve Objects, which can Perform Performances (see 8.6.1).
LinkObjects are Objects that are also Links, and linkObjects is the most general Feature typed by LinkObject.
LinkObjects occupy time and space, like other Objects, with potentially varying relationships to other things over
time, except for which things are its participants (the things being linked), identified by
its associationEnd Features (the "ends" of a link are permanent, though participants can be Occurrences
with changing relationships to other things). The values of LinkObject Features that are not
associationEnds can change over time. LinkObjects can exist between the same Occurrences for only some of
the time those Occurrences exist, reflecting changing relationships of those Occurrences. BinaryLinkObjects are
BinaryLinks that are also LinkObjects, and binaryLinkObjects is the most general Feature typed by
BinaryLinkObject.

## 8.5.2 Elements

### 8.5.2.1 binaryLinkObjects <Feature>
Description
General Types
linkObjects
BinaryLinkObject
binaryLinks
Features
[no name] : Anything
[no name] : Anything
Constraints
No constraints.

### 8.5.2.2 BinaryLinkObject <AssociationStructure>
Description
General Types
LinkObject
BinaryLink
Features
source : Anything [0..*]
target : Anything [0..*]
Constraints
No constraints.

### 8.5.2.3 LinkObject (AssociationStructure) 
**Description**
LinkObject is the most general AssociationStructure (M1 instance of M2 AssociationStructure). All other
AssociationStructures (in libraries or user models) specialize it (directly or indirectly).

- General Types: Object Link
- Features  No attributes.
- Constraints No constraints.

### 8.5.2.4 linkObjects (Feature)

**Description**
linkObjects is a specialization of links and objects restricted to type LinkObject. 
It is the most general feature typed by LinkObject. 
All other Features typed by LinkObject or its specializations (in libraries or user models)
specialize it (directly or indirectly).

- General Types LinkObject links objects
- Features No attributes.
- Constraints No constraints.

### 8.5.2.5 Object (Structure)

**Description**
An Object is an Occurrence that is not a Performance. 
It is most general Structure (M1 instance of M2 Structure).
All other Structures (in libraries or user models) specialize it (directly or indirectly).

- General Types Occurrence
- Features
    - enactedPerformance : Performance [0..*] {subsets suboccurrences}
Performances that are enacted by this object.
    - involvedIn : Performance [0..*]
Performances in which this Object is involved.
- Constraints No constraints.

### 8.5.2.6 objects (Feature)
**Description**
objects is a specialization of occurrences restricted to type Object. It is the most general feature typed by
Object. All other Features typed by Object or its specializations (in libraries or user models) specialize it (directly or
indirectly).

- General Types occurrences Object
- Features No attributes.
- Constraints No constraints

```SysMD
standard library package Objects {
    class Object :> Occurrences::Occurrence {
        // enactedPerformance: Performance [0 .. *]; 
        // involvedIn: Performance [0 .. *];
        ;  
    }; 
    feature objects: Object [0 .. *]; 
}
```