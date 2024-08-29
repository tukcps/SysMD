![SysMD](doc/pics/SysMD-logo.png)
# SysMD Notebook 

(c) TU Kaiserslautern, Chair of Cyber-Physical Systems

This project contains the AGILA SysMD Notebook, which allows the creation of
executable requirements documents in a *Notebook-like* way.
For this purpose, it implements

- the SysMD language, 
- a quite small subset of SysMLv2 textual, 
- a constraint solver that checks the consistency of SysMD models, and
- the mentioned Notebook-like user interface.

![SysMD](doc/pics/SysMD-Screenshot.png)

The compilers translate models into the SysMLv2 KerML metamodel. 
On this metamodel, the constraint solver checks consistency of

- values 
- units

More documentation is provided in the notebooks 'SysMD Kickstart' and 'SysML v2 tutorial.'
Also, in the folder 'doc' some documentation is provided. 

- [SysMD Kickstart](doc/SysMD%20Kickstart%20(English).pdf)
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

Take care that the gradle settings in IDEs like Intellij IDEA are set to use the gradle 
wrapper settings.
