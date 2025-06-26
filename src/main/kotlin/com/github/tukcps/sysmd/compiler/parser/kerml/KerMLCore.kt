@file:JvmName("Strings")
@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.settings
import io.github.tukcps.aadd.values.IntegerRange

/**
 * SPECIALIZES is a pseudo-lexical element that is either
 * :> (DPGT) or 'specializes'
 */
fun KerML.SPECIALIZES() {
    when(token.kind) {
        DPGT        -> { consume() }
        SPECIALIZES -> { consume() }
        else -> handleSyntaxError("Expect ':>' or 'specializes'")
    }
}

/**
 *      SpecializationPart = SPECIALIZES OwnedSpecialization (',' OwnedSpecialization)*
 */
fun KerML.SpecializationPart(): MutableList<QualifiedName> {
    val result = mutableListOf<QualifiedName>()
    SPECIALIZES()
    QualifiedName().also        { result.add(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also    { result.add(it) }
    }
    return result
}

/**
 *      ConjugationPart = 'conjugates' ownedRelationship += OwnedConjugation
 */
fun KerML.ConjugationPart(): MutableList<String> {
    val result = mutableListOf<String>()
    CONJUGATION.consume()
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { result.add(it) }
    }
    model.status.info("Not yet supported: Conjugation", this)
    return result
}

/**
 * 8.2.4.1.1 Types
 *
 *      Type = TypePrefix 'type' TypeDeclaration TypeBody
 *      TypePrefix = ('abstract')? (PrefixMetadataMember)*
 */
fun KerML.Type() {
    val type = TypeActions<Type>(this.semantics, creator = ::TypeImplementation)
    // Prefix handled in calling method.
    TYPE.consume()
    TypeDeclaration(type)
    TypeBody(Resolved(type.created!!))
}

/**
 *      TypeDeclaration = ( 'all' )? Identification (OwnedMultiplicity)?
 *           ( SpecializationPart | ConjugationPart )+
 *          TypeRelationshipPart*
 */
fun KerML.TypeDeclaration(type: TypeActions<Type>) {
    ALL.optional                { type.isSufficient = true }
    Identification().also       { type.create(it) }
    OwnedMultiplicity().also    { if (it != IntegerRange(1,1)) type.addMultiplicity(it) }
    alternatives {  // TODO: --> +, not only alternative, also + !!
        SPECIALIZES or DPGT starts { SpecializationPart().also { type.addSpecialization(it) } }
        CONJUGATION starts      { ConjugationPart().also    { type.addConjugation(it) } }
    }
}

/**
 *      TypeRelationshipPart =
 *          DisjoiningPart | UnioningPart | IntersectingPart | DifferencingPart
 *      DisjoiningPart =
 *          'disjoint' 'from' OwnedDisjoining ( ',' ownedRelationship += OwnedDisjoining )*
 */
fun KerML.TypeRelationshipPart(): List<QualifiedName> {
    val typeRelationships = mutableListOf<QualifiedName>()
    alternatives {
        DISJOINT starts     { DisjoiningPart() }
        UNIONS starts       { UnioningPart() }
        INTERSECTS starts   { IntersectingPart() }
        DIFFERENCES starts  { DifferencingPart() }
    }
    return typeRelationships
}

/**
 *      DisjoiningPart = 'disjoint' 'from' OwnedDisjoining ( ',' OwnedDisjoining )*
 */
