@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.ConnectorEnd
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureChain
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedExpression
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.model.generated.ElementType

/**
 * 8.2.2.17.8 Action Successions
 *
 *      ActionTargetSuccession: Usage =
 *          (TargetSuccession | GuardedTargetSuccession | DefaultTargetSuccession) UsageBody
 */
fun SysMLv2.ActionTargetSuccession() {
    TargetSuccession()
    UsageBody()
}

/**
 *      TargetSuccession: SuccessionAsUsage =
 *          SourceEndMember 'then' ConnectorEndMember
 */
fun SysMLv2.TargetSuccession() {
    // SourceEndMember t.b.d.
    THEN.consume()
    ConnectorEnd()
}

/**
 *      GuardedTargetSuccession = GuardExpressionMember 'then' TransitionSuccessionMember
 */

/**
 *      DefaultTargetSuccession = 'else' TransitionSuccessionMember
 */

/**
 *      GuardedSuccession: TransitionUsage =
 *          ('succession' UsageDeclaration)?
 *          'first' ownedRelationship += FeatureChainMember
 *          ownedRelationship += GuardExpressionMember
 *          'then' ownedRelationship += TransitionSuccessionMember
 *          UsageBody
 */
fun SysMLv2.GuardedSuccession() = FeatureAction(this.semantics, type = ElementType.TransitionUsage).parse {
    optional(SUCCESSION) {
        SUCCESSION.consume()
        UsageDeclaration()
    }
    FIRST.consume()
    FeatureChain()
    GuardExpression()
    THEN.consume()
    FeatureChain()
    UsageBody()
}
fun SysMLv2.guardedSuccessionStarts(): Boolean =
    token.kind == SUCCESSION || (token.kind == FIRST && nextToken.kind == IF)

// Fixme: Wrong, should be a TransitionFeatureMembership
fun SysMLv2.GuardExpression() = FeatureAction(semantics, ElementType.Feature).parse {
    IF.consume()
    val iBeforeExpression = token.indices.first
    val expr = OwnedExpression()
    semantics.addOwnedElement(expr, ElementType.FeatureMembership)
    expr.semantics {
        element.indices = iBeforeExpression..consumedToken.indices.last
        element.body = input.subSequence(element.indices!!).toString().trim()
    }
}

/**
 *  *     val guardCondition = FeatureActions<Expression>(semantics,
 *  *         creator = ::ExpressionImplementation, mutableListOf("ScalarValues::Boolean"))
 *  *     optional(start=IF, consume = true) {
 *  *         val iBeforeExpression = token.indices.first
 *  *         Expression().also {
 *  *             guardCondition.create(Identification("guard_" + UUID.randomUUID().toString()))
 *  *             guardCondition.created?.featureWithValue = AstRoot(model, guardCondition.created!!, it)
 *  *             guardCondition.created?.indices = iBeforeExpression..consumedToken.indices.last
 *  *             guardCondition.created?.expression = input.subSequence(guardCondition.created?.indices!!).toString().trim()
 *  *             // print("Set guard condition: " + guardCondition.created?.expression)
 *  *         }
 *  *     }
 */