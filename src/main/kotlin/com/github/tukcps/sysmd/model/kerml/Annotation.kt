package com.github.tukcps.sysmd.model.kerml

/**
 * An Annotation is a Relationship between an AnnotatingElement and the Element that
 * is annotated by that AnnotatingElement.
 * Attributes - annotatedElement : Element {redefines target}
 * The Element that is annotated by the annotatingElement of this Annotation.
 * annotatingElement : AnnotatingElement {redefines source}
 * The AnnotatingElement that annotates the annotatedElement of this Annotation.
 * owningAnnotatedElement : Element [0..1] {subsets annotatedElement, redefines owningRelatedElement}
 * The annotatedElement of this Annotation, when it is also its owningRelatedElement.
 */
interface Annotation: Relationship {
    // Owner of relationship shall be set during creation
    val annotatingElement: Resolved<AnnotatingElement>
    val annotatedElement: Resolved<Element>
    override fun clone(): Annotation
}