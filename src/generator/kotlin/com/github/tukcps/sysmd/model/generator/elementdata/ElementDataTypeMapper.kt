package com.github.tukcps.sysmd.model.generator.elementdata

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinTypeMapper
import com.github.tukcps.sysmd.model.generator.mof.MOFAttribute
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Maps MOF attributes to Kotlin types used by the generated element data
 * interface.
 *
 * Primitive and enumeration attributes are mapped using the common Kotlin type
 * mapper. Structural element references are represented by {@code Identified}
 * values.
 *
 * @param model MOF metamodel.
 */
class ElementDataTypeMapper(
    private val model: MOFMetaModel,
) {

    /**
     * Returns whether an attribute represents primitive or enumeration data.
     *
     * @param attribute MOF attribute.
     * @return Whether the attribute contains data.
     */
    fun isDataType(
        attribute: MOFAttribute,
    ): Boolean {

        val type = attribute.typeId
            ?.let(model::findClassById)
            ?: return true

        return type.name in DATA_TYPES ||
                type.name.endsWith("Kind")
    }

    /**
     * Returns the Kotlin type of the generated property.
     *
     * @param attribute MOF attribute.
     * @param isReference Whether the attribute is a structural reference.
     * @return Kotlin property type.
     */
    fun type(
        attribute: MOFAttribute,
        isReference: Boolean,
    ): String {

        if (isReference)
            return referenceType(attribute)

        val kotlinType =
            GeneratorConfiguration.ELEMENT_DATA_TYPE_OVERRIDES[
                attribute.name
            ] ?: KotlinTypeMapper.type(
                model,
                attribute,
            )

        return when {

            attribute.name in
                    GeneratorConfiguration.REQUIRED_ELEMENT_DATA_PROPERTIES ->
                kotlinType.removeSuffix("?")

            isMultiValued(attribute) ->
                kotlinType

            else ->
                kotlinType.removeSuffix("?") + "?"
        }
    }

    /**
     * Returns the Kotlin type of a structural element reference.
     *
     * @param attribute MOF attribute.
     * @return Kotlin reference type.
     */
    private fun referenceType(
        attribute: MOFAttribute,
    ): String =
        if (isMultiValued(attribute))
            "MutableList<Identified>"
        else
            "Identified?"

    /**
     * Returns whether an attribute is multi-valued.
     *
     * @param attribute MOF attribute.
     * @return Whether the attribute is multi-valued.
     */
    private fun isMultiValued(
        attribute: MOFAttribute,
    ): Boolean =
        attribute.upper == null || attribute.upper > 1

    companion object {

        /** Primitive metamodel data types. */
        private val DATA_TYPES = setOf(
            "Boolean",
            "Integer",
            "Real",
            "String",
            "UnlimitedNatural",
        )
    }
}