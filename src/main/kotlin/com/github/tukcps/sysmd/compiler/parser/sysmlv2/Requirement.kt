@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureSpecializationPart
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedReferenceSubsetting
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementConstraintMemberActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.RequirementUsageActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMember

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
fun SysMLv2.RequirementDefinition()  = RequirementDefinitionActions(semantics).parse {
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
 *
 *      SatisfyRequirementUsage = OccurrenceUsagePrefix 'assert' ('not'?) 'satisfy'
 *          (OwnedReferenceSubsetting FeatureSpecializationPart?
 *              | 'requirement' UsageDeclaration)
 *          ValuePart?
 *          ('by' ownedRelationship += SatisfactionSubjectMember)?
 *          RequirementBody
 *
 *      SatisfactionSubjectMember = SatisfactionParameter
 *      SatisfactionParameter = SatisfactionFeatureValue
 *      SatisfactionFeatureValue = SatisfactionReferenceExpression
 *      SatisfactionReferenceExpression = FeatureChainMember
 */
fun SysMLv2.RequirementUsage() = RequirementUsageActions(semantics).parse {
    REQUIREMENT.consume()
    Identification()   .also { semantics.create(it) }
    optional(TYPED_BY) {
        TYPED_BY.consume()
        QualifiedName().also { semantics.addTyping(it) }
        noOrMore(COMMA) {
            COMMA.consume()
            QualifiedName().also { semantics.addTyping(it) }
        }
    }
    RequirementBody()
}


/**
 * The subject of a requirement
 *
 *      SubjectUsage = 'subject' UsageExtensionKeyword* Usage
 */
fun SysMLv2.SubjectUsage() = FeatureActions<Feature>(semantics, ::FeatureImplementation).parse {
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
        ASSUME  -> ASSUME.consume().also  { semantics.element<RequirementConstraintMember>().kind = RequirementConstraintMember.Kind.ASSUME }
        REQUIRE -> REQUIRE.consume().also { semantics.element<RequirementConstraintMember>().kind = RequirementConstraintMember.Kind.REQUIRE }
        else -> handleSyntaxError("Expecting 'assume' or 'require'")
    }
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
fun SysMLv2.RequirementConstraintMember() = RequirementConstraintMemberActions(semantics).parse {
    RequirementKind()
    alternatives {
        NAME_LIT starts {
            OwnedReferenceSubsetting()
            FeatureSpecializationPart()
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