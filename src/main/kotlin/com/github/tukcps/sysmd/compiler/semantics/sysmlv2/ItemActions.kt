@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction
import com.github.tukcps.sysmd.model.generated.ElementType

class ItemDefinitionAction(
    context: ActionsContext,
): TypeAction(
    context,
    type = ElementType.ItemDefinition,
    isImplicit = "Items::Item",
)

class ItemUsageAction(
    context: ActionsContext,
): FeatureAction(
    context,
    type = ElementType.ItemUsage,
    isImplicit = "Items::Item",
)