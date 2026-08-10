package com.github.tukcps.sysmd.model.sysml

interface ViewpointUsage : RequirementUsage {

    val viewpointDefinition: ViewpointDefinition?
    val viewpointStakeholder: MutableList<PartUsage>

}
