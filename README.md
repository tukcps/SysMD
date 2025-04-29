![SysMD](doc/pics/SysMD-logo.png)
# SysMD Notebook

This is SysMD Notebook for SysML v2. 

SysMD Notebook supports the creation of requirements and specification documents and models,
where

- _Document cells_ are linked with a model in a Notebook-like way with document and code cells, and
- _Model cells_ can be executed, i.e., by computing values and checking consistency.
- Documents and models can be exchanged as Markdown documents via, e.g., Email. This allows the inclusion of many stakeholders that are not expert in systems engineering.

Furthermore, SysMD Notebook's requirements are _executable_. 
With executable requirements and specification documents, we mean that 
an integrated constraint solver checks the consistency of SysML v2 models and computes missing values.
Excel in early system analysis does a similar job; 
SysMD does it a bit more sophisticated and integrated with SysML v2.
SysMD Notebook's UI looks as follows:

![SysMD](doc/pics/SysMD-Screenshot.png)

The links below give a brief introduction into SysMD Notebook (Kickstart) and SysML v2.
Note that these Markdown-Documents with its integrated SysML v2 and KerML models can be edited
(and computed!) with SysMD Notebook:
- [Kickstart for the SysMD Notebook UI](install/1-SysMD-Kickstart/SysMD-Kickstart.md)
- [Kickstart_for modeling and computing with SysMD Notebook](install/1-SysMD-Kickstart/SysMD-Modeling.md)
- [SysML v2 Tutorial, Introduction](install/3-SysMLv2Tutorial/SysMLv2Tutorial.md)
- [SysML v2 Tutorial, Ecosystem](install/3-SysMLv2Tutorial/ecosystem.md)
- [SysML v2 Tutorial, KerML](install/3-SysMLv2Tutorial/kerml.md)
- [SysML v2 Tutorial, SysML v2](install/3-SysMLv2Tutorial/sysml.md)

> These Markdown files are also available after starting SysMD notebook as Projects. 
> Then, one can see how the solver computes and constrains values in the rendered documents. 

The compiler translates model cells into the SysMLv2 KerML metamodel. 
On this metamodel, the constraint solver checks the consistency of

- values and
- units

and returns an over-approximation of values that satisfy all constraints or an empty set if no consistent values exist.

- [Scientific Papers](doc/publications/papers.md)
- [Overview of SysMD specific extensions](doc/SysMDLanguageExtensions.md)
- [List of supported units](doc/AvailableUnits.md)
- [Modeling of time and date](doc/Time.md)

Also, in the folder 'doc' some documentation is provided.

## Running SysMD Notebook

### Via binary installer

1. Make sure you have at least Java 21 installed on your Computer.
2. Download the installer of the SysMD Notebook from the 'releases' page in GitHub (https://github.com/tukcps/SysMD/releases)
3. Run the Installer and use the SysMD Notebook.

### Via Gradle

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

The work was partially supported by EC and German BMBF within the research projects 
- Arrowhead Tools (EC & BMBF)
- GENIAL! (BMBF)
- KI4BoardNet (BMBF)
