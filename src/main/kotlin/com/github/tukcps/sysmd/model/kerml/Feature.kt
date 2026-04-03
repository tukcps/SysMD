package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.cspsolver.Variable
import io.github.tukcps.aadd.values.IntegerRange


/**
 * A feature definition incl. multiplicity, as in KerML (mostly).
 * The relationship can refer to an element, or to a property.
 * - multiplicity
 * - instances
 * - ofClass
 * - direction (in, out, inout)
 */
interface Feature: Type {

    val type: List<Type>
    val typing: List<FeatureTyping>
    val ownedTypeFeaturing: List<FeatureTyping>

    enum class FeatureDirectionKind { IN, OUT, INOUT}
    var direction: FeatureDirectionKind

    /**
     * Getter and setter for the specified multiplicity; via
     * the owned Multiplicity element.
     */
    val multiplicityRange: IntegerRange

    /** Variable that is true if the feature constrains the source/target of a relationship.*/
    var isEnd: Boolean
    var isComposite: Boolean
    var isPortion: Boolean
    var isUnique: Boolean
    var isOrdered: Boolean
    var isDerived: Boolean
    var isReadOnly: Boolean
    /** True if the feature value was assigned with ':=' or 'default =', meaning it can be overridden in subtypes. */
    var isDefaultValue: Boolean
    /** True if the feature value was assigned with ':=' or '=', meaning it's an initial value assignment. */
    var isInitialValue: Boolean

    /**
     * Gets the redefining
     */
    val redefining: Feature?
        get() = ownedRelationship.filterIsInstance<Redefinition>().firstOrNull()?.redefinedFeature

    override fun clone(): Feature

    /**
     * Standard-extensions; string-level only.
     * Together, they are serialized as textual representation body
     */
    val unitConstraint: String?
    var typeConstraint: MutableList<String>
    var expression: String?

    /**
     * Features can be references that are represented by an implied owned
     * ReferenceSubsetting.
     */
    val referencedFeature: Feature?

    /**
     * @return true if there are parts that are relevant for solver
     */
    fun isFeatureWithValue(): Boolean = typeConstraint.isNotEmpty()
            || unitConstraint != null || variable != null

    /**
     * A reference to the variable in the constraint solver
     */
    val variable: Variable?
}
