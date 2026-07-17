package com.github.tukcps.sysmd.model.generator.kotlin

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import java.nio.file.Path

/**
 * Resolves Kotlin source locations for MOF metaclasses.
 */
object KotlinModelResolver {

    /**
     * Returns the target source directory of a metaclass.
     *
     * @param clazz MOF metaclass.
     * @return Target source directory.
     */
    fun directory(clazz: MOFClass): Path =
        when {
            clazz.name in GeneratorConfiguration.EXPRESSION_CLASSES ->
                GeneratorConfiguration.EXPRESSION_MODEL

            clazz.owningPackage?.root()?.name == "KerML" ->
                GeneratorConfiguration.KERML_MODEL

            clazz.owningPackage?.root()?.name == "SysML" ->
                GeneratorConfiguration.SYSML_MODEL

            else -> error("Unknown metamodel: ${clazz.name}")
        }

    /**
     * Returns the Kotlin package of a metaclass.
     *
     * @param clazz MOF metaclass.
     * @return Kotlin package name.
     */
    fun packageName(clazz: MOFClass): String =
        when {
            clazz.name in GeneratorConfiguration.EXPRESSION_CLASSES ->
                GeneratorConfiguration.EXPRESSION_PACKAGE

            clazz.owningPackage?.root()?.name == "KerML" ->
                GeneratorConfiguration.KERML_PACKAGE

            clazz.owningPackage?.root()?.name == "SysML" ->
                GeneratorConfiguration.SYSML_PACKAGE

            else -> error("Unknown metamodel: ${clazz.name}")
        }
}