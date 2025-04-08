![SysMD](doc/pics/SysMD-logo.png)
# SysMD Notebook

This Gradle project contains the SysMD Notebook. 
It supports the creation of _executable_ requirements and specification documents in a *Notebook-like* way.
With executable requirements and specification documents, we mean that

- _Document cells_ are linked with a model in a Notebook-like way with document and code cells, and
- _Model cells_ can be executed, i.e., by computing values and checking consistency.

For this purpose, SysMD Notebook implements

- a Notebook-like user interface, 
- a SysML v2 textual and KerML compiler in code cells; unfortunately, still with incompatibilities and limitations,
- a constraint solver for checking the consistency of SysML v2 models 
  and computing an over-approximation of consistent values.

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
- [List of supported units](doc/AvailableUnits.md)
- [Modeling of time and date](doc/Time.md)

## Running SysMD Notebook
To run the frontend, just use the build system Gradle: 

```
./gradlew bootRun
```
resp. on Windows systems: 

```
gradlew.bat bootRun
```

## Creating installer

To create a platform-specific installer, use the gradle target  ```sysMDPackage```.

```
./gradlew sysMDPackage
```

# Release notes (only major versions)

- SysMD 4.0 implements the platform-specific REST API of SysML v2. 
For this purpose, SysMD 4.0 includes Spring Boot for the REST API. 

# Acknowledgements 
SysMD was developed and is maintained by
- University of Kaiserslautern-Landau
- HOOD Group

The work was partially supported by EC and German BMBF within the projects 
- Arrowhead Tools (EC & BMBF)
- GENIAL! (BMBF)
- KI4BoardNet (BMBF)
