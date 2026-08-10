package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Class

interface OccurrenceUsage: Usage {

    enum class PortionKind { timeslice, snapshot}
    val individualDefinition: OccurrenceDefinition?
    var isIndividual: Boolean?
    val occurrenceDefinition: MutableList<Class>
    var portionKind: PortionKind?
}