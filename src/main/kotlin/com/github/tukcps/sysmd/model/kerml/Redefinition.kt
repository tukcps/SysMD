package com.github.tukcps.sysmd.model.kerml

/**
 * A redefinition is a kind of subsetting.
 * The (owning) redefining feature changes some features of the redefined feature from the supertype.
 */
interface Redefinition: Subsetting {
    var redefiningFeature: Feature
    var redefinedFeature: Feature

    override fun clone(): Redefinition
    override fun updateFrom(template: Element)
}