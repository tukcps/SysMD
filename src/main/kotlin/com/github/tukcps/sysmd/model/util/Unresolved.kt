package com.github.tukcps.sysmd.model.util

import com.github.tukcps.sysmd.model.datamodel.ElementReference
import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * A reference that is still unresolved, as sealed interface for type safety.
 * @property reference A reference to the actual element, or null for placeholder elements
 * @property input the input stream
 * @property indices indices in the input stream with the relative Name
 */
sealed interface Unresolved: Element {
    val reference : ElementReference?
    override var input: CharSequence?
    override var indices: IntRange?

    override val qualifiedName: QualifiedName? get() = when(val r = reference) {
        is ElementReference.ByID -> null
        is ElementReference.ByName -> r.name
        ElementReference.ToRoot -> "$"
        null -> null
    }

    override fun escapedName(): String? = when(val r = reference)  {
        is ElementReference.ByID -> null
        is ElementReference.ByName -> r.name.split("::").last()
        ElementReference.ToRoot -> "global"
        null -> null
    }
}

/** Effective [toString] overload for [Unresolved] subtypes */
private fun Unresolved.describe() = when(val r = reference) {
    null -> "Placeholder [${elementType()}]"
    else -> "Unresolved [${elementType()}] " + when(r) {
        is ElementReference.ByID -> r.id.toString()
        is ElementReference.ByName -> r.name
        ElementReference.ToRoot -> "GLOBAL"
    }
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
 */
class UnresolvedElement(
    model : Session,
    override val reference : ElementReference? = null,
) : Unresolved, ElementImplementation(model, ) {
    override val qualifiedName: QualifiedName? get() = super<Unresolved>.qualifiedName
    override fun escapedName(): String? = super<Unresolved>.escapedName()
    override fun toString(): String = describe()
    override fun clone() = UnresolvedElement(model, reference)
}

/**
 * Unresolved reference to a Relationship.
 */
class UnresolvedRelationship(
    model : Session,
    override val reference : ElementReference? = null,
) : Unresolved, RelationshipImplementation(model, ) {
    override val qualifiedName: QualifiedName? get() = super<Unresolved>.qualifiedName
    override fun escapedName(): String? = super<Unresolved>.escapedName()
    override fun toString(): String = describe()
    override fun clone() = UnresolvedRelationship(model, reference)
}

/**
 * Unresolved Membership; must resolve to membership.
 */
class UnresolvedMembership(
    model : Session,
    override val reference: ElementReference? = null,
): Unresolved, MembershipImplementation(model) {
    override val qualifiedName: QualifiedName? get() = super<Unresolved>.qualifiedName
    override fun escapedName(): String? = super<Unresolved>.escapedName()
    override fun toString(): String = describe()
    override fun clone() = UnresolvedMembership(model, reference)
}

/**
 * Unresolved Membership; must resolve to membership.
 */
class UnresolvedOwningMembership(
    model : Session,
    override val reference: ElementReference? = null,
): Unresolved, OwningMembershipImplementation(model, elementType = "Unresolved OwningMembership"){
    override val qualifiedName: QualifiedName? get() = super<Unresolved>.qualifiedName
    override fun escapedName(): String? = super<Unresolved>.escapedName()
    override fun toString(): String = describe()
    override fun clone() = UnresolvedOwningMembership(model, reference)
}

/**
 * Unresolved Type; must resolve to type.
 */
class UnresolvedType(
    model : Session,
    override val reference: ElementReference? = null,
): Unresolved, TypeImplementation(model) {
    // convenience constructor
    constructor(model : Session, relativeName : String) : this(model, ElementReference.ByName(relativeName))

    override val qualifiedName: QualifiedName? get() = super<Unresolved>.qualifiedName
    override fun escapedName(): String? = super<Unresolved>.escapedName()
    override fun toString(): String = describe()

    override fun clone() = UnresolvedType(model, reference)
}

/**
 * Unresolved Feature; must resolve to a Feature
 */
open class UnresolvedFeature(
    model : Session,
    override val reference: ElementReference? = null
): Unresolved, FeatureImplementation(model){
    // convenience constructor
    constructor(model : Session, relativeName : String) : this(model, ElementReference.ByName(relativeName))


    override val qualifiedName: QualifiedName? get() = super<Unresolved>.qualifiedName
    override fun escapedName(): String? = super<Unresolved>.escapedName()
    override fun toString(): String = describe()

    override fun clone() = UnresolvedFeature(model, reference)
}

/**
 * Unresolved Feature Chain.
 */
class UnresolvedFeatureChain(
    model : Session,
    reference: ElementReference? = null,
): UnresolvedFeature(model, reference) {
    override fun clone() = UnresolvedFeatureChain(model, reference)
}

class UnresolvedExpression(
    model : Session,
    reference: ElementReference? = null,
) : UnresolvedFeature(model, reference), Expression
{
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

    override fun clone(): UnresolvedExpression = UnresolvedExpression(model, reference)
}

/**
 * Unresolved Namespace; must resolve to Namespace.
 */
class UnresolvedNamespace(
    model : Session,
    override val reference: ElementReference? = null
): Unresolved, NamespaceImplementation(model) {
    override val qualifiedName: QualifiedName? get() = super<Unresolved>.qualifiedName
    override fun escapedName(): String? = super<Unresolved>.escapedName()
    override fun toString(): String = describe()
    override fun clone() = UnresolvedNamespace(model, reference)

}