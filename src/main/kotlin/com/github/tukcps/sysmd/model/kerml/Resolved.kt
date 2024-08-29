package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.exceptions.FeatureExpected
import com.github.tukcps.sysmd.exceptions.NamespaceExpected
import com.github.tukcps.sysmd.exceptions.TypeExpected
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmlv2.entities.Identified
import java.util.*


/**
 * The identity of an element, given by
 * - id, a UUID for communication outside a running session
 * - reference, a reference in a running session
 * - name, as given by a user and processed in the parser
 */
class Resolved<out T: Element>(
    id: UUID?,
    var ref: @UnsafeVariance T?,
    var str: QualifiedName?
): Identified(id=id) {
    enum class RefType { ELEMENT, TYPE, NAMESPACE, FEATURE }
    constructor() : this(null, null, null)

    /**
     * Copy-Constructor that copies the reference of the target.
     * @param identified the identity that will be copied.
     */
    constructor(identified: Resolved<T>) : this(id=identified.id, ref=identified.ref, str=identified.str)
    constructor(id: UUID?): this(id=id, ref=null, str=null)

    /**
     * Returns a new Identity object of a given Element of type T.
     * All fields are set, including reference.
     */
    constructor(ref: T): this(id=ref.elementId, ref=ref, str=null)
    constructor(uid: UUID, ref: T?): this(uid, ref, null)
    constructor(str: QualifiedName): this(null, null, str)

    override fun toString() = when {
        ref != null -> ref!!.elementType + " '${if (ref!!.escapedName() != null) ref!!.qualifiedName else ""}'"
        id  != null -> "Unresolved id: " + id.toString()
        str != null -> "Unresolved name: $str?"
        else -> "(none)"
    }


    /**
     * The method resolves the name in the given namespace of the model and returns true if successful.
     * @param namespace the namespace where the name resolution starts.
     * @return true, if the reference was changed and updated.
     */
    @Suppress("UNCHECKED_CAST")
    fun resolveIdentity(namespace: Namespace, expectedType: RefType = RefType.ELEMENT): Boolean {
        // First, let's try to find reference via the given name.
        if (str != null) {
            // Identifiable via qualified name?
            val found = namespace.resolve<Element>(str!!)
            if (found !== ref && found != null) {
                checkTypeOfT(namespace, found, expectedType)
                ref = found as? T
                id = ref?.elementId
                return true
            }
        } else if (id != null) {   // Identifiable via the id?
            val found = namespace.model!![id!!] as T?
            if (found !== ref && found != null ) {
                ref = found
                return true
            }
        }
        if (ref != null)
            id = ref?.elementId
        return false
    }

    /**
     * Equality of two references to elements:
     * - true for sure if references are equal and not null.
     * - also, if ids are equal and not null.
     * - Also, if strings are equal and not null
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Resolved<*>) return false
        if (this.ref != null && other.ref != null && this.ref == other.ref) return true
        else if (this.id != null && other.id != null && this.id == other.id) return true
        else return (this.str == other.str) && (this.id == other.id)
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (ref?.hashCode() ?: 0)
        result = 31 * result + (str?.hashCode() ?: 0)
        return result
    }

    companion object Factory {
        fun <T: Element> copyOfIdentityList(list: MutableCollection<Resolved<T>>): MutableList<Resolved<T>> {
            val copy = mutableListOf<Resolved<T>>()
            list.forEach { copy += (Resolved(it)) }
            copy.forEach { it.ref = null }
            return copy
        }
    }
}

fun checkTypeOfT(namespace: Namespace, ref: Element, expected: Resolved.RefType) {
    when(expected) {
        Resolved.RefType.TYPE -> {
            if (ref !is Type)
                throw TypeExpected("Expecting a type, but '${ref.escapedName()}' is not a type.", element = namespace)
        }
        Resolved.RefType.NAMESPACE -> {
            if (ref !is Namespace)
                throw NamespaceExpected("Expecting a kind of namespace, but '${ref.escapedName()}' is not a namespace.", element = namespace)
        }
        Resolved.RefType.FEATURE -> {
            if (ref !is Feature)
                throw FeatureExpected("Expecting a feature in ${namespace.escapedName()}, but '${ref.escapedName()}' is not a feature.", element = namespace)
        }
        else -> return
    }
}


/**
 * Function that converts a collection of Qualified Names to a Mutable List of Qualified Name Infos.
 */
@JvmName("StringToIdentityList")
fun <T: Element> Collection<QualifiedName>.toIdentityList(): MutableList<Resolved<T>> {
    val result: MutableList<Resolved<T>> = mutableListOf()
    forEach { result.add(Resolved(str = it)) }
    return result
}

@JvmName("UuidToIdentityList")
fun Collection<UUID>.toIdentityList(): MutableList<Resolved<Element>> {
    val result: MutableList<Resolved<Element>> = mutableListOf()
    forEach { result.add(Resolved(id = it)) }
    return result}
