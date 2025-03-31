@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
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
    alternatives {
        SEMICOLON then { }  // EmptyActionUsage
        statePerformActionUsageStart starts { StatePerformActionUsage() }
        others {
            alternatives {
                ACCEPT then {

                }
                SEND then {

                }
            }
        }
    }
}


/**
 *      StatePerformActionUsage = PerformActionUsageDeclaration ActionBody
 *      PerformActionUsageDeclaration =
 *          ( OwnedReferenceSubsetting FeatureSpecializationPart? | 'action' UsageDeclaration )
 *          ValuePart?
 */
fun SysMLv2.StatePerformActionUsage() {
    val statePerformActionUsage = FeatureActions<StateUsage>(semantics, ::StateUsageImplementation, mutableListOf("States::StateAction"))
    PerformActionUsageDeclaration(statePerformActionUsage as FeatureActions<Feature>)
    ActionBody(Resolved(statePerformActionUsage.created!!))
    statePerformActionUsage.finish()
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
fun SysMLv2.StateDefBody(owner: Resolved<Type>){
    when {
        SEMICOLON.then() -> {}
        // PARALLEL.then() -> {}
        LCURBRACE.starts() -> {
            LCURBRACE.consume()
            semantics.pushOwner(owner)
            noOrMore(end = {token.kind == RCURBRACE}) {
                StateBodyItem()
            }
            semantics.popOwner()
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
        behaviorUsageElementStart.starts()  -> BehaviorUsageElement()
        TRANSITION.starts()        -> TransitionUsage()
        ENTRY.starts()             -> EntryActionMember()
        DO.starts()                -> DoActionMember()
        EXIT.starts()              -> ExitActionMember()
        else -> throwSyntaxError("Expected a valid state body item.")
    }
}


/**
 *      StateUsageBody =
 *            ';'
 *          | ( 'parallel' )? '{' StateBodyItem* '}'
 *
 */
fun SysMLv2.StateUsageBody(owner: Resolved<Type>) {
    when {
        SEMICOLON.then() -> {}
        // Parallel ...
        LCURBRACE.starts() -> {
            LCURBRACE.consume()
            semantics.pushOwner(owner)
            noOrMore(end = {token.kind == RCURBRACE}) {
                StateBodyItem()
            }
            semantics.popOwner()
            RCURBRACE.consume()
        }
    }
}