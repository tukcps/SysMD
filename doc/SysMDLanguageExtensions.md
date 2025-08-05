## SysMD language extensions 

SysMD has a few extensions to permit _interactive_ work.
Typically, this is done by interpreters or scripting languages, whereas SysML v2 is rather a language targeting a compiler target.
Concretely, SysMD language extensions permit working on or modifying an existing, 
compiled model that exists in memory. 

The language extensions are basically semantic triples of the form: 

```Subject Predicate Object "."```, 

where 
- ```Subject``` is an existing Element, specified by its Qualified Name, 
- ```Predicate``` is ```isA``` or ```hasA``` , 
- ```Object``` is a new Element to be added or existing element to be refined; it can also include expressions. 

### Lexical level extensions

Additional keywords: 

- ```hasA```, ```isA```

### Syntactical level 

Two production rules are in addition allowed at the top level of the SysML v2 resp. KerML syntax: 

#### Adding owned Elements to a Namespace 
```
    QualifiedName "hasA" ElementList "."
```
The ```QualifiedName``` must resolve to a Namespace ("owner").
The ```ElementList``` is a sequence of valid SysML v2 elements, separated by semicolon. 
The statement adds each SysML v2 element to the owner.  

#### Typing an Element  
```
    QualifiedName "isA" Type "."
```
The statement types an existing Classifier that is a kind of Type.
Depending on the kind of Classifier, a suitable Relationship (Specialization, FeatureTyping, etc.) is used. 

#### Examples
Example 1 shows a simple standalone use case, e.g., to interactively create a simple model: 

```
    Global hasA package Vehicles.    // The root namespace is always there ...
    Vehicles hasA class Vehicle.     // Default: Base::Anything. 
    Vehicles hasA class Car.         // Default: Base::Anything. 
    Vehicles::Car isA Vehicle.       // Changes specialization to Vehicle. 
    p::Car hasA 
        import ScalarValues::Real;   // adds import ... 
        attribute speed: Real = 10.0 . // adds attribute speed bound to 10.0  
```

Example 2 shows a use case in a notebook-like development environment (as, e.g., SysMD notebook).
Here, it allows us to distribute the contents of a larger ```package``` 
into different cells or files. 

Assume, we have a large package ```pkg``` and want to specify 
its contents in different cells of the notebook or even different files.
SysMD allows then the following: 
```
    package pkg {     // defined in a separate cell of a notebook 
     (...)          // lots of code 
    }
    
    // Other file or cell. 
    p hasA 
        // some SysML v2 statements follow that are owned by the package p
        part def Car :> Vehicle {
            attribute speed: Real
        }
```
#### Constraints and constraint propagation

_SysMLv2_ has no deciated support for modeling with ranges and uncertain values.
However, constraints can be modeled easily.

_SysMD_ has specific support for modeling uncertain values by ranges and BDD, and (WiP) probabilistic properties.
A real value is considered to be a special case of a range, where the value is  chosen as one of the values from the range.; 
constraint-propagation semantics are built-in and considered from ground up. 

```
   attribute x: Real = 0.0 .. 0.1; // or: ... = oneOf(0.0 .. 0.1); with oneOf: Range -> Real
   attribute y: RealInRange {:>> range = "0.0 .. 0.1";}  // or: y { inv bounds { y >= 0.0 and y <= 0.1} }       
```

_Note, that for the sake of interoperability, we in the next releases we will focus on the standard-compliant version and deprecate non-compliant 
syntax or semantics. Standard compliant is the use of : 

``` 
   // t.b.d. -- define function oneOf within standard semantics and syntax, e.g. 
   function oneOf: Real {
       in min: Real;
       in max: Real;
       feature r: Real {
           inv minBound { that >= min }
           inv maxBound { that <= max }
       } 
       return r 
   } 
   // use compliant functions only
   attribute x: Real = oneOf(0.0, 0.1); // Maybe also 0.0 .. 0.1 
   attribute y: Real { inv bounds {y >= 0.0 and y <= 0.1} }
```

#### Units 
SysMD includes Units as part of the value (quantity), and not as a data type. 
Units are hard-coded and not yet fully compliant with SysML v2. 

For example, in SysML v2 one writes ```[micro*m]```, in SysMD ```"[µm]"```. 
See description of the units for details. 