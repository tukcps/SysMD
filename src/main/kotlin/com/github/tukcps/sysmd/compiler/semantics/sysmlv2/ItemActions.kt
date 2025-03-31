package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.sysml.implementation.ItemDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.ItemUsageImplementation


class ItemDefinitionActions(
    context: ActionsContext
): TypeActions<ItemDefinitionImplementation>(
    context,
    creator = ::ItemDefinitionImplementation,
    specializes = mutableListOf("Items::Item"),
)


class ItemUsageActions(
    context: ActionsContext
): FeatureActions<ItemUsageImplementation>(
    context,
    creator = ::ItemUsageImplementation,
    defaultType = mutableListOf("Items::Item")
)