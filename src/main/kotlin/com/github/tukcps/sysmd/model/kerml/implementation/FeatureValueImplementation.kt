package com.github.tukcps.sysmd.model.kerml.implementation

class FeatureValueImplementation(
    elementType: String = "FeatureValue"
): OwningMembershipImplementation(
    elementType = elementType
){
    override fun clone(): FeatureValueImplementation {
        return FeatureValueImplementation().also {
            source = source.toMutableList()
            target = target.toMutableList()
        }
    }
}