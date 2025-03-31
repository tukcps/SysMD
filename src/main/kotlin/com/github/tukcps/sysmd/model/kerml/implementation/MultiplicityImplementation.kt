package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import java.util.*


/**
 * The multiplicity is a Feature that is an integer range or set.
 */
class MultiplicityImplementation(
    elementId: UUID = UUID.randomUUID(),
    name: String? = "multiplicity",
    shortName: String? = null,
    multiplicity: String = "1 .. 1",
    elementType: String = "Multiplicity"
): Multiplicity, FeatureImplementation(
    declaredName = name,
    declaredShortName = shortName,
    typeConstraint = mutableListOf(multiplicity),
    elementType = elementType
) {
    override fun resolveNames(): Boolean {
        super.resolveNames()
        return updated
    }

    override val generalization: List<Resolved<Type>>
        get() = listOf(Resolved(str="ScalarValues::Natural", ref = model!!.repo.naturalType, id=model?.repo?.naturalType?.elementId))

    override val ownedSpecialization: List<Specialization>
        get() = mutableListOf()

    override fun toString(): String {
        return "Multiplicity { constraint=$typeConstraint, value=${variable?.valueStr} }"
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
