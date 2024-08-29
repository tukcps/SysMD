package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import java.util.*


@Suppress("UNCHECKED_CAST")
open class SpecializationImplementation(
    elementId: UUID = UUID.randomUUID(),
    owner: Resolved<Element> = Resolved(),
    specific: Resolved<Type>? = null,
    general: Resolved<Type>? = null,
    elementType: String = "Specialization"
): Specialization, RelationshipImplementation(
    elementId = elementId,
    owner = owner,
    source = if (specific != null) mutableListOf(specific) else mutableListOf(),
    target = if (general != null) mutableListOf(general) else mutableListOf(),
    elementType = elementType
) {
    constructor(subclass: Type, superclass: String):
            this(owner= Resolved(), specific= Resolved(ref=subclass), general= Resolved(str=superclass))

    constructor(subclass: Type, superclass: Type):
            this(owner= Resolved(), specific= Resolved(ref=subclass), general= Resolved(ref =superclass)) {
        target[0].str = target[0].ref!!.qualifiedName
    }

    constructor() : this(owner= Resolved(), specific=null, general = null)

    override var general: Resolved<Type>
        get() = (target.firstOrNull()?: Resolved(model!!.any)) as Resolved<Type>
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
     */
    override fun resolveNames(): Boolean {
        require( model != null )

        // Search all sources & targets.
        source.forEach {
            if (it.resolveIdentity(owningNamespace!!, Resolved.RefType.TYPE))
                updated = true
        }
        target.forEach {
            if (it.resolveIdentity(owningNamespace!!, Resolved.RefType.TYPE))
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

}