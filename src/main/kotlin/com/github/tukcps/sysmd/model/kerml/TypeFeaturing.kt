package com.github.tukcps.sysmd.model.kerml

interface TypeFeaturing: Featuring {
    var featureOfType: Feature
    var featuringType: Type
    val owningFeatureOfType: Feature?
}