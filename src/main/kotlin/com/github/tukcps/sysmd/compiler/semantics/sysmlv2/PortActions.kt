package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.sysml.implementation.PortDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.PortUsageImplementation


class PortDefinitionActions(
    context: ActionsContext
): TypeActions<PortDefinitionImplementation>(
    context,
    specializes = mutableListOf("Ports::Port"),
    creator = ::PortDefinitionImplementation
)



class PortUsageActions(
    context: ActionsContext
): FeatureActions<PortUsageImplementation>(
    context,
    creator = ::PortUsageImplementation,
    defaultType = mutableListOf("Ports::Port")
)