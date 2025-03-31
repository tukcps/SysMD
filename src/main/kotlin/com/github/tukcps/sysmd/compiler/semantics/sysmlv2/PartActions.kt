package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.sysml.implementation.PartDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.PartUsageImplementation



class PartDefinitionActions(
    context: ActionsContext,
): TypeActions<PartDefinitionImplementation>(
    context,
    creator = ::PartDefinitionImplementation,
    specializes = mutableListOf("Parts::Part")
)



class PartUsageActions(
    context: ActionsContext
): FeatureActions<PartUsageImplementation>(
    context,
    creator = ::PartUsageImplementation,
    defaultType = mutableListOf("Parts::Part")
)