package com.github.tukcps.sysmd.services.util

import kotlinx.serialization.json.Json

/**
 * Shared JSON serialization support.
 */
object JsonSupport {

    /** Shared JSON configuration. */
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
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