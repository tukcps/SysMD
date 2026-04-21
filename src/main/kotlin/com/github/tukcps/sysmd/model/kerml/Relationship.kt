package com.github.tukcps.sysmd.model.kerml



/**
 * Following KerML Specification, 7.2.2.2.2 Relationships;
 * Represents the SysMD relationship source relation and inherits ElementImplementation
 */
interface Relationship: Element {
    /** The sources the relationship */
    var source: MutableList<Element>

    /** The targets of the relationship */
    var target: MutableList<Element>

    /** The owning element of this relationship */
    var owningRelatedElement: Element

    /** The owned elements */
    var ownedRelatedElement: MutableList<Element>

    var isImplied: Boolean

    // derived properties:
    val relatedElements
        get() = source+target+ownedRelatedElement

    override val owner: Element?
        get() = if (this !is Association && this !is Connector && this !is Dependency) owningRelatedElement else owningRelationship

    /** The owned Elements, for a Relationship */
    override var ownedElement: MutableList<Element>
        get() = if (this !is Association && this !is Connector) ownedRelatedElement else super.ownedElement.toMutableList()
        set(value) { ownedRelatedElement = value }

    /**
     * Initialize resolves the QualifiedNames and/or uid and replaces Unresolved Types or Features with model instances.
     */
    override fun updateFrom(template: Element)

    /**
     * Helper to permit customization of "toString" method.
     */
    fun relationshipString(from: String="from", to: String = "to"): String =
        "$from " +
        source.joinToString(", ") { it.escapedName()?:"[${it.elementType}]" }  + " $to " +
        target.joinToString(", ") { it.escapedName()?:"(no name)" }

    override fun clone(): Relationship
}
