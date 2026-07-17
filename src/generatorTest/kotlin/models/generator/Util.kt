package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel
import com.github.tukcps.sysmd.model.generator.mof.MOFXmiLoader
import java.nio.file.Files
import java.nio.file.Path


fun loadMetaModel(): MOFMetaModel =
    MOFMetaModel().also { model ->
        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.KERML_XMI,
            model
        )
        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.SYSML_XMI,
            model
        )
    }

private fun findGenerated(fileName: String): Path =
    GeneratorConfiguration.MODEL_DIRECTORIES
        .asSequence()
        .map { it.resolve(fileName) }
        .firstOrNull(Files::exists)
        ?: error("Generated file not found: $fileName")