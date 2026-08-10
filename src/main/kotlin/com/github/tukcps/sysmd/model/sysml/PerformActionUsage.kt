package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Feature

interface PerformActionUsage : ActionUsage, EventOccurrenceUsage {
    val performedAction: ActionUsage
    fun namingFeature(): Feature?
}
