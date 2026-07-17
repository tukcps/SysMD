package com.github.tukcps.sysmd.model.generator.elementdata

/**
 * Information about a MOF attribute used for generation of the element data
 * interface.
 *
 * The information is collected once and subsequently reused during ordering,
 * merging, documentation generation, and Kotlin source generation.
 *
 * @property className Declaring metaclass.
 * @property attributeName Declared MOF attribute.
 * @property kotlinType Generated Kotlin type.
 * @property lower Lower multiplicity bound.
 * @property upper Upper multiplicity bound, or {@code null} for unlimited.
 * @property isReference Whether the attribute represents a structural element
 * reference.
 * @property isDerived Whether the attribute is derived.
 * @property isReadOnly Whether the attribute is read-only.
 * @property classOrder Position of the declaring metaclass within the
 * inheritance hierarchy.
 * @property attributeOrder Position of the attribute within its declaring
 * metaclass.
 */
data class ElementDataPropertyInfo(

    /** Declaring metaclass. */
    val className: String,

    /** Declared MOF attribute. */
    val attributeName: String,

    /** Generated Kotlin type. */
    val kotlinType: String,

    /** Lower multiplicity bound. */
    val lower: Int,

    /** Upper multiplicity bound, or {@code null} for unlimited. */
    val upper: Int?,

    /** Whether the attribute represents a structural element reference. */
    val isReference: Boolean,

    /** Whether the attribute is derived. */
    val isDerived: Boolean,

    /** Whether the attribute is read-only. */
    val isReadOnly: Boolean,

    /** Position of the declaring metaclass. */
    val classOrder: Int,

    /** Position of the attribute within the declaring metaclass. */
    val attributeOrder: Int,
)