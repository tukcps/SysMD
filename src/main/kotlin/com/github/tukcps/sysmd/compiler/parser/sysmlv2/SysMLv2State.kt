@file:Suppress("FunctionName", "UNCHECKED_CAST", "GrazieInspection")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureChain
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.*
import com.github.tukcps.sysmd.model.datamodel.elementByName
import com.github.tukcps.sysmd.model.generated.ElementType


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
fun SysMLv2.StateUsage() = StateUsageAction(semantics).parse {
    STATE.consume()
    UsageDeclaration()
    StateUsageBody()
}

fun SysMLv2.StateDefinition() = TypeAction(semantics, ElementType.StateDefinition).parse {
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
fun SysMLv2.TransitionUsage() = TransitionUsageAction(semantics).parse {

    TRANSITION.consume()

    if(usageDeclarationStarts() || token.kind == FIRST) {
        UsageDeclaration()
        FIRST.consume()
    }

    val source = FeatureChain()

    // EmptyParameterMember()
    optional(ACCEPT) { TriggerActionMember() }
    optional(IF) { GuardExpression()}
    THEN.consume()

    TransitionSuccession(source)

    ActionBody()
}


fun SysMLv2.TransitionSuccession(source: String?=null) = SuccessionAsUsageAction(semantics).parse {
    QualifiedName().semantics { setTarget(elementByName(it)); if (source != null) setSource(elementByName(source)) }
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
    setIdentification(Identification(name = "payload"))
    QualifiedName().also { semantics.addTyping(it) }
    // other option not implemented ...
}

fun SysMLv2.TriggerValuePart() {

}

fun SysMLv2.TriggerActionMember() = AcceptActionUsageAction(semantics).parse {
    ACCEPT.consume()
    AcceptParameterPart()
}

