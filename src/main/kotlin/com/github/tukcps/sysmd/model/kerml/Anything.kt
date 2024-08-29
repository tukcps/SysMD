package com.github.tukcps.sysmd.model.kerml

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.implementation.ClassifierImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.session.Session

/**
 * The root of the inheritance tree.
 * Superclass is null.
 */
@Suppress("UNUSED_PARAMETER")
class Anything(
    owner: Resolved<Element>,
    model: Session
): Classifier, ClassifierImplementation(
    elementId = Generators.nameBasedGenerator().generate("Anything"),
    declaredName = "Anything",
    declaredShortName = "Any",
    owner = owner,
) {
    init {
        this.model = model
        isLibraryElement = true
        isStandard = true
    }

    override val ownedSpecialization: List<Specialization> = listOf()

    @Deprecated(
        "Use allSupertypes",
        replaceWith = ReplaceWith("generalization.firstOrNull()"),
        level = DeprecationLevel.WARNING
    )

    override var generalization: List<Resolved<Type>> = mutableListOf()

    override fun resolveNames() = false
    override fun clone(): Anything = this
    override fun toString(): String = "Anything"
}

