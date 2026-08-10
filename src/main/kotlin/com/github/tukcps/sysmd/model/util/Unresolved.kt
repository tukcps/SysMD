package com.github.tukcps.sysmd.model.util

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * A reference that is still unresolved, as sealed interface for type safety.
 * @property relativeName the qualified name that should resolve to the respective element.
 * @property id a UUID that should resolve to the respective element.
 * @property input the input stream
 * @property indices indices in the input stream with the relative Name
 */
sealed interface Unresolved: Element {
    var relativeName: QualifiedName?
    var id: Uuid?
    override var input: CharSequence?
    override var indices: IntRange?
}

/** Checks that a resolved element's type matches an unresolved Element.
 * Reports an error if this is not the case.
 * @param containing Optional element to report the error at
 * @return Whether the types were correct
 */
internal fun checkType(unresolved : Unresolved, resolved : Element, containing : Element? = null) : Boolean =
    when(unresolved) {
        is UnresolvedElement -> true // are relationships valid here?
        is UnresolvedExpression -> resolved is Expression
        is UnresolvedFeature -> resolved is Feature
        is UnresolvedMembership -> resolved is Membership
        is UnresolvedRelationship -> resolved is Relationship
        is UnresolvedOwningMembership -> resolved is OwningMembership
        is UnresolvedNamespace -> resolved is Namespace
        is UnresolvedType -> resolved is Type
    }

/**
 * Unresolved Referent to an Element in general.
 * @param relativeName Path relative to namespace of element as Qualified Name.
 * @param id The id of the element after name resolution.
 */
class UnresolvedElement(
    model : Session,
    override var relativeName: QualifiedName? = null,
    override var id: Uuid? = null
) : Unresolved, ElementImplementation(model, ) {
    override fun toString(): String = "Unresolved Element: ${relativeName?:if(id != null) "(by id)" else "null"}"
    override fun escapedName(): String? = relativeName
}

/**
 * Unresolved reference to a Relationship.
 */
class UnresolvedRelationship(
    model : Session,
    override var relativeName: QualifiedName? = null,
    override var id: Uuid? = null
) : Unresolved, RelationshipImplementation(model, ) {
    override fun toString(): String = "Unresolved Relationship: ${relativeName?:if(id != null) "(by id)" else "null"}"
    override fun escapedName(): String? = relativeName
}

/**
 * Unresolved Membership; must resolve to membership.
 */
class UnresolvedMembership(
    model : Session,
    override var relativeName: QualifiedName? = null,
    override var id: Uuid? = null
): Unresolved, MembershipImplementation(model) {
    override fun toString(): String = "Unresolved Membership: $relativeName"
    override fun escapedName(): String? = relativeName
}

/**
 * Unresolved Membership; must resolve to membership.
 */
class UnresolvedOwningMembership(
    model : Session,
    override var relativeName: QualifiedName? = null,
    override var id: Uuid? = null
): Unresolved, OwningMembershipImplementation(model, elementType = "Unresolved OwningMembership"){
    override fun toString(): String = "Unresolved Membership: $relativeName"
    override fun escapedName(): String? = relativeName
}

/**
 * Unresolved Type; must resolve to type.
 */
class UnresolvedType(
    model : Session,
    override var relativeName: QualifiedName? = null,
    override var id: Uuid? = null
): Unresolved, TypeImplementation(model, ) {
    override fun toString(): String = "Unresolved Type: $relativeName"
    override fun escapedName(): String? = relativeName
}

/**
 * Unresolved Feature; must resolve to a Feature
 */
open class UnresolvedFeature(
    model : Session,
    override var relativeName: QualifiedName? = null,
    override var id: Uuid? = null,
    elementType: String = "Unresolved Feature",
): Unresolved, FeatureImplementation(model){
    override fun toString(): String = "Unresolved Feature: $relativeName"
    override fun escapedName(): String? = relativeName
}

/**
 * Unresolved Feature Chain.
 */
class UnresolvedFeatureChain(
    model : Session,
    relativeName: QualifiedName? = null,
    id: Uuid? = null,
): UnresolvedFeature(model, relativeName, id, elementType = "Unresolved Feature Chain") {
    override fun toString(): String = "Unresolved Feature Chain: $relativeName"
    override fun escapedName(): String? = relativeName
}

class UnresolvedExpression(
    model : Session,
    relativeName: QualifiedName? = null,
    id: Uuid? = null,
) : UnresolvedFeature(model, relativeName, id), Expression
{
    override fun toString(): String = "Unresolved Expression: $relativeName"

    private fun error() : Nothing = throw IllegalStateException("Solver accessed unresolved expression!")

    override val isModelLevelEvaluable: Boolean get() = error()
    override var upQuantity: VectorQuantity
        get() = error()
        set(value) { error() }
    override var downQuantity: VectorQuantity
        get() = error()
        set(value) { error() }

    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean = error()

    override fun evaluate(target: Element): Set<Element> = error()
    override fun checkCondition(target: Element): Boolean = error()

    // this one might lead to unintended exceptions
    override val astString: String get() = error()

    override fun initialize() { error() }

    override fun evalUp() { error() }

    override fun evalDown() { error() }

    override fun evalUpRec() { error() }

    override fun evalDownRec() { error() }

    override fun initType() { error() }

    override fun toAstString(b: StringBuilder, precedence: Int) {
        error()
    }

    override fun clone(): UnresolvedExpression = UnresolvedExpression(model, relativeName, id)
}

/**
 * Unresolved Namespace; must resolve to Namespace.
 */
class UnresolvedNamespace(
    model : Session,
    override var relativeName: QualifiedName? = null,
    override var id: Uuid? = null,
): Unresolved, NamespaceImplementation(model) {
    override fun toString(): String = "Unresolved Namespace: $relativeName"
    override fun escapedName(): String? = relativeName
}