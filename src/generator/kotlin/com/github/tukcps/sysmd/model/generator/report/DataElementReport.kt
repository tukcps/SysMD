package com.github.tukcps.sysmd.model.generator.report

import com.github.tukcps.sysmd.model.generator.kotlin.KotlinTypeMapper
import com.github.tukcps.sysmd.model.generator.mof.MOFAttribute
import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Creates a report about data fields declared by the MOF metamodel.
 *
 * Attributes are grouped by name to estimate a common data representation
 * for KerML and SysML elements.
 *
 * @param model MOF metamodel.
 */
class DataElementReport(
    private val model: MOFMetaModel
) {

    /**
     * Creates the report.
     *
     * @return Report text.
     */
    fun create(): String {
        val attributes = model.allClasses()
            .flatMap { clazz ->
                clazz.attributes.map { attribute ->
                    AttributeDeclaration(clazz, attribute)
                }
            }

        val groups = attributes
            .groupBy { it.attribute.name }
            .toSortedMap()

        return buildString {
            appendLine("============================================================")
            appendLine("SysMD Data Element Report")
            appendLine("============================================================")
            appendLine()
            appendLine("Metaclasses           : ${model.allClasses().size}")
            appendLine("Attribute declarations: ${attributes.size}")
            appendLine("Unique attribute names: ${groups.size}")
            appendLine()

            appendLine("Attributes")
            appendLine("------------------------------------------------------------")

            groups.forEach { (name, declarations) ->
                val types = declarations
                    .map { type(it.attribute) }
                    .distinct()
                    .sorted()

                append(name)
                append(" : ")
                appendLine(types.joinToString(" | "))

                declarations
                    .sortedBy { it.clazz.name }
                    .forEach {
                        appendLine(
                            "    ${it.clazz.name}.${it.attribute.name}"
                        )
                    }
            }

            appendLine()
            appendLine("Type conflicts")
            appendLine("------------------------------------------------------------")

            val conflicts = groups.filterValues { declarations ->
                declarations
                    .map { type(it.attribute) }
                    .distinct()
                    .size > 1
            }

            if (conflicts.isEmpty()) {
                appendLine("    -")
            } else {
                conflicts.forEach { (name, declarations) ->
                    val types = declarations
                        .map { type(it.attribute) }
                        .distinct()
                        .sorted()

                    appendLine(
                        "$name : ${types.joinToString(" | ")}"
                    )
                }
            }
        }
    }

    /**
     * Returns the Kotlin type of attribute.
     *
     * @param attribute MOF attribute.
     * @return Kotlin type.
     */
    private fun type(attribute: MOFAttribute): String =
        KotlinTypeMapper.type(model, attribute)

    /**
     * Attribute declaration in a metaclass.
     *
     * @property clazz Declaring metaclass.
     * @property attribute Declared attribute.
     */
    private data class AttributeDeclaration(
        val clazz: MOFClass,
        val attribute: MOFAttribute
    )
}