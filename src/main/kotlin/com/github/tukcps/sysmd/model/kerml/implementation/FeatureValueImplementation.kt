package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.FeatureValue
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class FeatureValueImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    elementType: String = "FeatureValue"
): FeatureValue, OwningMembershipImplementation(
    model,
    elementId = elementId,
    elementType = elementType
){
    override var isInitial: Boolean = false
    override var isDefault: Boolean = false

    override fun updateFrom(template: Element) {
        if (template is FeatureValue) {
            super.updateFrom(template)
            isDefault = template.isDefault
            isInitial = template.isInitial
        }
    }

    override fun clone(): FeatureValue = FeatureValueImplementation(model).also {
        it.updateFrom(this)
    }
}