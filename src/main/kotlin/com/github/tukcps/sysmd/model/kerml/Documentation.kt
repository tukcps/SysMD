package com.github.tukcps.sysmd.model.kerml

/**
 * Documentation is an Annotation whose annotatingElement is a Comment that provides
 * documentation of the annotatedElement. Documentation is always an ownedRelationship
 * of its annotatedElement.
 * @param identification optional name
 * @param body the documentation as a string
 */
interface Documentation: Comment {
    override fun clone(): Documentation
}