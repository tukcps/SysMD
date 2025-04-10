@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.expression.InvariantActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*


/**
 * 8.2.5.4 Associations Concrete Syntax
 *
 *      Association = TypePrefix 'assoc' ClassifierDeclaration TypeBody
 */
fun KerML.Association() {
    val association = semantics.associationActions()
    ASSOC.consume()
    ClassifierDeclaration(association as ClassifierActions<Classifier>)
    TypeBody(Resolved(association.created!!))
    association.finish()
}


/**
 *      AssociationStructure = TypePrefix 'assoc' 'struct' ClassifierDeclaration TypeBody
 */
fun KerML.AssociationStructure() {
    val associationStructure = ClassifierActions<AssociationStructure>(semantics, ::AssociationStructureImplementation)
    ASSOC.consume()
    STRUCT.consume()
    ClassifierDeclaration(associationStructure as ClassifierActions<Classifier>)
    TypeBody(Resolved(associationStructure.created!!))
    associationStructure.finish()
}

/**
 * 8.2.5.5.1 Connectors
 *
 *      Connector = FeaturePrefix 'connector'
 *          ( FeatureDeclaration? ValuePart?
 *              | ConnectorDeclaration
 *          )
 *          TypeBody
 *
 *      ConnectorDeclaration =
 *          BinaryConnectorDeclaration | NaryConnectorDeclaration
 *
 *      BinaryConnectorDeclaration =
 *          ( FeatureDeclaration? 'from' | ('all' 'from')?)?
 *          ConnectorEndMember 'to'
 *          ConnectorEndMember
 *
 *      NaryConnectorDeclaration =
 *          FeatureDeclaration?
 *          '(' ownedRelationship += ConnectorEndMember ','
 *              ownedRelationship += ConnectorEndMember
 *              ( ',' ownedRelationship += ConnectorEndMember )*
 *          ')'
 *
 *      ConnectorEndMember = ConnectorEnd
 *
 *      ConnectorEnd = ( NAME REFERENCES )? OwnedReferenceSubsetting
 *          ( OwnedMultiplicity )?
 */
