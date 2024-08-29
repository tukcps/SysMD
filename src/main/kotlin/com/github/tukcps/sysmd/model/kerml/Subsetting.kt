package com.github.tukcps.sysmd.model.kerml

interface Subsetting: Specialization {

    var subsettedFeature: Resolved<Feature>
    var subsettingFeature: Resolved<Feature>

    override fun clone(): Specialization
    override fun updateFrom(template: Element)
}