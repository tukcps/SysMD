@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import java.util.*

/**
 * Connector / Binary link; usage of an Association
 * connector c: A from f1 to f2;
 */
open class ConnectorImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    override var owningRelatedElement: Resolved<Element> = Resolved(),
    owner: Resolved<Element> = Resolved(),
    from: MutableList<Resolved<Feature>> = mutableListOf(),
    to: MutableList<Resolved<Feature>> = mutableListOf(),
    elementType: String = "Connector",
) : Connector, Type, FeatureImplementation(
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    ownedElement = ownedElement,
    owner = owner,
    elementType = elementType
) {

    override val association: Association
        get() = generalization.first { it.ref is Association }.ref as Association

    override var isImplied: Boolean = false
    override var isDirected: Boolean = false

    final override var from: MutableList<Resolved<Feature>>
        get() = source as MutableList<Resolved<Feature>>
        set(value) { source = value as MutableList<Resolved<Element>> }

    final override var to: MutableList<Resolved<Feature>>
        get() = target as MutableList<Resolved<Feature>>
        set(value) { target = value as MutableList<Resolved<Element>> }

    final override var source: MutableList<Resolved<Element>> = from as MutableList<Resolved<Element>>
    final override var target: MutableList<Resolved<Element>> = to as MutableList<Resolved<Element>>


    init {
        this.from = from
        this.to = to
    }

    override fun resolveNames(): Boolean {
        updated = updated or super.resolveNames()

        // Search all sources & targets.
        source.forEach {
            if (it.resolveIdentity(this, expectedType = Resolved.RefType.FEATURE))
                updated = true
        }
        target.forEach {
            if (it.resolveIdentity(this, expectedType = Resolved.RefType.FEATURE))
                updated = true
        }

        source.forEach { relatedElement ->
            if (relatedElement.ref != null && model!!.repo.sourceOfRelationship[relatedElement.ref!!] == null)
                model!!.repo.sourceOfRelationship[relatedElement.ref!!] = mutableSetOf()
        }

        target.forEach { relatedElement ->
            if (relatedElement.ref != null && model!!.repo.targetOfRelationship[relatedElement.ref!!] == null)
                model!!.repo.targetOfRelationship[relatedElement.ref!!] = mutableSetOf()
        }

        // Add found sources & targets to hashmap for faster lookup
        source.forEach { relatedElement ->
            if (relatedElement.ref != null) {
                model!!.repo.sourceOfRelationship[relatedElement.ref!!]?.add(this)
            }
        }
        target.forEach { relatedElement ->
            if (relatedElement.ref != null) {
                model!!.repo.targetOfRelationship[relatedElement.ref!!]?.add(this)
            }
        }
        return updated
    }

    override fun clone(): Connector{
        return ConnectorImplementation(
            declaredName = declaredName, declaredShortName = declaredShortName,
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner),
            from = Resolved.copyOfIdentityList(from as MutableCollection<Resolved<Element>>) as MutableList<Resolved<Feature>>,
            to = Resolved.copyOfIdentityList(to as MutableCollection<Resolved<Element>>) as MutableList<Resolved<Feature>>,
        ).also { klon ->
            klon.model = model
            klon.direction = direction
            klon.updated = updated
        }
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Relationship) {
            target = Resolved.copyOfIdentityList(template.target)
            source = Resolved.copyOfIdentityList(template.source)
        }
    }

    override fun toString(): String {
        return "Connector { " +
                "name='$declaredName', " +
                ((if(declaredShortName != null)"shortName='$declaredShortName', " else "")) +
                "supertypes='${allSupertypes()}', "+
                "#sources=${source.size}, " +
                "#targets=${target.size}, " +
                "id='${elementId}' }"
    }

}
