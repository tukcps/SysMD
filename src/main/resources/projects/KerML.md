# 9.2.17 KerML
This package contains a reflective KerML model of the KerML abstract syntax. It is generated from the normative MOF abstract syntax model (see 8.3) as follows.
1. The KerML model contains subpackages for Root, Core, and Kernel, but all elements are also imported into the top-level package, so they can be referenced directly from the KerML namespace.
2. A metaclass from the MOF model is mapped into a Metaclass in the KerML package.
- The MOF metaclass name is mapped unchanged.
- Generalizations of the MOF metaclass are mapped to ownedSpecializations.
- All properties from the MOF metaclass are mapped to features of the corresponding KerML
Metaclass (see below). All non-association-end properties are grouped before association-end
properties.
3. A property from the MOF model is mapped into a Feature.
- The following feature properties are set as appropriate:
- isAbstract = true if the MOF property is a derived union ▪ isComposite = true if the MOF property is composite.
- isReadonly = true if the MOF property is read-only.
▪ isDerived = true if the MOF property is derived.
◦ The MOF property name is mapped unchanged.
◦ The MOF property type is mapped to an ownedTyping relationship.
▪ If the MOF property type is a primitive type, the relationship is to the corresponding type from the ScalarValues package (see 9.3.2).
▪ If the MOF property type is a metaclass, the relationship is to the corresponding reflective Metaclass.
- The MOF property multiplicity is mapped to an owned MultiplicityRange with bounds given by LiteralExpressions.
- Subsetted properties from the MOF property are mapped to ownedSubsettings of the corresponding reflective Features.
- Redefined properties from the MOF property are mapped to ownedRedefinitions of the corresponding reflective Features.
- If the MOF property is annotatedElement, then Metaobject::annotatedElement is added to the list of redefined properties for the mapping.
4. An enumeration from the MOF model is mapped into a DataType.
- The MOF enumeration name is mapped unchanged.
- Each enumeration literal from the MOF enumeration is mapped into a ownedMember Feature
(not and ownedFeature).
- The MOF enumeration literal name is mapped unchanged.
- The member Feature is given an owned MultiplicityRange of 1..1.
Note that associations are not mapped from the MOF model and, hence, non-navigable association-owned end properties are not included in the reflective model.
```SysMD
/**
 * The package is hard-wired. 
 */ 
standard library package KerML {
    import Root; 
    import Core;
    import Kernel; 
    package Root; 
    package Core; 
    package Kernel; 
}
```