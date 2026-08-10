package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Multiplicity

interface ControlNode : ActionUsage {
    fun multiplicityHasBounds(mult: Multiplicity, lower: Int, upper: Int): Boolean
}
