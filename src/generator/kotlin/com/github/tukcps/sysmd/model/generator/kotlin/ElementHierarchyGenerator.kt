package com.github.tukcps.sysmd.model.generator.kotlin

import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Generates the hierarchy of all metamodel types.
 *
 * The generated hierarchy contains only the direct supertypes of each metamodel
 * type. Transitive relationships are resolved later by utility functions.
 *
 * During migration this hierarchy is generated in parallel to the manually
 * maintained implementation.
 */
class ElementHierarchyGenerator : KotlinGenerator() {

    /**
     * Generates the Kotlin source code.
     *
     * @param model MOF metamodel.
     * @return Generated Kotlin source code.
     */
    override fun generate(model: MOFMetaModel): String {

        val writer = KotlinWriter()

        writer.generatedFile("ElementHierarchyGenerator")
        writer.line()
        writer.packageDeclaration("com.github.tukcps.sysmd.model.generated")

        writer.begin("object ElementHierarchy")
        writer.kdoc("Maps each metamodel type to its direct supertypes.")
        writer.line("val directSuperTypes = mapOf(")
        writer.indent()

        model.allClasses()
            .sortedBy { it.name }
            .forEach { clazz -> writer.line(generateHierarchyEntry(model, clazz)) }

        writer.unindent()
        writer.line(")")
        writer.end()

        return writer.toString()
    }

    /**
     * Generates one hierarchy entry.
     *
     * @param model MOF metamodel.
     * @param clazz Metaclass.
     * @return Kotlin source code for one hierarchy entry.
     */
    private fun generateHierarchyEntry(
        model: MOFMetaModel,
        clazz: MOFClass
    ): String {

        val superTypes = clazz.superClassIds
            .mapNotNull(model::findClassById)
            .sortedBy { it.name }

        val rhs = superTypes.joinToString(", ") { "ElementType.${it.name}" }

        return "ElementType.${clazz.name} to setOf($rhs),"
    }
}