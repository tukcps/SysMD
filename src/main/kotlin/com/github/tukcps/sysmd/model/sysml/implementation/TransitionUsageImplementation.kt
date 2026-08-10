package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.*
import com.github.tukcps.sysmd.model.util.ErrorElement
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class TransitionUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    TransitionUsage,
    OccurrenceUsageImplementation(model,elementId = elementId)
{
    override val source : Element
        get() = getOwnedElementOfType<SuccessionAsUsage>()?.source?.firstOrNull()
            ?: ErrorElement(model, null)
    override val target : Element
        get() = getOwnedElementOfType<SuccessionAsUsage>()?.target?.firstOrNull()
            ?: ErrorElement(model, null)

    override val guardCondition : Feature? // fixme: replace with Expression once implemented
        get() = member.find { it !is Usage && it !is Multiplicity && it is Feature } as? Feature

    override val triggerPayloadParameter: ReferenceUsage?
        get() {
            val actionUsage = getOwnedElementOfType<AcceptActionUsage>()
            return actionUsage?.payloadParameter
        }

    override fun clone(): TransitionUsage = TransitionUsageImplementation(model).also { klon ->
        klon.updateFrom(this)
        klon.isComposite = isComposite
     }
}
