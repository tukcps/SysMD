![SysMD](doc/pics/SysMD-logo.png)
# SysMD Notebook 

_(c) University of Kaiserslautern, Chair of Cyber-Physical Systems (Prof. Ch. Grimm)_

This Gradle project contains the SysMD Notebook. 
It supports the creation of _executable_ requirements and specification documents in a *Notebook-like* way.
With executable requirements and specification documents, we mean that

- _Document cells_ are linked with a model in a Notebook-like way with document and code cells, and
- _Model cells_ can be executed, i.e., by computing values and checking consistency.

For this purpose, SysMD Notebook implements

- a Notebook-like user interface, 
- a subset of SysML v2 textual and KerML in code cells; unfortunately, still with incompatibilities and limitations,
- a constraint solver for checking the consistency of SysML v2 models and computing an over-approximation of consistent values, and

![SysMD](doc/pics/SysMD-Screenshot.png)

The compiler translates model cells into the SysMLv2 KerML metamodel. 
On this metamodel, the constraint solver checks consistency of

- values 
- units

and returns an over-approximation of values that satisfy all constraints or an empty set if no consistent values exist.


> More documentation is provided in 'SysMD Kickstart' and 'SysML v2 tutorial.' projects.
> Open them after starting SysMD notebook! 


Also, in the folder 'doc' some documentation is provided. 

- [Scientific Papers](doc/publications/papers.md)
- [Overview of SysMD specific extensions](doc/SysMDLanguageExtensions.md)
- [List of supported units](doc/Quantities/AvaillableUnits.md)
- [Modeling of time and date](doc/Quantities/Time.md)


## Running SysMD Notebook
To run the frontend, just use the build system Gradle: 

```
./gradlew run
```
resp. on Windows systems: 

```
gradlew.bat
```

## Creating executables

Use the gradle target  ```packageDistributionForCurrentOS```.

```
./gradlew packageDistributionForCurrentOS
```

Take care that the Gradle settings in IDEs like IntelliJ IDEA are set to use the Gradle 
wrapper settings.
