package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.ConnectorImplementation
import com.github.tukcps.sysmd.model.sysml.SuccessionAsUsage
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*

open class SuccessionAsUsageImplementation(
    owner: Resolved<Element> = Resolved(),
    elementId: UUID = UUID.randomUUID(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    from: MutableList<Resolved<Feature>> = mutableListOf(),
    to: MutableList<Resolved<Feature>> = mutableListOf(),
    elementType: String = "SuccessionAsUsage"
) : SuccessionAsUsage, ConnectorImplementation(
    owner=owner,
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    ownedElement=ownedElement,
    from=from,
    to=to,
    elementType=elementType
) {
    override fun clone(): SuccessionAsUsage {
        return SuccessionAsUsageImplementation(
            declaredName = declaredName, declaredShortName = declaredShortName,
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner),
            from = Resolved.copyOfIdentityList(from),
            to = Resolved.copyOfIdentityList(to),
        ).also { klon ->
            klon.model = model
            klon.direction = direction
            klon.updated = updated
        }
    }
}