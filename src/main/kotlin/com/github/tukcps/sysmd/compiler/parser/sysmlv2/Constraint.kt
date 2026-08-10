@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureSpecializationPart
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedReferenceSubsetting
import com.github.tukcps.sysmd.compiler.parser.kerml.ValuePart
import com.github.tukcps.sysmd.compiler.parser.kerml.valuePartStart
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.CalculationDefinitionAction
import com.github.tukcps.sysmd.model.generated.ElementType

/**
 *      ConstraintDefinition = OccurrenceDefinitionPrefix 'constraint' 'def'
 *          DefinitionDeclaration CalculationBody
 */
fun SysMLv2.ConstraintDefinition() = CalculationDefinitionAction(
    semantics, ElementType.CalculationDefinition, "Constraints::ConstraintDefinition"
).parse {
    OccurrenceDefinitionPrefix()
    CONSTRAINT.consume()
    DEF.consume()
    DefinitionDeclaration()
    CalculationBody()
}

/**
 *      OccurrenceDefinitionPrefix : OccurrenceDefinition =
 *          BasicDefinitionPrefix?
 *          ( isIndividual ?= 'individual'
 *            ownedRelationship += EmptyMultiplicityMember
 *          )?
 *          DefinitionExtensionKeyword*
 */
fun SysMLv2.OccurrenceDefinitionPrefix() {
    BasicDefinitionPrefix()
}
fun SysMLv2.OccurrenceDefinitionPrefixStarts() =
    token.kind in setOf(ABSTRACT, VARIATION) && (nextToken.kind == DEF || nextNextToken.kind == DEF)

/**
 * ConstraintUsageDeclaration = UsageDeclaration ValuePart?
 */
fun SysMLv2.ConstraintUsageDeclaration() {
    UsageDeclaration()
    optional(valuePartStart) { ValuePart() }
}

/**
 *      ConstraintUsage =
 *          OccurrenceUsagePrefix 'constraint' ConstraintUsageDeclaration CalculationBody
 *
 */
fun SysMLv2.ConstraintUsage() = FeatureAction(semantics, type = ElementType.Feature, isImplicit = "ScalarValues::Boolean").parse {
    OccurrenceDefinitionPrefix()
    CONSTRAINT.consume()
    ConstraintUsageDeclaration()
    CalculationBody()
}

/**
 *      AssertConstraintUsage =
 *          OccurrenceUsagePrefix 'assert' (isNegated ?= 'not')?
 *          (OwnedReferenceSubsetting FeatureSpecializationPart?
 *              | 'constraint' ConstraintUsageDeclaration)
 *          CalculationBody
 */
fun SysMLv2.AssertConstraintUsage() = FeatureAction(
    semantics, ElementType.Invariant, "ScalarValues::Boolean",
).parse {
    OccurrenceUsagePrefix()
    ASSERT.consume()
    NOT.optional { semantics.element.isNegated = true  }

    alternatives {
        NAME_LIT starts  {
            OwnedReferenceSubsetting() .semantics { status.info(message = "Unimplemented: Assertion with feature chain", element) }
            optional({ tokenIsNot(LCURBRACE)} ){
                FeatureSpecializationPart()
            }
        }
        CONSTRAINT then {
            ConstraintUsageDeclaration()
        }
        others { }
    }
    CalculationBody()
}