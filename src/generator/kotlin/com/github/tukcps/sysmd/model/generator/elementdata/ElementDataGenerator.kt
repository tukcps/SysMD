package com.github.tukcps.sysmd.model.generator.elementdata

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinGenerator
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinWriter
import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Generates the mutable element data implementation.
 *
 * The generated data class implements {@code ElementDataIF}. Nullable
 * properties are initialized with {@code null} and multi-valued properties
 * with empty mutable lists.
 */
class ElementDataGenerator : KotlinGenerator() {

    /**
     * Generates the element data implementation.
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

        writer.packageDeclaration(
            "com.github.tukcps.sysmd.model.generated"
        )

        writer.imports(
            *GeneratorConfiguration.ELEMENT_DATA_IMPORTS
                .sorted()
                .toTypedArray(),
        )

        writeDataClass(writer, properties)

        return writer.toString()
    }

    /**
     * Collects all properties participating in generation.
     *
     * @param model MOF metamodel.
     * @param classifier Element classifier.
     * @param mapper Kotlin type mapper.
     * @return Property information.
     */
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

        val classes = model.allClasses()

        val classIndex = classes
            .withIndex()
            .associate { it.value.id to it.index }

        classes
            .sortedWith(
                compareBy<MOFClass>(::depth)
                    .thenBy { classIndex[it.id] }
            )
            .forEachIndexed { classOrder, clazz ->

                clazz.attributes.forEachIndexed {
                        attributeOrder,
                        attribute,
                    ->

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
     * Writes the generated data class.
     *
     * @param writer Kotlin source writer.
     * @param properties Generated properties.
     */
    private fun writeDataClass(
        writer: KotlinWriter,
        properties: List<ElementDataProperty>,
    ) {

        writer.line("/**")
        writer.line(" * Mutable data representation of a KerML or SysML")
        writer.line(" * element.")
        writer.line(" *")
        writer.line(" * Generated from the OMG KerML and SysML metamodels.")
        writer.line(" */")
        writer.line("data class ElementData(")

        writer.line("    override var type: ElementType,")
        writer.line("    override var elementId: Uuid,")

        properties
            .filter { it.name != "elementId" }
            .forEach {
                writeProperty(writer, it)
            }

        writer.line(") : ElementDataIF")
    }

    /**
     * Writes one constructor property.
     *
     * @param writer Kotlin source writer.
     * @param property Generated property.
     */
    private fun writeProperty(
        writer: KotlinWriter,
        property: ElementDataProperty,
    ) {

        val default = defaultValue(property.kotlinType)
        val initializer = default?.let { " = $it" }.orEmpty()

        writer.line(
            "    override var ${property.name}: " +
                    "${property.kotlinType}$initializer,"
        )
    }

    /**
     * Returns the default value of a generated property.
     *
     * @param type Kotlin property type.
     * @return Kotlin default value, or {@code null} if none exists.
     */
    private fun defaultValue(
        type: String,
    ): String? =
        when {
            type.endsWith("?") -> "null"
            type.startsWith("MutableList<") -> "mutableListOf()"
            else -> null
        }
}
