@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.RequirementUsage
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*

/**
 * From standard:
 *
 * RequirementDefinition = OccurrenceDefinitionPrefix 'requirement' 'def'
 *                         DefinitionDeclaration RequirementBody
 *
 * RequirementBody : Type = ';' | '{' RequirementBodyItem* '}'
 *
 * RequirementBodyItem : Type =
 *                DefinitionBodyItem
 *              | ownedRelationship += SubjectMember
 *              | ownedRelationship += RequirementConstraintMember
 *              | ownedRelationship += FramedConcernMember
 *              | ownedRelationship += RequirementVerificationMember
 *              | ownedRelationship += ActorMember
 *              | ownedRelationship += StakeholderMember
 *
 * SubjectMember : SubjectMembership = MemberPrefix ownedRelatedElement += SubjectUsage
 * SubjectUsage : ReferenceUsage = 'subject' UsageExtensionKeyword* Usage
 *
 * RequirementConstraintMember : RequirementConstraintMembership =
 *              MemberPrefix? RequirementKind
 *              ownedRelatedElement += RequirementConstraintUsage
 *
 * RequirementKind : RequirementConstraintMembership =
 *              'assume' { kind = 'assumption' }
 *              | 'require' { kind = 'requirement' }
 *
 * RequirementConstraintUsage : ConstraintUsage =
 *              ownedRelationship += OwnedReferenceSubsetting FeatureSpecializationPart? RequirementBody
 *              | ( UsageExtensionKeyword* 'constraint' | UsageExtensionKeyword+ )
 *                ConstraintUsageDeclaration CalculationBody
 *
 * FramedConcernMember : FramedConcernMembership =
 *              MemberPrefix? 'frame'
 *              ownedRelatedElement += FramedConcernUsage
 *
 * FramedConcernUsage : ConcernUsage =
 *              ownedRelationship += OwnedReferenceSubsetting
 *              FeatureSpecializationPart? CalculationBody
 *              | ( UsageExtensionKeyword* 'concern' | UsageExtensionKeyword+ )
 *              CalculationUsageDeclaration CalculationBody
 *
 * ActorMember : ActorMembership = MemberPrefix ownedRelatedElement += ActorUsage

 * ActorUsage : PartUsage = 'actor' UsageExtensionKeyword* Usage
 *              StakeholderMember : StakeholderMembership =
 *              MemberPrefix ownedRelatedElement += StakeholderUsage
 *
 * StakeholderUsage : PartUsage = 'stakeholder' UsageExtensionKeyword* Usage
 */



/**
 * RequirementDefinition :- "requirement" "def" RequirementDefBlock
 */
fun KerML.RequirementDefinition() {
    val requirementDefinition = sysMLSemantics.RequirementDefinitionSemantics()
    REQUIREMENT.consume()
    DEF.consume()
    Identification().also { requirementDefinition.identification = it }
    optional(SPECIALIZES) {
        SPECIALIZES.consume()
        QualifiedName().also { requirementDefinition.specialization = it}
    }
    requirementDefinition.create()
    RequirementBody(Resolved<Type>(ref=requirementDefinition.created!!))
}


/**
 *
 */
fun KerML.RequirementUsage() {
    val requirement = sysMLSemantics.RequirementUsageSemantics()
    REQUIREMENT.consume()
    Identification()   .also { requirement.identification = it }
    optional(DP) {
        DP.consume()
        QualifiedName().also { requirement.className = it }
    }
    requirement.create()
    RequirementBody(Resolved<RequirementUsage>(requirement.created!!))
}


/**
 * The subject of a requirement
 * subject :- "subject" Identification ":" QualifiedName
 */
fun KerML.SubjectUsage() {
    val subject = semantics.featureActions()
    SUBJECT.consume()
    Identification().also { subject?.identification = it }
    optional(DP) {
        DP.consume()
        QualifiedName().also { subject?.type = mutableListOf(it) }
    }
    optional(REFERENCES) {
        REFERENCES.consume()
        QualifiedName().also { subject?.references = it }
    }
    SEMICOLON.consume()
    subject?.create()
}

/**
 * RequirementConstraintMember : RequirementConstraintMembership =
 *              MemberPrefix? RequirementKind
 *              ownedRelatedElement += RequirementConstraintUsage
 *
 * RequirementKind : RequirementConstraintMembership =
 *              'assume' { kind = 'assumption' }
 *              | 'require' { kind = 'requirement' }
 *
 * RequirementConstraintUsage : ConstraintUsage =
 *              ownedRelationship += OwnedReferenceSubsetting FeatureSpecializationPart? RequirementBody
 *              | ( UsageExtensionKeyword* 'constraint' | UsageExtensionKeyword+ )
 *                ConstraintUsageDeclaration CalculationBody
 */
fun KerML.RequirementConstraintUsage() {
    val require = sysMLSemantics.RequirementConstraintUsageSemantics()
    alternatives {
        REQUIRE starts { REQUIRE.consume(); require.kindRequire=true
            require.types = mutableListOf("Requirements::RequirementUsage") }
        ASSUME starts  { ASSUME.consume(); require.kindRequire=false
            require.creator = { InvariantImplementation()}
            require.types = mutableListOf("Requirements::SatisfyRequirementUsage") }
    }
    Identification().also { require?.identification = it; require?.create() }
    alternatives {
        // Calculation body
        LCURBRACE starts  {
            LCURBRACE.consume() // TODO: Body
            val iBeforeExpression = token.indices.first
            Expression().also {
                require.created?.featureWithValue = AstRoot(model, require.created!!, it)
                require.created?.indices = iBeforeExpression .. consumedToken.indices.last
                require.created?.expression = input.subSequence(require.created!!.indices!!).toString().trim()
            }
            RCURBRACE.consume()
        }
        others {
            val iBeforeExpression = token.indices.first
            Expression().also {
                require.created?.featureWithValue = AstRoot(model, require.created!!, it)
                require.created?.indices = iBeforeExpression .. consumedToken.indices.last
                require.created?.expression = input.subSequence(require.created?.indices!!).toString().trim()
            }
            SEMICOLON.consume()
        }
    }
}

/**
 * RequireBlock :- "{" ("Expression" ";")* "}"
 */
fun KerML.RequireBlock() {
    LCURBRACE.consume()
    noOrMore(stop = RCURBRACE) {
        Expression()
    }
    RCURBRACE.consume()
}


/**
 * RequirementBody : Type = ';' | '{' RequirementBodyItem* '}'
 */
fun KerML.RequirementBody(owner: Resolved<Element>) {
    if (tokenIs(SEMICOLON))
        SEMICOLON.consume()
    else {
        LCURBRACE.consume()
        semantics.pushOwner(owner)
        noOrMore(stop = RCURBRACE) {
            RequirementBodyItem()
        }
        semantics.popOwner()
        RCURBRACE.consume()
    }
}

/**
 * RequirementBodyItem : Type =
 *                DefinitionBodyItem
 *              | ownedRelationship += SubjectMember
 *              | ownedRelationship += RequirementConstraintMember
 *              | ownedRelationship += FramedConcernMember
 *              | ownedRelationship += RequirementVerificationMember
 *              | ownedRelationship += ActorMember
 *              | ownedRelationship += StakeholderMember
 */
fun KerML.RequirementBodyItem() {
    noOrMore(stop = RCURBRACE) {
        alternatives {
            SUBJECT starts { SubjectUsage() }
            REQUIRE or ASSUME starts { RequirementConstraintUsage() }
            // others { DefinitionBodyItem() }
            others { NamespaceBodyElement() }
        }
    }
}