@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.services.reportInfo

/**
 * 8.2.4.1.1 Types
 * Type =
 * TypePrefix 'type'
 * TypeDeclaration TypeBody
 *
 * TypePrefix : Type =
 * ( isAbstract ?= 'abstract' )?
 * ( ownedRelationship += PrefixMetadataMember )*
 *
 * TypeDeclaration : Type =
 *  ( isSufficient ?= 'all' )? Identification
 *  ( ownedRelationship += OwnedMultiplicity )?
 *  ( SpecializationPart | ConjugationPart )+
 *  TypeRelationshipPart*
 *
 * SpecializationPart : Type =
 *  SPECIALIZES ownedRelationship += OwnedSpecialization
 *  ( ',' ownedRelationship += OwnedSpecialization )*
 *
 * ConjugationPart : Type =
 * CONJUGATES ownedRelationship += OwnedConjugation
 *
 * TypeRelationshipPart : Type =
 * DisjoiningPart
 * | UnioningPart
 * | IntersectingPart
 * | DifferencingPart
 * DisjoiningPart : Type =
 * 'disjoint' 'from' ownedRelationship += OwnedDisjoining
 * ( ',' ownedRelationship += OwnedDisjoining )*
 * UnioningPart : Type =
 * 'unions' ownedRelationship += Unioning
 * ( ',' ownedRelationship += Unioning )*
 * IntersectingPart : Type =
 * 'intersects' ownedRelationship += Intersecting
 * ( ',' ownedRelationship += Intersecting )*
 * DifferencingPart : Type =
 * 'differences' ownedRelationship += Differencing
 * ( ',' ownedRelationship += Differencing )*
 * TypeBody : Type =
 * ';' | '{' TypeBodyElement* '}'
 * TypeBodyElement : Type =
 * ownedRelationship += NonFeatureMember
 * | ownedRelationship += FeatureMember
 * | ownedRelationship += AliasMember
 * | ownedRelationship += Import
 */
/**
 * Type :- "type" Identification [ :> QualifiedName] Body
 */
fun KerML.Type() {
    val semantics = semantics.typeActions()
    TYPE.consume()
    Identification().also { semantics?.identification = it }
    oneOrMore(start = setOf(SPECIALIZES, CONJUGATION)) {
        alternatives {
            SPECIALIZES starts { SpecializationPart().also { semantics?.general?.addAll(it) }}
            CONJUGATION starts { ConjugationPart()}
        }
    }
    semantics?.create()
    Body(Resolved(semantics?.created!!))
}


/*
 * SpecializationPart : Type =
 * SPECIALIZES ownedRelationship += OwnedSpecialization
 * ( ',' ownedRelationship += OwnedSpecialization )* // WE USE + instead
 *
 */
fun KerML.SpecializationPart(): List<String> {
    val result = mutableListOf<String>()
    SPECIALIZES.consume()
    QualifiedName().also        { result.add(it) }
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also    { result.add(it) }
    }
    return result
}

fun KerML.ConjugationPart(): List<String> {
    val result = mutableListOf<String>()
    CONJUGATION.consume()
    noOrMore(start = COMMA) {
        COMMA.consume()
        QualifiedName().also { result.add(it) }
    }
    model.reportInfo(model.global, "Not yet supported: Conjugation")
    return result
}


/**
 * 8.2.4.2.1 Classifiers
 * Classifier =
 *      TypePrefix 'classifier'
 *      ClassifierDeclaration TypeBody
 *
 * ClassifierDeclaration : Classifier =
 *      ( isSufficient ?= 'all' )? Identification
 *      ( ownedRelationship += OwnedMultiplicity )?
 *      ( SuperclassingPart | ConjugationPart )?
 *      TypeRelationshipPart*
 */
data class ClassifierDeclarationInfo(
    var all: Boolean = false,
    var identification: Identification = Identification(),
    val superclassingPart: MutableList<String> = mutableListOf(),
    val conjugationPart: MutableList<String> = mutableListOf(),
)

fun KerML.ClassifierDeclaration(): ClassifierDeclarationInfo {
    val classifierDeclaration = ClassifierDeclarationInfo()
    ALL.optional            { classifierDeclaration.all = true }
    Identification().also   { classifierDeclaration.identification = it }
    alternatives {
        SPECIALIZES starts { SpecializationPart().also { classifierDeclaration.superclassingPart.addAll(it) } }
        CONJUGATION starts { ConjugationPart() }
        others {  }  // Just skip
    }
    return classifierDeclaration
}

/**
 * DataType :- "datatype" Identification ["specializes" QualifiedName] Body
 */
fun KerML.Datatype() {
    val semantics = semantics.datatypeActions()
    DATATYPE.consume()
    ClassifierDeclaration().also    { semantics?.classifierDeclaration = it; semantics?.create() }
    semantics?.create()
    Body(Resolved(semantics?.created!!))
}


/**
 * Class :- "class"  Identification [ :> QualifiedName] Body
 */
fun KerML.Class() {
    val semantics = semantics.classActions()
    CLASS.consume()
    ClassifierDeclaration().also    { semantics?.classifierDeclaration = it; semantics?.create() }
    Body(Resolved(semantics?.created!!))
}


