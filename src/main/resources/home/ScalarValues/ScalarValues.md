---
name:           ScalarValues
title:          Implementation of the KerML Package ScalarValues
logo:           Files/logo.png
maintainer:     RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:        2.12
description:    The definition of SysML v2 Scalar Values for SysMD notebook
website:        https://cps.cs.uni-kl.de
---
# ScalarValues
Following paragraph 8.18 of SysML v2:
_This package contains a basic set of primitive scalar (non-collection) data types. 
These include Boolean and String types and a hierarchy of concrete Number types, from the most general type of 
Complex numbers to the most specific type of Positive integers._

The package ScalarValues of SysMD defines the basic primitive Number and Value types:

- Real
- Integer
- Boolean
- String

Furthermore, it provides some specific value-types:

- Requirement
- Quality

Note, that in addition we provide a Requirement as well in the GBO; then, as a
complex element that permits hierarchical decomposition of requirements.

```SysMD
standard library package ScalarValues {
    abstract datatype ScalarValue :> KerML::Kernel::DataType;
    datatype Boolean specializes ScalarValue;
    datatype String specializes ScalarValue; 
    abstract datatype Number specializes ScalarValue;
    datatype Real specializes Number;
    datatype Integer specializes Number;   
    datatype Natural specializes Integer;  // TODO: constrained to (0 .. *)  
    datatype Positive specializes Natural; // TODO: constraint to (1 .. *)
}
/*
standard library package ScalarValues { 
    private import Base::*;
    abstract datatype ScalarValue specializes Value;
    datatype Boolean specializes ScalarValue;
    datatype String specializes ScalarValue;
    abstract datatype NumericalValue specializes ScalarValue;
    abstract datatype Number specializes NumericalValue; 
    datatype Complex specializes Number;
    datatype Real specializes Complex;
    datatype Rational specializes Real;
    datatype Integer specializes Rational; 
    datatype Natural specializes Integer; 
    datatype Positive specializes Natural;
}*/
```