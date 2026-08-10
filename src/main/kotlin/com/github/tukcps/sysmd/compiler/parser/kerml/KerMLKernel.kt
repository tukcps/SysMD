@file:Suppress("FunctionName", "UNCHECKED_CAST", "GrazieInspection")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.model.generated.ElementType

/**
 * 8.2.5.4 Associations Concrete Syntax
 *
 *      Association = TypePrefix 'assoc' ClassifierDeclaration TypeBody
 */
fun KerML.Association() = TypeAction(semantics, ElementType.Association, isImplicit = "Links::BinaryLink").parse {
    ASSOC.consume()
    ClassifierDeclaration()
    TypeBody()
}

/**
 *      AssociationStructure = TypePrefix 'assoc' 'struct' ClassifierDeclaration TypeBody
 */
fun KerML.AssociationStructure() = TypeAction(semantics, ElementType.AssociationStructure).parse {
    ASSOC.consume()
    STRUCT.consume()
    ClassifierDeclaration()
    TypeBody()
}


/**
 *      ConnectorDeclaration = BinaryConnectorDeclaration | NaryConnectorDeclaration
 *
 *      BinaryConnectorDeclaration =
 *          ( FeatureDeclaration? 'from' | ('all' 'from')? )?
 *          ConnectorEndMember 'to'
 *          ConnectorEndMember
 *
 *      NaryConnectorDeclaration =
 *          FeatureDeclaration?
 *          '(' ConnectorEndMember ',' ConnectorEndMember ( ',' ConnectorEndMember )* ')'
 */
fun KerML.ConnectorDeclaration() {
    FeatureDeclaration()
    when(token.kind) {
        FROM -> {
            FROM.consume()
            ConnectorEnd().also { semantics.setSourceEnd(it) }
            TO.consume()
            ConnectorEnd().also { semantics.setTargetEnd(it) }
        }
        ALL  -> {
            ALL.consume()
            FROM.consume()
        }
        LBRACE -> {
            LBRACE.consume()
            ConnectorEnd().also { semantics.setTargetEnd(it) }
            COMMA.consume()
            ConnectorEnd().also { semantics.addTargetEnd(it) }
            noOrMore(start = COMMA) {
                COMMA.consume()
                ConnectorEnd()
            }
            RBRACE.consume()
        }
        else -> {}
    }
}


/**
 *      ConnectorEndMember = ConnectorEnd
 *      ConnectorEnd = ( declaredName = NAME REFERENCES )?
 *          OwnedReferenceSubsetting ( OwnedMultiplicity )?
 */
fun KerML.ConnectorEnd() = FeatureAction(semantics).parse {
    if (nextToken.kind == REFERENCES) {
        NAME_LIT.consume() .also { setIdentification(Identification(consumedToken.string)) }
        REFERENCES.consume()
    }
    OwnedReferenceSubsetting()
    optional(LCBRACE) { OwnedMultiplicity() }
}

fun KerML.OwnedReferenceSubsetting() =
    GeneralType().also { semantics.addReferenceSubsetting(it) }


/**
 * 8.2.5.5.1 Connectors
 *
 *      Connector = FeaturePrefix 'connector'
 *          (FeatureDeclaration? ValuePart?
 *              | ConnectorDeclaration
 *          )
 *          TypeBody
 *
 *      ConnectorEndMember = ConnectorEnd
 *
 *      ConnectorEnd = ( NAME REFERENCES )? OwnedReferenceSubsetting
 *          ( OwnedMultiplicity )?
 */
fun KerML.Connector() = ConnectorAction(semantics).parse {
    CONNECTOR.consume()
    ConnectorDeclaration()
    TypeBody()
}

/**
 * 8.2.5.10 Feature Values Concrete Syntax
 *
 *      ValuePart = FeatureValue
 *      FeatureValue =
 *          (      '='
 *              |  ':='
 *              |  'default' ( '=' | ':=' )?
 *          )
 *          OwnedExpression
 */
