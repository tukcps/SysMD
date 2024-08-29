package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Step

interface AcceptActionUsage: ActionUsage{
    val payloadParameter : Resolved<ReferenceUsage>
}
