package com.github.tukcps.sysmd.model.generator.elementdata

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinGenerator
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinWriter
import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Generates the common data interface for KerML and SysML elements.
 *
 * The generated interface contains all primitive and enumeration properties
 * together with the structural references required to reconstruct the element
 * graph. Derived properties are excluded unless explicitly configured.
 */
class ElementDataIFGenerator : KotlinGenerator() {

    /**
     * Generates the element data interface.
     *
     * @param model MOF metamodel.
     * @return Generated Kotlin source.
     */
    override fun generate(
        model: MOFMetaModel,
    ): String {

        val classifier = ElementDataClassifier(model)
        val mapper = ElementDataTypeMapper(model)
        val infos = collectPropertyInfos(model, classifier, mapper).toMutableList()
        infos += collectSyntheticPropertyInfos(model)
        val properties = mergeProperties(infos)

        val writer = KotlinWriter()

        writer.generatedFile("ElementDataGenerator")
        writer.line()

        writer.packageDeclaration("com.github.tukcps.sysmd.model.generated")

        writer.imports(
            *GeneratorConfiguration.ELEMENT_DATA_IMPORTS
                .sorted()
                .toTypedArray(),
        )

        writeInterface(writer, properties)

        return writer.toString()
    }

    private fun collectPropertyInfos(
        model: MOFMetaModel,
        classifier: ElementDataClassifier,
        mapper: ElementDataTypeMapper,
    ): List<ElementDataPropertyInfo> {

        val infos = mutableListOf<ElementDataPropertyInfo>()

        val depthCache = mutableMapOf<String, Int>()

        fun depth(clazz: MOFClass): Int =
            depthCache.getOrPut(clazz.id) {
                clazz.superClassIds
                    .mapNotNull(model::findClassById)
                    .maxOfOrNull(::depth)
                    ?.plus(1)
                    ?: 0
            }

        val classIndex = model.allClasses()
            .withIndex()
            .associate { it.value.id to it.index }

        model.allClasses()
            .sortedWith(
                compareBy(::depth)
                    .thenBy { classIndex[it.id] }
            )
            .forEachIndexed { classOrder, clazz ->

                clazz.attributes.forEachIndexed { attributeOrder, attribute ->

                    if (!classifier.isIncluded(clazz, attribute))
                        return@forEachIndexed

                    val isReference =
                        classifier.isStructuralReference(
                            clazz,
                            attribute,
                        )

                    infos += ElementDataPropertyInfo(
                        className = clazz.name,
                        attributeName = attribute.name,
                        kotlinType = mapper.type(
                            attribute,
                            isReference,
                        ),
                        lower = attribute.lower,
                        upper = attribute.upper,
                        isReference = isReference,
                        isDerived = attribute.isDerived,
                        isReadOnly = attribute.isReadOnly,
                        classOrder = classOrder,
                        attributeOrder = attributeOrder,
                    )
                }
            }

        return infos
    }

    /**
     * Merges compatible property declarations.
     *
     * Properties having the same metamodel attribute name and Kotlin type are
     * represented by a single generated property. Type conflicts are resolved
     * by qualifying the property name with the declaring metaclass.
     *
     * @param infos Property information.
     * @return Generated properties.
     */
    private fun mergeProperties(
        infos: List<ElementDataPropertyInfo>,
    ): List<ElementDataProperty> =

        infos
            .groupBy { it.attributeName }
            .flatMap { (attributeName, candidates) ->

                val byType = candidates.groupBy { it.kotlinType }

                byType.map { (kotlinType, typedCandidates) ->
                    property(
                        attributeName = attributeName,
                        kotlinType = kotlinType,
                        infos = typedCandidates,
                        conflicting = byType.size > 1,
                    )
                }
            }
            .sorted()

    /**
     * Creates one generated property.
     *
     * @param attributeName MOF attribute name.
     * @param kotlinType Generated Kotlin type.
     * @param infos Compatible property information.
     * @param conflicting Whether multiple Kotlin types exist.
     * @return Generated property.
     */
    private fun property(
        attributeName: String,
        kotlinType: String,
        infos: List<ElementDataPropertyInfo>,
        conflicting: Boolean,
    ): ElementDataProperty {

        val info = infos.first()

        val name =
            if (conflicting) {
                info.className.replaceFirstChar { it.lowercase() } +
                        attributeName.replaceFirstChar { it.uppercase() }
            } else {
                attributeName
            }

        return ElementDataProperty(
            name = name,
            kotlinType = kotlinType,
            info = info,
        )
    }

    /**
     * Writes the generated element data interface.
     *
     * @param writer Kotlin source writer.
     * @param properties Generated properties.
     */
    private fun writeInterface(
        writer: KotlinWriter,
        properties: List<ElementDataProperty>,
    ) {

        writer.line("/**")
        writer.line(" * Common data properties of KerML and SysML elements.")
        writer.line(" *")
        writer.line(" * Generated from the OMG KerML and SysML metamodels.")
        writer.line(" */")

        writer.begin("interface ElementDataIF")

        writer.line()
        writer.line("/** Metamodel type of this element. */")
        writer.line("var type: ElementType")

        properties.forEach {

            writer.line()

            writeDocumentation(
                writer,
                it,
            )

            writer.line(
                "var ${it.name}: ${it.kotlinType}"
            )
        }

        writer.line()

        writer.end()
    }

    /**
     * Writes the documentation of one generated property.
     *
     * @param writer Kotlin source writer.
     * @param property Generated property.
     */
    private fun writeDocumentation(
        writer: KotlinWriter,
        property: ElementDataProperty,
    ) {

        val info = property.info

        writer.line("/**")
        writer.line(
            " * `${info.attributeName}` declared by " +
                    "`${info.className}`."
        )

        writer.line(
            " * MOF multiplicity: " +
                    "`${info.lower}..${info.upper ?: "*"}`."
        )

        if (info.isReference)
            writer.line(" * Structural element reference.")

        if (info.isDerived)
            writer.line(" * This property is derived.")

        if (info.isReadOnly)
            writer.line(" * This property is read-only.")

        writer.line(" */")
    }
}


fun collectSyntheticPropertyInfos(
    model: MOFMetaModel,
): List<ElementDataPropertyInfo> =

    GeneratorConfiguration.ELEMENT_DATA_SYNTHETIC_PROPERTIES.map {

        val classOrder =
            model.allClasses()
                .indexOfFirst { clazz ->
                    clazz.name == it.className
                }

        ElementDataPropertyInfo(
            className = it.className,
            attributeName = it.name,
            kotlinType = it.type,
            lower = 0,
            upper = 1,
            isReference = false,
            isDerived = false,
            isReadOnly = false,
            classOrder = classOrder,
            attributeOrder = Int.MAX_VALUE,
        )
    }

