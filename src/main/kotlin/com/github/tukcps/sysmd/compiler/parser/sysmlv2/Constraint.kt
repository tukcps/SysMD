@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureSpecializationPart
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedReferenceSubsetting
import com.github.tukcps.sysmd.compiler.parser.kerml.ValuePart
import com.github.tukcps.sysmd.compiler.parser.kerml.valuePartStart
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AssertActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.CalculationDefinitionActions
import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.CalculationDefinitionImplementation

/**
 *      ConstraintDefinition = OccurrenceDefinitionPrefix 'constraint' 'def'
 *          DefinitionDeclaration CalculationBody
 */
fun SysMLv2.ConstraintDefinition() = CalculationDefinitionActions(semantics,
        ::CalculationDefinitionImplementation, "Constraints::ConstraintDefinition").parse {
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
fun SysMLv2.OccurrenceDefinitionPrefixStarts() = token.kind in setOf(ABSTRACT, VARIATION)

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
fun SysMLv2.ConstraintUsage() = FeatureActions<Feature>(semantics, creator = ::FeatureImplementation, "ScalarValues::Boolean").parse {
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
fun SysMLv2.AssertConstraintUsage() = AssertActions(semantics).parse {
    OccurrenceUsagePrefix()
    ASSERT.consume()
    NOT.optional { semantics.element<Invariant>().isNegated = true  }

    alternatives {
        NAME_LIT starts  {
            OwnedReferenceSubsetting() .also {
                model.status.info(message = "Unimplemented: Assertion with feature chain", element = semantics.element())
            }
            optional({ tokenIsNot(LCURBRACE)} ){
                FeatureSpecializationPart()
            }
        }
        CONSTRAINT then {
            ConstraintUsageDeclaration()
        }
        others {  semantics.create(null) }
    }
    CalculationBody()
}