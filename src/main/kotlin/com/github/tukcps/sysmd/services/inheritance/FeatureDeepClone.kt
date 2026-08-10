package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.model.kerml.*

/** In-place map() */
private inline fun<T> MutableList<T>.replace(f : (T) -> T)
{
    val iter = listIterator()

    while(iter.hasNext()) {
        val old = iter.next()
        val new = f(old)

        if(new != old)
            iter.set(new)
    }
}

/** Clones an element including its entire ownership structure.
 * @return The clone. NOT added to any namespace.
 */
fun<T : Element> T.danglingDeepClone() : T
{
    // these have different ownership semantics
    require(this !is Relationship || this is Association || this is Connector || this is Dependency) {
        "Deep clone is only defined for non-relationships"
    }

    @Suppress("UNCHECKED_CAST")
    val clone = clone() as T // fixme: add a type-safe clone factory (generated?)
    clone.isImpliedIncluded = true // needed?

    for(rel in ownedRelationship) {
        val relClone = rel.clone() as Relationship
        relClone.isImpliedIncluded = true

        assert(relClone.owningRelatedElement === this) { "Inconsistent relation ownership" }
        relClone.owningRelatedElement = clone
        // replace the cloned element in this relationship
        relClone.source.replace { if(it == this) clone else it }
        relClone.target.replace { if(it == this) clone else it } // this _should_ do nothing

        if(relClone is OwningMembership) {
            relClone.target.replace {
                assert(it !== clone) { "Circular ownership" }
                it.danglingDeepClone()
            }
        }

        model.addOwnedRelationship(relClone)
    }

    return clone
}

/**
 * For inheritance: This function creates a deep copy of a feature.
 * The cloned feature, including its Multiplicity, Specialization and its owned features,
 * will be marked as a transient element and added to the namespace given as parameter.
 * @param addTo namespace, to which the cloned elements will be added.
 */
fun<T : Element> T.deepCloneWithInheritedFeature(addTo: Namespace): T {
    val clone = danglingDeepClone()
    model.addOwnedMember(clone, addTo)
    return clone
}