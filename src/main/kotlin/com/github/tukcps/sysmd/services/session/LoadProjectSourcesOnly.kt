package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.compiler.importMD
import io.github.tukcps.sysmlv2.interchange.InterchangeProject


/**
 * Loads a file from the repository, but limits the import to the
 * elements persisted as annotating elements for the notebook.
 * For this purpose, it uses the element navigation service implemented
 * independent of whether the project is in a file on a web project.
 */
fun Session.loadCellsOnly() {

    val elements = SessionManager.elementNavigationService.getElements(project!!, null)

    val documents = elements.filter {
        it.type == "AnnotatingElement" && it.owner?.id == null
    }

    val textualRepresentations = elements.filter {
        it.type == "TextualRepresentation"
    }

    textualRepresentations.forEach {
        it.ownedElement.clear()
        if(it.language=="YaML" && it.body != null) {
            importMD(it.body!!, null)
        }
    }

    import(documents + textualRepresentations)
}