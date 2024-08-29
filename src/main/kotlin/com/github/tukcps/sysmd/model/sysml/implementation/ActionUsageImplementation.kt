package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*

open class ActionUsageImplementation(
    owner: Resolved<Element> = Resolved(),
    elementId: UUID = UUID.randomUUID(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    elementType: String = "ActionUsage"
):
    ActionUsage, OccurrenceUsageImplementation(
    owner=owner,
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    ownedElement=ownedElement,
    elementType=elementType){
        override fun clone(): ActionUsage {
            val klon = ActionUsageImplementation(
                declaredName = declaredName,
                declaredShortName = declaredShortName,
                ownedElement = Resolved.copyOfIdentityList(ownedElement),
                owner = Resolved(owner)
            ).also { klon ->
                klon.model = model
                klon.updated = updated
                klon.isComposite = isComposite
            }
            return klon
        }
    }