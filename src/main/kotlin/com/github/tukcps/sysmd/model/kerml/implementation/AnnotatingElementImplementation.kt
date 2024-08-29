package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import java.util.*


/**
 * Annotating element following 7.2.3.3.1, 2 KerMLv2
 * - it is an Element
 * - Can own further annotating elements.
 * - The annotated element is defined by relationship Annotation, or else is the owning namespace.
 * An AnnotatingElement is an Element that provides additional description of or metadata on some other Element.
 * An AnnotatingElement is attached to its annotatedElement by an Annotation Relationship
 */
open class AnnotatingElementImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    override var body: String = "",
    elementType: String = "AnnotatingElement"
): AnnotatingElement, ElementImplementation(
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    owner = owner,
    ownedElement = ownedElement,
    elementType = elementType
) {
    override fun resolveNames() = false

    override fun clone(): AnnotatingElement {
        return AnnotatingElementImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner),
            body = body,
            elementType = elementType)
    }
    override fun toString(): String =
        "AnnotatingElement { declaredName='$declaredName', declaredShortName='${declaredShortName}', body=$body, id='${elementId}'}"

    /**
     * The annotated element is defined by an annotation (relationship) or, if no annotation
     * is available, is the owning element.
     */
    override fun annotatedElement(): Nothing = TODO()
}
