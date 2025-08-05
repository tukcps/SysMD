package com.github.tukcps.sysmd.model.kerml

interface Subsetting: Specialization {

    var subsettedFeature: Feature
    var subsettingFeature: Feature

    override fun clone(): Specialization
    override fun updateFrom(template: Element)
}