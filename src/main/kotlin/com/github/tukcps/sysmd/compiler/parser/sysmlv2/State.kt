@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.model.sysml.implementation.StateUsageImplementation


/**
 *      StateActionUsage = EmptyActionUsage ';'
 *          | StatePerformActionUsage
 *          | StateAcceptActionUsage
 *          | StateSendActionUsage
 *          | StateAssignmentActionUsage
 */
fun SysMLv2.StateActionUsage() {
    when(token.kind) {
        SEMICOLON                       -> { SEMICOLON.consume() }  // EmptyActionUsage
        in statePerformActionUsageStart -> { StatePerformActionUsage() }
        else                            -> {
            when(token.kind) {
                ACCEPT  -> { ACCEPT.consume() }
                SEND    -> { SEND.consume() }
                else    -> {}
            }
        }
    }
}


/**
 *      StatePerformActionUsage = PerformActionUsageDeclaration ActionBody
 *      PerformActionUsageDeclaration =
 *          (OwnedReferenceSubsetting FeatureSpecializationPart? | 'action' UsageDeclaration)
 *          ValuePart?
 */
fun SysMLv2.StatePerformActionUsage() = FeatureActions<StateUsage>(semantics, ::StateUsageImplementation, "States::StateAction").parse {
    PerformActionUsageDeclaration()
    ActionBody()
}
val statePerformActionUsageStart = performActionUsageDeclarationStart

/**
 * StateAcceptActionUsage = AcceptNodeDeclaration ActionBody
 */
fun SysMLv2.StateAcceptActionUsage() {

}
val stateAcceptActionUsageStart = performActionUsageDeclarationStart

/**
 * StateSendActionUsage : SendActionUsage
 * SendNodeDeclaration ActionBody
 * StateAssignmentActionUsage : AssignmentActionUsage =
 * AssignmentNodeDeclaration ActionBody
 * TransitionUsageMember : FeatureMembership =
 * MemberPrefix ownedRelatedElement += TransitionUsage
 * 174 Systems Modeling Language v2.0 Beta 2.1
 * TargetTransitionUsageMember : FeatureMembership =
 * MemberPrefix ownedRelatedElement += TargetTransitionUsage
 *
 */


/**
 * StateAcceptActionUsage = AcceptNodeDeclaration ActionBody
 */


/**
 *      EntryActionMember = MemberPrefix 'entry' StateActionUsage
 */
fun SysMLv2.EntryActionMember() {
    ENTRY.consume()
    StateActionUsage()
}

/**
 *      DoActionMember = MemberPrefix 'do' StateActionUsage
 */
fun SysMLv2.DoActionMember() {
    DO.consume()
    StateActionUsage()
}

/**
 *      ExitActionMember = MemberPrefix 'exit' StateActionUsage
 */
fun SysMLv2.ExitActionMember() {
    EXIT.consume()
    StateActionUsage()
}

/**
 *      StateDefBody =
 *          ';'
 *          | ('parallel')? '{' StateBodyItem* '}'
 */
fun SysMLv2.StateDefBody(){
    when {
        SEMICOLON.then() -> {}
        LCURBRACE.starts() || PARALLEL.starts() -> {
            PARALLEL.optional()
            LCURBRACE.consume()
            noOrMore(end = {token.kind == RCURBRACE}) {
                StateBodyItem()
            }
            RCURBRACE.consume()
        }
    }
}

/**
 *      StateBodyItem =
 *          NonBehaviorBodyItem
 *          | ( SourceSuccessionMember )? BehaviorUsageMember
 *              ( TargetTransitionUsageMember )*
 *          | TransitionUsageMember
 *          | EntryActionMember ( EntryTransitionMember )*
 *          | DoActionMember
 *          | ExitActionMember
 */
fun SysMLv2.StateBodyItem() {
    when {
        nonBehaviorBodyItemStart() -> NonBehaviorBodyItem()
        behaviorUsageElementStart.starts() -> {
            THEN.optional { SourceSuccessionMember() }
            BehaviorUsageElement()
            // TargetTransitionUsageMember()
        }
        TRANSITION.starts()        -> TransitionUsage()
        ENTRY.starts()             -> { EntryActionMember(); noOrMore(THEN) { EntryTransitionMember() } }
        DO.starts()                -> DoActionMember()
        EXIT.starts()              -> ExitActionMember()
        else -> throwSyntaxError("At ${token}: Expected a valid state body item.")
    }
}

/**
 *      TargetTransitionUsage : TransitionUsage = ownedRelationship += EmptyParameterMember
 *      ( 'transition'
 *              ( ownedRelationship += EmptyParameterMember
 *              ownedRelationship += TriggerActionMember )?
 *              ( ownedRelationship += GuardExpressionMember )?
 *              ( ownedRelationship += EffectBehaviorMember )?
 *          | ownedRelationship += EmptyParameterMember
 *              ownedRelationship += TriggerActionMember
 *              ( ownedRelationship += GuardExpressionMember )?
 *              ( ownedRelationship += EffectBehaviorMember )?
 *          | ownedRelationship += GuardExpressionMember
 *              ( ownedRelationship += EffectBehaviorMember )?
 *      )?
 *      'then' ownedRelationship += TransitionSuccessionMember
 *      ActionBody
 */
fun SysMLv2.TargetTransitionUsageMember() {
    THEN.consume()
    TransitionSuccessionMember()
    ActionBody()
}


/**
 *      EntryTransitionMember = MemberPrefix
 *          ( GuardedTargetSuccession | 'then' TargetSuccession ) ';'
 */
fun SysMLv2.EntryTransitionMember() {
    // THEN.consume()
    TargetSuccession()
    SEMICOLON.consume()
}


/**
 *      StateUsageBody =
 *            ';'
 *          | ('parallel')? '{' StateBodyItem* '}'
 *
 */
fun SysMLv2.StateUsageBody() {
    when {
        SEMICOLON.then() -> {}
        LCURBRACE.starts() -> {
            PARALLEL.optional()
            LCURBRACE.consume()
            noOrMore(end = {token.kind == RCURBRACE}) {
                StateBodyItem()
            }
            RCURBRACE.consume()
        }
    }
}