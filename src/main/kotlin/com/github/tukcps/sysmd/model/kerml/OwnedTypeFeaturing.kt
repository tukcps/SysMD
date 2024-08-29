package com.github.tukcps.sysmd.model.kerml

/**
 * TODO: Define Featuring relationship.
 */
interface OwnedTypeFeaturing: Relationship {
    val featureOfType: Feature
}