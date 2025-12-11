package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
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
    elementType=elementType
) {
    override val source : Element
        get() = getOwnedElementOfType<SuccessionAsUsage>()!!.source[0]
    override val target : Element
        get() = getOwnedElementOfType<SuccessionAsUsage>()!!.target[0]

    override val guardCondition : Feature? // fixme: replace with Expression once implemented
        get() = member.firstOrNull { it !is Usage && it is Feature } as? Feature

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
