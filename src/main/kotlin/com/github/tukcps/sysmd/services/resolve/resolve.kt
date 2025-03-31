package com.github.tukcps.sysmd.services.resolve

import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.dropFirstName
import com.github.tukcps.sysmd.model.util.firstName
import com.github.tukcps.sysmd.model.util.hasNoName
import com.github.tukcps.sysmd.model.util.isSimpleName
import com.github.tukcps.sysmd.services.session.report


/**
 * Resolves a qualified name in a namespace.
 * Furthermore, the method has a template parameter to which it, if possible, casts the result.
 * @param T Type for which will be searched.
 * @param qualifiedName QualifiedName that will be searched.
 * @param searchInSuperClass Whether to search in superclasses as well to consider inheritance
 * @param resolveReferences Whether to follow reference if the element found is a reference
 */
inline fun <reified T: Element> Namespace.resolve(
    qualifiedName: QualifiedName,
    searchInSuperClass: Boolean = true,
    resolveReferences: Boolean = true
): T?  {
    var found = findRecursive(qualifiedName, emptySet(), emptySet(),true, searchInSuperClass, resolveReferences)

    // If the element found is a referenced feature, follow the reference and use it
    if (found is Feature && found.referencedFeature?.ref != null && resolveReferences) {
        found = found.referencedFeature?.ref
    }

    if (found is T?)
        return found

    model?.report(found,"'$qualifiedName' could be resolved, but is of wrong type",
        cause = ElementNotFoundException(this, "'$qualifiedName' could be resolved, but is of wrong type"))
    return null
}


/**
 * Returns the owned element with a given name.
 * @param name A SimpleName that is searched for
 */
fun Namespace.resolveLocal(name: SimpleName): Element? {

    visibleMemberships().forEach {
        if (it.ref?.name == name || it.ref?.shortName == name)
            return it.ref
    }
    return null
}

/**
 * Searches recursively for a qualified name, starting from the namespace.
 * @param qualifiedName the qualified name that is searched
 * @param searchedImports optionally, a set of Namespaces that have already been searched; needed to detect cyclic includes.
 * @param searchedSuperClasses optionally, the set of searched superclasses; needed to detect cyclic definitions
 * @param searchInOwner optionally, whether the given qualified name is initially given as a simple name, or just
 * became a simple name via recursion that cuts qualified name down.
 * @param searchInSuperClass optionally, whether to recurse into superclasses.
 * Since 3.1 default off! This has an impact on the redefinitions that might not be handled properly (?), but 20% speedup.
 * @return An element of or null, if the name cannot be resolved.
 */
fun Namespace.findRecursive(
    qualifiedName: QualifiedName,
    searchedImports: Set<Namespace>,
    searchedSuperClasses: Set<Type>,
    searchInOwner: Boolean = true,
    searchInSuperClass: Boolean = true,
    resolveReferences: Boolean = true,
): Element? {

    if (qualifiedName == "self")
        return this

    if (qualifiedName == "that") {
        return owner.ref
    }

    // Stop search if we search in Any, or if the name has been shortened to an empty string.
    if (this is Anything)
        return null
    if (qualifiedName.hasNoName())
        return null

    // If we have a simple name, we can search in owned elements that are identified by simple names.
    if (qualifiedName.isSimpleName()) {
        if (this == model?.global && qualifiedName == "Global") return model?.global
        var found = resolveLocal(qualifiedName)
        if (found is Feature && found.referencedFeature?.ref != null && resolveReferences) {
            found = found.referencedFeature?.ref
        }
        if (found != null) return found
    } else {
        // Check for Self and Global name that are just pre-defined alias names.
        val newName: QualifiedName = qualifiedName.dropFirstName()

        // Search downwards in matching owned namespaces, shortening the qualified name by 1st name.
        // SysMD: We allow "Global" as explicit entry to start of global search.
        var matchingNamespace =
            if (qualifiedName.firstName() == "Global") model?.global
            else {
                resolveLocal(qualifiedName.firstName())
            }
        if (matchingNamespace is Feature && matchingNamespace.referencedFeature?.ref != null && resolveReferences) {
            matchingNamespace = matchingNamespace.referencedFeature!!.ref
        }
        if (matchingNamespace != null && matchingNamespace is Namespace) {
            val found = matchingNamespace.findRecursive(newName, emptySet(), emptySet(),false, searchInSuperClass)
            if (found != null) return found
        }
    }

    // Search in imports, and track searched paths to avoid cyclic search.
    imports.forEach {
        if (this !in searchedImports) {
            if (it.ref != null && it.ref is Namespace) {
                val found = it.ref!!.findRecursive(qualifiedName, searchedImports+this, searchedSuperClasses, searchInOwner, searchInSuperClass)
                if (found != null) return found
            }
        }
    }

    // Search in supertypes
    if (this is Type && searchInSuperClass && this !in searchedSuperClasses) {
        this.allSupertypes().forEach {
            val found = it.findRecursive(qualifiedName, searchedImports+this, searchedSuperClasses+this, searchInOwner=false, searchInSuperClass=true)
            if (found != null) return found
        }
    }

    // Search in owning namespace
    return if (searchInOwner)
        owningNamespace?.findRecursive(qualifiedName, searchedImports, searchedSuperClasses, true)
    else
        null
}



/**
 * Recursive collection of all owned features, including inherited from general types until Anything.
 * @param z counter to detect depth of search
 * @return list of all elements found
 */
fun Namespace.findAllOwnedElements(z: Int=0): Collection<Element> {
    val owned = mutableListOf<Element>()
    ownedElement.forEach { owned.add(model?.get(it.id!!)?:throw SysMDError("Unresolvable Id: $it.id")) }
    if (this is Type) {
        allSupertypes().forEach { superclass ->
            if (this == superclass)
                model?.report(this, "Cyclic supertype: ${this.qualifiedName}")
            else {
                val inherited = if (superclass !is Anything) superclass.findAllOwnedElements(z + 1)
                else emptySet()
                return mergeOwnedElements(owned, inherited)
            }
        }
    }
    return owned
}



/**
 * Merges the owned elements specified by the hasA relationships from a class with
 * inherited relations that are potentially overridden.
 * The merge method merges features of a class with those of its superclass
 * such that the Liskov principle holds.
 * @param own Features of subclass
 * @param inherited Features of superclass
 */
private fun mergeOwnedElements(own: Collection<Element>, inherited: Collection<Element>): Collection<Element> {
    val merged = mutableSetOf<Element>()
    merged.addAll(own)

    for (i in inherited) {
        var overridden = false
        own.forEach { o ->
            overridden = true
        }
        if (!overridden)
            merged.add(i)
    }
    return merged
}
