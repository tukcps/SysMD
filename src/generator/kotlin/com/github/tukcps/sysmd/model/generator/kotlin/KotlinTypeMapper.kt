package com.github.tukcps.sysmd.model.generator.kotlin

import com.github.tukcps.sysmd.model.generator.mof.MOFAttribute
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel
import com.github.tukcps.sysmd.model.generator.mof.MOFParameter

/**
 * Maps MOF types to Kotlin types.
 */
object KotlinTypeMapper {

    /**
     * Returns the Kotlin type of an attribute.
     *
     * @param model MOF metamodel.
     * @param attribute MOF attribute.
     * @return Kotlin type.
     */
    fun type(
        model: MOFMetaModel,
        attribute: MOFAttribute
    ): String =
        type(
            model = model,
            typeId = attribute.typeId,
            lower = attribute.lower,
            upper = attribute.upper,
            isOrdered = attribute.isOrdered
        )

    /**
     * Returns the Kotlin type of an operation parameter.
     *
     * @param model MOF metamodel.
     * @param parameter MOF parameter.
     * @return Kotlin type.
     */
    fun type(
        model: MOFMetaModel,
        parameter: MOFParameter
    ): String =
        type(
            model = model,
            typeId = parameter.typeId,
            lower = parameter.lower,
            upper = parameter.upper,
            isOrdered = parameter.isOrdered
        )

    /**
     * Returns whether an attribute is represented as a collection.
     *
     * @param attribute MOF attribute.
     * @return Whether the attribute is a collection.
     */
    fun isCollection(attribute: MOFAttribute): Boolean =
        isCollection(attribute.upper)

    /**
     * Returns whether a parameter is represented as a collection.
     *
     * @param parameter MOF parameter.
     * @return Whether the parameter is a collection.
     */
    fun isCollection(parameter: MOFParameter): Boolean = isCollection(parameter.upper)

    /**
     * Returns the Kotlin declaration keyword of an attribute.
     *
     * Collections use immutable property references to mutable collections.
     *
     * @param attribute MOF attribute.
     * @return Kotlin declaration keyword.
     */
    fun declarationKeyword(attribute: MOFAttribute): String =
        if (
            isCollection(attribute) ||
            attribute.isDerived ||
            attribute.isReadOnly
        ) {
            "val"
        } else {
            "var"
        }

    /**
     * Returns a Kotlin type.
     *
     * @param model MOF metamodel.
     * @param typeId Referenced type identifier.
     * @param lower Lower multiplicity bound.
     * @param upper Upper multiplicity bound, or null for unlimited.
     * @param isOrdered Whether the value is ordered.
     * @return Kotlin type.
     */
    private fun type(
        model: MOFMetaModel,
        typeId: String?,
        lower: Int,
        upper: Int?,
        isOrdered: Boolean
    ): String {
        val elementType = elementType(model, typeId)

        return when {
            isCollection(upper) ->
                collectionType(elementType, isOrdered)

            lower == 0 ->
                "$elementType?"

            else ->
                elementType
        }
    }

    /**
     * Returns whether a multiplicity is represented as a collection.
     *
     * @param upper Upper multiplicity bound.
     * @return Whether the multiplicity represents a collection.
     */
    private fun isCollection(upper: Int?): Boolean =
        upper == null || upper > 1

    /**
     * Returns the Kotlin element type of a MOF type.
     *
     * @param model MOF metamodel.
     * @param typeId Referenced type identifier.
     * @return Kotlin element type.
     */
    private fun elementType(
        model: MOFMetaModel,
        typeId: String?
    ): String {
        if (typeId == null) return "Any"

        model.findClassById(typeId)?.let {
            return it.name
        }

        return primitiveType(typeId) ?: referencedType(typeId)
    }

    /**
     * Returns the Kotlin collection type.
     *
     * @param elementType Kotlin element type.
     * @param isOrdered Whether the collection is ordered.
     * @return Kotlin collection type.
     */
    private fun collectionType(
        elementType: String,
        isOrdered: Boolean
    ): String =
        if (isOrdered) {
            "MutableList<$elementType>"
        } else {
            "MutableSet<$elementType>"
        }

    /**
     * Maps a MOF primitive type to its Kotlin type.
     *
     * @param typeId MOF type identifier.
     * @return Kotlin primitive type, or null if the type is not primitive.
     */
    private fun primitiveType(typeId: String): String? =
        when (typeId) {
            "Boolean" -> "Boolean"
            "String" -> "String"
            "Integer" -> "Long"
            "UnlimitedNatural" -> "Long"
            "Real" -> "Double"
            else -> null
        }

    /**
     * Returns the simple name of an unresolved referenced type.
     *
     * @param typeId XMI type identifier.
     * @return Simple referenced type name.
     */
    private fun referencedType(typeId: String): String =
        typeId.substringAfterLast('-')
}