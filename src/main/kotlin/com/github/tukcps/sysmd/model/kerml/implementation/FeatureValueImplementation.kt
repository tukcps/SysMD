package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.FeatureValue

class FeatureValueImplementation(
    elementType: String = "FeatureValue"
): FeatureValue, OwningMembershipImplementation(
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

    override fun clone(): FeatureValue = FeatureValueImplementation().also {
        it.updateFrom(this)
    }
}