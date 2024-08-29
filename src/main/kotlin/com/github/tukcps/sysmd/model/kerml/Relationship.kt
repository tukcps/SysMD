package com.github.tukcps.sysmd.model.kerml



/**
 * Following KerML Specification, 7.2.2.2.2 Relationships;
 * Represents the SysMD relationship source relation and inherits ElementImplementation
 */
interface Relationship: Element {
    // Fields:
    var source: MutableList<Resolved<Element>>
    var target: MutableList<Resolved<Element>>
    var isImplied: Boolean
    var owningRelatedElement: Resolved<Element>

    // derived properties:
    val relatedElements
        get() = source+target


    /**
     * Initialize resolves the QualifiedNames and/or uid and adds references and uid to Elements.
     */
    override fun resolveNames(): Boolean
    override fun clone(): Relationship
    override fun updateFrom(template: Element)
}
