---
logo: Files/logo.png
description: Tutorial on modeling with SysML v2
subtitle: SysML v2 Ecosystem and Methodology
author: RPTU Kaiserslautern-Landau, Chair of Cyber-Physical Systems
---
[toc]

---
**Learning objectives**

In this part, you will learn about for which purpose and how to use SysML v2.
After reading this section, you will


- understand what SysML v2 is, and when to use SysML v2 and when not,
- have an overview of the development process and how SysML v2 can be applied within it,
- know about the features of SysML v2 tool ecosystems. 

---
# What is SysML? And SysML "v2?" 

SysML (“Systems Modeling Language”) is a standard for Model-Based Systems Engineering.
The standard is developed and provided by the Object Management Group (OMG). 
SysML allows us to model and exchange
- Requirements
- Specification
- Verification and Use cases
- Test cases

SysML provides a rather *general language* for arbitrary domains, including software, 
digital hardware, mechanical components, and many other domains. 
SysML is hence suitable for modeling systems that combine multiple domains.

Often, different domains have established domain-specific solutions. 
These are usually highly optimized for e.g. microelectronic systems.
Then, SysML can nicely be used for crossing different domains, by 
linking them with system requirements (e.g. safety of an automobile), 
and associated use-, analysis, and verification cases.  

![SysMLv2](Files/SysMLv2.png){width=1000 height=400}

SysML **v2** is the latest version of SysML.
However, version 2 is not a simple “update” of v1.X.
Compared with a previous version of UML and SysML, it is mostly entirely new, but not entirely different. 
The SysML v2 standard includes

- KerML, a new metamodel that is, unlike in earlier versions, not based on UML. 
- SysML v2 diagrams, 
- SysML v2 textual modeling language, 
- SysML v2 API, in particular a REST API.

# SysML v2 in Systems Engineering and Development Process  

As mentioned above, SysML v2 is not a domain-specific tool development or for modeling/simulation.
Its use cases go over the whole development where it provides the "glue" between different domains. 
A reasonable methodology to use the SysML v2 ecosystem might be as follows: 

## Requirements elicitation
The requirements elicitation documents and organizes needs of stakeholder.
They are often not modeling experts. 
For this purpose, documents in natural language, but as well figures, equations, 
and more are used. These usually end up in human-readable text. 
Furthermore, methods for the verification of a requirement should be given. 
In SysMD Notebook, this can be either a 
- Documentation cell in SysMD Notebook, or a 
- Requirement of a SysML v2 model, in which a ```requirement``` statement has a ```doc``` statement includes the human-readable text. 
- Verification method is given in a SysML v2 model, in which a ```verification``` statement has a ```doc``` statement that includes the human-readable description of a test.
An introduction with examples is given in the SysML v2 part of the tutorial. 
Furthermore, use- and analysis- cases can be given (not yet included). 

## System specification
System-specification creates machine-readable models -- e.g., concrete constraints, etc. --  from the human-readable requirements. 
This is, where typical Model-Based Systems Engineering (MBSE) approaches begin. 
In SysML v2, the ```requirement``` and ```verification``` statements can be enriched with modeling concrete 
dependencies and constraints.

A key aspect of modern MBSE is now to *link* requirements, specification and constraints with the design that is created 
within the following development process. 
In the best case -- supported by SysMD notebook -- a contiuous verification and validation of requirement, constraints, and
the design artifacts is done. 

## Design  
The design process creates concrete components that satisfy the specification and requirements. 
This is typically done by (at least) two steps that are linked again with suitable relationships: 
- Functional view in which the functions are modeled and broken down to sub-functions, using parts and items of SysML v2. 
- Physical (or: technical) view in which the physical elements that realize the functions are modeled. 

Furthermore, one can add a geometrical view. 

## Use cases of SysML v2 models

The benefit of MBSE modeling effort lies in its use cases.
The model created throughout the requirements elicitation, specification ,and the design process an the be used 
in the following tasks: 

**Documentation** by diagrams that enable many stakeholders to quickly understand relationships
of a model. However, often this is considered as not worth the effort of creating models. 

**Analysis of relationships** by tracing changes and updating 
requirements, constraint, often allowing a detailed change-impact analysis, by 
following links from a component that does not satisfy its constraints.
Unfortunately, most tools do not automatically solve all expressions and dependencies 
and focus on creating and propagating "check-marks". 

**Continuous verification and validation** by checking whether specified constraints 
can be satisfied by the existing design. 
This includes also the bidirectional evaluation of constraints to functions and parts, and
vice versa. 
SysMD Notebook is able to automate this task via its solver.

**Safety and hazard assessment** Analysis of faults and its impacts.

**Production** Result of the development process is a cloud database that includes all parts and its variants. 
This can be used as a bill of materials (BOM) for production, and complementary information can be added to the models.

**Operation** The BOM derived from the SysML v2 model, together with complementary behavioral models for testing and 
verification can be used as a starting point for a *digital twin* that links 
- development, where basically types and features of things are modeled, and  
- operation, where unique, single individuals (SysML v2: ```individual```) 
are linked with models and individual-related data. 
 
# SysML v2 Ecosystem   
A typical SysML v2 ecosystem might consist of

- _Frontend tools_ for different purposes including all activities of a product life cycle.
  This includes SysMD Notebook which is in suitable particular for systems engineering.
  Other use cases might involve e.g. change management of modeled parts. 

- A _backend_ in the cloud in which model elements are persisted and versioned. 
  A backend functionality can be done in two ways: 
  - A simple _file/text-based_ backend in which text is persisted. 
    This is suitable during developing models and documentation.
    Version management can then be done with Git which is well suited for text-based 
    artifacts, i.e., source code.  
  - A complete _object-oriented versioning_ by a repository, e.g. in the web in which versions of the modeled elements
    are persisted and can be tracked. 
    This is suitable for version management of large projects in which e.g. different tools
    are used for e.g. change management for single parts. 

- _Libraries_ that are very specific for different domains and use cases.  

SysMD Notebook is basically a frontend tool for early analysis in systems engineering with 
- integrated file/text-based persistence of models and documents that can be versioned with Git, and
- a REST API to an object database for usage of models with object-oriented versioning. 

With such a scenario in mind, the SysML v2 standard goes beyond the scope of a pure 
modeling language! 
The SysML v2 standard hence covers the following aspects: 

**KerML** KerML is the metamodel and a basic modeling language. 
It provides basic modeling artifacts ("classes"), e.g. Types, Classes, Features, Expressions, 
and semantic libraries based on these artifacts.
KerML can be represented by 
- An abstract representation of instances of the metamodel classes. 
- KerML Textual representation

**SysML v2**, a language for Systems modeling with basic constructs based on KerML. 
SysML v2 models can be represented by
- SysML v2 Diagrams 
- SysML v2 Textual notation

In the tutorial we focus only on the textual notation. 

**API** The SysML v2 standard also specifies interfaces for model exchange and versioning: 

- For a cloud-based environment, a REST API and OSLC endpoints are specified. 
- For file-based use cases, file format for projects is specified. 

In the tutorial we cover all above aspects: 
- KerML introduces the basic metamodel for SysML v2.
- SysML introduces basics of the SysML v2 textual modeling language. 
- API gives a brief introduction into the REST API.   
