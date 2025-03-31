package com.github.tukcps.sysmd.model.kerml


/**
 * The multiplicity is a Feature that is an integer range or set.
 */
interface Multiplicity: Feature {
    override fun resolveNames(): Boolean

    /**
     * initializes the fields quantity, up- and downQuantity, and valueSpec.
     */
    override fun toString(): String
    override fun updateFrom(template: Element)

    /**
     * Clone creates a copy of all fields, but NOT of the owned elements;
     * only some well-understood owned elements from the metamodel are copied (e.g. Multiplicity, ...).
     * Hence, the consistency of the model is NOT by itself preserved.
     * Function should NOT be used to copy parts of the model;
     * ONLY to create a backup or in really internal logic when there are no or well-understood owned elements.
     * TAKE CARE!
     */
    override fun clone(): Multiplicity
}