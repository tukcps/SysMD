package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import java.util.*

interface Unresolved {
    var relativeName: QualifiedName?
    var id: UUID?
    var input: CharSequence?
    var indices: IntRange?
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