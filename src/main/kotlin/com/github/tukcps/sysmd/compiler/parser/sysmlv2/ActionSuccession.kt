package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.model.kerml.Resolved

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