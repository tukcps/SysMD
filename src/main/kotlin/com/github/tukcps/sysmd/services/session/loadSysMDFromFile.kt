package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.model.kerml.implementation.MetadataFeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.repositories.local.getCells
import com.github.tukcps.sysmd.model.datamodel.toElement
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem


/**
 * Imports a SysMD project file, including its usages.
 * The file may be in .md format, and will first be pre-processed by an MD parser, and then
 * the code cells will be processed by the SysMD parser.
 * @param path The file to be loaded
 * @param runlevel To which runlevel initialization is done
 */
fun ProjectSession.loadSysMDFromFile(path: Path) {
    if ( SystemFileSystem.metadataOrNull(path)?.isRegularFile != true) {
        if (path.parent != null) { SystemFileSystem.createDirectories(path.parent!!) }
        SystemFileSystem.sink(path).buffered().use {  }
    }

    // Delete a maybe existing annotation
    var fileAnnotation = global.getOwned<MetadataFeatureImplementation>(path.name)
    if (fileAnnotation != null) { delete(fileAnnotation) }

    // first read the MD into the memory; creates only annotations with textual models and description annotations
    fileAnnotation = addOwnedMember(MetadataFeatureImplementation(this, declaredName = path.name), global)
    val cells = path.getCells()
    cells.forEach { cell -> addOwnedMember(cell.toElement(this), fileAnnotation) }
    loadUsages()
}
