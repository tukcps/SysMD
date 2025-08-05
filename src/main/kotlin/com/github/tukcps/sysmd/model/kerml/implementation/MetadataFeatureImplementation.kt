package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.util.SimpleName


class MetadataFeatureImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "MetadataFeature"
): MetadataFeature, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override var body: String = ""
    override fun clone(): MetadataFeature =
        MetadataFeatureImplementation(this.declaredName, this.declaredShortName).also {
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