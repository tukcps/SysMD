package com.github.tukcps.sysmd.model.sysml

interface RenderingDefinition : PartDefinition {
    val rendering: MutableList<RenderingUsage>
}
