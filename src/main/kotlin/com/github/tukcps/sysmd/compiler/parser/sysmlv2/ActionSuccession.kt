@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureChain
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation

/**
 * 8.2.2.17.8 Action Successions
 *
 *      ActionTargetSuccession : Usage =
 *          ( TargetSuccession | GuardedTargetSuccession | DefaultTargetSuccession ) UsageBody
 */
fun SysMLv2.ActionTargetSuccession() {
    TargetSuccession()
    UsageBody(Resolved(null, null, null))
}

/**
 *      TargetSuccession : SuccessionAsUsage =
 *          SourceEndMember 'then' ConnectorEndMember
 */
fun SysMLv2.TargetSuccession() {
    // SourceEndMember t.b.d.
    THEN.consume()
    ConnectorEndMember()
}

/**
 *      GuardedTargetSuccession = GuardExpressionMember 'then' TransitionSuccessionMember
 */

/**
 *      DefaultTargetSuccession = 'else' TransitionSuccessionMember
 */

/**
 *      GuardedSuccession : TransitionUsage =
 *          ( 'succession' UsageDeclaration )?
 *          'first' ownedRelationship += FeatureChainMember
 *          ownedRelationship += GuardExpressionMember
 *          'then' ownedRelationship += TransitionSuccessionMember
 *          UsageBody
 */
fun SysMLv2.GuardedSuccession() {
    val guardedSuccession = FeatureActions<Feature>(this.semantics, ::FeatureImplementation, mutableListOf("Base::Anything"))
    optional(SUCCESSION) {
        SUCCESSION.consume()
        UsageDeclaration(guardedSuccession)
    }
    guardedSuccession.finish()
    FIRST.consume()
    FeatureChain()
    GuardExpressionMember()
    THEN.consume()
    FeatureChain()
    UsageBody(Resolved(guardedSuccession.created!!))
}
fun SysMLv2.guardedSuccessionStarts(): Boolean =
    token.kind == SUCCESSION || (token.kind == FIRST && nextToken.kind == IF)


fun SysMLv2.GuardExpressionMember() {
    IF.consume()
    Expression()
}