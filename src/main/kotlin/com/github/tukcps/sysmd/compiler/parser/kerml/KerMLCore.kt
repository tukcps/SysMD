@file:Suppress("FunctionName", "GrazieInspection")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.legacy.ConstInt
import com.github.tukcps.sysmd.compiler.parser.kerml.legacy.Number
import com.github.tukcps.sysmd.compiler.parser.kerml.legacy.Unit
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.model.datamodel.IdentificationKind
import com.github.tukcps.sysmd.model.datamodel.IdentifiedByName
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.IntegerRange
import com.github.tukcps.sysmd.model.util.MultiplicityRange
import com.github.tukcps.sysmd.model.util.TypeConstraint
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified

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
fun KerML.SpecializationPart() {
    SPECIALIZES()
    QualifiedName().semantics       { addSpecialization(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().semantics   { addSpecialization(it) }
    }
}

fun KerML.SuperclassingPart() {
    SPECIALIZES()
    QualifiedName().semantics       { addSubclassification(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().semantics   { addSubclassification(it) }
    }
}

/**
 *      ConjugationPart = 'conjugates' ownedRelationship += OwnedConjugation
 */
fun KerML.ConjugationPart() {
    CONJUGATES.consume()
    QualifiedName().semantics       { addConjugation(it) }
}

/**
 *      TypePrefix = ('abstract')? (PrefixMetadataMember)*
 */
fun KerML.TypePrefix() {
    if (ABSTRACT in semantics.prefixes)
        semantics.element.isAbstract = true
}

/**
 * 8.2.4.1.1 Types
 *
 *      Type = TypePrefix 'type' TypeDeclaration TypeBody
 */
fun KerML.Type() = TypeAction(semantics, ElementType.Type).parse {
    TypePrefix()
    TYPE.consume()
    TypeDeclaration()
    TypeBody()
}

/**
 *      TypeDeclaration = ( 'all' )? Identification (OwnedMultiplicity)?
 *           ( SpecializationPart | ConjugationPart )+
 *          TypeRelationshipPart*
 */
fun KerML.TypeDeclaration() {
    ALL.optional                { semantics.element.isSufficient = true }
    Identification().semantics  { action.setIdentification(it) }
    optional(LCBRACE) {
        OwnedMultiplicity()
    }
    oneOrMore(SPECIALIZES or DPGT or CONJUGATES) {
        alternatives {
            SPECIALIZES or DPGT starts { SpecializationPart() }
            CONJUGATES starts { ConjugationPart() }
        }
    }
    noOrMore(DISJOINT or UNIONS or INTERSECTS or DIFFERENCES) {
        TypeRelationshipPart()
    }
}

/**
 *      TypeRelationshipPart =
 *          DisjoiningPart | UnioningPart | IntersectingPart | DifferencingPart
 */
fun KerML.TypeRelationshipPart() {
    alternatives {
        DISJOINT starts     { DisjoiningPart() }
        UNIONS starts       { UnioningPart() }
        INTERSECTS starts   { IntersectingPart() }
        DIFFERENCES starts  { DifferencingPart() }
    }
}

/**
 *      DisjoiningPart = 'disjoint' 'from' OwnedDisjoining ( ',' OwnedDisjoining )*
 */
fun KerML.DisjoiningPart() {
    DISJOINT.consume()
    FROM.consume()
    QualifiedName().semantics       { addDisjoining(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().semantics   { addDisjoining(it) }
    }
}

/**
 *      UnioningPart = 'unions' Unioning ( ',' Unioning )*
 */
fun KerML.UnioningPart() {
    UNIONS.consume()
    QualifiedName().semantics     { addUnioning(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().semantics { addUnioning(it) }
    }
}

/**
 *      IntersectingPart = 'intersects' Intersecting( ',' ownedRelationship += Intersecting )*
 */
fun KerML.IntersectingPart() {
    INTERSECTS.consume()
    QualifiedName().semantics     { addIntersecting(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().semantics { addIntersecting(it) }
    }
}

/**
 *      DifferencingPart : Type = 'differences' Differencing( ',' ownedRelationship += Differencing )*
 */
fun KerML.DifferencingPart() {
    DIFFERENCES.consume()
    QualifiedName().semantics     { addDifferencing(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().semantics { addDifferencing(it) }
    }
}

/**
 * 8.2.4.2.1 Classifiers
 *
 *      Classifier = TypePrefix 'classifier' ClassifierDeclaration TypeBody
 */
fun KerML.Classifier() = TypeAction(this.semantics, ElementType.Classifier).parse {
    TypePrefix()
    CLASSIFIER.consume()
    ClassifierDeclaration()
    TypeBody()
}

/**
 *      ClassifierDeclaration =
 *          ( 'all' )?
 *          Identification
 *          ( OwnedMultiplicity )?
 *          ( SuperclassingPart | ConjugationPart )?
 *          TypeRelationshipPart*
 */
fun KerML.ClassifierDeclaration() {
    ALL.optional                    { semantics.element.isSufficient = true }
    Identification().semantics      { action.setIdentification(it) }
    optional(LCBRACE) {
        OwnedMultiplicity()
    }
    alternatives {
        SPECIALIZES or DPGT starts  { SuperclassingPart() }
        CONJUGATES starts           { ConjugationPart() }
        others                      {  }
    }
    noOrMore(DISJOINT or UNIONS or INTERSECTS or DIFFERENCES) {
        TypeRelationshipPart()
    }
}

/**
 *      DataType :- "datatype" ClassifierDeclaration TypeBody
 */
fun KerML.Datatype() = TypeAction(semantics, ElementType.DataType, isImplicit = "Base::DataValue").parse {
    DATATYPE.consume()
    ClassifierDeclaration()
    TypeBody()
}

/**
 *  8.2.5.2 Classes Concrete Syntax
 *
 *      Class = TypePrefix 'class' ClassifierDeclaration TypeBody
 *          Note: Calling production handles prefixes before.
 */
fun KerML.Class() = ClassAction(semantics).parse {
    TypePrefix()
    CLASS.consume()
    ClassifierDeclaration()
    TypeBody()
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
    RelationshipBody()
}

fun KerML.SpecificType() =
    when(nextToken.kind) {
        DOT ->  FeatureChain()
        else -> QualifiedName()
    }

fun KerML.GeneralType(): Identified =
    when(nextToken.kind) {
        DOT ->  FeatureChain().let { return IdentifiedByName(name=it, IdentificationKind.FeatureChain) }
        else -> QualifiedName().let { return IdentifiedByName(name=it, IdentificationKind.Feature) }
    }

/**
 * TODO: Move FeatureChain into Expressions properly
 */
fun KerML.FeatureChain(): String {
    val result: StringBuilder = StringBuilder()
    NAME_LIT.consume()          .also { result.append(consumedToken.string) }
    noOrMore(DOT) {
        DOT.consume()           .also { result.append(".") }
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
fun KerML.Conjugation() = OwnedRelationshipAction(semantics, ElementType.Conjugation).parse {
    if (token.kind ==CONJUGATION) {
        CONJUGATION.consume()
        Identification()    .semantics { setIdentification(it) }
    }
    CONJUGATE.consume()
    QualifiedName()         .semantics { setSource(IdentifiedByName(it, IdentificationKind.Type)) }
    CONJUGATES.consume()
    QualifiedName()         .semantics { setTarget(IdentifiedByName(it, IdentificationKind.Type)) }
    RelationshipBody()
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
    RelationshipBody()
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
fun KerML.Feature(membershipOverride : ElementType? = null) = FeatureAction(
    semantics, owningMembershipType = membershipOverride ?: ElementType.FeatureMembership
).parse {
    FEATURE.optional()
    FeatureDeclaration()
    optional(valuePartStart) {
        ValuePart()
    }
    TypeBody()
}

/**
 * 8.2.5.7.2 Expressions
 *
 *      Expression =   // FeaturePrefix --> consumed in production calling Feature
 *          'expr' FeatureDeclaration ValuePart?
 *          FunctionBody
 */
fun KerML.ExpressionFeature() = FeatureAction(semantics, ElementType.Feature).parse {
    EXPR.consume()
    FeatureDeclaration()
    optional(valuePartStart) {
        ValuePart()
    }
    FunctionBody()
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
fun KerML.FeatureDeclaration() {
    ALL.optional { semantics.element.isSufficient = true }
    alternatives {
        NAME_LIT or LT starts {
            Identification().also { semantics.action.setIdentification(it) }
            alternatives {
                featureSpecializationPartStart starts { FeatureSpecializationPart() }
                CONJUGATION starts { ConjugationPart() }
                others {  } // Optional
            }
        }
        featureSpecializationPartStart starts { FeatureSpecializationPart() }
        CONJUGATION starts { ConjugationPart() }
        // ? It seems that in contradiction to grammar, features without identification are allowed ... ?
        others { }
    }
    noOrMore(start = setOf(CHAINS, FEATURED, INVERSE, DISJOINT, UNIONS, DIFFERENCES, INTERSECTS)) {
        FeatureRelationshipPart()
    }

    // The Following is a non-standard extension / might be replaced later by the standard
    TypeConstraint().semantics { addTypeConstraint(it) }
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
fun KerML.FeatureSpecializationPart() {
    alternatives {
        featureSpecializationStart starts {
            oneOrMore(featureSpecializationStart) { FeatureSpecialization()  }
            optional( {token.kind == LCBRACE && nextToken.kind == INTEGER_LIT} ) {
                MultiplicityPart()
            }
            noOrMore(featureSpecializationStart) { FeatureSpecialization() }
        }
        LCBRACE then TIMES  starts {
            MultiplicityPart()
            noOrMore(featureSpecializationStart) { FeatureSpecialization() }
        }
        LCBRACE then INTEGER_LIT starts {
            MultiplicityPart()
            noOrMore(featureSpecializationStart) { FeatureSpecialization() }
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
fun KerML.MultiplicityPart() {
    OwnedMultiplicity()
    alternatives {
        ORDERED starts {
            ORDERED.consume         { semantics.element.isOrdered = true }
            NONUNIQUE.optional      { semantics.element.isUnique = false }
        }
        NONUNIQUE starts {
            NONUNIQUE.consume       { semantics.element.isUnique = false }
            ORDERED.optional        { semantics.element.isOrdered = true }
        }
        others {  }
    }
}

/**
 * Parses an optional Multiplicity; if it is not present, the result is [1, 1]
 *
 *      Multiplicity :- ["[" (IntegerLiteral | "*") [".." ( IntegerLiteral | "*" ] ) "]"]
 */
fun KerML.OwnedMultiplicity() = MultiplicityAction(semantics).parse {
    optional(LCBRACE, consume = true) {
        parseMultiplicityRange().semantics { (action as MultiplicityAction).typeConstraint = TypeConstraint(it.toString()) }
        RCBRACE.consume               { (semantics.action as TypeAction).multiplicityAdded = true}
    }
}

/**
 *      MultiplicityRange :- ConstInt [".." ConstInt]
 */
fun KerML.parseMultiplicityRange(): MultiplicityRange {
    val result = MultiplicityRange(0L, null)
    ConstInt().also {
        if (consumedToken.kind == TIMES) {
            result.min = 0
        } else {
            result.min = it; result.max = it
        }
    }
    optional(DOTDOT, consume = true) {
        ConstInt().also {
            if (consumedToken.kind == TIMES) {
                result.max = null
            } else
                result.max = it
        }
    }
    if (result.min > (result.max ?: Long.MAX_VALUE))
        throw SyntaxError(this, message = "max of range must be larger or equal min")
    return result
}

/**
 *      IntegerRange :- ConstInt [".." ConstInt]
 */
fun KerML.parseIntegerRange(): IntegerRange {
    val result = IntegerRange(null, null)
    ConstInt().also {
        if (consumedToken.kind == TIMES) {
            result.min = null; result.max = null
        } else {
            result.min = it; result.max = it
        }
    }
    optional(DOTDOT, consume = true) {
        ConstInt().also { result.max = it }
    }
    if ((result.min ?: Long.MIN_VALUE) > (result.max ?: Long.MAX_VALUE))
        throw SyntaxError(this, message = "max of range must be larger or equal min")
    return result
}

/**
 *      FeatureSpecialization = Typings | Subsettings | References | Redefinitions | Crosses
 */
fun KerML.FeatureSpecialization() {
    alternatives {
        TYPED_BY then {

            // In SysML v2, there is additionally port conjugation!
            if (semantics.compiler is SysMLv2 && token.kind == NOT) { // must be tilde; dirty.
                NOT.consume()
            }

            QualifiedName().also { semantics.addTyping(it) }

            noOrMore(COMMA) {
                COMMA.consume()
                QualifiedName().also { semantics.addTyping(it) }
            }
        }
        SUBSETS or DPGT then {
            QualifiedName().also { semantics.addSubsetting(it) }
            noOrMore(COMMA) {
                COMMA.consume()
                QualifiedName().also { semantics.addSubsetting(it) }
            }
        }
        CROSSES then { Unsupported("Crossing not supported yet") }
        REFERENCES then { QualifiedName().also { semantics.addReferences(it) } }
        REDEFINES then { QualifiedNameList().also { semantics.addRedefinitions(it) } }
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
fun KerML.TypeConstraint(): TypeConstraint {
    val value = mutableListOf<String>()
    var unit = ""

    if (tokenIs(LBRACE) &&
        nextToken.kind in setOf(MINUS, TIMES, INTEGER_LIT, FLOAT_LIT, STRING_LIT, TRUE, FALSE)) {
        LBRACE.consume()
        alternatives {
            (MINUS or TIMES or INTEGER_LIT or FLOAT_LIT) starts  {
                var min: String? = null
                var max: String? = null
                Number()                    .also { min = it  }
                optional(DOTDOT, noMatch = min) {
                    DOTDOT.consume()
                    Number()                .also { max = it  }
                }
                value.add("$min .. ${max?:min}")

                noOrMore(start = COMMA) { // Multiple constraints for vector
                    COMMA.consume             { min = null; max = null }
                    Number()                  .also { min = it  }
                    optional(DOTDOT, noMatch = min) {
                        DOTDOT.consume()
                        Number()              .also { max = it  }
                    }
                    value.add("$min .. ${max?:min}")
                }
            }
            (TRUE or FALSE) starts {
                while(!tokenIs(RBRACE)){
                    if(consumeIfTokenIs(TRUE))
                        value.add("true")
                    else if(consumeIfTokenIs(FALSE))
                        value.add("false")
                }
            }
            (STRING_LIT) starts {
                consume { value.add(token.string) }
            }
        }
        UnitConstraint().also { unit = it?:"" }
        RBRACE.consume()
    }
    return TypeConstraint(value, unit)
}

/**
 *      TypeBody : Type = ';' | '{' TypeBodyElement* '}'
 *      TypeBodyElement : Type =
 *          NonFeatureMember
 *          | FeatureMember
 *          | AliasMember
 *          | Import
 */
fun KerML.TypeBody() {
	semantics.prefixes.clear()
    when(token.kind) {
        LCURBRACE -> {
            LCURBRACE.consume()
            noOrMore(stop = RCURBRACE){
                TypeBodyElement()
            }
            RCURBRACE.consume()
        }
        SEMICOLON -> { SEMICOLON.consume() }
        DOT -> { DOT.consume() }                // Iff Triple
        else -> { }
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
    CONST.optional   { semantics.prefixes.add(CONST) }
    DERIVED.optional    { semantics.prefixes.add(DERIVED) }
    END.optional        { semantics.prefixes.add(END) }
}
val FeaturePrefixStart = setOf(IN, OUT, INOUT, COMPOSITE, PORTION, CONST, DERIVED, END)

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
        FeaturePrefixStart starts           { FeatureElement() }
        featureElementStart starts          { FeatureElement() }
        ALIAS starts                        { AliasMember() }
        IMPORT starts                       { Import() }
        others                              { handleSyntaxError("Invalid TypeBodyElement") }
    }
}