fun KerML.ValuePart() {
    val feature = semantics.element
    when(token.kind) {
        EQ ->       {
            EQ.consume()
            feature.isInitial = false
            feature.isDefault = false
        }
        DPEQ ->     {
            DPEQ.consume()
            feature.isInitial = true
            feature.isDefault = false
        }
        DEFAULT ->  {
            DEFAULT.consume()
            feature.isDefault = true
            feature.isInitial = when(token.kind) {
                DPEQ -> { DPEQ.consume(); true }
                EQ -> { EQ.consume(); false }
                else -> false
            }
        }
        else -> { }
    }
    val l = token.indices.first
    semantics.addOwnedElement(OwnedExpression(), ElementType.FeatureValue)
    val r = consumedToken.indices.last // otherwise left '(' might be lost
    feature.body = input.slice(l..r)
}

/**
 * 8.2.5.12 Metadata Concrete Syntax
 *
 *      Metaclass = TypePrefix 'metaclass' ClassifierDeclaration TypeBody
 */
fun KerML.Metaclass() = MetaclassAction(semantics).parse {
    METACLASS.consume()
    ClassifierDeclaration()
    TypeBody()
}

/**
 *      PrefixMetadataAnnotation = '#' PrefixMetadataFeature
 *
 * Note: PrefixMetadataMember is a qualified Name or FeatureChain
 */
fun KerML.PrefixMetadataAnnotation() {
    HASHTAG.consume()
    QualifiedName()
}

/**
 *      PrefixMetadataMember = '#' PrefixMetadataFeature
 *      PrefixMetadataFeature  = OwnedFeatureTyping
 *
 * Note: Annotation is a Qualified Name for which an Annotation is created.
 * Note2: OwnedFeatureTyping is a QualifiedName or FeatureChain
 */
fun KerML.PrefixMetadataMember() {
    HASHTAG.consume()
    QualifiedName()
}

 /**
 *      MetadataFeature = ( PrefixMetadataMember )*
 *          ( '@' | 'metadata' )
 *          MetadataFeatureDeclaration
 *          ( 'about' Annotation ( ',' Annotation )* )?
 *          MetadataBody

 */
fun KerML.MetadataFeature() = MetadataFeatureAction(semantics).parse {
    setOf( METADATA, ATSIGN ).consume()
    MetadataFeatureDeclaration()
    optional(start = ABOUT) {
        ABOUT.consume()
        QualifiedNameList() // Annotations
    }
    MetadataBody()
}

/**
 *      MetadataFeatureDeclaration = ( Identification ( ':' | 'typed' 'by' ) )? OwnedFeatureTyping
 *
 *  Note: OwnedFeatureTyping is a QualifiedName (eventually with Feature Chain) that resolves to a Type
 */
fun KerML.MetadataFeatureDeclaration() {
    Identification()            .semantics { action.setIdentification(it) }
    alternatives {
        TYPED_BY    starts {
            TYPED_BY.consume()
            QualifiedName()     .semantics { (action as MetadataFeatureAction).typeIfPresent = it }
        }
        TYPED       starts {
            TYPED.consume()
            BY.consume()
            QualifiedName()     .semantics{ (action as MetadataFeatureAction).typeIfPresent = it }
        }
        others  { }
    }
}

/**
 *      MetadataBody = ';' | '{' ( ownedRelationship += MetadataBodyElement )* '}'
 */
fun KerML.MetadataBody() {
    alternatives {
        SEMICOLON starts  { SEMICOLON.consume() }
        LCURBRACE starts  {
            LCURBRACE.consume()
            noOrMore( end = { token.kind == RCURBRACE} ) {
                MetadataBodyElement()
            }
            RCURBRACE.consume()
        }
    }
}

/**
 *      MetadataBodyElement =
 *            NonFeatureMember
 *          | MetadataBodyFeatureMember
 *          | AliasMember
 *          | Import
 */
fun KerML.MetadataBodyElement() {
    alternatives {
        nonFeatureElementStart starts { NonFeatureElement() }
        setOf(METADATA, ATSIGN) starts { MetadataFeature() }
        ALIAS starts { AliasMember() }
        IMPORT starts { Import() }
        others { MetadataBodyFeatureMember() }
    }
}

