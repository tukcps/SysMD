@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*

/**
 * From Standard:
 * ConstraintDefinition =
 *     OccurrenceDefinitionPrefix 'constraint' 'def'
 *     DefinitionDeclaration CalculationBody
 *
 * ConstraintUsage =
 *     OccurrenceUsagePrefix 'constraint'
 *     ConstraintUsageDeclaration CalculationBody
 *
 * AssertConstraintUsage =
 *     OccurrenceUsagePrefix 'assert' ( isNegated ?= 'not' )?
 *     ( ownedRelationship += OwnedReferenceSubsetting
 *       FeatureSpecializationPart?
 *     | 'constraint' ConstraintUsageDeclaration ) CalculationBody
 *
 * ConstraintUsageDeclaration : ConstraintUsage =
 *     UsageDeclaration ValuePart?
 */

/** Just checks the prefixes already parsed for compliance */
fun KerML.OccurrenceDefinitionPrefix() {
    // TODO
}

/**
 *  * ConstraintUsage =
 *  *     OccurrenceUsagePrefix 'constraint'
 *  *     ConstraintUsageDeclaration CalculationBody
 */
fun KerML.ConstraintUsage() {
    val constraint = semantics.constraintActions()
    OccurrenceDefinitionPrefix()
    CONSTRAINT.consume()
    Identification().also { constraint?.identification = it; constraint?.create() }

    LCURBRACE.consume() // TODO: Body
    val kerml = constraint?.created!!
    val iBeforeExpression = token.indices.first
    Expression().also {
        kerml.featureWithValue = AstRoot(model, kerml, it)
        kerml.indices = iBeforeExpression .. consumedToken.indices.last
        kerml.expression = input.subSequence(kerml.indices!!).toString().trim()
    }
    RCURBRACE.consume()
}


/**
 * AssertConstraintUsage =
 *     OccurrenceUsagePrefix 'assert' ( isNegated ?= 'not' )?
 *     ( ownedRelationship += OwnedReferenceSubsetting
 *       FeatureSpecializationPart?
 *     | 'constraint' ConstraintUsageDeclaration ) CalculationBody
 *
 */
fun KerML.AssertConstraintUsage() {
    val assert = sysMLSemantics.AssertSemantics()
    OccurrenceDefinitionPrefix()
    ASSERT.consume()
    Identification().also { assert.identification = it; assert.create() }

    LCURBRACE.consume() // TODO: Body
    val iBeforeExpression = token.indices.first
    Expression().also {
        assert.created?.featureWithValue = AstRoot(model, assert.created!!, it)
        assert.created?.indices = iBeforeExpression .. consumedToken.indices.last
        assert.created?.expression = input.subSequence(assert.created!!.indices!!).toString().trim()
    }
    RCURBRACE.consume()
}