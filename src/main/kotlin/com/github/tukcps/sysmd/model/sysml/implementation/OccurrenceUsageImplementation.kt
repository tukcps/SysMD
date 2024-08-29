package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*

open class OccurrenceUsageImplementation(
    owner: Resolved<Element> = Resolved(),
    elementId: UUID = UUID.randomUUID(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    elementType: String = "OccurrenceUsage",
    isEnd : Boolean = false
): OccurrenceUsage, FeatureImplementation(
    owner =owner,
    elementId =elementId,
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    ownedElement =ownedElement,
    direction =direction,
    elementType =elementType,
    isEnd = isEnd)
{
    override fun clone(): OccurrenceUsage {
        val klon = OccurrenceUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner),
            isEnd = isEnd,
            direction = direction
        ).also { klon ->
            klon.model = model
            klon.updated = updated
            klon.isComposite = isComposite
        }
        return klon
    }
}