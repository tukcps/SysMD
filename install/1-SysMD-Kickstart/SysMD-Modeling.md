
# SysML v2 textual and SysMD extensions
## Creating a model
### Header 

t.b.d.

### Packages
It is a good practice to avoid a hierarchically flat model.
Structure a projects into different packages.
For the kickstart we put everything in the package ```kickstart```.
The package is created by the following code cell.
```SysML
package kickstart; 
```
> Note that for each cell one can choose the _language_ and the scope (_namespace_) in which it will be executed.

Run this statement and check in the tree view left under _hasA_ what has been added:
Initially, there are only the KerML standard libraries.
### Import of namespaces
Other namespaces can be imported to simplify the notations.
For example, the standard package ```ScalarValues``` introduces standard data types like:

- Real
- Boolean
- Integer

To use an element from another package like ```ScalarValues```, one has to do this by its fully
qualified name, i.e., an attribute of type Real has to be declared by ```ScalarValues::Real```.
By importing the namespace of the package ```ScalarValues``` we can access it
by its simple name as follows:
```SysML::kickstart
  private import ScalarValues::*;            // Allows us shortcuts to Real, Integer, etc. 
  private import SI::*;                      // Allows us shortcuts to use Units. 
  private import ISQ::*;                     // Allows us to use ISQ dimensions.  
  private import Ranges::*;                  // Allows us to specify constraints & co/contravariance 
  attribute r: Real = oneOf(2.0 .. 3.0);     // assigns r a value, constraine to the range 2 to 3.
  attribute i: Integer = 2;                  // assigns i the value 2.  
```
Note that with the SysMD extensions we attach the cell above to the package ```kickstart```;
hence, we work in this cell inside this package.
This pattern of extending elements allows us to deviate from the linear ordering
of adding model elements to packages.
This allows us to mix modeling and description in different cells
and permits to stepwise explain and introduce an element.

## Constraint propagation 

One of the main features of SysMD Notebook is that it propagates constraints and checks the
consistency of values and units.

### Ranges
For execution in the sense of constraint propagation, we use the profile ```Ranges```.
It provides additional means to add constraints to numbers; the constraints can be added by
- inheriting from InRange or, not in standard SysML v2, by adding _(min, max)_ after a type:
```Markdown
library package Ranges {
    // Abstract concept of a Range from which a Real is chosen
    abstract datatype InRange {
      feature range: Ranges::Range; // Models a range 
      inv { (that.min <= self) and (that <= range.max) }
    }
    abstract datatype AllInRange :> InRange; // ... 
    abstract datatype QuantityInRange :> InRange; // ... 
}
```
  These constraints are used to represent and propagate constraints, where

- _range_ bounds the range of a Number (e.g. Integer, Real) to the given range. 

- _AllInRange_ specifies that the constraint system shall be satisfied for all values in the range _min .. max_.

Below, we give some simple examples.
#### Example 1: Real values and its dependencies
Below, we give an example for SysML v2 textual code.
Note that all values are constraint to some ranges in different units.
Also note that there are dependencies between all the values:
- From the height, width, length to the volume
- From the volume to the height, width, length
- Also, in-between height, width, etc.

_To calculate consistent values for all the above quantities considering the
dependency ```volume = height*width*length```, click on the calculator symbol left.
To display the values, click on the i in a circle left of the cell._
```SysML::kickstart
part rangeExample {
    attribute height:  LengthValue = oneOf(10.0 .. 100.0 [cm]);
    attribute width:   LengthValue = oneOf(1.0 .. 1.1 [m]);
    attribute length:  LengthValue = oneOf(1.0 .. 1.1 [m]);
    attribute volume:  VolumeValue = height * width * length {
        :>> range = "1000.0 .. 2000.0";
        :>> unit =  "l";  
    }
}
```
Note that we can also use SysML v2's capabilities to create a more sophisticated _constraint_ or _requirement_ for 
the volume -- but for being brief, we just directly add some constraints to the volume in a direct way. 

_Exercise:_ Try different values, units.
#### Example 2: Boolean values and its dependencies

Boolean values can be instantiated via the class ```Boolean``` that is declared
in the package ```ScalarValues```.
This package is imported by default, so we don't have to import it.
```SysML::kickstart
    package booleanExample {
        attribute a: Boolean;
        attribute b: Boolean;
        assert { a and b }
    }
```

