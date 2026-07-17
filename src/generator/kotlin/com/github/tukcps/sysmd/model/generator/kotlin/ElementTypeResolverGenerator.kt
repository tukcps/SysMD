package com.github.tukcps.sysmd.model.generator.kotlin

import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Generates runtime metamodel type resolution.
 *
 * The generated extension function maps runtime elements to their corresponding
 * generated metamodel type. Metaclasses are ordered from most specific to most
 * general to ensure correct matching of inherited model interfaces.
 */
class ElementTypeResolverGenerator : KotlinGenerator() {

    /**
     * Generates the runtime element type resolver.
     *
     * @param model MOF metamodel.
     * @return Generated Kotlin source.
     */
    override fun generate(model: MOFMetaModel): String {
        val classes = model.allClasses()
            .sortedWith(
                compareByDescending<MOFClass> {
                    inheritanceDepth(model, it)
                }.thenBy { it.name }
            )

        val writer = KotlinWriter()

        writer.generatedFile("ElementTypeResolverGenerator")
        writer.line()
        writer.packageDeclaration("com.github.tukcps.sysmd.model.generated")

        writer.imports(
            "com.github.tukcps.sysmd.model.generated.ElementType",
            *classes.map {"${KotlinModelResolver.packageName(it)}.${it.name}" }
                .distinct()
                .sorted()
                .toTypedArray()
        )

        writer.line("/**")
        writer.line(" * Returns the metamodel type of this runtime element.")
        writer.line(" *")
        writer.line(" * @return Corresponding element type.")
        writer.line(" */")
        writer.begin("fun Element.elementType(): ElementType")
        writer.begin("return when (this)")

        classes.forEach { clazz ->
            writer.line(
                "is ${clazz.name} -> ElementType.${clazz.name}"
            )
        }

        writer.end()
        writer.end()

        return writer.toString()
    }

    /**
     * Returns the maximum inheritance depth of a metaclass.
     *
     * @param model MOF metamodel.
     * @param clazz Metaclass.
     * @return Inheritance depth.
     */
    private fun inheritanceDepth(
        model: MOFMetaModel,
        clazz: MOFClass
    ): Int {
        val superClasses = clazz.superClassIds
            .mapNotNull(model::findClassById)

        return if (superClasses.isEmpty()) {
            0
        } else {
            1 + superClasses.maxOf {
                inheritanceDepth(model, it)
            }
        }
    }
}