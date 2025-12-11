package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type

interface TransitionUsage: ActionUsage {
    val source : Element
    val target : Element
    val triggerPayloadParameter : ReferenceUsage?
    val triggerPayloadParameterType: Type?
        get() = triggerPayloadParameter?.allSupertypes()?.firstOrNull()
    val guardCondition : Feature?
}
