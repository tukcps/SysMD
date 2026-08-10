package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


/**
 * The multiplicity is a Feature that is an integer range or set.
 */
open class MultiplicityImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    name: String? = "cardinality",
    shortName: String? = null,
): Multiplicity, FeatureImplementation(
    model,
    elementId = elementId,
    declaredName = name,
    declaredShortName = shortName
) {

    /**
     * The range is a natural, maybe bounded by some constraints.
     * A MultiplicityRange gives upper and lower value.
     */
    override val generalization: List<Type>
        get() = listOf(model.repo.naturalType?: UnresolvedType(model, "ScalarValues::Natural"))

    override val ownedSpecialization: List<Specialization>
        get() = mutableListOf()

    override fun toString(): String {
        return "[${elementType().name}] = " + try {variable?.vectorQuantity.toString()} catch (_: Exception) { ""}
    }

    override fun updateFrom(template: Element) {
        check (template is Multiplicity)
        super.updateFrom(template)
    }

    /**
     * Clone creates a copy of all fields, but NOT of the owned elements;
     * only some well-understood owned elements from the metamodel are copied (e.g., Multiplicity, ...).
     * Hence, the consistency of the model is NOT by itself preserved.
     * Function should NOT be used to copy parts of the model;
     * ONLY to create a backup or in really internal logic when there are no or well-understood owned elements.
     * TAKE CARE!
     */
    override fun clone(): Multiplicity = MultiplicityImplementation(model).also {
        it.updateFrom(this)
    }
}
