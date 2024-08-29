package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.sysml.*
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*

class TransitionUsageImplementation(
    owner: Resolved<Element> = Resolved(),
    elementId: UUID = UUID.randomUUID(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    elementType: String = "TransitionUsage"
):
    TransitionUsage, OccurrenceUsageImplementation(
    owner=owner,
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    ownedElement=ownedElement,
    elementType=elementType) {

    @Suppress("UNCHECKED_CAST")
    override val source : Resolved<ActionUsage>
        get() = getOwnedElementOfType<SuccessionAsUsage>()?.source?.get(0) as Resolved<ActionUsage>
    @Suppress("UNCHECKED_CAST")
    override val target : Resolved<StateUsage>
        get() = getOwnedElementOfType<SuccessionAsUsage>()?.target?.get(0) as Resolved<StateUsage>

    override val triggerPayloadParameter: Resolved<ReferenceUsage>?
        get() {
            val actionUsage = getOwnedElementOfType<AcceptActionUsage>()
            return actionUsage?.payloadParameter
        }

    override fun clone(): TransitionUsage {
        val klon = TransitionUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner),
        ).also { klon ->
            klon.model = model
            klon.updated = updated
            klon.isComposite = isComposite
        }
        return klon
    }
}
