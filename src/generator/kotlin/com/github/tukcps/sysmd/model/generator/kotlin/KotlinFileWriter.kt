package com.github.tukcps.sysmd.model.generator.kotlin

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Writes Kotlin source files.
 */
class KotlinFileWriter {

    /**
     * Writes one generated Kotlin source file.
     *
     * Existing files are overwritten.
     *
     * @param directory Output directory.
     * @param fileName Kotlin file name.
     * @param source Kotlin source code.
     */
    fun writeGenerated(
        directory: Path,
        fileName: String,
        source: String
    ) {
        val file = prepare(directory, fileName)

        writeFile(file, source)
    }

    /**
     * Creates the output directory and resolves the target file.
     *
     * @param directory Output directory.
     * @param fileName File name.
     * @return Target file.
     */
    private fun prepare(
        directory: Path,
        fileName: String
    ): Path {
        Files.createDirectories(directory)
        return directory.resolve(fileName)
    }

    /**
     * Writes a source file and replaces existing content.
     *
     * @param file Target file.
     * @param source Source code.
     */
    private fun writeFile(
        file: Path,
        source: String
    ) {
        Files.writeString(
            file,
            source,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
    }
}