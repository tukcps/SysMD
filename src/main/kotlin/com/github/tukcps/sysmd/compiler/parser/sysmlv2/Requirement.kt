@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureSpecializationPart
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementConstraintMemberActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementConstraintUsageActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementUsageActions
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMember
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
    val subject = FeatureActions<Feature>(semantics, ::FeatureImplementation, mutableListOf("Base::Anything"))
    SUBJECT.consume()
    Usage(subject)
}

/**
 *      RequirementKind =
 *              'assume' { kind = 'assumption' }
 *              | 'require' { kind = 'requirement' }
 */
fun SysMLv2.RequirementKind(require: RequirementConstraintMemberActions) {
    when(token.kind) {
        ASSUME  -> ASSUME.consume().also  { require.kind = RequirementConstraintMember.Kind.ASSUME }
        REQUIRE -> REQUIRE.consume().also { require.kind = RequirementConstraintMember.Kind.REQUIRE }
        else -> handleSyntaxError("Expecting 'assume' or 'require'")
    }
}

/**
 *      RequirementConstraintMember : RequirementConstraintMembership =
 *              MemberPrefix? RequirementKind RequirementConstraintUsage
 *
 *      RequirementConstraintUsage =
 *              OwnedReferenceSubsetting FeatureSpecializationPart? RequirementBody
 *              | ( UsageExtensionKeyword* 'constraint' | UsageExtensionKeyword+ )
 *                ConstraintUsageDeclaration CalculationBody
 */
fun SysMLv2.RequirementConstraintMember() {
    val require = RequirementConstraintMemberActions(semantics)
    RequirementKind(require)
    alternatives {
        NAME_LIT starts {
            OwnedReferenceSubsetting()
            FeatureSpecializationPart(require as FeatureActions<Feature>)
            require.finish()
            RequirementBody(Resolved(require.created!!))
        }
        CONSTRAINT then {
            ConstraintUsageDeclaration(require as FeatureActions<Feature>)
            require.finish()
            CalculationBody(Resolved(require.created!!))
        }
    }
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
        REQUIRE or ASSUME starts { RequirementConstraintMember() }
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