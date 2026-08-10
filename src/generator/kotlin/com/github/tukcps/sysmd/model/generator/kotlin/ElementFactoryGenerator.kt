package com.github.tukcps.sysmd.model.generator

import com.github.tukcps.sysmd.model.generator.kotlin.KotlinGenerator
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinWriter
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

class ElementFactoryGenerator : KotlinGenerator() {

    /**
     * Generates the element factory source.
     *
     * @param model MOF metamodel.
     * @return Generated Kotlin source.
     */
    override fun generate(model: MOFMetaModel): String {
        val classes = model.allClasses()
            .filterNot { it.isAbstract }
            .sortedBy { it.name }

        val writer = KotlinWriter()

        writer.generatedFile("ElementFactoryGenerator")
        writer.line()
        writer.packageDeclaration("com.github.tukcps.sysmd.model.generated")

        writer.imports(
            "com.github.tukcps.sysmd.model.generated.ElementType",
            "com.github.tukcps.sysmd.model.kerml.Element",
            "com.github.tukcps.sysmd.model.kerml.implementation.*",
            "com.github.tukcps.sysmd.model.sysml.implementation.*",
            "com.github.tukcps.sysmd.model.expression.implementation.*",
            "com.github.tukcps.sysmd.services.session.Session",
            "kotlin.uuid.Uuid"
        )

        writer.line("/**")
        writer.line(" * Factory for Elements. ")
        writer.line(" * For configuration, check the file GeneratorConfiguration.")
        writer.line(" * @param type ElementType, for which an Element will be created.")
        writer.line(" */")
        writer.begin("fun createElement(type: ElementType, model : Session, id : Uuid): Element = when(type)")

        classes.forEach { clazz ->
            val remap = GeneratorConfiguration.REMAPPED_CONSTRUCTORS[clazz.name]

            writer.line(when {
                remap === null -> "ElementType.${clazz.name} -> ${clazz.name}Implementation(model, elementId = id)"
                remap.isEmpty -> "// ElementType.${clazz.name} -> ${clazz.name}Implementation(model, elementId = id)"
                else -> "ElementType.${clazz.name} -> ${remap.get()}(model, elementId = id)"
            })
        }
        writer.line("else -> throw Exception(\"Cannot instantiate abstract element type\")")
        writer.end()

        return writer.toString()
    }
}