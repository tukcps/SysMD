@file:Suppress("FunctionName", "UNCHECKED_CAST", "GrazieInspection")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.expression.InvariantActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*


/**
 * 8.2.5.4 Associations Concrete Syntax
 *
 *      Association = TypePrefix 'assoc' ClassifierDeclaration TypeBody
 */
fun KerML.Association() = AssociationActions<Association>(semantics, ::AssociationImplementation).parse {
    ASSOC.consume()
    ClassifierDeclaration()
    TypeBody()
}


/**
 *      AssociationStructure = TypePrefix 'assoc' 'struct' ClassifierDeclaration TypeBody
 */
fun KerML.AssociationStructure() = ClassifierActions<AssociationStructure>(semantics, ::AssociationStructureImplementation).parse {
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
            ConnectorEndMember().also { semantics.setSourceEnd(it) }
            TO.consume()
            ConnectorEndMember().also { semantics.setTargetEnd(it) }
        }
        ALL  -> {
            ALL.consume()
            FROM.consume()
        }
        LBRACE -> {
            LBRACE.consume()
            ConnectorEndMember().also { semantics.setTargetEnd(it) }
            COMMA.consume()
            ConnectorEndMember().also { semantics.addTargetEnd(it) }
            noOrMore(start = COMMA) {
                COMMA.consume()
                ConnectorEndMember()
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
fun KerML.ConnectorEndMember() = FeatureActions<Feature>(semantics, ::FeatureImplementation).parse {
    if (nextToken.kind == REFERENCES) {
        NAME_LIT.consume() .also { semantics.create(Identification(consumedToken.string)) }
        REFERENCES.consume()
    } else
        semantics.create(null)
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
fun KerML.Connector() = ConnectorActions<Connector>(semantics, ::ConnectorImplementation).parse {
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
    when(token.kind) {
        EQ ->       EQ.consume()
        DPEQ ->     DPEQ.consume()
        DEFAULT ->  {
            DEFAULT.consume()
            when(token.kind) {
                EQ -> EQ.consume()
                DPEQ -> DPEQ.consume()
                else ->  {}
            }
        }
        else -> { }
    }
    OwnedExpression()
}


/**
 * Owned Expression is the interface towards the proprietary handling of expressions.
 * We keep and serialize them as strings and work on AST that are proprietary.
 */
fun KerML.OwnedExpression() {
    val iBeforeExpression = token.indices.first
    val feature = semantics.element<Feature>()
    feature.variable = VariableImplementation(feature)
    semantics.expression = feature
    Expression().also {
        feature.variable?.ast = AstRoot(model, semantics.expression!!, it)
        feature.indices = iBeforeExpression..consumedToken.indices.last
        feature.expression = input.subSequence(feature.indices!!).toString().trim()
    }
}


/**
 * 8.2.5.12 Metadata Concrete Syntax
 *
 *      Metaclass = TypePrefix 'metaclass' ClassifierDeclaration TypeBody
 */
fun KerML.Metaclass() = MetaclassActions(semantics, ::MetaclassImplementation).parse {
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
fun KerML.MetadataFeature() = MetadataFeatureActions(semantics, ::MetadataFeatureImplementation).parse {
    METADATA.consume()
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
    Identification().also { (semantics.currentActions() as MetadataFeatureActions).identificationOrType = it }
    alternatives {
        TYPED_BY    starts {
            TYPED_BY.consume()
            QualifiedName().also { (semantics.currentActions() as MetadataFeatureActions<MetadataFeature>).typeIfPresent = it }
        }
        TYPED       starts {
            TYPED.consume(); BY.consume()
            QualifiedName().also{ (semantics.currentActions() as MetadataFeatureActions<MetadataFeature>).typeIfPresent = it }
        }
        others  { }
    }
    semantics.create(null)
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
        METADATA starts { MetadataFeature() }
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
fun KerML.MetadataBodyFeature() = FeatureActions<Feature>(semantics, ::FeatureImplementation).parse {
    FEATURE.optional()
    REDEFINES.optional()
    NAME_LIT.consume().also { semantics.create(Identification(name=consumedToken.string))}
    optional(featureSpecializationPartStart) {
        FeatureSpecializationPart()
    }
    optional(valuePartStart) {
        ValuePart()
    }
    MetadataBody()
}



/**
 *      Invariant :- "inv" Identification "{" Expression "}"
 *
 *  Note: Expression must be of type Boolean and must be satisfied
 */
fun KerML.Invariant() = InvariantActions(semantics, ::InvariantImplementation).parse {
    INV.consume()
    Identification().also { semantics.create(it) }
    alternatives {
        TRUE then  { semantics.element<Invariant>().isNegated = false }
        FALSE then { semantics.element<Invariant>().isNegated = true }
        others     { semantics.element<Invariant>().isNegated = false }
    }
    FunctionBody()
}

/**
 * 8.2.5.13 Packages Concrete Syntax
 *
 *      Package = ( PrefixMetadataMember )* PackageDeclaration PackageBody
 *      PackageDeclaration = 'package' Identification
 */
fun KerML.Package() = NamespaceActions(semantics, ::PackageImplementation).parse {
    PACKAGE.consume()
    Identification().also { semantics.create(it) }
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
fun KerML.LibraryPackage() = NamespaceActions(semantics, ::PackageImplementation).parse {
    STANDARD.optional         { semantics.prefixes.add(STANDARD) }
    LIBRARY.consume()   .also { semantics.prefixes.add(LIBRARY) }
    PACKAGE.consume()
    Identification()    .also { semantics.create(it) }
    PackageBody()
}

/**
 * 8.2.5.7.1 Functions
 *
 *      Function = TypePrefix 'function' ClassifierDeclaration FunctionBody
 */
fun KerML.Function() = FunctionActions(semantics, ::FunctionImplementation).parse {
    FUNCTION.consume()
    ClassifierDeclaration()
    FunctionBody()
}

/**
 * 8.2.5.5.3 Successions
 *
 *      Succession = FeaturePrefix 'succession' SuccessionDeclaration TypeBody
 */
fun KerML.Succession() = ConnectorActions<Succession>(semantics, ::SuccessionImplementation, "Occurrences::Occurrence").parse {
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
    else
        semantics.create(null)
    alternatives {
        FIRST then {
            ConnectorEndMember()
            THEN.consume()
            ConnectorEndMember()
        }
        ALL then { FIRST.optional()
            ConnectorEndMember()
            THEN.consume()
            ConnectorEndMember()
        }
        NAME_LIT starts {   // All is optional, First as well ...
            ConnectorEndMember()
            THEN.consume()
            ConnectorEndMember()
        }
        others { }          // All productions are optional ...
    }
}

/**
 * TODO
 */
fun KerML.Predicate() = FunctionActions<Predicate>(semantics, ::PredicateImplementation).parse {
    PREDICATE.consume()
    ClassifierDeclaration()
    FunctionBody()
}

/**
 * 8.2.5.6.1 Behaviors
 *
 *      Behavior = TypePrefix 'behavior' ClassifierDeclaration TypeBody
 */
fun KerML.Behavior() = ClassifierActions<Classifier>(this.semantics, ::BehaviorImplementation).parse {
    BEHAVIOR.consume()
    ClassifierDeclaration()
    TypeBody()
}


/**
 * 8.2.5.6.2 Steps
 *
 *      Step = FeaturePrefix 'step' FeatureDeclaration ValuePart? TypeBody
 */
fun KerML.Step() = FeatureActions<Feature>(semantics, ::StepImplementation, "Performances::Performance").parse {
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
    alternatives {
        LCURBRACE then {
            noOrMore(typeBodyElementStarts+HASHTAG+RETURN) {
                noOrMore(HASHTAG) { PrefixMetadataMember() }
                alternatives {
                    RETURN then  { semantics.prefixes.add(OUT); Feature() }
                    typeBodyElementStarts starts { TypeBodyElement() }
                }
            }
            if (token.kind != RCURBRACE) {
                OwnedExpression()
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
fun KerML.Structure() = StructureActions(semantics, ::StructureImplementation).parse {
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
fun KerML.Interaction()  = ClassifierActions<Interaction>(this.semantics, ::InteractionImplementation).parse {
    INTERACTION.consume()
    ClassifierDeclaration()
    TypeBody()
}