package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type

interface TransitionUsage: ActionUsage{
    val source : Resolved<ActionUsage>
    val target : Resolved<ActionUsage>
    val triggerPayloadParameter : Resolved<ReferenceUsage>?
    val triggerPayloadParameterType: Type?
        get() = triggerPayloadParameter?.ref?.allSupertypes()?.firstOrNull()
}
