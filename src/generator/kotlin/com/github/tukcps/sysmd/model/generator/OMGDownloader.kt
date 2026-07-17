package com.github.tukcps.sysmd.model.generator

import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Downloads official OMG resources (KerML, SysML, ...).
 *
 * This tool is intended to be executed manually whenever the
 * metamodel should be updated to a newer OMG release.
 */
object OMGDownloader {


    val KERML_FILE =
        Path.of("src/main/resources/metamodel/KerML.xmi")

    val SYSML_FILE =
        Path.of("src/main/resources/metamodel/SysML.xmi")

    fun updateKerML() = ensureDownloaded(GeneratorConfiguration.KERML_SOURCE, KERML_FILE)
    fun updateSysML() = ensureDownloaded(GeneratorConfiguration.SYSML_SOURCE, SYSML_FILE)

    fun updateAll() {
        updateKerML()
        updateSysML()
    }

    fun ensureDownloaded(url: String, target: Path) {
        if (Files.exists(target))
            return

        download(url, target)
    }

    fun download(url: String, target: Path) {

        Files.createDirectories(target.parent)

        println("Downloading")
        println("  $url")
        println("→ $target")

        URI.create(url)
            .toURL()
            .openStream()
            .use { input ->
                Files.copy(
                    input,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }

        println("Done.")
    }
}