@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.sysml.implementation.ItemDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.ItemUsageImplementation


class ItemDefinitionActions(
    context: ActionsContext,
): TypeActions<ItemDefinitionImplementation>(
    context,
    creator = ::ItemDefinitionImplementation,
    isImplicit = "Items::Item",
)


class ItemUsageActions(
    context: ActionsContext,
): FeatureActions<ItemUsageImplementation>(
    context,
    creator = ::ItemUsageImplementation,
    defaultType = "Items::Item",
)