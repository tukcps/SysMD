@file:Suppress("FunctionName", "UNCHECKED_CAST", "GrazieInspection")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.*
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.SuccessionAsUsage
import com.github.tukcps.sysmd.model.sysml.implementation.SuccessionAsUsageImplementation
import java.util.*


/**
 * 8.2.2.17.2 State Usages
 *
 *      StateUsage = OccurrenceUsagePrefix 'state'
 *          ActionUsageDeclaration StateUsageBody
 *
 *      StateUsageBody =
 *            ';'
 *          | ( isParallel ?= 'parallel' )?
 *          '{' StateBodyItem* '}'
 *
 *      ExhibitStateUsage = OccurrenceUsagePrefix 'exhibit'
 *          ( OwnedReferenceSubsetting FeatureSpecializationPart?
 *          | 'state' UsageDeclaration )
 *      ValuePart? StateUsageBody
 */
fun SysMLv2.StateUsage() = StateUsageActions(semantics).parse {
    STATE.consume()
    UsageDeclaration()
    StateUsageBody()
}

fun SysMLv2.StateDefinition() = TypeActions<Type>(semantics, ::ClassImplementation).parse {
    STATE.consume()
    DEF.consume()
    DefinitionDeclaration()
    StateDefBody()
}


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
fun SysMLv2.TransitionUsage() = TransitionUsageActions(semantics).parse {

    TRANSITION.consume()

    if(usageDeclarationStarts() || token.kind == FIRST) {
        UsageDeclaration()
        FIRST.consume()
    } else semantics.create(Identification("trans_"+UUID.randomUUID().toString()))

    // FeatureChainMember()
    val succession = SuccessionAsUsageSemantics<SuccessionAsUsage>(semantics, ::SuccessionAsUsageImplementation)
    succession.parse {
        semantics.create(Identification("succ_" + UUID.randomUUID().toString()))
        QualifiedName().also { semantics.setSourceEnd(unresolvedFeature(it)) }
    }

    // EmptyParameterMember()
    optional(ACCEPT) { TriggerActionMember() }
    optional(IF) { GuardExpressionMember()}
    THEN.consume()
    succession.parse {
        TransitionSuccessionMember()
    }
    ActionBody()
}


fun SysMLv2.TransitionSuccessionMember() { // = SuccessionAsUsageSemantics(semantics, ::SuccessionAsUsageImplementation).parse {
    QualifiedName().also {
        semantics.create(null)
        semantics.setTargetEnd(unresolvedFeature(it))
    }
}



/**
 *      PayloadParameterMember: ParameterMembership =
 *              ownedRelatedElement += PayloadParameter
 *      PayloadParameter: ReferenceUsage =
 *              PayloadFeature
 *              | Identification PayloadFeatureSpecializationPart? TriggerValuePart
 */
fun SysMLv2.PayloadParameter() = PayloadParameterActions(semantics).parse { //typeName = QualifiedName())
    // PayloadFeature only
    semantics.create(Identification(name = "payload"))
    QualifiedName().also { semantics.addTyping(it) }
    // other option not implemented ...
}

fun SysMLv2.TriggerValuePart() {

}


fun SysMLv2.TriggerActionMember() = AcceptActionUsageActions(semantics).parse {
    semantics.create(null)
    ACCEPT.consume()
    AcceptParameterPart()
}

