package com.github.tukcps.sysmd.model.datamodel

import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.model.generated.ElementHierarchy.directSuperTypes
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import kotlin.uuid.Uuid

/**
 * Copies all matching properties from this [ElementDataIF] instance into a new
 * instance of the target type [T].
 *
 * This function uses runtime reflection to dynamically determine the serializer
 * of the concrete implementation class, avoiding serialization failures when
 * called on the interface reference.
 *
 * @param T The target type implementing [ElementDataIF] to instantiate.
 * @param overrides Optional custom field overrides or specific parameters for the target class.
 * @return A new instance of [T] populated with the properties of this instance and any overrides.
 * @throws IllegalArgumentException If no serializer is found for the concrete runtime class.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : ElementDataIF> ElementDataIF.createFrom(
    overrides: Map<String, JsonElement> = emptyMap()
): T {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(Identified::class) {
                subclass(IdentifiedImplementation::class)
                subclass(IdentifiedByName::class)
            }
        }
    }
    // 1. Force retrieval of the concrete class serializer directly,
    // bypassing the polymorphic scope matching of the parent interface.
    val runtimeSerializer = serializer(this::class.java)

    // 2. Convert the current runtime instance into an in-memory map using its specific serializer
    val sourceMap = jsonConfig.encodeToJsonElement(runtimeSerializer, this)
        .jsonObject
        .toMutableMap()

    // 3. Override or complement class-specific fields (e.g., auto-generated IDs)
    sourceMap.putAll(overrides)

    // 4. Decode the combined map directly into the requested target class
    return jsonConfig.decodeFromJsonElement(JsonObject(sourceMap))
}




/**
 * Merges properties from this [ElementDataIF] instance into an [existing] instance of type [T].
 *
 * Properties present in this instance will overwrite matching properties in the [existing] instance.
 * Any properties unique to the [existing] instance (or not present in this source instance)
 * will be preserved.
 *
 * @param T The concrete type implementing [ElementDataIF] that is being updated.
 * @param existing The baseline instance whose properties will be overwritten by this instance.
 * @param overrides Optional custom field overrides to apply at the very end of the merge process.
 * @return A new instance of [T] representing the merged result of both objects.
 */
inline fun <reified T : ElementDataIF> ElementDataIF.copyIntoExisting(
    existing: T,
    overrides: Map<String, JsonElement> = emptyMap()
): T {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
    }

    // 1. Serialize the existing instance to use its values as the baseline map
    val targetMap = jsonConfig.encodeToJsonElement(existing).jsonObject.toMutableMap()

    // 2. Serialize this source instance and overwrite matching keys in the baseline map
    val sourceMap = jsonConfig.encodeToJsonElement(this).jsonObject
    targetMap.putAll(sourceMap)

    // 3. Inject any explicit manual overrides (e.g., timestamps or specific IDs)
    targetMap.putAll(overrides)

    // 4. Decode the final merged JSON object into a new instance of the target type
    return jsonConfig.decodeFromJsonElement(JsonObject(targetMap))
}


/**
 * Updates an [existing] instance of type [T] with non-null properties from this [ElementDataIF] instance.
 *
 * Unlike a standard copy operation, this function performs a partial update (patch):
 * - Properties with active values in this source instance will overwrite the [existing] values.
 * - Properties that are `null` or missing in this source instance will NOT overwrite the [existing] values.
 *
 * @param T The concrete type implementing [ElementDataIF] that is being updated.
 * @param existing The baseline instance containing the current state to be patched.
 * @param overrides Optional custom field overrides to enforce specific values regardless of source state.
 * @return A new instance of [T] containing the updated and merged state.
 */
inline fun <reified T : ElementDataIF> ElementDataIF.updateExisting(
    existing: T,
    overrides: Map<String, JsonElement> = emptyMap()
): T {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
    }

    // 1. Base map from the current existing instance
    val targetMap = jsonConfig.encodeToJsonElement(existing).jsonObject.toMutableMap()

    // 2. Source map containing the requested changes
    val sourceMap = jsonConfig.encodeToJsonElement(this).jsonObject

    // 3. Filter out all null values from the source to prevent overwriting existing data
    val nonNullSourceChanges = sourceMap.filterValues { element ->
        element !is JsonNull
    }

    // 4. Apply only the valid changes to the base map
    targetMap.putAll(nonNullSourceChanges)

    // 5. Apply explicit manual overrides at the very end
    targetMap.putAll(overrides)

    // 6. Decode back into the target class
    return jsonConfig.decodeFromJsonElement(JsonObject(targetMap))
}


object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("IntRange") {
        element<Int>("start")
        element<Int>("endInclusive")
    }

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.first)
            encodeIntElement(descriptor, 1, value.last)
        }
    }

    override fun deserialize(decoder: Decoder): IntRange {
        return decoder.decodeStructure(descriptor) {
            var start = 0
            var endInclusive = 0
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> start = decodeIntElement(descriptor, 0)
                    1 -> endInclusive = decodeIntElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            start..endInclusive
        }
    }
}


/**
 * Extension property to extract the UUID version directly from its bits.
 */
val Uuid.version: Int
    get() = this.toLongs { msb, _ ->
        ((msb ushr 12) and 0x0F).toInt()
    }

/**
 * Extension property to extract the UUID variant from its least significant bits.
 *
 * Returns:
 * - 0: Reserved for NCS backward compatibility
 * - 2: IETF / RFC 4122 / RFC 9562 (Standard for most UUIDs, including v4, v5, v7)
 * - 6: Reserved for Microsoft backward compatibility (GUID)
 * - 7: Reserved for future definition
 */
val Uuid.variant: Int
    get() = this.toLongs { _, lsb ->
        val variantBits = (lsb ushr 61).toInt()
        when {
            (variantBits and 0x04) == 0 -> 0 // 0xx
            (variantBits and 0x06) == 4 -> 2 // 10x (Standard)
            (variantBits and 0x07) == 6 -> 6 // 110
            else -> 7                        // 111
        }
    }



/**
 * Returns whether the given type is equal to or specializes another type.
 *
 * @param type Candidate subtype.
 * @param superType Candidate supertype.
 * @return Whether {@code type} specializes {@code superType}.
 */
fun isSubclassOf(
    type: ElementType,
    superType: ElementType,
): Boolean {

    if (type == superType)
        return true

    return directSuperTypes[type]
        ?.any { isSubclassOf(it, superType) }
        ?: false
}

/**
 * Returns whether the given metamodel type specializes another type.
 *
 * @param typeName Candidate subtype.
 * @param superTypeName Candidate supertype.
 * @return Whether the first type specializes the second.
 */
fun isSubclassOf(
    typeName: String,
    superTypeName: String,
): Boolean {

    val type = ElementType.fromString(typeName) ?: return false
    val superType = ElementType.fromString(superTypeName) ?: return false

    return isSubclassOf(type, superType)
}