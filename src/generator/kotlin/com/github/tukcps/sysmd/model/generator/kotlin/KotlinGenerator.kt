package com.github.tukcps.sysmd.model.generator.kotlin

import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

abstract class KotlinGenerator {

    protected val writer = KotlinWriter()

    /**
     * Generates the Kotlin source code.
     *
     * @param model MOF metamodel.
     * @return Generated Kotlin source code.
     */
    abstract fun generate(model: MOFMetaModel): String

    /**
     * Starts a generated Kotlin source file.
     */
    protected fun beginFile() {
        writer.generatedFile("GeneratedElementTypeGenerator")
        writer.line()
    }
}