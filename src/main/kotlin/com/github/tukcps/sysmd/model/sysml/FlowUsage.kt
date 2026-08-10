package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Flow
import com.github.tukcps.sysmd.model.kerml.Interaction

interface FlowUsage : ActionUsage, ConnectorAsUsage, Flow {
    val flowDefinition: MutableList<Interaction>
}
