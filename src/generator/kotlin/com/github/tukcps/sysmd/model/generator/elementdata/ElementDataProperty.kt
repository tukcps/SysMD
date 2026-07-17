package com.github.tukcps.sysmd.model.generator.elementdata

/**
 * A property of the generated element data interface.
 *
 * A property is obtained by merging one or more compatible MOF attributes.
 * Compatible attributes have the same metamodel name and Kotlin type. If
 * attributes with identical names map to different Kotlin types, the property
 * name is qualified by its declaring metaclass.
 *
 * @property name Generated Kotlin property name.
 * @property kotlinType Generated Kotlin property type.
 * @property info Information about the originating MOF attribute.
 */
data class ElementDataProperty(

    /** Generated Kotlin property name. */
    val name: String,

    /** Generated Kotlin property type. */
    val kotlinType: String,

    /** Information about the originating MOF attribute. */
    val info: ElementDataPropertyInfo
): Comparable<ElementDataProperty> {

    override fun compareTo(
        other: ElementDataProperty,
    ): Int =
        compareValuesBy(
            this,
            other,
            { it.info.isReference },
            { it.info.classOrder },
            { it.info.attributeOrder },
            { it.name },
        )

}