package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Structure

interface ItemUsage: OccurrenceUsage {
    val itemDefinition: MutableList<Structure>
}
