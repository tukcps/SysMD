package com.github.tukcps.sysmd.model.sysml

interface ViewpointDefinition : RequirementDefinition {
    val viewpointStakeholder: MutableList<PartUsage>
}