Again, _a_ and _b_ might be some features anywhere in a model, and _a and b_ might be part of a SysML v2 _requirement_. 

#### Example 3: Hybrid (mixed Boolean/arithmetic) dependencies

Let's mix Boolean and arithmetic dependencies.
This time, we give dependencies that are not satisfiable.
We use two arithmetic values, ```a, b``` and a Boolean condition ```c``` that shall be true.
```SysML::kickstart
    package hybridExample {
        attribute a: Real = oneOf(1.0 .. 2.0); 
        attribute b: Real = a + 0.1;
        assert { a > b }
    }
```
_Exercise:_ In place of ```>``` try the relations ```<, ==```. Instead of assert try ```constraint``` .
#### Units

Even more, the concrete values of a property or the multiplicity of a feature can be constrained by dependencies; then, SysMD notebook computes the possible values while considering all constraints.
The library SI of SysMD supports

- SI units with prefixes,
- derived units,
- many national unit systems,
- per-cent notations,
- logarithmic units (Decibel).
- units for digital information (e.g., KiB, MB)

Note that in SysML v2 standard the package with dimensions is ```ISQ```.
We will change the name in future versions.

Units are converted automatically before computations are done, and the consistency of units in equations is checked:
the unit left of a dependency, and the unit right of it must be convertible into each other.
```SysML::kickstart::units
attribute t: TimeValue         = 1.0 [s];
attribute v: SpeedValue        = 3.0 [m/s];
attribute g: AccelerationValue = 4.0 [m/s^2];
attribute s: SpeedValue        = sqrt(sqr(v)+sqr(g)*sqr(t)); 
```
Play with the units, e.g., by changing the unit after the type declaration or try ms instead of s.

For date and time, the ISO format is supported.
We can add and subtract times in this format.
```SysML::kickstart::units_datetime
attribute date: Time [DateTime] = DateTime("2021-10-10T03:00:00");
attribute time: Time = 1.0 a {:>> unit="a";}
attribute dateResult: Time [DateTime] = date + time;
```

### Vectors
There is also the possibility to use vectors instead of scalar values.
They can be used with the same operations as normal values in addition to some special
operations like the angle or cross-product.
Below is an example for defining vectors:

```SysML::kickstart::vecors
attribute a: Mass = (0.5,1.5) kg { :>> range = "0.0..1.0, 1.0..2.0"; }
attribute b: Mass = (0.5,1.5) kg; 
attribute c: Mass = (-5.0, -1.0, 3.0) kg { :>> range="-5.0..-1.0, -1.0..2.0, 2.0..4.0"; }
```

In the next example, there is a calculation with Vectors with the cross-product and angle.
```SysML::kickstart::vectorfunctions
attribute a2: Real, InRange { :>> range="1..1,5..5, 10..10";}
attribute b2: Real, InRange { :>> range="5..5,1..1, 10..10";}
attribute c2: Real  = a2 cross b2;
attribute d2: Quantity = angle(a2,b2) {:>>unit="°";} 
```

## Types and Functions in Expressions
SysMD supports the following types:

- ```ScalarValues::Real```
- ```ScalarValues::Integer```
- ```ScalarValues::Boolean```
- ```ScalarValues::String```

For Quantities with units, Domains from the SI package must be used as a type (see doc/AvailableUnits.md). If no domain is
known, ```SI::Quantity``` should be used

In expressions, the following functions can be used:

- ```ceil(x)``` - rounds a Real x to next higher Integer.
- ```floor(x)``` - rounds a Real x to next lower Integer.
- ```exp(x)``` - exponential function of a Real x.
- ```log(x)``` - natural logarithm of x
- ```power2(x)``` – computes 2 to the power of x
- ```powerb(base, x)``` – base to the power of x
- ```sqr(x)``` - square of x
- ```sqrt(x)``` - square root of x
- ```linear(a, b, c, d, …)``` – linear interpolation through pairs of values specifying (x, y).
- ```ITE(condition, if, else)``` – ITE function; if Condition then if-value, else then-value
- ```sum_i(...)``` Iteration over i – Not for IRIS.
- ```not(x)```
- ```a and b```
- ```a or b```

