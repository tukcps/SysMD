package com.github.tukcps.sysmd.model.kerml


/**
 * Description
 * FeatureTyping is Specialization in which the specific Type is a Feature. This means the set of instances of the (specific) typedFeature is a subset of the set of instances of the (general) type. In the simplest case, the type is a Classifier, whereupon the typedFeature subset has instances interpreted as sequences ending in things (in the modeled universe) that are instances of the Classifier.
 * General Classes
 * Kernel Modeling Language (KerML) v1.0, Submission 169
 * Specialization
 * Attributes
 * /owningFeature : Feature [0..1] {subsets typedFeature, redefines owningType}
 * The Feature that owns this FeatureTyping (which must also be the typedFeature). type : Type {redefines general}
 * The Type that is being applied by this FeatureTyping.
 * typedFeature : Feature {redefines specific}
 * The Feature that has its Type determined by this FeatureTyping.
 * Operations: None.
 * Constraints: None.
 */
interface FeatureTyping: Specialization {
    val owningFeature: Feature?
    val type: Type
    val typedFeature: Feature
}