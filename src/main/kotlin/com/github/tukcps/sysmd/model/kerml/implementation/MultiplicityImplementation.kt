package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*


/**
 * The multiplicity is a Feature that is an integer range or set.
 */
class MultiplicityImplementation(
    name: String? = "cardinality",
    shortName: String? = null,
    multiplicity: String = "1 .. 1",
    elementType: String = "Multiplicity"
): Multiplicity, FeatureImplementation(
    declaredName = name,
    declaredShortName = shortName,
    typeConstraint = mutableListOf(multiplicity),
    elementType = elementType
) {

    override val generalization: List<Type>
        get() = listOf(model?.repo?.naturalType?: UnresolvedType("ScalarValues::Natural"))

    override val ownedSpecialization: List<Specialization>
        get() = mutableListOf()

    override fun toString(): String {
        return "[$elementType] = " + try {variable?.vectorQuantity.toString()} catch (_: Exception) { ""}
    }

    override fun updateFrom(template: Element) {
        require (template is Multiplicity)
        this.typeConstraint = template.typeConstraint
    }

    /**
     * Clone creates a copy of all fields, but NOT of the owned elements;
     * only some well-understood owned elements from the metamodel are copied (e.g., Multiplicity, ...).
     * Hence, the consistency of the model is NOT by itself preserved.
     * Function should NOT be used to copy parts of the model;
     * ONLY to create a backup or in really internal logic when there are no or well-understood owned elements.
     * TAKE CARE!
     */
    override fun clone(): Multiplicity {
        return MultiplicityImplementation(
            name=declaredName,
            shortName=declaredShortName,
        ).also {
            it.typeConstraint = typeConstraint
        }
    }
}