*Additional functions are available that permit computing over collections of values.*

#### Functions over collections of values

SysMD also has pre-defined functions that query values and calculate aggregations over the collection.
Examples are the functions

- ```sumOverParts(lambda-expr)```
- ```productOverParts(lambda-expr)```
- ```sumOverSpecific(lambda-expr)```
- ```productOverSpecific(lambda-expr)```
- ```bySpecific(expression)```

These functions take an expression as a parameter that is evaluated as a lambda expression in the scope
of parts owned by or subclasses of an element.

For example, the function ```sumOverParts``` executes the ```lambda-expr``` in
each part of the current element and returns the sum of it.
The function is transitive; it will recursively search aggregate the executions in parts of parts etc.
If this should not be done, the function _sumOverPartsNotTransitive_ can be used.
This function only looks in directly owned parts.
Instead of the sum, the product can be also calculated with the function _productOverParts_.
#### Functions over specializations

For some classes, its properties might be clear;
e.g., for bicycles we can guess its mass, or for cars as well.
But it can be challenging the more abstract we are in the taxonomy.
What is the possible mass of arbitrary vehicles?
SysMD can help by the function ```bySpecific```.
However, note that the function ```bySpecific``` does not add constraints; it just computes the
possible mass by the consistent values of its specific subtypes.
And this intention is shown in the textual model by having no specific constraint besides the fact that it is some Real-valued quantity.
## Classification and Featuring

Modeling a domain mostly uses two relationships:

- Classification. This means we give a kind of taxonomy in which parts are classified regarding common features.
- Featuring. This means we describe features and its amount that a part may have.

In the following, we give an example:a model of the domain _Vehicles_.
We first create a package that defines vehicle parts and add e.g., an engine and wheels to it.
Then, we can explain what differentiates a bicycle from a car.
Reminder: we can access the elements of this package from the package of
vehicles via its path as shown in the example below.

```SysML::kickstart
package carParts {
    part def Body {
        attribute mass: MassValue {:>> range="300.0"; :>> unit = "kg"; }
    }
    part def Engine {
        attribute mass: MassValue {:>> range="300.0"; :>> unit = "kg"; }
    }
    part def Wheel {
        attribute mass: MassValue {:>> range="50.0"; :>> unit = "kg"; }
    }
}
```

As can be seen, we can now make statements about the different kinds of vehicle's different features.
This is done by listing them as a feature using the hasA relationship.
Generally, each feature is listed with:

- Name (i.e. ```wheels```), followed by a double point,
- Amount (multiplicity), that is a range of Integers like (i.e. [4..4]),
- Class, that is what class describes the feature (i.e. “CarParts::Wheels”).
### Classification

For modeling a domain, we typically start with a taxonomy that classifies and explains the kind of things in a domain.
For example, if we model Vehicles, we can classify different kinds of vehicles.
Taxonomies are trees with a single node as root, some internal nodes, and leaves.
In SysML v2, we hence always have a single root of all types: the node “Anything.”
It is the default class if nothing else is specified.
Classes of the domains are then broken down to the most specialized classes that are
the leaves of the taxonomy:

- The root node (Anything) is the most general class
- Towards the leaves, classes become more and more specific.

To model the taxonomy, we use the relationship “specializes.”
In SysML v2 this is done by using the Keyword ```specializes``` or the shortcut ```:>```.
definitions are directly visible only inside a package.
In SysML v2 there are part definitions (```part def```) and part usages (```part```).
The definitions create a kind of class; the usages create an instance.
Part definitions and usages can feature parts and attributes.
Specializations inherit features.

```SysML::kickstart::mass_rollup
// We consider a vehicle to be anything that has at least one wheel. 
// The bySubclasses determines a consistent value for mass with min diameter. 
part def Vehicle {
  attribute mass: MassValue = bySpecializations(mass) {:>> range ="0..1000";}
  part wheels: carParts::Wheel[1 .. *];        
}

// A car is a vehicle with Body and Engine. 
// the sumOverParts determines a consistent minimal range consistent with parts.
part def Car  :> Vehicle {
   attribute redefines mass: Mass = sumOverParts(mass) {:>> range ="0 .. 1000";}
   part wheels: carParts::Wheel[4 .. 10]; 
   part body:   carParts::Body;
   part engine: carParts::Engine;
}

part def Bicycle :> Vehicle {
   attribute :>> mass: MassValue = 10.0 .. 20.0 [kg]; 
}
part def VW   :> Car;
part def BMW  :> Car;
```
Besides a decomposition into further elements, we can also model some numerical or
Boolean properties. They may also have a physical unit. Then, we specify:

