package com.github.tukcps.sysmd.model.kerml.implementation

import io.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*

/**
 * A feature definition including Multiplicity, as in KerML (mostly).
 */
open class FeatureImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: String? = null,
    final override var direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    final override var isEnd: Boolean = false,
    final override var isComposite: Boolean = true,
    final override var isPortion: Boolean = false,
    final override var isSufficient: Boolean = false,
    final override var isUnique: Boolean = false,
    final override var isOrdered: Boolean = false,
    final override var isRedefined: Boolean = false,
    final override var isDerived: Boolean = false,
    override var isReadOnly: Boolean = false,
    textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "Feature",
    override var unitConstraint: String? = null,
    override var typeConstraint: MutableList<String> = mutableListOf(),
    override var expression: String? = null,
): Feature, TypeImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    textualRepresentation = textualRepresentation,
    elementType = elementType
){
    override val type: List<Resolved<Type>>
        get() = generalization

    override val typing: List<FeatureTyping>
        get() = getOwnedElementsOfType()

    override val ownedTypeFeaturing: List<FeatureTyping>
        get() = getOwnedElementsOfType()

    /** Getter and setter for the owned Multiplicity (if any). */
    override val multiplicityProperty: Multiplicity?
        get() = getOwnedElementOfType()

    /** Getter and setter for the specified multiplicity. */
    override var multiplicity: IntegerRange
        get() = IntegerRange(multiplicityProperty?.typeConstraint?.firstOrNull()?:"1..1")
        set(value) { multiplicityProperty?.variable?.valueSpecs = mutableListOf(value)}

    /**
     * Initialize searches for (qualified) names in the element and
     * adds UId where the search was successful or reports an error where not.
     */
    override fun resolveNames(): Boolean {
        //
        // We also need to add multiplicity as property.
        // Add multiplicity as owned property is done in create-function of Session!
        //
        updated = super.resolveNames() or updated
        return updated
    }

    /**
     * Just string representation for debugging.
     */
    override fun toString() =
        "$elementType { name=${escapedName()}, type=${type}, multiplicity=${multiplicity} }"

    override fun clone(): Feature {
        val klon = FeatureImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            isEnd = isEnd,
            direction = direction,
            isComposite =isComposite,
            isPortion = isPortion,
            isSufficient = isSufficient,
            isUnique = isUnique,
            isOrdered = isOrdered,
            isDerived = isDerived,
        ).also { klon ->
            klon.model = model
            klon.updated = updated
            klon.typeConstraint = typeConstraint
            klon.unitConstraint = unitConstraint
            klon.expression = expression
            klon.isAbstract = isAbstract
        }
        return klon
    }

    override var featureWithValue: AstNode? = null

    final override val referencedFeature: Resolved<Feature>?
        get() = getOwnedElementOfType<ReferenceSubsetting>()?.referencedFeature

    override fun updateFrom(template: Element) {
        if (template is Feature) {
            direction = template.direction
            isSufficient = template.isSufficient
            isComposite = template.isComposite
            isPortion = template.isPortion
            typeConstraint = template.typeConstraint
            unitConstraint = template.unitConstraint
            expression = template.expression
            isEnd = template.isEnd
            if (template.getOwnedElementOfType<Multiplicity>() != null)
                multiplicity = template.multiplicity
            super.updateFrom(template)
        }
    }

    override var variable: Variable?
        get() = variables.firstOrNull()
        set(value) { variables = mutableListOf(value) }
    // For nested attributes multiple variables are needed
    override var variables: MutableList<Variable?> = mutableListOf()
}

/**
 * For standard-conform serialization, we use the body field
 * resp. a TextualRepresentation of the feature.
 */
fun Feature.toTextualRepresentation(): String? {
    // for Features without value we return null
    if (!isFeatureWithValue())
        return null

    var sysml = "feature"
    // name, short name:
    if (declaredShortName != null)
        sysml += " <$declaredShortName>"
    if (declaredName != null)
        sysml += " $declaredName"
    type.forEach {
        sysml += ": ${it.ref!!.qualifiedName}"
    }
    if (typeConstraint.isNotEmpty()) {
        sysml += "($typeConstraint)"
    }
    if (unitConstraint != null) {
        sysml += "[$unitConstraint]"
    }
    if (featureWithValue != null) {
        sysml += " = $expression"
    }
    if (variable != null) {
        variables.forEach {
            sysml += " = ${it!!.dependency}"
        }
    }
    sysml +=";"
    return sysml
}
