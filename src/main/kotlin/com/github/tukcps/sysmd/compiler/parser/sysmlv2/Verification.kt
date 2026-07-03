package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.MemberPrefix
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.VerificationCaseDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.VerificationCaseUsageActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.sysml.implementation.CaseUsageImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.VerificationCaseDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.VerificationCaseUsageImplementation


/**
 *      VerificationCaseDefinition = OccurrenceDefinitionPrefix 'verification' 'def'
 *              DefinitionDeclaration CaseBody
 */
fun SysMLv2.VerificationCaseDefinition() = VerificationCaseDefinitionActions(context = semantics, creator = ::VerificationCaseDefinitionImplementation).parse {
    VERIFICATION.consume()
    DEF.consume()
    DefinitionDeclaration()
    CaseBody()
}

/**
 *      VerificationCaseUsage = OccurrenceUsagePrefix 'verification'
 *              ConstraintUsageDeclaration CaseBody
 */
fun SysMLv2.VerificationCaseUsage() = VerificationCaseUsageActions(context = semantics, creator = ::VerificationCaseUsageImplementation).parse {
    VERIFICATION.consume()
    ConstraintUsageDeclaration()
    CaseBody()
}

/**
 *      CaseBody : Type = ';'
 *          | '{' CaseBodyItem*
 *             ( ownedRelationship += ResultExpressionMember )?
 *            '}'
 */
fun SysMLv2.CaseBody() {
    MemberPrefix()
    when {
        SEMICOLON.starts() -> SEMICOLON.consume()
        LCURBRACE.starts() -> {
            LCURBRACE.consume()
            noOrMore( end ={ ! CaseBodyItemStarts() } ) {  CaseBodyItem() }
            optional( matchingCondition = { token.kind != RCURBRACE} ) { ResultExpressionMember()}
            RCURBRACE.consume()
        }
    }
}


/**
 *      CaseBodyItem : Type = ActionBodyItem
 *          | ownedRelationship += SubjectMember
 *          | ownedRelationship += ActorMember
 *          | ownedRelationship += ObjectiveMember
 */
fun SysMLv2.CaseBodyItem() {
    MemberPrefix()
    when {
        actionBodyItemStarts() -> ActionBodyItem()
        SUBJECT.starts()       -> SubjectMember()
        ACTOR.starts()         -> ActorMember()
        OBJECTIVE.starts()     -> ObjectiveMember()
        else -> throwSyntaxError("Unknown case body item ${token.kind}")
    }
}
fun SysMLv2.CaseBodyItemStarts(): Boolean = actionBodyItemStarts() ||
        token.kind in setOf(SUBJECT, ACTOR, OBJECTIVE) ||
        nextToken.kind in setOf(SUBJECT, ACTOR, OBJECTIVE)

/**
 *      ObjectiveMember : ObjectiveMembership =
 *          MemberPrefix 'objective'
 *          ownedRelatedElement += ObjectiveRequirementUsage
 */
fun SysMLv2.ObjectiveMember() = FeatureActions<CaseUsageImplementation>(semantics, ::CaseUsageImplementation).parse {
    OBJECTIVE.consume()
    ObjectiveRequirementUsage()
}

/**
 *      ObjectiveRequirementUsage : RequirementUsage =
 *          UsageExtensionKeyword* ConstraintUsageDeclaration
 *          RequirementBody
 */
fun SysMLv2.ObjectiveRequirementUsage() {
    ConstraintUsageDeclaration()
    RequirementBody()
}

fun SysMLv2.SubjectMember() {
    SubjectUsage()
}

fun SysMLv2.ActorMember() {
    Unsupported("ActorMember not yet implemented")
}