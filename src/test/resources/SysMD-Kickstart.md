---
title:    SysMD Kickstart
author:   Christoph Grimm, TU Kaiserslautern, Lehrstuhl CPS
logo:     Files/SysMD-Icon.png
version:  1.0
---
- For the installation and start of the application have a look at the PDF file. 

- It gives a detailed description of *installation* and launching the 
SysMD Notebook application. 

- This Kickstart just gives a quick introduction some of the main capabilities of SysMD Notebook.
# 1. Documents and Cells
## Create a new SysMD Document

To create a new SysMD document, we must first create a file in which it will be saved. 
For this purpose, we select the treeview “Files” and navigate to the small “+” 
(see screenshot). 

![SysMD-Screenshot-AddFile.png](Files/SysMD-Screenshot-AddFile.png)

By clicking on the small “+” we create a new SysMD Document that is, once saved, 
in a Markdown-File. After clicking on “+”, SysMD Notebook asks for a filename. 
Enter a name with the extension “.md” and confirm with “Return”. 
The new file is then, once saved, in the folder “SysMD” in the home folder.
## Add, Delete, Edit Cells of a SysMD Document
A SysMD Document (File) is structured into *cells*. 
A cell can be of the kinds

- Documentation. Documentation is written in the Markdown Markup-Language that is inbetween a 
  What-You-See-Is-What-You get Editor like Word or Excel and pure text. 
  Markdown allows us to document our model in a structured way, including a document structure 
  (Chapters, Sections, etc. ), Pictures, Tables. 
  This helps us to better explain how we came to requirements – and others to understand it as well. 
  In SysMD, Documentation is tightly interwoven and linked (via Relationships) with more formal, textual models.
  
- Textual representation of a model. 
  We can formulate models in the modeling languages SysMD or a subset of SysML v2 textual. 
  Both languages are translated into SysML’s KerML metamodel class instances and can be exchanged via the SysMLv2 API.  
## Add and Delete Cells
A new SysMD document is empty in the beginning. 
To add a cell, click on the small gray circle with a "+" that is shown. 

![SysMD-Screenshot-AddCell.png](Files/SysMD-Screenshot-AddCell.png)

It adds a document-cell, either in the start, the end or inbetween existing cells.
Now you have created your first document-cell.
Left of each document-cell, icons are shown. 
If an element is not selected, they are gray;
if an element is selected, the icons are in different colors. 
If the mouse is over an icon, an explanation is shown what action is done when clicking the item. 
To delete an element, select the trash bin, to edit the pencil, 
and to minimize the document-cell, select the “-”.
## Documentation vs. Code-Elementes. 
Markdown is _easy_ to learn, efficient, and effective.
The following text shows how to create a 3rd level heading, how to emphasize; 
just to give you an example. 

*Double-click into the cell below!*  
### 3rd level Heading -- click here! 
This is _emphasized_ 
Double click into this segment to edit it.
You want to learn more on Markdown? 

