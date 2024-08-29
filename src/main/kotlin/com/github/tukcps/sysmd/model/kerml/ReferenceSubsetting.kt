package com.github.tukcps.sysmd.model.kerml

interface ReferenceSubsetting: Subsetting {

    var referencedFeature: Resolved<Feature>
    var referencingFeature: Resolved<Feature>

    override fun clone(): Specialization
    override fun updateFrom(template: Element)
}