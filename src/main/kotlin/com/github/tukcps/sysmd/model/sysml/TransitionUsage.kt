package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type

interface TransitionUsage: ActionUsage{
    val source : Resolved<ActionUsage>
    val target : Resolved<ActionUsage>
    val triggerPayloadParameter : ReferenceUsage?
    val triggerPayloadParameterType: Type?
        get() = triggerPayloadParameter?.allSupertypes()?.firstOrNull()
    val guardCondition : Resolved<Feature>?
}
