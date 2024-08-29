## SysMD vs. SysML v2

### Lexical level
| Feature                   | SysMD                | SysML v2 textual      |
|---------------------------|----------------------|-----------------------|
| Package                   | owner hasA Package x | package x             |
| Comment                   | via MD               | doc /* ... */         |
| Lexical comment           | //                   | //                    |
| Component definition      | name isA superclass  | part def name         |
| Comp. usage               | name hasA Part ...   | part name {...}       |
| Property                  | name hasA Value ...  | attribute def (value) |
| Qualified names           | ::, .(tbd)           | ::, .                 |
| Import from other package | name imports ...     | import ...            |
| Alias                     | ./.                  | alias                 |
| Real                      | Real                 | Real                  |
| Integer                   | Integer              | Integer               |
| String                    | String               | String                |
| Inheritance               | isA                  | : >, specialization   |
| Decomposition             | hasA                 | part / part def       |
| Multiplicity              | [Range] Type         | Type [Range]          |

### Syntactical level 
Syntactical differences: 

- SysMD uses explicit scope via subject and sequences following it, separated by comma or semicolon. 
- SysML v2 textual encloses elements in { }

Example 1: 

```
    p isA Package. 
    p defines Car isA Vehicle. 
    p::Car hasA speed: Real. 
```
Example 2: 

```
    package p {
        part def Car :> Vehicle {
            attribute speed: Real
        }
    }
```
### Semantic level 

#### Specialization
- Specialization used in a similar way to model variants
- SysML v2: Multiple inheritance, SysMD: _NO_ Multiple inheritance (yet -- do we want?)  
    - SysMLv2: inheritance of properties, features, but: relationships?, Liskov principle (?)
    - Appel: Liskov principle, strict refinement 

- Overloading supported by SysMD and SysML
    - SysMD: just by using the same name
    - SysMLv2: can overload a name with a new name or same name with refined constraint. 
      `part redefines cyl[4]` or short: `part :>> cyl[4]` etc. 
    

#### Decomposition
- Similar for simple occurrences, both support uncertain number of parts (multiplicity) 
- SysMLv2 has additional mechanism for subsetting: 

```
    part parts: VehicleParts[*]
    part wheels: Wheel[4] :> parts 
```
#### Structures
- SysMD does not (yet) have built-in support for structure, but can define it via constraint propagation mechanism and meta-model.
- SysMLv2 has association between blocks and its parts. 
```
    connext x to y::z 
```
furthermore, there are port definitions that make properties available from outside: 
```
    port def FuelOutPort {
        value temperature : Temp; 
        ref out fuelSupply : Fuel; 
        ref in fuelRetur : Fuel; 
    }
```
#### Constraints and constraint propagation

_SysMLv2_ has no specific support for modeling with ranges and uncertain values. 
In consequence, constraints, properties, etc. are likely formulated by users on real values.
Constraint-propagation on non-linear and/or periodic dependencies is made very difficult by this.
The reason is that such semantics is not in line with the direct application of IA or other interval constraint propagation mechanisms.

_Example:_ 
In SysML, one would define a value and a constraint separately, e.g. (in pseudo-code, not SysML v2 textual):

```
    val x, y: Real;
    constraint 0.0 < x < 0.1;
    constraint y < 0.1;  
    x = sin(y) * y  
```
The above system of constraints requires some effort to set up a problem that can be 
handled by interval constraint propagation mechanisms.

_SysMD_ has specific support for modeling uncertain values by ranges, BDD, and probabilistic properties.
A real value us just a special case of a range; constraint-propagation semantics are built-in and
considered from ground up. 

```
   val x: Real(0.0 .. 0.1)
   val y: Real(-INF .. 0.1) 
   x = sin(y) * y 
```
The above system of constraints directly considers variables as ranges; hence,
the (interval) constraint propagation can be applied directly. 

Also, it is clearer to a user that restricting the domain of _y_ might be useful. 

#### Units 
- SysMLv2 definiert Units in package. 
- SysMD has Units pre-defined.