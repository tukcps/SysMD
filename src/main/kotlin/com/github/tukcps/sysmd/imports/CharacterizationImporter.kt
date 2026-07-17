package com.github.tukcps.sysmd.imports

import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.services.util.JsonSupport
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

object CharacterizationImporter {
    /**
     * Imports constraint results from a JSON file.
     *
     * The JSON file contains serialized constraint results including result and
     * reference values, units, evaluation status, and qualified attribute names.
     *
     * @param filePath Path of the JSON file.
     * @return Imported results, or {@code null} if the file cannot be imported.
     */
    fun importJson(
        filePath: String
    ): MutableList<Result>? =
        try {
            val path = Path(filePath)

            val json = SystemFileSystem
                .source(path)
                .buffered()
                .use { it.readString() }

            JsonSupport
                .decode<List<Result>>(json)
                .toMutableList()
        } catch (exception: Exception) {
            logger.error("Could not import constraint results from '$filePath'.", exception)
            null
        }
}