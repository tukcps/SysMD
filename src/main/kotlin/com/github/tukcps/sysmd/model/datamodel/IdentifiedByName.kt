package com.github.tukcps.sysmd.model.datamodel

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.model.util.*
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.services.session.Session
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Different kind of identification results.
 */
enum class IdentificationKind {
    Element,
    Namespace,
    Type,
    Feature,
    FeatureChain, // Reference to feature
    Relationship,
    Membership,
    OwningMembership
}

/**
 * An unresolved reference.
 * @param name A name that is not yet resolved to an element with elementId.
 * @param resolvesTo The type to which it must resolve.
 * @param indices Optionally, the indices in the input.
 * @param input Optionally, the input.
 */
@Serializable
data class IdentifiedByName(
    var name : QualifiedName,
    var resolvesTo : IdentificationKind,
    @Serializable(with = IntRangeSerializer::class)
    var indices: IntRange? = null,
    var input: CharSequence? = null
): Identified {
    override var id: Uuid? = null

    override fun toString(): String {
        val ids = if (id != null) "id" else null
        val identified = if (ids != null) id else name
        return "[IdentifiedByName] $identified : $resolvesTo"
    }

    fun toUnresolved(model : Session): Unresolved = when(resolvesTo) {
        IdentificationKind.Element -> UnresolvedElement(model, relativeName = name, id = id)
        IdentificationKind.Namespace -> UnresolvedNamespace(model, relativeName = name, id = id)
        IdentificationKind.Type -> UnresolvedType(model, relativeName = name, id = id)
        IdentificationKind.Feature -> UnresolvedFeature(model, relativeName = name, id = id)
        IdentificationKind.FeatureChain -> UnresolvedFeatureChain(model, relativeName = name, id = id)
        IdentificationKind.Relationship -> UnresolvedRelationship(model, relativeName = name, id = id)
        IdentificationKind.Membership -> UnresolvedMembership(model, relativeName = name, id = id)
        IdentificationKind.OwningMembership -> UnresolvedOwningMembership(model, relativeName = name, id = id)
    }.also {
        it.indices = indices
        it.input = input
    }

    override fun clone(): IdentifiedByName = IdentifiedByName(name, resolvesTo, indices, input)
}

/**
 * Helper that builds a IdentifiedByName model from the name and the context in
 * KerML.
 */
fun KerML.elementByName(name: QualifiedName) = IdentifiedByName(
    name = name,
    resolvesTo = IdentificationKind.Element,
    indices = token.indices,
    input = input
)


/**
 * Helper that builds a IdentifiedByName model from the name and the context in
 * KerML.
 */
fun KerML.featureByName(name: QualifiedName) = IdentifiedByName(
    name = name,
    resolvesTo = IdentificationKind.Feature,
    indices = token.indices,
    input = input
)


/**
 * Helper that builds a IdentifiedByName model from the name and the context in
 * KerML.
 */
fun KerML.typeByName(name: QualifiedName) = IdentifiedByName(
    name = name,
    resolvesTo = IdentificationKind.Type,
    indices = token.indices,
    input = input
)

/**
 * Helper that builds a IdentifiedByName model from the name and the context in
 * KerML.
 */
fun KerML.namespaceByName(name: QualifiedName) = IdentifiedByName(
    name = name,
    resolvesTo = IdentificationKind.Namespace,
    indices = token.indices,
    input = input
)


