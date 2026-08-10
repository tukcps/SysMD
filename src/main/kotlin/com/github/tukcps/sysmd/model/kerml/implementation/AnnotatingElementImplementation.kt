package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * Annotating element following 7.2.3.3.1, 2 KerMLv2
 * - it is an Element
 * - Can own further annotating elements.
 * - The annotated element is defined by relationship Annotation, or else is the owning namespace.
 * An AnnotatingElement is an Element that provides additional description of or metadata on some other Element.
 * An AnnotatingElement is attached to its annotatedElement by an Annotation Relationship
 */
open class AnnotatingElementImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    override var body: String = "",
): AnnotatingElement, ElementImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {

    override fun clone(): AnnotatingElement = AnnotatingElementImplementation(model).also { klon ->
        klon.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is AnnotatingElement) {
            body = template.body
        }
    }

    override fun toString(): String = super.toString() + " = '$body'"

    /**
     * The annotated element is defined by an annotation (relationship) or, if no annotation
     * is available, is the owning element.
     */
    override fun annotatedElement(): List<Element> {
        val ownedAnnotations = annotation().map { it.annotatedElement }
        return if (ownedAnnotations.isEmpty()) ownedAnnotations else listOf(owner!!)
    }
}
