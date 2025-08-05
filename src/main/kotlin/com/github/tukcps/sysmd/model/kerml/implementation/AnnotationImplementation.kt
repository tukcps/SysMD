package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.UnresolvedElement


/**
 * An Annotation is a Relationship between an AnnotatingElement and the Element that
 * is annotated by that AnnotatingElement.
 * Attributes - annotatedElement : Element {redefines target}
 * The Element that is annotated by the annotatingElement of this Annotation.
 * annotatingElement: AnnotatingElement {redefines source}
 * The AnnotatingElement that annotates the annotatedElement of this Annotation.
 * owningAnnotatedElement: Element [0..1] {subsets annotatedElement, redefines owningRelatedElement}
 * The annotatedElement of this Annotation, when it is also its owningRelatedElement.
 */
open class AnnotationImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    owningRelatedElement: Element = UnresolvedElement(),
    annotatingElement: Element = UnresolvedElement(),
    annotatedElement:  Element = UnresolvedElement(),
    elementType: String = "Annotation"
): Annotation, RelationshipImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    owningRelatedElement=owningRelatedElement,
    source = mutableListOf(annotatingElement),
    target = mutableListOf(annotatedElement),
    elementType = elementType
) {
    @Suppress("UNCHECKED_CAST")
    override val annotatingElement: Element
        get() = source.first()

    override val annotatedElement: Element
        get() = target.firstOrNull() as Element

    override fun clone(): Annotation {
        return AnnotationImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
            owningRelatedElement=owningRelatedElement,
            annotatedElement = annotatedElement,
            annotatingElement = annotatingElement,
        )
    }
}