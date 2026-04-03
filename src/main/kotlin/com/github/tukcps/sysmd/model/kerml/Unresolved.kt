package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.exceptions.Issue.Kind.ERROR_TYPE_WRONG
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.services.session.Session
import java.util.*

sealed interface Unresolved {
    var relativeName: QualifiedName?
    var id: UUID?
    var input: CharSequence?
    var indices: IntRange?
}

/** Checks that a resolved element's type matches an unresolved Element.
 * Reports an error if this is not the case.
 * @param containing Optional element to report the error at
 * @return Whether the types were correct
 */
internal fun Session.checkType(unresolved : Unresolved, resolved : Element, containing : Element? = null) : Boolean
= when(unresolved) {
    is UnresolvedElement -> true // are relationships valid here?
    is UnresolvedFeature -> resolved is Feature
    is UnresolvedMembership -> resolved is Membership
    is UnresolvedNamespace -> resolved is Namespace
    is UnresolvedType -> resolved is Type
}.also {
    if(!it)
    {
        val kind = unresolved.elementType.removePrefix("Unresolved ").lowercase()
        status.error("Expecting a kind of $kind", kind = ERROR_TYPE_WRONG, element = containing ?: unresolved)
    }
}

class UnresolvedElement(
    override var relativeName: QualifiedName? = null,
    override var id: UUID? = null
) : Unresolved, ElementImplementation(elementType = "Unresolved Element") {
    override fun toString(): String = "Unresolved Element: $relativeName"
    override fun escapedName(): String? = relativeName
}

class UnresolvedMembership(
    override var relativeName: QualifiedName? = null,
    override var id: UUID? = null
): Unresolved, MembershipImplementation(elementType = "Unresolved Relationship"){
    override fun toString(): String = "Unresolved Membership: $relativeName"
    override fun escapedName(): String? = relativeName
}

class UnresolvedType(
    override var relativeName: QualifiedName? = null,
    override var id: UUID? = null
): Unresolved, TypeImplementation(elementType = "Unresolved Type"){
    override fun toString(): String = "Unresolved Type: $relativeName"
    override fun escapedName(): String? = relativeName
}

open class UnresolvedFeature(
    override var relativeName: QualifiedName? = null,
    override var id: UUID? = null,
    elementType: String = "Unresolved Feature",
): Unresolved, FeatureImplementation(elementType = elementType){
    override fun toString(): String = "Unresolved Feature: $relativeName"
    override fun escapedName(): String? = relativeName
}

class UnresolvedFeatureChain(
    relativeName: QualifiedName? = null,
    id: UUID? = null,
): UnresolvedFeature(relativeName, id, elementType = "Unresolved Feature Chain") {
    override fun toString(): String = "Unresolved Feature Chain: $relativeName"
    override fun escapedName(): String? = relativeName
}

class UnresolvedNamespace(
    override var relativeName: QualifiedName? = null,
    override var id: UUID? = null,
): Unresolved, NamespaceImplementation(elementType = "Unresolved Namespace"){
    override fun toString(): String = "Unresolved Namespace: $relativeName"
    override fun escapedName(): String? = relativeName
}