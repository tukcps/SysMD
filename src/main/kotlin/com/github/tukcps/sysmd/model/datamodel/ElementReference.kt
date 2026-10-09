package com.github.tukcps.sysmd.model.datamodel

import com.github.tukcps.sysmd.exceptions.InternalError
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.*
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.services.session.Session
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import nl.adaptivity.xmlutil.core.impl.multiplatform.name
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

/** A reference between serialized Element Data */
@Serializable(with = ElementReferenceSerializer::class)
sealed class ElementReference : Identified
{
    /** The element type this reference should resolve to */
    abstract val kind : IdentificationKind
    protected abstract val name : QualifiedName?
    abstract override fun clone(): ElementReference

    /** Coerces the referenced element to a Relationship */
    fun toUnresolvedRelationship(model : Session) = UnresolvedRelationship(model, this)
    /** Coerces the referenced element to an OwningMembership */
    fun toUnresolvedOwningMembership(model : Session) = UnresolvedOwningMembership(model, this)

    fun toUnresolved(model : Session) = when(kind) {
        IdentificationKind.Element -> UnresolvedElement(model, this)
        IdentificationKind.Namespace -> UnresolvedNamespace(model, this)
        IdentificationKind.Type -> UnresolvedType(model, this)
        IdentificationKind.Feature -> UnresolvedFeature(model, this)
        IdentificationKind.FeatureChain -> UnresolvedFeatureChain(model, this)
        IdentificationKind.Relationship -> UnresolvedRelationship(model, this)
        IdentificationKind.Membership -> UnresolvedMembership(model, this)
        IdentificationKind.OwningMembership -> UnresolvedOwningMembership(model, this)
    }


    inline fun<reified T : Element> toUnresolvedT(model : Session) : T
    {
        val ur = toUnresolved(model)

        try {
            return ur as T
        } catch(ex : Exception) {
            throw InternalError("Reference has kind $kind, but a subtype of ${T::class.name} was expected", element = ur, cause = ex)
        }
    }

    /** An implicit (non-standard) reference to the root namespace */
    @Serializable(with = ToRoot.Serializer::class)
    data object ToRoot : ElementReference() {
        override val id: Nothing? = null
        override val name = null
        override val kind: IdentificationKind = IdentificationKind.Namespace

        override fun clone() = this

        internal object Serializer : EmptyObjectSerializer<ToRoot>(this)

    }

    /** A reference to another element by its ID */
    @Serializable
    data class ByID(
        @SerialName("@id")
        override val id : Uuid,
        @Transient
        override val kind : IdentificationKind = IdentificationKind.Element,
    ) : ElementReference()
    {
        @Transient
        override val name = null

        override fun clone() = copy()

    }

    /** A reference to another element by a name relative to the element making the reference */
    @Serializable // default serializer is correct here
    data class ByName(
        public override val name : QualifiedName,
        @Transient
        override val kind : IdentificationKind = IdentificationKind.Element,
    ) : ElementReference()
    {
        @Transient
        override val id : Nothing? = null

        override fun clone() = copy()
    }

}

object ElementReferenceSerializer : KSerializer<ElementReference> {
    override val descriptor = buildClassSerialDescriptor(ElementReference::class.qualifiedName!!) {
        element<String>("@id", isOptional = true)
        element<String>(ElementReference.ByName::name.name, isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: ElementReference)
    {
        encoder.encodeStructure(descriptor) {
            when(value) {
                is ElementReference.ByID -> encodeStringElement(descriptor, 0, value.id.toString())
                is ElementReference.ByName -> encodeStringElement(descriptor, 1, value.name)
                ElementReference.ToRoot -> {} // empty element, fixme: or null?
            }
        }
    }

    override fun deserialize(decoder: Decoder): ElementReference = decoder.decodeStructure(descriptor) {
        var id : Uuid? = null
        var name : String? = null

        while(true) when(val ix = decodeElementIndex(descriptor))
        {
            CompositeDecoder.DECODE_DONE -> break
            // permitting duplicate fields is in line with the default behavior of generated serializers
            0 -> id = Uuid.parse(decodeStringElement(descriptor, ix))
            1 -> name = decodeStringElement(descriptor, ix)
            else -> throw SerializationException("Invalid index $ix for ${ElementReference::class.simpleName}")
        }


        when {
            // we permit a superfluous name here, which is incongruent with ByID's own serializer...
            id !== null -> ElementReference.ByID(id)
            name !== null -> ElementReference.ByName(name)
            else -> ElementReference.ToRoot
        }
    }
}

/** Serializes a singleton object to the empty JSON object */
open class EmptyObjectSerializer<T : Any>(val instance : T) : KSerializer<T>
{
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(instance::class.qualifiedName!!) {}

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeStructure(descriptor) {}
    }

    override fun deserialize(decoder: Decoder): T = decoder.decodeStructure(descriptor) {
        val ix = decodeElementIndex(descriptor)
        if(ix != CompositeDecoder.DECODE_DONE)
            throw SerializationException("Invalid index $ix for ${instance::class.simpleName}")

        instance
    }
}
