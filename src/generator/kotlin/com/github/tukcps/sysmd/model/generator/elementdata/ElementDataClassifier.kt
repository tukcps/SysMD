package com.github.tukcps.sysmd.model.generator.elementdata

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.mof.MOFAttribute
import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Classifies MOF attributes for generation of the element data interface.
 *
 * Primitive and enumeration attributes are treated as element data. Selected
 * structural references describing the element graph are included as
 * structural references. Derived attributes are excluded by default.
 *
 * @param model MOF metamodel.
 */
class ElementDataClassifier(
    private val model: MOFMetaModel,
) {

    /** Cached structural references of metaclasses. */
    private val referenceCache = mutableMapOf<String, Set<String>>()

    /**
     * Returns whether the attribute is included in the generated element data.
     *
     * @param clazz Declaring metaclass.
     * @param attribute MOF attribute.
     * @return Whether the attribute is included.
     */
    fun isIncluded(
        clazz: MOFClass,
        attribute: MOFAttribute,
    ): Boolean = when {
        isStructuralReference(clazz, attribute) -> true
        attribute.name in GeneratorConfiguration.ELEMENT_DATA_DERIVED_PROPERTIES -> true
        attribute.isDerived -> false
        else -> isData(attribute)
    }

    /**
     * Returns whether an attribute represents primitive or enumeration data.
     *
     * @param attribute MOF attribute.
     * @return Whether the attribute represents data.
     */
    fun isData(
        attribute: MOFAttribute,
    ): Boolean {

        val type =
            attribute.typeId
                ?.let(model::findClassById)
                ?: return true

        return type.name in DATA_TYPES ||
                type.name.endsWith("Kind")
    }

    /**
     * Returns whether the attribute is a structural element reference.
     *
     * Structural references configured for superclasses are inherited.
     *
     * @param clazz Declaring metaclass.
     * @param attribute MOF attribute.
     * @return Whether the attribute is a structural reference.
     */
    fun isStructuralReference(
        clazz: MOFClass,
        attribute: MOFAttribute,
    ): Boolean {

        if (attribute.name !in structuralReferences(clazz))
            return false

        val type =
            attribute.typeId
                ?.let(model::findClassById)
                ?: return false

        return isElement(type)
    }

    /**
     * Returns all configured structural references of a metaclass.
     *
     * References configured for superclasses are inherited.
     *
     * @param clazz Metaclass.
     * @return Structural reference names.
     */
    private fun structuralReferences(
        clazz: MOFClass,
    ): Set<String> =
        referenceCache.getOrPut(clazz.id) {

            buildSet {

                clazz.superClassIds
                    .mapNotNull(model::findClassById)
                    .forEach {
                        addAll(structuralReferences(it))
                    }

                addAll(
                    GeneratorConfiguration.ELEMENT_DATA_REFERENCES[
                        clazz.name
                    ].orEmpty()
                )
            }
        }

    /**
     * Returns whether the metaclass specializes Element.
     *
     * @param clazz Metaclass.
     * @return Whether the metaclass is an element.
     */
    private fun isElement(
        clazz: MOFClass,
    ): Boolean =
        clazz.name == "Element" ||
                clazz.superClassIds
                    .mapNotNull(model::findClassById)
                    .any(::isElement)

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

