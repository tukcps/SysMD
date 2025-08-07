@file:JvmName("Strings")
@file:Suppress("FunctionName", "GrazieInspection")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName
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
fun KerML.SpecializationPart() {
    SPECIALIZES()
    QualifiedName().also        { semantics.addSpecialization(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also    { semantics.addSpecialization(it) }
    }
}

fun KerML.SuperclassingPart() {
    SPECIALIZES()
    QualifiedName().also        { semantics.addSubclassification(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also    { semantics.addSubclassification(it) }
    }
}

/**
 *      ConjugationPart = 'conjugates' ownedRelationship += OwnedConjugation
 */
fun KerML.ConjugationPart() {
    CONJUGATES.consume()
    QualifiedName().also    { semantics.addConjugation(it) }
}


/**
 *      TypePrefix = ('abstract')? (PrefixMetadataMember)*
 */
fun KerML.TypePrefix() {
    if (ABSTRACT in semantics.prefixes)
        semantics.element<Type>().isAbstract = true
}

/**
 * 8.2.4.1.1 Types
 *
 *      Type = TypePrefix 'type' TypeDeclaration TypeBody
 */
fun KerML.Type() = TypeActions<Type>(semantics, creator = ::TypeImplementation).parse {
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
    ALL.optional            { semantics.element<Type>().isSufficient = true }
    Identification().also   { semantics.create(it) }
    OwnedMultiplicity().also { if (it != IntegerRange(1, 1)) semantics.addMultiplicity(it) }
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
    QualifiedName().also { semantics.addDisjoining(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { semantics.addDisjoining(it) }
    }
}

/**
 *      UnioningPart = 'unions' Unioning ( ',' Unioning )*
 */
fun KerML.UnioningPart() {
    UNIONS.consume()
    QualifiedName().also     { semantics.addUnioning(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { semantics.addUnioning(it) }
    }
}

/**
 *      IntersectingPart = 'intersects' Intersecting( ',' ownedRelationship += Intersecting )*
 */
fun KerML.IntersectingPart() {
    INTERSECTS.consume()
    QualifiedName().also     { Unsupported("Intersection not yet implemented") }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { Unsupported("Interaection not yet implemented") }
    }
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
fun KerML.Classifier() = ClassifierActions<Classifier>(this.semantics, creator =  ::ClassifierImplementation).parse {
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
    ALL.optional             { semantics.element<Classifier>().isSufficient = true }
    Identification().also    { semantics.create(it) }
    OwnedMultiplicity().also { if (it != IntegerRange(1,1) ) semantics.addMultiplicity(it)}
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
fun KerML.Datatype() = DataTypeActions<DataType>(semantics, ::DataTypeImplementation).parse {
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
fun KerML.Class() = ClassActions(semantics, ::ClassImplementation).parse {
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

fun KerML.GeneralType(): Feature =
    when(nextToken.kind) {
        DOT ->  FeatureChain().let { return UnresolvedFeatureChain(it) }
        else -> QualifiedName().let { return UnresolvedFeature(it) }
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
fun KerML.Conjugation() {
    val conjugation = RelationshipActionsImpl<Conjugation>(semantics, ::ConjugationImplementation)
    if (token.kind ==CONJUGATION) {
        CONJUGATION.consume()
        Identification().also { conjugation.create(it) }
    } else conjugation.create(null)
    CONJUGATE.consume()
    QualifiedName().also { conjugation.created.conjugatedType = UnresolvedType(it) }
    CONJUGATES.consume()
    QualifiedName().also { conjugation.created.originalType = UnresolvedType(it) }
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
fun KerML.Feature() = FeatureActions<Feature>(semantics, ::FeatureImplementation).parse {
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
fun KerML.ExpressionFeature() = FeatureActions<Feature>(semantics, ::FeatureImplementation).parse {
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
    ALL.optional { semantics.element<Feature>().isSufficient = true }
    alternatives {
        NAME_LIT starts {
            Identification().also { semantics.create(it) }
            alternatives {
                featureSpecializationPartStart starts { FeatureSpecializationPart() }
                CONJUGATION starts { ConjugationPart() }
                others {  }
            }
        }
        featureSpecializationPartStart starts { semantics.create(null); FeatureSpecializationPart() }
        CONJUGATION starts { semantics.create(null); ConjugationPart() }
    }
    noOrMore(start = setOf(CHAINS, FEATURED, INVERSE, DISJOINT, UNIONS, DIFFERENCES, INTERSECTS)) {
        FeatureRelationshipPart()
    }

    // The Following is a non-standard extension / might be replaced later by the standard
    TypeConstraint().also { semantics.addTypeConstraint(it) }
    UnitConstraint().also { semantics.addUnitConstraint(it) }
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
            optional( {token.kind == LCBRACE && nextToken.kind == INTEGER_LIT} ) { MultiplicityPart().also { semantics.addMultiplicity(it) } }
            noOrMore(featureSpecializationStart) { FeatureSpecialization() }
        }
        LCBRACE then TIMES  starts {
            MultiplicityPart().also { semantics.addMultiplicity(it) }
            noOrMore(featureSpecializationStart) { FeatureSpecialization() }
        }
        LCBRACE then INTEGER_LIT starts {
            MultiplicityPart().also { semantics.addMultiplicity(it) }
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
fun KerML.FeatureSpecialization() {
    alternatives {
        TYPED_BY then {
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
fun KerML.TypeConstraint(): MutableList<String> {
    val constraintSpec = mutableListOf<String>()
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
                constraintSpec.add("$min .. ${max?:min}")

                noOrMore(start = COMMA) { //Multiple constraints for vector
                    COMMA.consume()
                    var min: String? = null
                    var max: String? = null
                    Number()                    .also { min = it  }
                    optional(DOTDOT, noMatch = min) {
                        DOTDOT.consume()
                        Number()                .also { max = it  }
                    }
                    constraintSpec.add("$min .. ${max?:min}")
                }
            }
            (TRUE or FALSE) starts {
                while(!tokenIs(RBRACE)){
                    if(consumeIfTokenIs(TRUE))
                        constraintSpec.add("true")
                    else if(consumeIfTokenIs(FALSE))
                        constraintSpec.add("false")
                }
            }
            (STRING_LIT) starts {
                val string = token.string
                consume()
                constraintSpec.add(string)
            }
        }
        RBRACE.consume()
    }
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
fun KerML.TypeBody() {
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
        others                              { handleSyntaxError("Invalid TypeBodyElement") }
    }
}