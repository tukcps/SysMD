package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotatingElementImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.getCells
import com.github.tukcps.sysmd.services.repositories.local.toElement
import java.io.File


/**
 * Imports a SysMD project file, including its usages.
 * The file may be in .md format, and will first be pre-processed by an MD parser, and then
 * the code cells will be processed by the SysMD parser.
 * @param file The file to be loaded
 * @param initialize Whether to do also initialization and first propagation or only name resolution
 */
fun Session.loadSysMDFromFile(file: File, compile: Boolean, initialize: Int = 1) {

    /**
     * Recursively walk through the owned elements of type TextualRepresentation and language SysMD.
     * Compile them, while considering usage of other libraries/projects, load them, etc.
     */
    fun compileCell(element: Element) {
        element.ownedElement.forEach {
            val ownedElement = get(it.id!!) as Element
            if (ownedElement is TextualRepresentation
                && (ownedElement.language.startsWith("SysMD")
                        ||ownedElement.language.startsWith("SysML")
                        ||ownedElement.language.startsWith("KerML"))) {
                ownedElement.compile()
            }
        }
    }

    if ( ! file.exists() ) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }


    // first read the MD into the memory; creates only annotations with textual models and description annotations
    val fileAnnotation = createOrReplace(AnnotatingElementImplementation(declaredName = file.name), global)
    val cells = file.getCells()
    cells.forEach { cell -> create(cell.toElement(), fileAnnotation) }
    loadUsages()
    if (compile) compileCell(fileAnnotation)
    initialize(initialize)
}
