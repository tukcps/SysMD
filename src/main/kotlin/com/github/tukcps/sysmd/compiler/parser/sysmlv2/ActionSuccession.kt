@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.ConnectorEndMember
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureChain
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.implementation.ExpressionImplementation
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.sysml.TransitionUsage
import com.github.tukcps.sysmd.model.sysml.implementation.TransitionUsageImplementation
import java.util.UUID

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
    ConnectorEndMember()
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
fun SysMLv2.GuardedSuccession() = FeatureActions<TransitionUsage>(this.semantics, ::TransitionUsageImplementation).parse {
    optional(setOf(SUCCESSION), noMatch = { semantics.create(null)}) {
        SUCCESSION.consume()
        UsageDeclaration()
    }
    FIRST.consume()
    FeatureChain()
    GuardExpressionMember()
    THEN.consume()
    FeatureChain()
    UsageBody()
}
fun SysMLv2.guardedSuccessionStarts(): Boolean =
    token.kind == SUCCESSION || (token.kind == FIRST && nextToken.kind == IF)


fun SysMLv2.GuardExpressionMember() = FeatureActions<Feature>(semantics, ::ExpressionImplementation).parse {
    IF.consume()
    val iBeforeExpression = token.indices.first
    Expression().also {
        val guardCondition = semantics.element<Expression>()
        semantics.create(Identification("guard_" + UUID.randomUUID().toString()))
        semantics.element<Expression>().featureWithValue = AstRoot(model, guardCondition, it)
        guardCondition.indices = iBeforeExpression..consumedToken.indices.last
        guardCondition.expression = input.subSequence(guardCondition.indices!!).toString().trim()
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