package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import java.util.*


/**
 * Association inherits from Relationship and Classifier (Classifiable)
 */
open class AssociationImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    sources: MutableList<Resolved<Element>> = mutableListOf(),
    targets: MutableList<Resolved<Element>> = mutableListOf(),
    ownedElements: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    override var isAbstract: Boolean = false,
    elementType: String = "Association"
): Association, RelationshipImplementation(
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    source = sources,
    target = targets,
    ownedElement = ownedElements,
    owner = owner,
    elementType = elementType
) {

    override var isSufficient: Boolean = false
    override val isConjugated: Boolean
        get() = TODO()



    override fun resolveNames(): Boolean {
        updated = super.resolveNames()
        generalization.forEach {
            if (it.resolveIdentity( this, Resolved.RefType.TYPE) )
                updated = true
        }
        return updated
    }

    override fun toString(): String {
        return "Association { " +
                (if (declaredName != null) "name='$declaredName', " else "") +
                (if (declaredShortName != null) "shortName='$declaredShortName', " else "") +
                "#sources=${source.size}, " +
                "#targets=${target.size}, " +
                "id='${elementId}' }"
    }

    override fun clone(): Association {
        return AssociationImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            ownedElements = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner),
            sources = Resolved.copyOfIdentityList(source),
            targets = Resolved.copyOfIdentityList(target)
        )
    }
}