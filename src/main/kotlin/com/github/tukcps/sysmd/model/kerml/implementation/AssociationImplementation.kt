package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type


/**
 * Association inherits from Relationship and Classifier (Classifiable)
 */
open class AssociationImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    sources: MutableList<Resolved<Element>> = mutableListOf(),
    targets: MutableList<Resolved<Element>> = mutableListOf(),
    override var isAbstract: Boolean = false,
    elementType: String = "Association"
): Association, RelationshipImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    source = sources,
    target = targets,
    elementType = elementType
) {

    override var isSufficient: Boolean = false
    override var isConjugated: Boolean = false

    override fun resolveNames(): Boolean {
        updated = super<RelationshipImplementation>.resolveNames() or updated
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
            sources = Resolved.copyOfIdentityList(source),
            targets = Resolved.copyOfIdentityList(target)
        )
    }

    override val subtypes: MutableSet<Type> = mutableSetOf()
}