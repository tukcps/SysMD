package com.github.tukcps.sysmd.model.kerml

/**
 * Annotating element following 7.2.3.3.1, 2 KerMLv2
 * - it is an Element
 * - Can own further annotating elements.
 * - The annotated element is defined by relationship Annotation, or else is the owning namespace.
 * An AnnotatingElement is an Element that provides additional description of or metadata on some other Element.
 * An AnnotatingElement is attached to its annotatedElement by an Annotation Relationship
 */
interface AnnotatingElement: Element {
    var body: String
    override fun clone(): AnnotatingElement
    /**
     * The annotated element is defined by an annotation (relationship) or, if no annotation
     * is available, is the owning element.
     */
    fun annotation(): List<Annotation> = getOwnedElementsOfType<Annotation>()
    fun annotatedElement(): List<Element>
}
