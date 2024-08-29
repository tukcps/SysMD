---
name:           Requirements
title:          Implementation of the SysML Systems Library Requirements
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Parts for SysMD notebook
website:        https://cps.cs.uni-kl.de
usage:          Items
---

(below is from SysML std. 1/2014)

# 9.2.13 Requirements
## 9.2.13.1 Requirements Overview

This package defines the base types for requirements and related behavioral elements in the SysML language.
## 9.2.13.2 Elements
### 9.2.13.2.1 ConcernCheck
Element
- ConcernDefinition

Description
- ConcernCheck is the most general class for concern checking. ConcernCheck is the base type of all
ConcernDefinitions.

General Types
- RequirementCheck

Features
- None.
Constraints
- None.

### 9.2.13.2.2 concernChecks
Element
ConcernUsage
Description
- concernChecks is the base feature of all ConcernUsages.

General Types
- ConcernCheck
- requirementChecks

Features
- None.

Constraints
- None.

### 9.2.13.2.3 DesignConstraintCheck
Element
- ConstraintDefinition

Description
- A DesignConstraint specifies a constraint on the implementation of the system or system part, such as the system
must use a commercial-off-the-shelf component.

General Types
- RequirementCheck

Features
- part : Part {redefines subj}

Constraints
- None.

### 9.2.13.2.4 FunctionalRequirementCheck
Element
ConstraintDefinition

Description
A FunctionalRequirementCheck specifies an action that a system, or part of a system, must perform.

General Types
RequirementCheck

Features
subject : Action {redefines subj}

Constraints
None.

### 9.2.13.2.5 InterfaceRequirementCheck
Element
ConstraintDefinition

Description
An InterfaceRequirement Check specifies an Interface for connecting systems and system parts, which optionally
may include item flows across the Interface and/or Interface constraints.

General Types
RequirementCheck

Features
subject : BinaryInterface {redefines subj}

Constraints
None.

### 9.2.13.2.6 PerformanceRequirementCheck
Element
ConstraintDefinition

Description
A PerformanceRequirementCheck quantitavely measures the extent to which a system, or a system part, satisfies a
required capability or condition.

General Types
RequirementCheck

Features
subject : AttributeValue {redefines subj}

Constraints
None.

### 9.2.13.2.7 PhysicalRequirementCheck
Element
ConstraintDefinition

Description
A PhysicalRequirementCheck specifies physical characteristics and/or physical constraints of the system, or a
system part.

General Types
RequirementCheck

Features
subject : Part {redefines subj}

Constraints
None.

### 9.2.13.2.8 RequirementCheck

Element
RequirementDefinition

Description
RequirementCheck is the most general class for requirements checking. RequirementCheck is the base type of all
RequirementDefinitions.

General Types
ConstraintCheck

Features
-  actors : Part [0..*]
The Parts that fill the role of actors for this RequirementCheck.
- assumptions : ConstraintCheck [0..*] {ordered}
The checks of assumptions that must hold for the required constraints to apply.
- concerns : ConcernCheck [0..*] {subsets constraints}
The checks of any concerns being addressed (as required constraints).
- constraints : ConstraintCheck [0..*] {ordered}
The checks of required constraints.
- stakeholders : Part [0..*]
The Parts that represent stakeholders interested in the requirement being checked.
- subj : Anything
The entity that is being check for satisfaction of the required constraints.

Constraints
[no name]
allTrue(assumptions) implies allTrue(constraints)

### 9.2.13.2.9 requirementChecks
Element
RequirementUsage

Description
requirementChecks is the base feature of all RequirementUsages.

General Types
- constraintChecks
- RequirementCheck

Features
- None.

Constraints
- None

```SysMD

standard library package Requirements {
    class SatisfyRequirementUsage :> Constraints::AssertConstraintUsage;
    class RequirementUsage :> Constraints::ConstraintUsage; 
    class RequirementDefinition :> Constraints::ConstraintDefinition; 
}
```