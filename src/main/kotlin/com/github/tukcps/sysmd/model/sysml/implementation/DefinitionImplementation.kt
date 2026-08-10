package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.implementation.ClassifierImplementation
import com.github.tukcps.sysmd.model.sysml.Definition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class DefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    Definition, ClassifierImplementation(model,elementId = elementId)
{
    override var isVariation: Boolean? = false

    override fun clone(): Definition = DefinitionImplementation(model).also { it.updateFrom(this) }
    override fun updateFrom(template: Element) {
        check(template is Definition)
        isVariation = template.isVariation
        super.updateFrom(template)
    }
}