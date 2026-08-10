package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction
import com.github.tukcps.sysmd.model.generated.ElementType


/**
 * Semantic action for the definition of a Calculation.
 * @param context The context of the parser
 * @param isImplicit The default class
 */
class CalculationDefinitionAction (
    context: ActionsContext,
    type: ElementType = ElementType.CalculationDefinition,
    isImplicit: String = "Calculations::Calculation"
): TypeAction(context, type, isImplicit = isImplicit) {}
