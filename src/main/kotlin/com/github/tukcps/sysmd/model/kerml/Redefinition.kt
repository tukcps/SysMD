package com.github.tukcps.sysmd.model.kerml

interface Redefinition: Subsetting {
    var redefiningFeature: Resolved<Feature>
    var redefinedFeature: Resolved<Feature>

    override fun clone(): Subsetting
    override fun updateFrom(template: Element)
}