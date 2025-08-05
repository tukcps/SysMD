package com.github.tukcps.sysmd.model.kerml

interface ReferenceSubsetting: Subsetting {

    var referencedFeature: Feature
    var referencingFeature: Feature

    override fun clone(): Specialization
    override fun updateFrom(template: Element)
}