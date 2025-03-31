@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AssertActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.CalculationDefinitionActions
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.CalculationDefinition
import com.github.tukcps.sysmd.model.sysml.implementation.CalculationDefinitionImplementation

/**
 *      ConstraintDefinition = OccurrenceDefinitionPrefix 'constraint' 'def'
 *          DefinitionDeclaration CalculationBody
 */
fun SysMLv2.ConstraintDefinition() {
    val constraintDefinition = CalculationDefinitionActions<CalculationDefinition>(semantics,
        ::CalculationDefinitionImplementation, mutableListOf("Constraints::ConstraintDefinition"))
    OccurrenceDefinitionPrefix()
    CONSTRAINT.consume()
    DEF.consume()
    @Suppress("UNCHECKED_CAST")
    DefinitionDeclaration(constraintDefinition as TypeActions<Type>)
    CalculationBody(Resolved(constraintDefinition.created!!))
    constraintDefinition.finish()
}

/**
 *      AssertConstraintUsage =
 *          OccurrenceUsagePrefix 'assert' ( isNegated ?= 'not' )?
 *          ( ownedRelationship += OwnedReferenceSubsetting
 *          FeatureSpecializationPart?
 *          | 'constraint' ConstraintUsageDeclaration ) CalculationBody
 */

/** Just checks the prefixes already parsed for compliance */
fun SysMLv2.OccurrenceDefinitionPrefix() {
    // TODO
}

/**
 *      ConstraintUsage =
 *          OccurrenceUsagePrefix 'constraint' ConstraintUsageDeclaration CalculationBody
 *      ConstraintUsageDeclaration = UsageDeclaration ValuePart?
 */
fun SysMLv2.ConstraintUsage() {
    val constraint = semantics.constraintActions()
    OccurrenceDefinitionPrefix()
    CONSTRAINT.consume()
    UsageDeclaration(constraint)
    // ValuePart

    LCURBRACE.consume() // TODO: Body
    val createdElement = constraint.created!!
    val iBeforeExpression = token.indices.first
    Expression().also {
        createdElement.featureWithValue = AstRoot(model, createdElement, it)
        createdElement.indices = iBeforeExpression .. consumedToken.indices.last
        createdElement.expression = input.subSequence(createdElement.indices!!).toString().trim()
    }
    RCURBRACE.consume()
    constraint.finish()
}

/**
 *      AssertConstraintUsage =
 *          OccurrenceUsagePrefix 'assert' ( isNegated ?= 'not' )?
 *          ( OwnedReferenceSubsetting FeatureSpecializationPart?
 *              | 'constraint' ConstraintUsageDeclaration )
 *          CalculationBody
 */
fun SysMLv2.AssertConstraintUsage() {
    val assert = AssertActions(semantics)
    OccurrenceDefinitionPrefix()
    ASSERT.consume()
    Identification().also { assert.create(it) }

    LCURBRACE.consume() // TODO: Body
    val iBeforeExpression = token.indices.first
    Expression().also {
        assert.created?.featureWithValue = AstRoot(model, assert.created!!, it)
        assert.created?.indices = iBeforeExpression .. consumedToken.indices.last
        assert.created?.expression = input.subSequence(assert.created!!.indices!!).toString().trim()
    }
    RCURBRACE.consume()
    assert.finish()
}