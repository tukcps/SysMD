# Contributing to SysMD

Thanks for your interest in contributing to SysMD Notebook. 

## Getting Started
- Requirements: JDK, Gradle, Kotlin in recent versions
- Maybe IntelliJ IDEA if you like to use a UI
- Clone the repo and build from the sysmd directory:
  ```bash
  ./gradlew build
  ```
  
- Start SysMD Notebook
  ```bash 
  ./gradlew bootRun
  ```
  
## Overall architecture of project 

### File system 
The project is structured in the following directories 
- doc : complementary documentation to SysMD. 
- gradle : in particular the version catalog `libs.versions.toml`.
- install : files that will be copied by SysMD Notebook into its start folder when started.
- src : the source code 
- build.gradle.kts (main build file for gradle)
- settings.gradle.kts 

### Source packages 

The project has two folders, main and test. 

#### Main packages
- compiler (KerML, SysML parser)
- configuration (for Spring Boot, REST)
- cspsolver (CSP solver)
- exceptions
- exports (export filter)
- imports (import filter)
- model (KerML and SysML abstract model classes)
- quantities (libraries for computing with vectors of quatities)
- rest (Rest API)
- services (to be moved into model ... )
- ui 

#### Test packages 

We use JUnit Jupyter for testing. 
Test packages should follow the hierarchy as in Main. 

## Making a contribution 

The main project is hosted at the gitlab of the CPS chair. 
To make a contribution, send a diff to 

cgrimm|herzog@

