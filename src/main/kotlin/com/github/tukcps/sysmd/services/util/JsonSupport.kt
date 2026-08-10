package com.github.tukcps.sysmd.services.util

import com.github.tukcps.sysmd.model.datamodel.IdentifiedByName
import com.github.tukcps.sysmd.model.datamodel.IdentifiedImplementation
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Shared JSON serialization support.
 */
object JsonSupport {

    /** Shared JSON configuration. */
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {
            polymorphic(Identified::class) {
                subclass(IdentifiedImplementation::class)
                subclass(IdentifiedByName::class)
            }
        }
    }

    /**
     * Serializes an object to JSON.
     *
     * @param value Object.
     * @return JSON representation.
     */
    inline fun <reified T> encode(value: T): String =
        json.encodeToString(value)

    /**
     * Deserializes an object from JSON.
     *
     * @param text JSON.
     * @return Object.
     */
    inline fun <reified T> decode(text: String): T =
        json.decodeFromString(text)
}