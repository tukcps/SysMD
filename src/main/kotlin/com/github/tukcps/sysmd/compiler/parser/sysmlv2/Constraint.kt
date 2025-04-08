@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureSpecializationPart
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AssertActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.CalculationDefinitionActions
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Feature
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
 * ConstraintUsageDeclaration = UsageDeclaration ValuePart?
 */
fun SysMLv2.ConstraintUsageDeclaration(constraint: FeatureActions<Feature>) {
    UsageDeclaration(constraint)
    optional(Token.Kind.EQ) {
        Unsupported("Production rule for Value Part in ConstraintUsageDeclaration not yet implemented.")
    }
}




/**
 *      ConstraintUsage =
 *          OccurrenceUsagePrefix 'constraint' ConstraintUsageDeclaration CalculationBody
 *
 */
fun SysMLv2.ConstraintUsage() {
    val constraint = semantics.constraintActions()
    OccurrenceDefinitionPrefix()
    CONSTRAINT.consume()
    ConstraintUsageDeclaration(constraint)

    // Calculation Body
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
    OccurrenceUsagePrefix()
    ASSERT.consume()
    NOT.optional { assert.isNegated = true  }

    alternatives {
        NAME_LIT starts  {
            OwnedReferenceSubsetting() .also { assert.create(Identification(it)) }
            optional({ tokenIsNot(LCURBRACE)} ){
                FeatureSpecializationPart(assert as FeatureActions<Feature>)
            }
        }
        CONSTRAINT then {
            ConstraintUsageDeclaration(assert as FeatureActions<Feature>)
        }
        others {  }
    }
    assert.finish() // Before the Calculation Body ...
    // CalculationBody(Resolved(assert.created!!))
    // TODO ... Calculation Body
    LCURBRACE.consume() // TODO: Body
    val iBeforeExpression = token.indices.first
    Expression().also {
        assert.created?.featureWithValue = AstRoot(model, assert.created!!, it)
        assert.created?.indices = iBeforeExpression .. consumedToken.indices.last
        assert.created?.expression = input.subSequence(assert.created!!.indices!!).toString().trim()
    }
    RCURBRACE.consume()
}