@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementAssumeUsageActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementConstraintUsageActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementUsageActions
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.RequirementUsage

/**
 * From standard:
 *
 *
 * SubjectMember : SubjectMembership = MemberPrefix SubjectUsage
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
 */

/**
 *      RequirementDefinition = OccurrenceDefinitionPrefix
 *          'requirement' 'def' DefinitionDeclaration RequirementBody
 */
fun SysMLv2.RequirementDefinition() {
    val requirementDefinition = RequirementDefinitionActions(semantics)
    REQUIREMENT.consume()
    DEF.consume()
    DefinitionDeclaration(requirementDefinition as TypeActions<Type>)
    RequirementBody(Resolved<Type>(ref=requirementDefinition.created!!))
    requirementDefinition.finish()
}



/**
 * 8.2.2.20.2 Requirement Usages
 *
 *      RequirementUsage = OccurrenceUsagePrefix 'requirement'
 *          ConstraintUsageDeclaration RequirementBody
 *
 *      SatisfyRequirementUsage = OccurrenceUsagePrefix 'assert' ( 'not'? ) 'satisfy'
 *          ( OwnedReferenceSubsetting FeatureSpecializationPart?
 *              | 'requirement' UsageDeclaration )
 *          ValuePart?
 *          ( 'by' ownedRelationship += SatisfactionSubjectMember )?
 *          RequirementBody
 *
 *      SatisfactionSubjectMember = SatisfactionParameter
 *      SatisfactionParameter = SatisfactionFeatureValue
 *      SatisfactionFeatureValue  = SatisfactionReferenceExpression
 *      SatisfactionReferenceExpression  = FeatureChainMember
 */
fun SysMLv2.RequirementUsage() {
    val requirement = RequirementUsageActions(semantics)
    REQUIREMENT.consume()
    Identification()   .also { requirement.create(it) }
    optional(TYPED_BY) {
        TYPED_BY.consume()
        QualifiedNameList().also { requirement.addTyping(it) }
    }
    RequirementBody(Resolved<RequirementUsage>(requirement.created as RequirementUsage))
    requirement.finish()
}


/**
 * The subject of a requirement
 *
 *      SubjectUsage = 'subject' UsageExtensionKeyword* Usage
 */
fun SysMLv2.SubjectUsage() {
    val subject = semantics.featureActions()
    SUBJECT.consume()
    Usage(subject as FeatureActions<Feature>)
}

/**
 *      RequirementConstraintMember : RequirementConstraintMembership =
 *              MemberPrefix? RequirementKind
 *              ownedRelatedElement += RequirementConstraintUsage
 *
 *      RequirementKind : RequirementConstraintMembership =
 *              'assume' { kind = 'assumption' }
 *              | 'require' { kind = 'requirement' }
 *
 *      RequirementConstraintUsage =
 *              OwnedReferenceSubsetting FeatureSpecializationPart? RequirementBody
 *              | ( UsageExtensionKeyword* 'constraint' | UsageExtensionKeyword+ )
 *                ConstraintUsageDeclaration CalculationBody
 */
fun SysMLv2.RequirementConstraintUsage() {
    var require : FeatureActions<Feature> = RequirementConstraintUsageActions(semantics)
    alternatives {
        REQUIRE then {
            require = RequirementConstraintUsageActions(semantics)
            Identification().also { require.create(it) }
        }
        ASSUME then  {
            require = RequirementAssumeUsageActions(semantics)
            Identification().also { require.create(it) }
            require.addTypeConstraint(mutableListOf("true"))
        }
    }
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
    require.finish()
}

/**
 *      RequireBlock :- "{" ("Expression" ";")* "}"
 */
fun SysMLv2.RequireBlock() {
    LCURBRACE.consume()
    noOrMore(stop = RCURBRACE) {
        Expression()
    }
    RCURBRACE.consume()
}


/**
 *      RequirementBody = ';' | '{' RequirementBodyItem* '}'
 */
fun SysMLv2.RequirementBody(owner: Resolved<Element>) {
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
 *     RequirementBodyItem : Type =
 *                DefinitionBodyItem
 *              | SubjectMember
 *              | RequirementConstraintMember
 *              | FramedConcernMember
 *              | RequirementVerificationMember
 *              | ActorMember
 *              | StakeholderMember
 */
fun SysMLv2.RequirementBodyItem() {
    alternatives {
        SUBJECT starts { SubjectUsage() }
        REQUIRE or ASSUME starts { RequirementConstraintUsage() }
        FRAME starts { Unsupported() }
        ACTOR starts { Unsupported() }
        STAKEHOLDER starts { Unsupported() }
        others { DefinitionBodyItem() }
    }
}

/**
 * The following productions are not supported yet:
 *
 *      FramedConcernMember =
 *              MemberPrefix? 'frame'
 *              ownedRelatedElement += FramedConcernUsage
 *
 *      FramedConcernUsage =
 *              ownedRelationship += OwnedReferenceSubsetting
 *              FeatureSpecializationPart? CalculationBody
 *              | ( UsageExtensionKeyword* 'concern' | UsageExtensionKeyword+ )
 *              CalculationUsageDeclaration CalculationBody
 *
 *      ActorMember = MemberPrefix ownedRelatedElement += ActorUsage
 *
 *      ActorUsage = 'actor' UsageExtensionKeyword* Usage
 *              StakeholderMember : StakeholderMembership =
 *              MemberPrefix ownedRelatedElement += StakeholderUsage
 *
 *      StakeholderUsage = 'stakeholder' UsageExtensionKeyword* Usage
 */