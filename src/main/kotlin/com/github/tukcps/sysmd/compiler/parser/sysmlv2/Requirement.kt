@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementConstraintAction
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementDefinitionAction
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementUsageActions
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMembership

/**
 * From standard:
 *
 * SubjectMember: SubjectMembership = MemberPrefix SubjectUsage
 *
 * RequirementConstraintMember: RequirementConstraintMembership =
 *              MemberPrefix? RequirementKind
 *              ownedRelatedElement += RequirementConstraintUsage
 *
 * RequirementKind: RequirementConstraintMembership =
 *              'assume' { kind = 'assumption' }
 *              | 'require' { kind = 'requirement' }
 *
 * RequirementConstraintUsage: ConstraintUsage =
 *              ownedRelationship += OwnedReferenceSubsetting FeatureSpecializationPart? RequirementBody
 *              | (UsageExtensionKeyword* 'constraint' | UsageExtensionKeyword+)
 *                ConstraintUsageDeclaration CalculationBody
 */

/**
 *      RequirementDefinition = OccurrenceDefinitionPrefix
 *          'requirement' 'def' DefinitionDeclaration RequirementBody
 */
fun SysMLv2.RequirementDefinition()  = RequirementDefinitionAction(semantics).parse {
    REQUIREMENT.consume()
    DEF.consume()
    DefinitionDeclaration()
    RequirementBody()
}



/**
 * 8.2.2.20.2 Requirement Usages
 *
 *      RequirementUsage = OccurrenceUsagePrefix 'requirement'
 *          ConstraintUsageDeclaration RequirementBody
 */
fun SysMLv2.RequirementUsage() = RequirementUsageActions(semantics).parse {
    REQUIREMENT.consume()
    ConstraintUsageDeclaration()
    RequirementBody()
}


/**
 * The subject of a requirement
 *
 *      SubjectUsage = 'subject' UsageExtensionKeyword* Usage
 */
fun SysMLv2.SubjectUsage() = FeatureAction(semantics, ElementType.Feature).parse {
    SUBJECT.consume()
    Usage()
}

/**
 *      RequirementKind =
 *              'assume' { kind = 'assumption' }
 *              | 'require' { kind = 'requirement' }
 */
fun SysMLv2.RequirementKind() {
    when(token.kind) {
        ASSUME  -> ASSUME.consume().semantics {
            element.requirementConstraintMembershipKind = RequirementConstraintMembership.RequirementConstraintKind.ASSUME }
        REQUIRE -> REQUIRE.consume().semantics {
            element.requirementConstraintMembershipKind = RequirementConstraintMembership.RequirementConstraintKind.REQUIRE }
        else -> handleSyntaxError("Expecting 'assume' or 'require'")
    }
}

/**
 *      SatisfyRequirementUsage = OccurrenceUsagePrefix 'assert' ('not'?) 'satisfy'
 *          (OwnedReferenceSubsetting FeatureSpecializationPart? | 'requirement' UsageDeclaration)
 *          ValuePart?
 *          ('by' SatisfactionSubjectMember)?
 *          RequirementBody
 */
fun SysMLv2.SatisfyRequirementUsage() = RequirementUsageActions(semantics).parse {
    ASSERT.optional()
    NOT.optional()
    SATISFY.consume()
    when (token.kind) {
        REQUIREMENT -> {
            REQUIREMENT.consume()
            UsageDeclaration()
        }
        else -> {
            OwnedReferenceSubsetting()
            optional(featureSpecializationStart) { FeatureSpecializationPart() }
        }
    }
    optional(valuePartStart) { ValuePart() }
    optional(BY) {
        BY.consume()
        SatisfactionSubjectMember()
    }
    RequirementBody()
}

/**
 *      SatisfactionSubjectMember = SatisfactionParameter
 *      SatisfactionParameter = SatisfactionFeatureValue
 *      SatisfactionFeatureValue = SatisfactionReferenceExpression
 *      SatisfactionReferenceExpression = FeatureChainMember
 */
fun SysMLv2.SatisfactionSubjectMember() {
    FeatureChain()
}


/**
 *      RequirementConstraintMember: RequirementConstraintMembership =
 *              MemberPrefix? RequirementKind RequirementConstraintUsage
 *
 *      RequirementConstraintUsage =
 *              OwnedReferenceSubsetting FeatureSpecializationPart? RequirementBody
 *              | (UsageExtensionKeyword* 'constraint' | UsageExtensionKeyword+)
 *                ConstraintUsageDeclaration CalculationBody
 */
fun SysMLv2.RequirementConstraintMember() = RequirementConstraintAction(semantics).parse {
    RequirementKind()
    alternatives {
        NAME_LIT starts {
            OwnedReferenceSubsetting()
            optional(featureSpecializationPartStart) { FeatureSpecializationPart() }
            RequirementBody()
        }
        CONSTRAINT then {
            ConstraintUsageDeclaration()
            CalculationBody()
        }
    }
}


/**
 *      RequirementBody = ';' | '{' RequirementBodyItem* '}'
 */
fun SysMLv2.RequirementBody() {
    if (tokenIs(SEMICOLON))
        SEMICOLON.consume()
    else {
        LCURBRACE.consume()
        noOrMore(stop = RCURBRACE) {
            RequirementBodyItem()
        }
        RCURBRACE.consume()
    }
}

/**
 *     RequirementBodyItem: Type =
 *                DefinitionBodyItem
 *              | SubjectMember
 *              | RequirementConstraintMember
 *              | FramedConcernMember
 *              | RequirementVerificationMember
 *              | ActorMember
 *              | StakeholderMember
 */
fun SysMLv2.RequirementBodyItem() {
    when(token.kind) {
        SUBJECT         -> { SubjectUsage() }
        REQUIRE, ASSUME -> { RequirementConstraintMember() }
        FRAME           -> { Unsupported() }
        VERIFY          -> { RequirementVerificationMember() }
        ACTOR           -> { Unsupported() }
        STAKEHOLDER     -> { Unsupported() }
        else            -> { DefinitionBodyItem() }
    }
}

/**
 *      RequirementVerificationMember : RequirementVerificationMembership =
 *          MemberPrefix 'verify' { kind = 'requirement' }
 *          ownedRelatedElement += RequirementVerificationUsage
 */
fun SysMLv2.RequirementVerificationMember() {
    VERIFY.consume()
    RequirementVerificationUsage()
}

/**
 *      RequirementVerificationUsage : RequirementUsage =
 *          ownedRelationship += OwnedReferenceSubsetting FeatureSpecialization* RequirementBody
 *          | ( UsageExtensionKeyword* 'requirement' | UsageExtensionKeyword+ )
 *             ConstraintUsageDeclaration RequirementBody
 */
fun SysMLv2.RequirementVerificationUsage() {
    OwnedReferenceSubsetting()
    RequirementBody()
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
 *              | ( UsageExtensionKeyword* 'concern' | UsageExtensionKeyword+)
 *              CalculationUsageDeclaration CalculationBody
 *
 *      ActorMember = MemberPrefix ownedRelatedElement += ActorUsage
 *
 *      ActorUsage = 'actor' UsageExtensionKeyword* Usage
 *
 *      StakeholderMember: StakeholderMembership =
 *              MemberPrefix ownedRelatedElement += StakeholderUsage
 *
 *      StakeholderUsage = 'stakeholder' UsageExtensionKeyword* Usage
 */
