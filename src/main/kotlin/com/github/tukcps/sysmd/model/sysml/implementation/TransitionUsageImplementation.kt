package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.sysml.*
import com.github.tukcps.sysmd.model.util.SimpleName

class TransitionUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "TransitionUsage"
    ): TransitionUsage, OccurrenceUsageImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType) {

    @Suppress("UNCHECKED_CAST")
    override val source : Resolved<ActionUsage>
        get() = getOwnedElementOfType<SuccessionAsUsage>()?.source?.get(0) as Resolved<ActionUsage>
    @Suppress("UNCHECKED_CAST")
    override val target : Resolved<StateUsage>
        get() = getOwnedElementOfType<SuccessionAsUsage>()?.target?.get(0) as Resolved<StateUsage>

    override var guardCondition : Resolved<Feature>? = null

    override val triggerPayloadParameter: ReferenceUsage?
        get() {
            val actionUsage = getOwnedElementOfType<AcceptActionUsage>()
            return actionUsage?.payloadParameter
        }

    override fun clone(): TransitionUsage {
        val klon = TransitionUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            klon.model = model
            klon.updated = updated
            klon.isComposite = isComposite
        }
        return klon
    }
}