[[Learn more about markdown by clicking this link!]](https://www.markdownguide.org/getting-started/)
# 2. The Language SysMD
## 2.1 Dependencies of values

One of the main features of SysMD is that it propagates and checks the 
consistency of values and units.
Below some simple examples! 
#### Example 1: Real values and its dependencies
Below an example for SysMD code. 
Note, that all values are constraint to some ranges in different units.
Also note, that there are dependencies between all the values: 
- From the height, width, length to the volume
- From the volume to the height, width, length
- Also in-between height, width, etc.

_To calculate consistent values for all of the above quantities considering the
dependency ```volume = height*width*length```, click on the calculator symbol left.
To display the values, click on the i in a circle left of the cell._
```SysMD
Document uses ScalarValues.
Document uses ISO26262.
Document uses SI.

PartWithVolume isA Component.
PartWithVolume imports SI. 
PartWithVolume hasA
    height:  Length(10 .. 100)[cm],
    width:   Length(1 .. 1.1) [m],
    length:  Length(1 .. 1.1) [m],
    volume:  Volume(1000 .. 2000) [l] = height * width * length.
```
#### Example 2: Boolean values and its dependencies

Boolean values can be instantiated via the class ```Boolean``` that is declared 
in the package ```ScalarValues```. 
This package is imported by default, so yout don't have to import it.
```SysMD
Global hasA Package BooleanExample. 
BooleanExample hasA
    a: Boolean,
    b: Boolean,
    c: Boolean(true) = a and b.
```
## 2.2 Classification with the IsA-Relationship

For modeling a domain we typically start with a taxonomy that explains the 
different classes that we find in a domain. 
For example, if we model Vehicles, we can classify different kinds of vehicles. 
SysMD taxonomies are trees (with a node as root, internal nodes, and leaves as 
usual in computer science). 
In SysMD, we hence always have a single root: the node “Any(thing)”.  
Classes of the domains are then broken down to the most specialized classes that are 
the leaves of the taxonomy:

- The root node (Any) is the most general class
- Towards the leaves, classes become more and more specific.

To model the taxonomy, we use the relationship “is a”. 
In SysMD this is done by using the Keyword “isA”.  
Hence, we can model  a taxonomy for vehicles in SysMD as follows:
```SysMD
Vehicle isA Any.  
Car isA Vehicle.
Bicycle isA Vehicle.  
VW isA Car.  
BMW isA Car.
```
This is nice, but with a growing number of classes and domains we will lose the overview.
We need to organize our class definitions hierarchically. 
For this purpose we use a ```Package```. 
A package separates its contents from the rest; 
definitions are directly visible only inside a package. 
Or, we can specify the “path” towards a definition, 
where we separate the names by two double points (“::”); we will see examples later.
Note: for the syntax of paths and the visibility of names (“name resolution”), 
SysMD uses equal definitions as SysMLv2.  
Syntactically the use of a package and the definitions are shown below. 
Note, that the definition-statements that belong to a common package are separated 
by semicolons. The enumeration of statements ends with a dot:
```SysMD
Global hasA Package Vehicles.
Vehicles defines
    Vehicle isA Any;
    Car isA Vehicle;
    Bicycle isA Vehicle;  
    VW isA Car;
    BMW isA Car.
```
## 2.3 Features and ownership by the hasA-Relationship

Once we have a taxonomy with different classes, we describe what formally 
differentiates the classes. For this purpose, we describe features. 
In SysMD, we use for this purpose the  “hasA” relationship.
Assume, we want to describe features of our Vehicle classes. 
We then create a package of vehicle parts and add e.g. an engine and wheels to it. 
Then, we can explain what differentiates a bicycle from a car. 
Reminder: we can access the elements of this package from the package of 
vehicles via its path as shown in the example below.
```SysMD
Global hasA Package CarParts.
CarParts defines
    Body isA Component;
    Engine isA Component;  
    Wheel isA Component.

CarParts::Engine hasA mass: Real(200.0) [kg].
CarParts::Wheel  hasA mass: Real(50.0) [kg].
CarParts::Body   hasA mass: Real(100.0) [kg].

Vehicles::Car hasA
    Component body:   CarParts::Body,
    Component wheels: [4 .. 4] CarParts::Wheel,
    Component engine: [1 .. 2] CarParts::Engine,
    Value mass:       SI::Mass (100 .. 2000) [kg].
```
As can be seen, we can now make statements about different features of different 
kinds of vehicles by listing them as a feature using the hasA relationship. 
Generally, each feature is listed with:

- Name (i.e. ```wheels```), followed by a double point,
- Amount (multiplicity), that is a range of Integers like (i.e. [4..4]),
- Class, that is what class describes the feature (i.e. “Autoteile::Wheels”).

Besides a decomposition into further elements, we can also model some numerical or 
Boolean properties. They may also have a physical unit. Then, we specify:

- Name (i.e.. mass)
- The specified type and range (i.e.. Real (100 .. 100)
- The required unit  (i.e.. [kg])
#### Units

Even more, the concrete values of a property or the multiplicity of a feature 
can be constrained by dependencies; then, SysMD notebook computes the feasible 
values by considering all constraints.
SysMD supports 

- all SI units, 
- derived units, 
- many national unit systems, 
- per-cent notations, 
- logarithmic units (Dezibel).
- units for digital information (e.g. KiB, MB)
- add prefixes for units

Units are converted automatically before computations are done, and consistency of units in equations is checked: the unit left of a dependency, and the unit right of it must be convertible into each other.
```SysMD
SIUnitsExample isA Package.
SIUnitsExample imports SI. 
SIUnitsExample hasA
     t: Length [s]            = 1.0 [s],
     v: Speed [m/s]           = 3.0 [m/s],
     g: Accelleration [m/s^2] = 4.0 [m/s^2],
     s: Speed [m/s]           = sqrt(sqr(v)+sqr(g)*sqr(t)).
```
For data and time the ISO format is supported.
We can add and subtract times in this format.
```SysMD
DateTime isA Package.

DateTime hasA
    date: SI::Time [DateTime] = DateTime("2021-10-10T03:00:00"),
    time: SI::Time [a] = 1.0 a,
    dateResult: SI::Time [DateTime] = date + time.
```
## 2.4 Types and Functions in Expressions
SysMD supports the following types: 

- Real
- Integer
- Boolean
- String

Note, that SysMD always computes with sets or ranges. 
Hence, a Real number is represented and treated as a range from a lower to an upper bound. 

In SysMD expressions, the following functions can be used: 

- ceil(x) - rounds a Real x to next higher Integer.  
- floor(x) - rounds a Real x to next lower Integer.
- exp(x) - exponential function of a Real x.
- log(x) - natural logarithm of x
- power2(x) – computes 2 to the power of x
- powerb(base, x) – base to the power of x
- sqr(x) - square of x
- sqrt(x) - square root of x
- linear(a, b, c, d, …) – linear interpolation through pairs of values specifying (x, y).
- ITE(condition, if, else) – ITE function; if Bedingung then if-value, else then-value
- sum_i(...) Iteration over i – Not for IRIS.  
- not(x)
- a and b
- a or b

*Additional functions are available that permit to compute over collections of values.*
#### Functions over collections of values
Let’s extend our library of car parts and see what the solver in SysMD can do. 
Assume, we are interested in the overall mass of different configurations of features 
and which combinations satisfy some constraints.
Often, properties of a system are defined by its parts. 
A simple example is the mass. 
In the example, we use the function ```sumOverParts(mass)``` for this purpose; 
it models that 
_the mass of a Vehicle is the mass of all of its parts._
```SysMD
Vehicles::Car hasA
    Component body:   CarParts::Body,
    Component wheels: [4 .. 4] CarParts::Wheel,
    Component engine: [1 .. 2] CarParts::Engine,
    mass: SI::Mass [kg] = sumOverParts(mass).
```
The function ```sumOverParts``` computes the sum of a property given as a parameter over all features, 
considering its multiplicity. If no property is given, it continues in the features 
of a feature, and so on (transitivity). If this should not be done, the function _sumOverPartsNotTransitive_ can be used. This function only looks in direct subclasses.

Instead of the sum the product can be also calulcated with the function _productOverParts_. 
#### Functions over Subclasses / bySubclasses-Function via isA Relationship

For some classes its properties might be clear; 
e.g. for bicycles we can guess its mass, or for cars as well. 
But it can be difficult the more abstract we are in the taxonomy. 
What is the possible mass of arbitrary vehicles? 
SysMD can help by the function bySubclasses(name).
In the Vehicle library, we can hence find out the mass of all kind of vehicles as follows:
```SysMD
Vehicles::Bicycle hasA mass: SI::Mass(10 .. 20) [kg].
Vehicles::Vehicle hasA mass: SI::Mass [kg] = bySubclasses(mass).
```
However, note that this does not give any constraints or specifications; 
it just computes the possible mass via subclasses. 
And this intention is shown in the textual model by having no specific constraint besides the fact that it is some Real-valued quantity.

For Inheritance there is also the posibility to use again the aggregation functions like _productOverSubclasses_ and _sumOverSubclasses_ as above.
An example for this functions, which uses an expression instead of an variable for calculation, can
be seen in the following example:
```SysMD::Global
Vehicles::Car hasA allCarsAvaillable: Real(0..100) [%] = 1.0 -  productOverSubclasses(1.0 - notAvailable).
Vehicles::BMW hasA notAvailable: Real (0..100) [%] = 3.0 [%].
Vehicles::VW hasA notAvailable: Real (0..100) [%] = 1.5 [%].
```
## 2.5 Inheritance

As of now, we skipped one important thing: inheritance. 
In taxonomies, SysMD applies inheritance to reduce the modeling effort, 
and the Liskoy principle to have sound semantics when reasoning about possible configurations. 
What does the Liskov-Principle mean? That's simple:

_“A specialization can always replace its superclass”._ 

What is its impact? 
If we ask for a vehicle, all of its subclasses 
- i.e. bicycle, car, and subclasses thereoff - 
- would satisfy our modeled needs. 
In other words, if we specify that we want a class, all of its subclasses are 
possible variants. In our example, variants or possible solutions would be:

- Bicycle
- Car, including its specializations 
  - VW 
  - BMW

Of course, this is only possible if the Liskov principle holds. 
To support this, the semantics of inheritance of SysMD strictly follow this principle 
and when we define a subclass, it inherits everything from the superclass such that it is fulfilled.
Note, that this does not mean that subclasses must be similar to its superclasses. 
Subclasses can have additional features, but not limitations. 
This is checked by the SysMD solver.
Obviously, a VW and a BMW can differ from a generic “Car”. 
But only in a way such that the Liskov principle holds. 
Assume, we model the power of Cars and its subclasses as follows:
```SysMD
Vehicles::Car hasA power: SI::Power(10 .. 1000) [kW].
Vehicles::VW  hasA power: SI::Power(20 .. 100) [kW].
Vehicles::BMW hasA power: SI::Power(150 .. 400) [kW].
```
These specifications are consistent with the Liskov principle: 
If one wants a car with a power from 10 to 1000 kW, 
a VW or BMW will satisfy this constraint. However, if we change the specification 
of VW to a power of max. 1100 kW, this is in contradiction to line 1 which says 
that all vehicles have a power in the range of 10 to 100 kW. 
Then, SysMD notebook displays an error!
# 3. Hybrid Spaces: SysMD, SysMLv2 and more

In SysMD notebook, we can use the languages
Markdown for documentation and elicitation of requirements,
SysMD for moving modeling requirements and checking its consistency,
SysML v2 Textual subsed for modeling requirements in a more formal way than in SysMD, addressing the stakeholders that are more familiar with systems engineering.
Other languages and interfaces are planned.
The language can be selected in the pulldown menu of each element.
```SysML
package SysMLv2 {
    //*123*/
    part def Vehicle {
    part def eng : Component;
        value weight: Real = 5.0 + 3.0;
        part def wheels : Component;
    }
    part def car : Vehicle;   
}
```
# 4. AGILA

SysMD Notebook can be used as a frontend for the backend AGILA.
AGILA implements - currently only partially - the REST API 
of the SysMLv2 REST API as drafted in the current version 
of the upcoming OMG standard.