- Name (i.e., mass)
- The specified type and range (i.e., Real (100 .. 100)
- The required unit  (i.e, [kg])
## User defined Calculations

There is also a possibility to define user defined functions with any number of input variables .
These functions can be defined once and used multiple times.
```SysML::kickstart::calculation
// Definition of a Calculation
calc def calcEnergy {
  in v : SpeedValue; 
  in m : MassValue; 
  return result : EnergyValue = 0.5 * m * sqr(v); 
}

// Usage of the defined calculation Energy
attribute a: SpeedValue        = 36.0 [km/h]; 
attribute b: MassValue         = 200.0 [kg]; 
attribute energy1: EnergyValue = calcEnergy(a, b); 
attribute e: SpeedValue        = 72.0 [km/h]; 
attribute f: MassValue         = 800.0 [kg]; 
attribute energy2: EnergyValue = calcEnergy(e,f); 
```

## Inheritance

As of now, we skipped one important thing: inheritance.
Inheritance allows us to reduce the modeling effort,
and the Liskov principle is the basis for sound semantics when we reason
about possible configurations.
What does the **Liskov-Principle** mean? In simple words:

>“A specialization can always replace its superclass.”

What is its impact?
If we ask for a vehicle, all of its subclasses - i.e., bicycle, car, and subclasses thereof - would satisfy our modeled needs.
In other words, if we specify that we want a class, all of its subclasses are
possible variants. In our example, variants or possible solutions would be:

- Bicycle
- Car, including its specializations
    - VW
    - BMW

Of course, this is only possible if the Liskov principle holds.
To support this, SysMD's semantics of inheritance for ranges and constraint propagation
strictly follow this principle.
When we define a specialized type, it inherits everything from the general type such that it is fulfilled.
Note that this does not mean that subclasses must be similar to its superclasses.
Subclasses can have additional features, but not limitations.
This is checked by the SysMD solver.
A VW and a BMW can differ from a generic “Car.”
But only in a way such that the Liskov principle holds.
Assume, we model the power of Cars and its subclasses as follows:

```SysML::kickstart::vehicles
part def Car { 
    attribute power: PowerValue(10..1000) [kW]; 
}
part def VW :> Car { 
    :>> power: PowerValue(20..100) [kW]; 
}
part def BMW :> Car { 
    :>> power: PowerValue(150..1100) [kW]; 
}
```

These specifications might be consistent with the Liskov principle:
If one wants a car with a power from 10 to 1000 kW,
a VW or BMW will satisfy this constraint. However, if we change the specification
of VW to a power of max. 1100 kW, this is in contradiction to line 1 which says
that all vehicles have a power in the range of 10 to 100 kW.
Then, SysMD notebook displays an error!

Note that
- features and hence, attributes, calculations or expressions are first inherited. 
  That means we create a concrete clone of it that can take values independent of its superclasses' values.
- then, in a second step, the Liskov principle is checked as above on the constraints given for the (sub)-type;
  violations are reported as errors and not "repaired" by constraint propagation.
  Note that if NO constraints are given, SysMD just uses the supertype's constraints.
- finally, the expressions are evaluated by constraint propagation.

The below example demonstrates this behavior.
```SysML::kickstart
    package inheritanceExample {
        part def Coin {
            attribute diameter: SI::Length = oneOf(5.0 ..200.0 [mm]);
            attribute circumference: SI::Length [mm] = diameter*3.141; // 15.7 .. 628.2 mm 
        }
        
        part oneEuroCoin : Coin { 
            attribute diameter: SI::Length [mm] = 23.25 mm; 
            // circumference is inherited. Must be re-evaluated with correct diameter.
            // Expected behavior:  re-evaluate dependency in new scope, but without changing diameter of Coin. 
        }
    }
```