fun KerML.DisjoiningPart(): List<QualifiedName> {
    val disjoining = mutableListOf<QualifiedName>()
    DISJOINT.consume()
    FROM.consume()
    QualifiedName().also { disjoining.add(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { disjoining.add(it) }
    }
    return disjoining
}

/**
 *      UnioningPart = 'unions' Unioning ( ',' Unioning )*
 */
fun KerML.UnioningPart(): List<QualifiedName> {
    val unions = mutableListOf<QualifiedName>()
    UNIONS.consume()
    QualifiedName().also     { unions.add(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { unions.add(it) }
    }
    return unions
}

/**
 *      IntersectingPart = 'intersects' Intersecting( ',' ownedRelationship += Intersecting )*
 */
fun KerML.IntersectingPart(): List<QualifiedName> {
    val intersecting = mutableListOf<QualifiedName>()
    INTERSECTS.consume()
    QualifiedName().also     { intersecting.add(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { intersecting.add(it) }
    }
    return intersecting
}

/**
 *      DifferencingPart : Type = 'differences' Differencing( ',' ownedRelationship += Differencing )*
 */
fun KerML.DifferencingPart(): List<QualifiedName> {
    val differencing = mutableListOf<QualifiedName>()
    DIFFERENCES.consume()
    QualifiedName().also     { differencing.add(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { differencing.add(it) }
    }
    return differencing
}

/**
 * 8.2.4.2.1 Classifiers
 *
 *      Classifier = TypePrefix 'classifier' ClassifierDeclaration TypeBody
 */
fun KerML.Classifier() {
    val classifier = ClassifierActions<Classifier>(this.semantics, creator =  ::ClassifierImplementation)
    CLASSIFIER.consume()
    ClassifierDeclaration(classifier)
    TypeBody(Resolved(classifier.created!!))
    classifier.finish()
}

/**
 *      ClassifierDeclaration : Classifier =
 *          ( 'all' )?
 *          Identification
 *          ( OwnedMultiplicity )?
 *          ( SuperclassingPart | ConjugationPart )?
 *          TypeRelationshipPart*
 */
fun KerML.ClassifierDeclaration(classifier: ClassifierActions<Classifier>) {
    ALL.optional             { classifier.isSufficient = true }
    Identification().also    { classifier.create(it) }
    OwnedMultiplicity()
    alternatives {
        SPECIALIZES or DPGT starts  { SpecializationPart().also { classifier.addSubclassification(it) } }
        CONJUGATION starts  { ConjugationPart().also    { classifier.addConjugation(it) } }
        others                                          {  }
    }
}


/**
 *      DataType :- "datatype" Identification ["specializes" QualifiedName] Body
 */
fun KerML.Datatype() {
    val datatype = DataTypeActions<DataType>(semantics, ::DataTypeImplementation)
    DATATYPE.consume()
    @Suppress("UNCHECKED_CAST")
    ClassifierDeclaration(datatype as ClassifierActions<Classifier>)
    TypeBody(Resolved(datatype.created!!))
    datatype.finish()
}

/**
 *  8.2.5.2 Classes Concrete Syntax
 *
 *      Class = TypePrefix 'class' ClassifierDeclaration TypeBody
 *          Note: Calling production handles prefixes before.
 */
fun KerML.Class() {
    val klass = ClassActions(semantics, ::ClassImplementation)
    CLASS.consume()
    @Suppress("UNCHECKED_CAST")
    ClassifierDeclaration(klass as ClassifierActions<Classifier>)
    TypeBody(Resolved(klass.created!!))
    klass.finish()
}

/**
 * _8.2.4.1.2 Specialization_
 *
 *      Specialization = ( 'specialization' Identification )?
 *          'subtype' SpecificType SPECIALIZES GeneralType RelationshipBody
 *
 *      OwnedSpecialization = GeneralType
 *      SpecificType =  [QualifiedName] | OwnedFeatureChain
 *      GeneralType =  [QualifiedName] | OwnedFeatureChain
 */
val specializationStart = setOf(SPECIALIZATION, SUBTYPE)
fun KerML.Specialization() {
    SPECIALIZATION.optional {
        Identification()
    }
    SUBTYPE.consume()
    SpecificType()
    SPECIALIZES()
    GeneralType()
    RelationshipBody(Resolved(null, null, null))
}

fun KerML.SpecificType() =
    when(nextToken.kind) {
        DOT ->  FeatureChain()
        else -> QualifiedName()
    }

fun KerML.GeneralType() =
    when(nextToken.kind) {
        DOT ->  FeatureChain()
        else -> QualifiedName()
    }

/**
 * TODO: Move FeatureChain into Expressions properly
 */
fun KerML.FeatureChain(): String {
    val result: StringBuilder = StringBuilder()
    NAME_LIT.consume()          .also { result.append(consumedToken.string) }
    noOrMore(DOT) {
        DOT.consume()           .also { result.append("::") }
        NAME_LIT.consume()      .also { result.append(consumedToken.string) }
    }
    return result.toString()
}

/**
 * _8.2.4.1.3 Conjugation_
 *
 *      Conjugation = ( 'conjugation' Identification )?
 *          'conjugate' ( [QualifiedName] |  FeatureChain )
 *          CONJUGATES ( [QualifiedName] | FeatureChain)
 *          RelationshipBody
 *
 *      OwnedConjugation = [QualifiedName] | FeatureChain
 */
val CONJUGATION_START = setOf(CONJUGATION, CONJUGATE)
fun KerML.Conjugation() {
    val conjugation = RelationshipActionsImpl<Conjugation>(semantics, ::ConjugationImplementation)
    CONJUGATION.optional {
        Identification().also {     conjugation.create(it) }
    }
    if (conjugation.created == null) conjugation.create(Identification(null, null))
    CONJUGATE.consume()
    QualifiedName()
    CONJUGATES.consume()
    QualifiedName()
    RelationshipBody(Resolved(conjugation.created!!))
}

/**
 * _8.2.4.1.4 Disjoining_
 *
 *      Disjoining = ( 'disjoining' Identification )?
 *          'disjoint' ( [QualifiedName] | FeatureChain )
 *          'from' ( [QualifiedName] | FeatureChain )
 *      RelationshipBody
 *
 *      OwnedDisjoining = [QualifiedName]
 */
val DISJOINING_START = setOf(DISJOINING, DISJOINT)
fun KerML.Disjoining() {
    DISJOINING.optional {
        Identification()
    }
    DISJOINT.consume()
    QualifiedName()
    FROM.consume()
    QualifiedName()
    RelationshipBody(Resolved(null, null, null))
}

/**
 * 8.2.4.3.1 Features
 *
 *      Feature =   // FeaturePrefix --> consumed in production calling Feature
 *          (    'feature'? FeatureDeclaration
 *              | 'feature'
 *              | PrefixMetadataMember
 *          )
 *          ValuePart? TypeBody
 */
fun KerML.Feature() {
    val feature = FeatureActions<Feature>(semantics, ::FeatureImplementation, mutableListOf("Base::Anything"))
    FEATURE.optional()
    FeatureDeclaration(feature)
    optional(valuePartStart) {
        ValuePart(feature)
    }
    TypeBody(Resolved(feature.created!!))
    feature.finish()
}


/**
 *      FeatureDeclaration  =
 *          ( 'all' )?
 *          (   FeatureIdentification ( FeatureSpecializationPart | ConjugationPart )?
 *              | FeatureSpecializationPart
 *              | ConjugationPart
 *          )
 *          FeatureRelationshipPart*
 */
fun KerML.FeatureDeclaration(feature: FeatureActions<Feature>) {
    ALL.optional { feature.isSufficient = true }
    alternatives {
        NAME_LIT starts {
            Identification().also { feature.create(it) }
            alternatives {
                featureSpecializationPartStart starts { FeatureSpecializationPart(feature) }
                CONJUGATION starts { ConjugationPart() }
                others {  }
            }
        }
        featureSpecializationPartStart starts { FeatureSpecializationPart(feature) }
        CONJUGATION starts { ConjugationPart() }
    }
    noOrMore(start = setOf(CHAINS, FEATURED, INVERSE, DISJOINT, UNIONS, DIFFERENCES, INTERSECTS)) {
        FeatureRelationshipPart()
    }

    // The Following is a non-standard extension / might be replaced later by the standard
    TypeConstraint().also { feature.addTypeConstraint(it) }
    UnitConstraint().also { feature.addUnitConstraint(it) }
    feature.addTyping(mutableListOf())
}

/**
 * Proprietary SysMD; might be dropped
 *
 *      '[' Unit ']'
 */
fun KerML.UnitConstraint(): String? {
    var unit: String? = null
    optional(IN or LCBRACE, consume = false) {
        IN.optional()
        LCBRACE.consume()
        Unit().also { unit = it }
        RCBRACE.consume()
    }
    return unit
}

/**
 *      FeatureSpecializationPart : Feature =
 *          FeatureSpecialization+ MultiplicityPart? FeatureSpecialization*
 *          | MultiplicityPart FeatureSpecialization*
 */
fun KerML.FeatureSpecializationPart(feature: FeatureActions<Feature>) {
    alternatives {
        featureSpecializationStart starts {
            oneOrMore(featureSpecializationStart) { FeatureSpecialization(feature)  }
            optional( {token.kind == LCBRACE && nextToken.kind == INTEGER_LIT} ) { MultiplicityPart().also { feature.addMultiplicity(it) } }
            noOrMore(featureSpecializationStart) { FeatureSpecialization(feature) }
        }
        LCBRACE then TIMES  starts {
            MultiplicityPart().also { feature.addMultiplicity(it) }
            noOrMore(featureSpecializationStart) { FeatureSpecialization(feature) }
        }
        LCBRACE then INTEGER_LIT starts {
            MultiplicityPart().also { feature.addMultiplicity(it) }
            noOrMore(featureSpecializationStart) { FeatureSpecialization(feature) }
        }
    }
}
val featureSpecializationPartStart get() = featureSpecializationStart + LCBRACE


/**
 *      MultiplicityPart = OwnedMultiplicity
 *                      |  OwnedMultiplicity? ( 'ordered' ( 'nonunique' )? | 'nonunique' ( 'ordered' )? )
 *
 * implemented by:
 *
 *      MultiplicityPart = OwnedMultiplicity? ( 'ordered' ( 'nonunique')? | 'nonunique' ('ordered')?))
 */
fun KerML.MultiplicityPart(): IntegerRange {
    val multiplicity = OwnedMultiplicity()
    alternatives {
        ORDERED starts { ORDERED.consume(); NONUNIQUE.optional() }
        NONUNIQUE starts { NONUNIQUE.consume(); ORDERED.optional() }
        others {  }
    }
    return multiplicity
}


/**
 * Parses an optional Multiplicity; if it is not present, the result is [1, 1]
 *
 *      Multiplicity :- ["[" (IntegerLiteral | "*") [".." ( IntegerLiteral | "*" ] ) "]"]
 */
fun KerML.OwnedMultiplicity(): IntegerRange {
    var multiplicity = IntegerRange(1, 1)
    optional(LCBRACE, consume = true) {
        parseIntegerRange().also { multiplicity = it }
        RCBRACE.consume()
    }
    return multiplicity
}

/**
 *      IntegerRange :- ConstInt [".." ConstInt]
 */
fun KerML.parseIntegerRange(): IntegerRange {
    val result = IntegerRange(IntegerRange.Integers)
    ConstInt().also {
        if (consumedToken.kind == TIMES) {
            result.min = -Long.MIN_VALUE; result.max = Long.MAX_VALUE
        } else {
            result.min = it; result.max = it
        }
    }
    optional(DOTDOT, consume = true) {
        ConstInt().also { result.max = it }
    }
    if (result.min > result.max)
        throw SyntaxError(this, message = "max of range must be larger or equal min")
    return result
}

/**
 *      FeatureSpecialization = Typings | Subsettings | References | Redefinitions
 */
fun KerML.FeatureSpecialization(feature: FeatureActions<Feature>) {
    alternatives {
        TYPED_BY then { QualifiedNameList().also { feature.addTyping(it) } }
        SUBSETS or DPGT then { QualifiedNameList().also { feature.addSubsetting(it) } }
        REFERENCES then { QualifiedName().also { feature.addReferences(it) } }
        REDEFINES then { QualifiedNameList().also { feature.addRedefinitions(it.first()) } }
    }
}
val featureSpecializationStart = setOf(TYPED_BY, SUBSETS, DPGT, REFERENCES, REDEFINES)

/**
 *      FeatureRelationshipPart =
 *          TypeRelationshipPart | ChainingPart | InvertingPart | TypeFeaturingPart
 */
fun KerML.FeatureRelationshipPart() {
    alternatives {
        DISJOINT or UNIONS or INTERSECTS or DIFFERENCES starts {
            TypeRelationshipPart()
        }
        CHAINS starts {
            CHAINS.consume()
        }
        INVERSE starts {
            INVERSE.consume()
        }
        FEATURED starts {
            FEATURED.consume()
            BY.consume()
        }
    }
}

/**
 * Proprietary; avoid use
 *
 *      TypeConstraint = ["(" (LiteralExpression [.. LiteralExpression])* | (true | false)* ")"]
 */
fun KerML.TypeConstraint(): MutableList<String> {
    val constraintSpec = mutableListOf<String>()
    optional (LBRACE, consume = true, noMatch=mutableListOf(null)) {
        alternatives {
            (MINUS or TIMES or INTEGER_LIT or FLOAT_LIT) starts  {
                noOrMore(start = {tokenIsNot(RBRACE)}) { //Multiple constraints for vector
                    val min = Number()
                    val max = optional(DOTDOT, noMatch = min) {
                        DOTDOT.consume()
                        Number()
                    }
                    constraintSpec.add("$min .. $max")
                    optional(COMMA, consume =true)
                }
                optional(RBRACE, consume =true)
            }
            (TRUE or FALSE) starts {
                while(!tokenIs(RBRACE)){
                    if(consumeIfTokenIs(TRUE))
                        constraintSpec.add("true")
                    else if(consumeIfTokenIs(FALSE))
                        constraintSpec.add("false")
                }
                optional(RBRACE, consume = true)
            }
            (STRING_LIT) starts {
                val string = token.string
                consume()
                constraintSpec.add(string)
                optional(RBRACE, consume = true)
            }
        }
    }!!
    return constraintSpec
}

/**
 *      TypeBody : Type = ';' | '{' TypeBodyElement* '}'
 *      TypeBodyElement : Type =
 *          NonFeatureMember
 *          | FeatureMember
 *          | AliasMember
 *          | Import
 */
fun KerML.TypeBody(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            noOrMore(stop = RCURBRACE){
                TypeBodyElement()
            }
            semantics.popOwner()
            RCURBRACE.consume()
        }
        SEMICOLON then { }
        DOT then { } // Iff Triple
    }
}

/**
 *      FeaturePrefix =
 *          ( FeatureDirection )?
 *          ( 'abstract' )?
 *          ( 'composite' | 'portion' )?
 *          ( 'readonly' )?
 *          ( 'derived' )?
 *          ( 'end' )?
 *          ( PrefixMetadataMember )*
 *
 *      FeatureDirection = 'in' | 'out' | 'inout'
 */
fun KerML.FeaturePrefix() {
    alternatives {
        IN        then  { semantics.prefixes.add(IN) }
        OUT       then  { semantics.prefixes.add(OUT) }
        INOUT     then  { semantics.prefixes.add(INOUT) }
        others          {  }
    }
    ABSTRACT.optional   { semantics.prefixes.add(ABSTRACT) }
    alternatives {
        COMPOSITE then  { semantics.prefixes.add(COMPOSITE) }
        PORTION   then  { semantics.prefixes.add(PORTION) }
        others          {  }
    }
    READONLY.optional   { semantics.prefixes.add(READONLY) }
    DERIVED.optional    { semantics.prefixes.add(DERIVED) }
    END.optional        { semantics.prefixes.add(END) }
}
val FEATURE_PREFIX_START = setOf(IN, OUT, INOUT, COMPOSITE, PORTION, READONLY, DERIVED, END)

/**
 *      TypeBodyElement =
 *          ownedRelationship += NonFeatureMember
 *          | ownedRelationship += FeatureMember
 *          | ownedRelationship += AliasMember
 *          | ownedRelationship += Import
 */
fun KerML.TypeBodyElement() {
    MemberPrefix()
    noOrMore(HASHTAG) { PrefixMetadataMember()}
    alternatives {
        nonFeatureElementStart starts       { NonFeatureElement() }
        FEATURE_PREFIX_START   starts       { FeatureElement() }
        featureElementStart starts          { FeatureElement() }
        ALIAS starts                        { AliasMember() }
        IMPORT starts                       { Import() }
    }
}