fun KerML.Connector() {
    val connector = ConnectorActions<Connector>(semantics, ::ConnectorImplementation)

    CONNECTOR.consume()
    Identification().also { connector.create(it)  }

    alternatives {
        TYPED_BY then {
            QualifiedNameList().also { connector.addTyping(it) }
            optional(FROM, true) { QualifiedNameList().also { connector.addSource(it) } }
            optional(TO, true)   { QualifiedNameList().also { connector.addTarget(it) } }
        }
        FROM then {
            QualifiedNameList().also { connector.addSource(it) }
            optional(TO, true) { QualifiedNameList().also { connector.addTarget(it) } }
        }
        TO then {
            QualifiedNameList().also { connector.addTarget(it) }
        }
    }
    TypeBody(Resolved(connector.created!!))
    connector.finish()
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
fun KerML.ValuePart(feature: SemanticAction<Feature>) {
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
    OwnedExpression(feature)
}


/**
 * Owned Expression is the interface towards the proprietary handling of expressions.
 * We keep and serialize them as strings and work on AST that are proprietary.
 */
fun KerML.OwnedExpression(feature: SemanticAction<Feature>) {
    val iBeforeExpression = token.indices.first
    feature.created?.variable = VariableImplementation(feature.created!!)
    semantics.expression = feature.created
    Expression().also {
        feature.created?.variable?.ast = AstRoot(model, semantics.expression!!, it)
        feature.created?.indices = iBeforeExpression..consumedToken.indices.last
        feature.created?.expression = input.subSequence(feature.created?.indices!!).toString().trim()
    }
}
fun KerML.OwnedExpression(owner: Feature) {
    val iBeforeExpression = token.indices.first
    owner.variable = VariableImplementation(owner)
    semantics.expression = owner
    Expression().also {
        owner.variable?.ast = AstRoot(model, semantics.expression!!, it)
        owner.indices = iBeforeExpression..consumedToken.indices.last
        owner.expression = input.subSequence(owner.indices!!).toString().trim()
    }
}


/**
 * 8.2.5.12 Metadata Concrete Syntax
 *
 *      Metaclass = TypePrefix 'metaclass' ClassifierDeclaration TypeBody
 */
fun KerML.Metaclass() {
    val metaclass = MetaclassActions(this.semantics, ::MetaclassImplementation, mutableListOf("Base::Anything"))
    METACLASS.consume()
    ClassifierDeclaration(metaclass as ClassifierActions<Classifier>)
    TypeBody(Resolved(ref=metaclass.created!!))
    metaclass.finish()
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
fun KerML.MetadataFeature() {
    val metadataFeature = MetadataFeatureActions<MetadataFeature>(semantics, ::MetadataFeatureImplementation, mutableListOf("Base::Anything"))
    METADATA.consume()
    MetadataFeatureDeclaration(metadataFeature)
    optional(start = ABOUT) {
        ABOUT.consume()
        QualifiedNameList() // Annotations
    }
    MetadataBody(metadataFeature as FeatureActions<Feature>)
    metadataFeature.finish()
}

/**
 *      MetadataFeatureDeclaration = ( Identification ( ':' | 'typed' 'by' ) )? OwnedFeatureTyping
 *
 *  Note: OwnedFeatureTyping is a QualifiedName (eventually with Feature Chain) that resolves to a Type
 */
fun KerML.MetadataFeatureDeclaration(metadataFeature: MetadataFeatureActions<MetadataFeature>) {
    Identification().also  { metadataFeature.identificationOrType = it }
    alternatives {
        TYPED_BY    starts {
            TYPED_BY.consume()
            QualifiedName().also { metadataFeature.typeIfPresent = it }
        }
        TYPED       starts {
            TYPED.consume(); BY.consume()
            QualifiedName().also{ metadataFeature.typeIfPresent = it }
        }
        others  { }
    }
    metadataFeature.create()
}

/**
 *      MetadataBody = ';' | '{' ( ownedRelationship += MetadataBodyElement )* '}'
 */
fun KerML.MetadataBody(owner: FeatureActions<Feature>) {
    alternatives {
        SEMICOLON starts  { SEMICOLON.consume() }
        LCURBRACE starts  {
            semantics.pushOwner(Resolved(owner.created!!))
            LCURBRACE.consume()
            noOrMore( end = { token.kind == RCURBRACE} ) {
                MetadataBodyElement()
            }
            RCURBRACE.consume()
            semantics.popOwner()
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
fun KerML.MetadataBodyFeature() {
    val metadataBodyFeature = FeatureActions<Feature>(semantics, ::FeatureImplementation, mutableListOf("Base::Anything"))
    FEATURE.optional()
    REDEFINES.optional()
    NAME_LIT.consume().also { metadataBodyFeature.create(Identification(name=consumedToken.string))}
    optional(featureSpecializationPartStart) {
        FeatureSpecializationPart(metadataBodyFeature)
    }
    optional(valuePartStart) {
        ValuePart(metadataBodyFeature)
    }
    MetadataBody(metadataBodyFeature)
    metadataBodyFeature.finish()
}



/**
 *      Invariant :- "inv" Identification "{" Expression "}"
 *
 *  Note: Expression must be of type Boolean and must be satisfied
 */
fun KerML.Invariant() {
    val invariant = InvariantActions(semantics, ::InvariantImplementation)
    INV.consume()
    Identification().also { invariant.create(it) }
    invariant.finish()
    alternatives {
        TRUE then { invariant.created?.isNegated = false }
        FALSE then { invariant.created?.isNegated = true }
        others {  invariant.created?.isNegated = false }
    }
    FunctionBody(Resolved(invariant.created!!))
}

/**
 * 8.2.5.13 Packages Concrete Syntax
 *
 *      Package = ( PrefixMetadataMember )* PackageDeclaration PackageBody
 *      PackageDeclaration = 'package' Identification
 */
fun KerML.Package() {
    val pkg = NamespaceActions(semantics, ::PackageImplementation)
    PACKAGE.consume()
    Identification().also { pkg.create(it) }
    Body(Resolved(null, pkg.created, null))
}

/**
 *      PackageBody : Package =
 *              ';'
 *          | '{' ( NamespaceBodyElement |  ElementFilterMember)* '}'
 *
 *      ElementFilterMember = MemberPrefix 'filter' OwnedExpression ';'
 */
fun KerML.PackageBody(owner: Resolved<Namespace>) {
    alternatives {
        SEMICOLON then { }
        LCURBRACE then {
            semantics.pushOwner(owner)
            noOrMore(end = { token.kind == RCURBRACE} ) {
                NamespaceBodyElement()
                // ElementFilter aren't supported yet
            }
            semantics.popOwner()
            RCURBRACE.consume()
        }
    }
}

/**
 *      LibraryPackage =
 *          ( 'standard' )? 'library' ( PrefixMetadataMember )* PackageDeclaration PackageBody
 */
fun KerML.LibraryPackage() {
    val pkg = NamespaceActions(semantics, ::PackageImplementation)
    STANDARD.optional       { semantics.prefixes.add(STANDARD) }
    LIBRARY.consume().also  { semantics.prefixes.add(LIBRARY) }
    PACKAGE.consume()
    Identification().also { pkg.create(it) }
    PackageBody(Resolved(null, pkg.created, null))
}


/**
 * 8.2.5.7.1 Functions
 *
 *      Function = TypePrefix 'function' ClassifierDeclaration FunctionBody
 */
fun KerML.Function() {
    val function = FunctionActions<FunctionImplementation>(semantics, ::FunctionImplementation)
    FUNCTION.consume()
    ClassifierDeclaration(function as ClassifierActions<Classifier>)
    FunctionBody(Resolved(function.created!!))
    function.finish()
}

/**
 * 8.2.5.5.3 Successions
 *
 *      Succession = FeaturePrefix 'succession' SuccessionDeclaration TypeBody
 */
fun KerML.Succession() {
    val succession = FeatureActions<Feature>(this.semantics, ::SuccessionImplementation, mutableListOf("Occurrences::happensBeforeLinks"))
    SUCCESSION.consume()
    SuccessionDeclaration(succession)
    TypeBody(Resolved(succession.created!!))
    succession.finish()

}

/**
 *      SuccessionDeclaration = FeatureDeclaration
 *      ( 'first' ConnectorEndMember 'then' ConnectorEndMember )?
 *      | ( 'all' )? ( 'first'? ConnectorEndMember 'then' ConnectorEndMember )?
 */
fun KerML.SuccessionDeclaration(feature: FeatureActions<Feature>) {
    FeatureDeclaration(feature)
    alternatives {
        FIRST then {  }
        ALL then { FIRST.optional() }
    }
}


/**
 * TODO
 */
fun KerML.Predicate() {
    val predicate = semantics.functionActions()
    PREDICATE.consume()
    ClassifierDeclaration(predicate as ClassifierActions<Classifier>)
    FunctionBody(Resolved(predicate.created!!))
    predicate.finish()
}

/**
 * 8.2.5.6.1 Behaviors
 *
 *      Behavior = TypePrefix 'behavior' ClassifierDeclaration TypeBody
 */
fun KerML.Behavior() {
    val behavior = ClassifierActions<Classifier>(this.semantics, ::BehaviorImplementation)
    BEHAVIOR.consume()
    ClassifierDeclaration(behavior)
    TypeBody(Resolved(behavior.created!!))
    behavior.finish()
}


/**
 * 8.2.5.6.2 Steps
 *
 *      Step = FeaturePrefix 'step' FeatureDeclaration ValuePart? TypeBody
 */
fun KerML.Step() {
    val step = FeatureActions<Feature>(this.semantics, ::StepImplementation, mutableListOf("Performances::performances"))
    STEP.consume()
    FeatureDeclaration(step)
    optional(valuePartStart) {
        ValuePart(step)
    }
    TypeBody(Resolved(step.created!!))
    step.finish()
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
internal fun KerML.FunctionBody(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            noOrMore(typeBodyElementStarts+HASHTAG+RETURN) {
                MemberPrefix()
                noOrMore(HASHTAG) { PrefixMetadataMember() }
                alternatives {
                    RETURN then  { semantics.prefixes.add(OUT); Feature() }
                    typeBodyElementStarts then { TypeBodyElement() }
                }
            }
            if (token.kind != RCURBRACE) {
                OwnedExpression(owner.ref as Feature)
            }
            semantics.popOwner()
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
fun KerML.Structure() {
    val structure = StructureActions(semantics, ::StructureImplementation)
    STRUCT.consume()
    @Suppress("UNCHECKED_CAST")
    ClassifierDeclaration(structure as ClassifierActions<Classifier>)
    TypeBody(Resolved(structure.created!!))
    structure.finish()
}

/**
 * 8.2.5.9 Interactions Concrete Syntax
 * 8.2.5.9.1 Interactions
 *
 *      Interaction = TypePrefix 'interaction' ClassifierDeclaration TypeBody
 */
fun KerML.Interaction() {
    val interaction = ClassifierActions<Interaction>(this.semantics, ::InteractionImplementation)
    INTERACTION.consume()
    ClassifierDeclaration(interaction as ClassifierActions<Classifier>)
    TypeBody(Resolved(interaction.created!!))
    interaction.finish()
}