/**
 * _8.2.4.1.2 Specialization_
 * Specialization = ( 'specialization' Identification )?
 * 'subtype' SpecificType SPECIALIZES GeneralType
 * RelationshipBody
 * OwnedSpecialization : Specialization =
 * GeneralType
 * SpecificType : Specialization :
 * specific = [QualifiedName]
 * | specific += OwnedFeatureChain
 * { ownedRelatedElement += specific }
 * GeneralType : Specialization =
 * general = [QualifiedName]
 * | general += OwnedFeatureChain
 * { ownedRelatedElement += general }
 */


/**
 * _8.2.4.1.3 Conjugation_
 * Conjugation =
 * ( 'conjugation' Identification )?
 * 'conjugate'
 * ( conjugatedType = [QualifiedName]
 * | conjugatedType = FeatureChain
 * { ownedRelatedElement += conjugatedType }
 * )
 * CONJUGATES
 * ( originalType = [QualifiedName]
 * | originalType = FeatureChain
 * { ownedRelatedElement += originalType }
 * )
 * RelationshipBody
 * OwnedConjugation : Conjugation =
 * originalType = [QualifiedName]
 * | originalType = FeatureChain
 * { ownedRelatedElement += originalType }
 */

/**
 * _8.2.4.1.4 Disjoining_
 * Disjoining =
 * ( 'disjoining' Identification )?
 * 'disjoint'
 * ( typeDisjoined = [QualifiedName]
 * | typeDisjoined = FeatureChain
 * { ownedRelatedElement += typeDisjoined }
 * )
 * 'from'
 * ( disjoiningType = [QualifiedName]
 * | disjoiningType = FeatureChain
 * { ownedRelatedElement += disjoiningType }
 * )
 * RelationshipBody
 * OwnedDisjoining : Disjoining =
 * disjoiningType = [QualifiedName]
 */

/**
 * Feature =
 *     FeaturePrefix
 *     ( 'feature'? FeatureDeclaration
 *     | 'feature'
 *     | ownedRelationship += PrefixMetadataMember
 *     )
 *     ValuePart? TypeBody
 * (See Note 1)
 *
 */
fun KerML.Feature() {
    val feature = semantics.featureActions()
    FEATURE.optional()  // DIRTY!!!
    EXPR.optional()     // DIRTY!!!
    Identification().also { feature?.identification = it }

    // Type, Redefinition, ... in arbitrary order; not clean.
    FeatureType(feature)
    FeatureType(feature)
    FeatureType(feature)

    // For SysMLV2, multiplicity is after the type:
    optional(LCBRACE) {
        Multiplicity().also { feature?.multiplicity = it }
    }
    feature?.create()
    optional(EQ, consume =true) {
        val iBeforeExpression = token.indices.first
        feature?.created?.variable = if (feature!=null) VariableImplementation(feature.created!!) else null
        semantics.expression = feature?.created
        Expression().also {
            feature?.created?.variable?.ast = AstRoot(model, semantics.expression!!, it)
            feature?.created?.indices = iBeforeExpression..consumedToken.indices.last
            feature?.created?.expression = input.subSequence(feature?.created?.indices!!).toString().trim()
        }
    }
    TypeBody(Resolved(feature?.created!!))
}



fun KerML.FeatureType(feature: FeatureActions?) {
    alternatives {
        DP starts {
            DP.consume()
            ALL.optional().also { if (consumedToken.kind == ALL) feature?.isSufficient = true }
            QualifiedNameList().also { feature?.type = it }
            TypeConstraint().also { feature?.typeConstraint = it }
            optional(start = LCBRACE) {
                if (nextToken.kind == INTEGER_LIT)
                    Multiplicity().also { feature?.multiplicity = it }
            }
            optional(start = LCBRACE) {
                LCBRACE.consume()
                Unit().also { feature?.unitConstraint = it }
                RCBRACE.consume()
            }
        }
        TYPED starts {
            TYPED.consume()
            BY.consume()
            ALL.optional().also { if (consumedToken.kind == ALL) feature?.isSufficient = true }
            QualifiedNameList().also { feature?.type = it }
            TypeConstraint().also { feature?.typeConstraint = it }
            optional(start = LCBRACE) {
                if (nextToken.kind == INTEGER_LIT)
                    Multiplicity().also { feature?.multiplicity = it }
            }
            optional(start = LCBRACE) {
                LCBRACE.consume()
                Unit().also { feature?.unitConstraint = it }
                RCBRACE.consume()
            }
        }
        REFERENCES starts {
            REFERENCES.consume()
            QualifiedName().also { feature?.references = it }
        }
        REDEFINES starts {
            REDEFINES.consume()
            QualifiedName().also { feature?.redefines = it }
        }
        others {  }
    }
}



/**
 * TypeConstraint :- ["(" (LiteralExpression [.. LiteralExpression])* | (true | false)* ")"]
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
 * TypeBodyElement : Type =
 *       ownedRelationship += NonFeatureMember
 *     | ownedRelationship += FeatureMember
 *     | ownedRelationship += AliasMember
 *     | ownedRelationship += Import
 */
fun KerML.TypeBodyElement() {
    MemberPrefix()
    alternatives {
        NON_FEATURE_ELEMENT_TOKENS starts   { NonFeatureElement() }
        FEATURE_ELEMENT_TOKENS starts       { FeatureElement() }
        ALIAS starts                        { AliasMember() }
        IMPORT starts                       { Import() }
    }
}