/**
 *      MetadataBodyFeatureMember = MetadataBodyFeature
 */
fun KerML.MetadataBodyFeatureMember() {
    MetadataBodyFeature()
}

/**
 *      MetadataBodyFeature = 'feature'? ( ':>>' | 'redefines')? OwnedRedefinition FeatureSpecializationPart? ValuePart?
 *      MetadataBody
 *
 *  Note: OwnedRedefinition is a simple name that is defined in the general class and that is redefined.
 */
fun KerML.MetadataBodyFeature() = FeatureAction(semantics, ElementType.Feature).parse {
    FEATURE.optional()
    REDEFINES.optional()
    NAME_LIT.consume().semantics { setIdentification(Identification(name=consumedToken.string))}
    optional(featureSpecializationPartStart) {
        FeatureSpecializationPart()
    }
    optional(valuePartStart) {
        ValuePart()
    }
    MetadataBody()
}


/**
 *      Predicate = TypePrefix 'predicate' ClassifierDeclaration FunctionBody
 */
fun KerML.Predicate() = TypeAction(semantics, ElementType.Predicate).parse {
    PREDICATE.consume()
    ClassifierDeclaration()
    FunctionBody()
}

/**
 *      BooleanExpression = FeaturePrefix 'bool' FeatureDeclaration ValuePart? FunctionBody
 */
fun KerML.BooleanExpression() = FeatureAction(semantics, ElementType.BooleanExpression).parse {
    BOOL.consume()
    FeatureDeclaration()
    optional(valuePartStart) { ValuePart() }
    FunctionBody()
}

/**
 *      Invariant = FeaturePrefix 'inv' ( 'true' | isNegated ?= 'false' )?
 *                  FeatureDeclaration ValuePart?
 *                  FunctionBody
 */
fun KerML.Invariant() = FeatureAction(semantics, ElementType.Invariant, "ScalarValues::Boolean").parse {
    INV.consume()
    alternatives {
        TRUE then  { semantics.element.isNegated = false }
        FALSE then { semantics.element.isNegated = true }
        others     { semantics.element.isNegated = false }
    }
    FeatureDeclaration()
    optional(valuePartStart) { ValuePart() }
    FunctionBody()
}

/**
 * 8.2.5.13 Packages Concrete Syntax
 *
 *      Package = ( PrefixMetadataMember )* PackageDeclaration PackageBody
 *      PackageDeclaration = 'package' Identification
 */
fun KerML.Package() = NamespaceAction(semantics, ElementType.Package).parse {
    PACKAGE.consume()
    Identification()    .semantics { action.setIdentification(it) }
    PackageBody()
}

/**
 *      PackageBody : Package =
 *              ';'
 *          | '{' ( NamespaceBodyElement |  ElementFilterMember)* '}'
 *
 *      ElementFilterMember = MemberPrefix 'filter' OwnedExpression ';'
 */
fun KerML.PackageBody() {
    alternatives {
        SEMICOLON then { }
        LCURBRACE then {
            noOrMore(end = { token.kind == RCURBRACE} ) {
                NamespaceBodyElement()
                // ElementFilter aren't supported yet
            }
            RCURBRACE.consume()
        }
        DOT then { /* SysMD only */ }
    }
}

/**
 *      LibraryPackage =
 *          ( 'standard' )? 'library' ( PrefixMetadataMember )* PackageDeclaration PackageBody
 */
fun KerML.LibraryPackage() = NamespaceAction(semantics, ElementType.LibraryPackage).parse {
    STANDARD.optional         { element.isStandard = true }
    LIBRARY.consume           { element.isLibraryElement = true }
    PACKAGE.consume()
    Identification().semantics{ action.setIdentification(it) }
    PackageBody()
}

/**
 * 8.2.5.7.1 Functions
 *
 *      Function = TypePrefix 'function' ClassifierDeclaration FunctionBody
 */
fun KerML.Function() = TypeAction(semantics, ElementType.Function).parse {
    TypePrefix()
	FUNCTION.consume()
    ClassifierDeclaration()
    FunctionBody()
}

/**
 * 8.2.5.5.3 Successions
 *
 *      Succession = FeaturePrefix 'succession' SuccessionDeclaration TypeBody
 */
