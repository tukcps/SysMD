package com.github.tukcps.sysmd.model.sysml

interface ExhibitStateUsage : PerformActionUsage, StateUsage {
    val exhibitedState: StateUsage
}
