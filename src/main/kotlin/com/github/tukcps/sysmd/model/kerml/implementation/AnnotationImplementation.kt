package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import java.util.*


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
open class AnnotationImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    annotatingElement: Resolved<AnnotatingElement>? = null,
    annotatedElement:  Resolved<Element>? = null,
    ownedElements: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    elementType: String = "Annotation"
): Annotation, RelationshipImplementation(
    elementId=elementId, declaredName=declaredName, declaredShortName=declaredShortName,
    source = if (annotatingElement!= null) mutableListOf(annotatingElement) else mutableListOf(),
    target = if (annotatedElement != null) mutableListOf(annotatedElement) else mutableListOf(),
    ownedElement=ownedElements,
    owner = owner,
    elementType = elementType
) {
    @Suppress("UNCHECKED_CAST")
    override val annotatingElement: Resolved<AnnotatingElement>
        get() = source.firstOrNull() as Resolved<AnnotatingElement>

    override val annotatedElement: Resolved<Element>
        get() = target.firstOrNull() as Resolved<Element>

    override fun clone(): Annotation {
        return AnnotationImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
            annotatedElement = annotatedElement,
            annotatingElement = annotatingElement,
            ownedElements = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner)
        )
    }

    override fun toString(): String {
        return "Annotation { " +
                "name='$declaredName', " +
                (if (declaredShortName != null) "shortName=' $declaredShortName" else "") +
                "annotatingElement=${source.firstOrNull()?.ref?.qualifiedName}, " +
                "annotatedElement=${target.firstOrNull()?.ref?.qualifiedName}, " +
                "id='${elementId}' }"
    }
}