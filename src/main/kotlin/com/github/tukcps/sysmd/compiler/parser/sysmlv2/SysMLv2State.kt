@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.sysml.implementation.TransitionUsageImplementation
import java.util.UUID



/**
 * 8.2.2.17.2 State Usages
 *
 *      StateUsage = OccurrenceUsagePrefix 'state'
 *          ActionUsageDeclaration StateUsageBody
 *          StateUsageBody : StateUsage =
 *          ';'
 *          | ( isParallel ?= 'parallel' )?
 *          '{' StateBodyItem* '}'
 *
 *      ExhibitStateUsage = OccurrenceUsagePrefix 'exhibit'
 *          ( OwnedReferenceSubsetting FeatureSpecializationPart?
 *          | 'state' UsageDeclaration )
 *      ValuePart? StateUsageBody
 */
fun SysMLv2.StateUsage() {
    val stateUsage = sysMLSemantics.StateUsageSemantics()
    STATE.consume()
    UsageDeclaration(stateUsage as FeatureActions<Feature>)
    StateUsageBody(Resolved(stateUsage.created!!))
    stateUsage.finish()
}

fun SysMLv2.StateDefinition() {
    val stateDefinition = TypeActions<Type>(semantics, ::ClassImplementation)
    STATE.consume()
    DEF.consume()
    DefinitionDeclaration(stateDefinition)
    DefinitionBody(Resolved(stateDefinition.created!!))
    stateDefinition.finish()
}

/* Moved to Action.kt
fun SysMLv2.Action() {
}
*/


/**
 *      TransitionUsage = 'transition' ( UsageDeclaration 'first' )?
 *          FeatureChainMember
 *          EmptyParameterMember
 *          ( EmptyParameterMember TriggerActionMember )?
 *          ( GuardExpressionMember )?
 *          ( EffectBehaviorMember )?
 *          'then' TransitionSuccessionMember
 *          ActionBody
 */
fun SysMLv2.TransitionUsage() {
    val transitionUsage = sysMLSemantics.TransitionUsageSemantics()

    TRANSITION.consume()
    @Suppress("UNCHECKED_CAST")
    UsageDeclaration(transitionUsage as FeatureActions<Feature>)

    // We need to push the owner since we're creating owned features, but without the usual curly braces of the body
    if (transitionUsage.created!!.declaredName == null && transitionUsage.created!!.declaredShortName == null)
        transitionUsage.created!!.declaredName = "trans_" + UUID.randomUUID().toString()
    transitionUsage.finish()

    semantics.pushOwner(Resolved(transitionUsage.created!!))

    // Process the source of the connection
    FIRST.consume()
    val succession = sysMLSemantics.SuccessionAsUsageSemantics()
    QualifiedName().also { succession.source += it }

    optional(start=ACCEPT, consume = true) {
        // We need to push the owner since we're creating owned features, but without the usual curly braces of the body
        val acceptActionUsage = sysMLSemantics.AcceptActionUsageSemantics()
        acceptActionUsage.create()
        semantics.pushOwner(Resolved(ref=acceptActionUsage.created!!))

        val triggerPayloadParameter =
            sysMLSemantics.PayloadParameterSemantics() //typeName = QualifiedName())

        triggerPayloadParameter.create(Identification(name="payload"))
        QualifiedName().also { triggerPayloadParameter.addTyping(mutableListOf(it)) }

        triggerPayloadParameter.created.also {
            acceptActionUsage.payloadParameter = it as ReferenceUsage
        }
        acceptActionUsage.finish()

        // We're done with creating owned features, so we need to pop the owner (i.e. the transition) again
        semantics.popOwner()
    }

    val guardCondition = sysMLSemantics.GuardConditionSemantics()
    optional(start=IF, consume = true) {
        val iBeforeExpression = token.indices.first
        Expression().also {
            guardCondition.create(Identification("guard_" + UUID.randomUUID().toString()))
            guardCondition.created?.featureWithValue = AstRoot(model, guardCondition.created!!, it)
            guardCondition.created?.indices = iBeforeExpression..consumedToken.indices.last
            guardCondition.created?.expression = input.subSequence(guardCondition.created?.indices!!).toString().trim()
            // print("Set guard condition: " + guardCondition.created?.expression)
        }
    }

    // Process the target of the connection & create the connection
    THEN.consume()
    QualifiedName().also { succession.target += it }
    succession.create()

    // We're done with creating owned features, so we need to pop the owner (i.e. the transition) again
    SEMICOLON.consume()
    semantics.popOwner()

    // Finally, we can create the transition
    if(guardCondition.created != null) {
        (transitionUsage.created as TransitionUsageImplementation).guardCondition = Resolved(guardCondition.created!!)
    }
    transitionUsage.finish()
}
