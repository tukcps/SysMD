![SysMD](doc/pics/SysMD-logo.png)
# SysMD Notebook

This Gradle project contains the SysMD Notebook for SysML v2. 
The links below give a brief introduction into SysMD Notebook (Kickstart) and SysML v2. 
- [Kickstart UI](install/1-SysMD-Kickstart/SysMD-Kickstart.md)
- [Kickstart_Modeling](install/1-SysMD-Kickstart/SysMD-Modeling.md)
- [SysML v2 Tutorial](install/3-SysMLv2Tutorial/SysMLv2Tutorial.md)
- [SysML v2 Tutorial 2](install/3-SysMLv2Tutorial/ecosystem.md)
- [SysML v2 Tutorial 3](install/3-SysMLv2Tutorial/kerml.md)
- [SysML v2 Tutorial 3](install/3-SysMLv2Tutorial/sysml.md)

 
SysMD Notebook supports the creation of _executable_ requirements and specification documents in a *Notebook-like* way.
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

### via Installer

1. Make shure you have at least Java 21 installed on your Computer.
2. Download the Installer of the SysMD Notebook from the "Releases" page in GitHub.
3. Run the Installer  and use the SysMD Notebook.

### via Build System

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

# Supported and unsupported parts of KerML and SysML v2

SysMD Notebook is a work in progress and does not (yet) support the full range of KerML and SysML v2.
However, a significant subset is supported with a focus on the intended use case.
The following gives some indications on what is supported: 
 
- _Supported_: modeling of packages, parts, ports, attributes, calculations, expressions, interfaces, requirements and constraints; both usages and definitions.
- _Not supported_: modeling of automata, states, time slices, user-defined keywords, etc. 

Note that automata and states might compile, but the constraint propagation mechanism does not use the respective parts properly. 
Also, KerML is implemented with support for features, classes, packages, expressions, etc. -- but with some restrictions for expressions.  


# Release notes (only major versions)

- SysMD 4.0 implements parts of the platform-specific REST API of SysML v2 (projects endpoint, but not versioning). For this purpose, SysMD 4.0 includes Spring Boot for the REST API.

# Acknowledgements 
SysMD was developed and is maintained by
- University of Kaiserslautern-Landau
- HOOD Group

The work was partially supported by EC and German BMBF within the projects 
- Arrowhead Tools (EC & BMBF)
- GENIAL! (BMBF)
- KI4BoardNet (BMBF)
