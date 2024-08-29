package com.github.tukcps.sysmd.model.kerml

/**
 * Association inherits from Relationship and Classifier (Classifiable).
 *
 * 8.3.4.4.2 Association
 * Description
 * An Association is a Relationship and a Classifier to enable classification of links between things (in the universe). The co-domains (types) of the associationEnd Features are the relatedTypes, as co-domain and participants (linked things) of an Association identify each other.
 * General Classes
 * Relationship Classifier
 * Attributes
 * /associationEnd : Feature [0..*] {redefines endFeature}
 * The features of the Association that identify the things that can be related by it. A concrete Association must have
 * at least two associationEnds. When it has exactly two, the Association is called a binary Association. /relatedType : Type [0..*] {redefines relatedElement, ordered, nonunique}
 * The types of the associationEnds of the Association, which are the relatedElements of the Association considered as a Relationship.
 * /sourceType : Type [0..1] {subsets relatedType, redefines source}
 * The source relatedType for this Association. It is the first relatedType of the Association.
 * 176 Kernel Modeling Language (KerML) v1.0, Submission
 * /targetType : Type [0..*] {subsets relatedType, redefines target}
 * The target relatedTypes for this Association. This includes all the relatedTypes other than the sourceType.
 */
interface Association: Relationship, Classifier {
    override fun resolveNames(): Boolean
    override fun updateFrom(template: Element)
    override fun toString(): String
    override fun visibleMemberships(): List<Resolved<Element>> = ownedElement + source + target

    // TODO: Add associationEnd, sourceType, targetType.
}