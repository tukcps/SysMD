package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


open class MetadataFeatureImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): MetadataFeature, FeatureImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override var body: String = ""
    override fun clone(): MetadataFeature = MetadataFeatureImplementation(model).also {
        it.updateFrom(this)
        it.body = body
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is MetadataFeature)
            body = template.body
    }


    /**
     * The annotated element is defined by an annotation (relationship) or, if no annotation
     * is available, is the owning element.
     */
    override fun annotatedElement(): List<Element> {
        val ownedAnnotations = annotation().map { it.annotatedElement }
        return if (ownedAnnotations.isEmpty()) ownedAnnotations else listOf(owner!!)
    }
}