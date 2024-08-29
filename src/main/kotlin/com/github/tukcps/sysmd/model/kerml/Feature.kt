package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.expression.AstNode


/**
 * A feature definition incl. multiplicity, as in KerML (mostly).
 * The relationship can refer to an element, or to a property.
 * - multiplicity
 * - instances
 * - ofClass
 * - direction (in, out, inout)
 */
interface Feature: Type {

    val type: List<Resolved<Type>>
    val typing: List<FeatureTyping>
    val ownedTypeFeaturing: List<FeatureTyping>

    enum class FeatureDirectionKind { IN, OUT, INOUT}
    var direction: FeatureDirectionKind
    
    /** Getter and setter for the owned Multiplicity (if any). */
    val multiplicityProperty: Multiplicity?

    /**
     * Getter and setter for the specified multiplicity; via
     * the owned Multiplicity element.
     */
    val multiplicity: IntegerRange

    /** Variable that is true if the feature constrains the source/target of a relationship.*/
    var isEnd: Boolean
    var isComposite: Boolean
    var isPortion: Boolean
    var isUnique: Boolean
    var isOrdered: Boolean
    var isRedefined: Boolean
    var isDerived: Boolean
    var isReadOnly: Boolean

    /**
     * Initialize searches for (qualified) names in the element and
     * adds UId where the search was successful or reports an error where not.
     */
    override fun resolveNames(): Boolean
    override fun clone(): Feature

    /**
     * Features can 'have' a value which is determined by an owned
     * expression --> implied FeatureValue!
     */
    var featureWithValue: AstNode?

    /**
     * Standard-extensions; string-level only.
     * Together, they are serialized as textual representation body
     */
    var unitConstraint: String?
    var typeConstraint: MutableList<String>
    var expression: String?

    /**
     * Features can be references that are represented by an implied owned
     * ReferenceSubsetting.
     */
    val referencedFeature: Resolved<Feature>?

    /**
     * @return true if there are parts that are relevant for solver
     */
    fun isFeatureWithValue(): Boolean = typeConstraint.isNotEmpty()
            || unitConstraint != null || featureWithValue != null || variable != null

    /**
     * A reference to the variable in the constraint solver
     */
    var variable: Variable?
        get() = variables.firstOrNull()
        set(value) { variables = mutableListOf(value) }
    var variables: MutableList<Variable?> // For nested attributes multiple variables are needed
}
