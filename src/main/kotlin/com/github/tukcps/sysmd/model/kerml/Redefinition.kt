package com.github.tukcps.sysmd.model.kerml

interface Redefinition: Subsetting {
    var redefiningFeature: Feature
    var redefinedFeature: Feature

    override fun clone(): Redefinition
    override fun updateFrom(template: Element)
}