fun KerML.Succession() = ConnectorAction(semantics, ElementType.Succession, "Occurrences::Occurrence").parse {
    SUCCESSION.consume()
    SuccessionDeclaration()
    TypeBody()
}

/**
 *      SuccessionDeclaration = FeatureDeclaration
 *      ( 'first' ConnectorEndMember 'then' ConnectorEndMember )?
 *      | ( 'all' )? ( 'first'? ConnectorEndMember 'then' ConnectorEndMember )?
 */
fun KerML.SuccessionDeclaration() {
    if (nextToken.kind !in setOf(DOT, ALL, THEN))
        FeatureDeclaration()
    alternatives {
        FIRST then {
            ConnectorEnd()
            THEN.consume()
            ConnectorEnd()
        }
        ALL then { FIRST.optional()
            ConnectorEnd()
            THEN.consume()
            ConnectorEnd()
        }
        NAME_LIT starts {   // All is optional, First as well ...
            ConnectorEnd()
            THEN.consume()
            ConnectorEnd()
        }
        others { }          // All productions are optional ...
    }
}

/**
 * 8.2.5.6.1 Behaviors
 *
 *      Behavior = TypePrefix 'behavior' ClassifierDeclaration TypeBody
 */
fun KerML.Behavior() = TypeAction(this.semantics, ElementType.Behavior, isImplicit = "Performances::Performance").parse {
    BEHAVIOR.consume()
    ClassifierDeclaration()
    TypeBody()
}

/**
 * 8.2.5.6.2 Steps
 *
 *      Step = FeaturePrefix 'step' FeatureDeclaration ValuePart? TypeBody
 */
fun KerML.Step() = FeatureAction(semantics, ElementType.Step, "Performances::Performance").parse {
    STEP.consume()
    FeatureDeclaration()
    optional(valuePartStart) {
        ValuePart()
    }
    TypeBody()
}


/**
 *      FunctionBody = ';' | '{' FunctionBodyPart '}'
 *
 *      FunctionBodyPart = ( TypeBodyElement | ReturnFeatureMember )* ( ResultExpressionMember )?
 *
 *      ReturnFeatureMember = MemberPrefix 'return' FeatureElement
 *
 *      ResultExpressionMember = MemberPrefix OwnedExpression
 */
internal fun KerML.FunctionBody() {
	semantics.prefixes.clear()
    alternatives {
        LCURBRACE then {
            noOrMore(typeBodyElementStarts+HASHTAG+RETURN) {
                noOrMore(HASHTAG) { PrefixMetadataMember() }
                alternatives {
                    RETURN then  { semantics.prefixes.add(OUT); Feature(ElementType.ReturnParameterMembership) }
                    typeBodyElementStarts starts { TypeBodyElement() }
                }
            }
            if (token.kind != RCURBRACE) {
                val owner = semantics.element
                val iBeforeExpression = token.indices.first
                MemberPrefix() // standard only wants visibility here, we parse 'abstract' too
                OwnedExpression().semantics {
                    owner.indices = iBeforeExpression..consumedToken.indices.last
                    owner.body = input.slice(owner.indices!!).trim()
                    addOwnedElement(it, ElementType.ResultExpressionMembership)
                }
            }
            RCURBRACE.consume()
        }
        SEMICOLON then { }
    }
}

/**
 *      Structure :- "struct"  Identification [ :> QualifiedName] Body
 *
 * Note: Prefixes are handled separately
 */
fun KerML.Structure() = TypeAction(semantics, ElementType.Structure, isImplicit = "Objects::Object").parse {
    STRUCT.consume()
    ClassifierDeclaration()
    TypeBody()
}

/**
 * 8.2.5.9 Interactions Concrete Syntax
 * 8.2.5.9.1 Interactions
 *
 *      Interaction = TypePrefix 'interaction' ClassifierDeclaration TypeBody
 */
fun KerML.Interaction()  = TypeAction(this.semantics, ElementType.Interaction).parse {
    INTERACTION.consume()
    ClassifierDeclaration()
    TypeBody()
}