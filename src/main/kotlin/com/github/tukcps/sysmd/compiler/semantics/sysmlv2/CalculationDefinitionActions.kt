package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FunctionActions
import com.github.tukcps.sysmd.model.sysml.CalculationDefinition
import com.github.tukcps.sysmd.model.util.SimpleName


/**
 * Semantic action for the definition of a Calculation.
 * @param context The context of the parser
 * @param creator lambda that creates a Calculation element
 * @param specializes The default class
 */
class CalculationDefinitionActions <T: CalculationDefinition> (
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<QualifiedName> = mutableListOf("Base::Anything")
): FunctionActions<T>(context, creator, specializes)
