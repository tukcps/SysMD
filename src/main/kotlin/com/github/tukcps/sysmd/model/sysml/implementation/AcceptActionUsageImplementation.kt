package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.sysml.AcceptActionUsage
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*

open class AcceptActionUsageImplementation(
    owner: Resolved<Element> = Resolved(),
    elementId: UUID = UUID.randomUUID(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    elementType: String = "AcceptActionUsage"
):
    AcceptActionUsage, ActionUsageImplementation(
    owner=owner,
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    ownedElement=ownedElement,
    elementType=elementType){
        override lateinit var payloadParameter : Resolved<ReferenceUsage>
        override fun clone(): AcceptActionUsage {
            val klon = AcceptActionUsageImplementation(
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