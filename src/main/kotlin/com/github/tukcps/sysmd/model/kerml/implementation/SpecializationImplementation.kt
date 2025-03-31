package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.QualifiedName

@Suppress("UNCHECKED_CAST")
open class SpecializationImplementation(
    specific: Resolved<Type>? = null,
    general: Resolved<Type>? = null,
    elementType: String = "Specialization"
): Specialization, RelationshipImplementation(
    source = if (specific != null) mutableListOf(specific) else mutableListOf(),
    target = if (general != null) mutableListOf(general) else mutableListOf(),
    elementType = elementType
) {
    constructor(subclass: Type, superclass: QualifiedName):
            this(specific= Resolved(ref=subclass), general= Resolved(str=superclass))

    constructor(subclass: Type, superclass: Type):
            this(specific= Resolved(ref=subclass), general= Resolved(ref =superclass)) {
        target[0].str = target[0].ref!!.qualifiedName
    }

    constructor() : this( specific=null, general = null)

    override var general: Resolved<Type>
        get() = (target.firstOrNull()?: Resolved(model!!.anything)) as Resolved<Type>
        set(value) { target = mutableListOf(value) }

    override var specific: Resolved<Type>
        get() = (source.firstOrNull()?: Resolved(this)) as Resolved<Type>
        set(value) { source= mutableListOf(value) }

    override fun toString(): String {
        return "$elementType ${source.firstOrNull()} :> ${target.firstOrNull()}"
    }

    override fun clone(): Specialization {
        return SpecializationImplementation(
            // owner=Resolved(owner),
            specific = Resolved(source.firstOrNull() as Resolved<Type>),
            general = if (target.firstOrNull() == null) Resolved() else Resolved(target.firstOrNull() as Resolved<Type>)
        ).also { klon ->
            klon.isTransient = isTransient
            klon.model = model
        }
    }

    override fun updateFrom(template: Element) {
        require(template is Specialization)
        super.updateFrom(template)
    }

    /**
     * Initialize resolves the QualifiedNames and/or uid and adds references and uid to Elements.
     * @return true if there was a change in this.
     */
    override fun resolveNames(): Boolean {
        require( model != null )

        // Search all sources and targets.
        source.forEach {
            if (it.resolveIdentity(owner.ref?.owningNamespace?:model!!.global, Resolved.RefType.TYPE))
                updated = true
        }

        // The target of the specialization cannot be in the owner itself, must be in the owner
        target.forEach {
            if (it.resolveIdentity(owner.ref?.owner?.ref?.owningNamespace?:model!!.global, Resolved.RefType.TYPE))
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

}