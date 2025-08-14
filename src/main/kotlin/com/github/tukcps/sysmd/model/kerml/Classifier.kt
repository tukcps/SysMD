package com.github.tukcps.sysmd.model.kerml


/**
 * Classifier is a specific Type; difference is most notably its different use and ability to distinguish it from
 * Features that are also a type, but not a Classifier.
 */
interface Classifier: Type {
    fun subclassification(): List<Subclassification> = ownedSubclassification() // Separate subclassification not implemented
    fun ownedSubclassification(): List<Subclassification> = generalization.filterIsInstance<Subclassification>()
    fun ownedSuperclassification(): List<Subclassification> = TODO()
}