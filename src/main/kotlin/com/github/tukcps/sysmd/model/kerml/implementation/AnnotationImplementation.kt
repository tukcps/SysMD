package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


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
    model: Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    owningRelatedElement: Element = UnresolvedElement(model),
    annotatingElement: Element = UnresolvedElement(model),
    annotatedElement: Element = UnresolvedElement(model),
): Annotation, RelationshipImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    owningRelatedElement=owningRelatedElement,
    source = mutableListOf(annotatingElement),
    target = mutableListOf(annotatedElement),
) {
    @Suppress("UNCHECKED_CAST")
    override val annotatingElement: Element
        get() = source.first()

    override val annotatedElement: Element
        get() = target.first()

    override fun clone(): Annotation = AnnotationImplementation(
        model,
        declaredName=declaredName,
        declaredShortName=declaredShortName,
        owningRelatedElement=owningRelatedElement,
        annotatingElement = annotatingElement,
        annotatedElement = annotatedElement,
    ).also { it.updateFrom(this) }
}