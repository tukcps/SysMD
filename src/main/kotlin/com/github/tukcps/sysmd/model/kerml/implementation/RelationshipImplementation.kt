package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.util.SimpleName


/**
 * Following KerML Specification, 7.2.2.2.2 Relationships;
 * Represents the SysMD relationship source relation and inherits ElementImplementation
 */
open class RelationshipImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    final override var source: MutableList<Resolved<Element>> = mutableListOf(),
    final override var target: MutableList<Resolved<Element>> = mutableListOf(),
    override var isImplied: Boolean = false,
    elementType: String = "Relationship"
): Relationship, ElementImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    /**
     * Initialize resolves the QualifiedNames and/or uid and adds references and uid to Elements.
     */
    override fun resolveNames(): Boolean {
        updated = super.resolveNames()
        require( model != null )
        if (owningNamespace == null)
            model?.status?.warn(Issue.Kind.WARN_UNRESOLVED_OWNER, "unowned element: $this", element = this)

        // Search all sources and targets.
        source.forEach {
            if (it.resolveIdentity(owningNamespace!!))
                updated = true
        }
        target.forEach {
            if (it.resolveIdentity(owningNamespace!!))
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

        // Add found sources and targets to hashmap for faster lookup
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

    override fun clone(): Relationship {
        return RelationshipImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            source = Resolved.copyOfIdentityList(source),
            target = Resolved.copyOfIdentityList(target),
        ).also{
            it.model = model
        }
    }

    override fun toString(): String {
        return "Relationship { " +
                (if (name != null) "name='$declaredName', " else "") +
                (if (shortName != null) "shortName=' $declaredShortName, " else "") +
                "#sources=${source.size}, " +
                "#targets=${target.size}, " +
                "elementId='${elementId}' }"
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Relationship) {
          target = Resolved.copyOfIdentityList(template.target)
          source = Resolved.copyOfIdentityList(template.source)
        }
    }
}
