package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.dropFirstName


/**
 * A textual model of something in a modeling language.
 * In extension of the SysMLv2 metamodel, we also save here results of the compilation:
 * - errorsByLine in a hashmap lineno -> error message
 * - infoByLine in a hashmap lineno -> info-text
 */
interface TextualRepresentation: AnnotatingElement {
    var language: String

    /**
     * Runs the parser depending on the language field.
     */
    fun compile()
    override fun clone(): TextualRepresentation

    fun getOwnerPrefix(): QualifiedName {
        var namespace =  language.dropFirstName()
        if (namespace.isBlank())
            namespace = "Global"
        return namespace
    }
}