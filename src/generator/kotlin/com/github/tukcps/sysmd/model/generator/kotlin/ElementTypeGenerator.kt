package com.github.tukcps.sysmd.model.generator.kotlin

import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Generates the temporary GeneratedElementType enumeration from a MOF metamodel.
 *
 * During migration this enumeration is generated in parallel to the manually
 * maintained ElementType enumeration. After validation, it will replace the
 * manually maintained version.
 */
class ElementTypeGenerator : KotlinGenerator() {

    override fun generate(model: MOFMetaModel): String {

        beginFile()
        writer.packageDeclaration("com.github.tukcps.sysmd.model.generated")

        writer.begin("enum class ElementType")

        model.allClasses()
            .sortedBy { it.name }
            .forEachIndexed { index, clazz ->

                writer.line(
                    clazz.name + if (index < model.allClasses().lastIndex) "," else ""
                )
            }

        writer.line(";")
        writer.line()
        writer.begin("companion object")
        writer.line("private val byName = entries.associateBy { it.name }")
        writer.line()
        writer.begin("fun fromString(name: String): ElementType?")
        writer.line("return byName[name]")
        writer.end()
        writer.end()
        writer.end()

        return writer.toString()
    }
}