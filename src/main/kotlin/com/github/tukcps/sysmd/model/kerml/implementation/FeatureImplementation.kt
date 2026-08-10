package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.MultiplicityRange
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.TypeConstraint
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.IntegerRange
import kotlin.uuid.Uuid

/**
 * A feature definition including Multiplicity, as in KerML (mostly).
 */
open class FeatureImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: String? = null,
    override var expression: String? = null,
    override var isDefaultValue: Boolean = false,
    override var isInitialValue: Boolean = false,
): Feature, TypeImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
){
    final override var direction: Feature.FeatureDirectionKind? = null
    final override var isEnd: Boolean = false
    final override var isComposite: Boolean = true
    final override var isPortion: Boolean = false
    final override var isSufficient: Boolean = false
    final override var isUnique: Boolean = false
    final override var isOrdered: Boolean = false
    final override var isDerived: Boolean = false
    final override var isConstant: Boolean = false
    final override var isVariable: Boolean = false
    override var isReadOnly: Boolean = false

    override val type: List<Type>
        get() = generalization

    override val typeConstraint: MutableList<String>
        get() = TypeConstraint(getOwned<Feature>("range")?.expression?:"").value

    override val unitConstraint: String?
        get() = TypeConstraint(getOwned<Feature>("range")?.expression?:"").unit

    override val typing: List<FeatureTyping>
        get() = getOwnedElementsOfType()

    override val ownedTypeFeaturing: List<FeatureTyping>
        get() = getOwnedElementsOfType()

    /**
     * Getter and setter for the specified multiplicity.
     * Setter only works for solver ... TODO!
     *  - should be only for model, separated approach for solver needed.
     */
    @Deprecated("Use function call", ReplaceWith("multiplicityRange()"))
    override var multiplicityRange: MultiplicityRange
        get() = multiplicityRange()
        set(value) { multiplicity()?.variable?.intSpecs = mutableListOf(IntegerRange(value.toLongRange())) }

    override fun multiplicityRange(): MultiplicityRange =
        when {
            getOwnedElementOfType<Multiplicity>() != null ->
                MultiplicityRange(getOwnedElementOfType<Multiplicity>()?.getOwnedElementOfType<Feature>()?.expression ?: "0..*")
            redefining != null -> redefining!!.multiplicityRange
            referencedFeature != null -> referencedFeature!!.multiplicityRange
            else -> defaultMultiplicityRange
        }

    override val name: String?
        get() = declaredName?: referencedFeature?.name

    override fun clone(): Feature = FeatureImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
    ).also { klon ->
        klon.isImpliedIncluded = isImpliedIncluded
        klon.updated = updated
        klon.expression = expression
        klon.isDefaultValue = isDefaultValue
        klon.isInitialValue = isInitialValue
        klon.isAbstract = isAbstract
        klon.isSufficient = isSufficient
        klon.isDerived = isDerived
        klon.isReadOnly = isReadOnly
        klon.isOrdered = isOrdered
        klon.isUnique = isUnique
        klon.isSufficient = isSufficient
        klon.isPortion = isPortion
        klon.isComposite = isComposite
        klon.direction = direction
        klon.isEnd = isEnd
        // klon.updateFrom(this)
        // super.updateFrom causes failing tests with Connections for unclear reason.
        // reason lies in isLibraryElement or isStandard?
    }

    final override val referencedFeature: Feature?
        get() = getOwnedElementOfType<ReferenceSubsetting>()?.referencedFeature

    override fun updateFrom(template: Element) {
        if (template is Feature) {
            updated = template.updated
            expression = template.expression
            expression = template.expression
            isDefaultValue = template.isDefaultValue
            isInitialValue = template.isInitialValue
            isAbstract = template.isAbstract
            direction = template.direction
            isEnd = template.isEnd
            isComposite = template.isComposite
            isPortion = template.isPortion
            isSufficient = template.isSufficient
            isUnique = template.isUnique
            isOrdered = template.isOrdered
            isDerived = template.isDerived
            isReadOnly = template.isReadOnly
            super.updateFrom(template)
        }
    }

    @Deprecated("To get a variable, one must use getVariable and the suitable membership of a feature.")
    override val variable: Variable?
        get() = model.solver.getVariable(this.path())

    override fun toString(): String = super.toString() +
            (if (model.solver.getVariable(path()) !== null) " = " +
                    try { model.solver.getVariable(path())!!.vectorQuantity.toString() }
                    catch (_: Exception) {"(?)"} else "") +
            (if (isEnd) " end" else "") +
            (if (isComposite) " composite" else "") +
            (if (isPortion) " portion" else "") +
            (if (isOrdered) " ordered" else "") +
            (if (isUnique) " unique" else "")
}

/**
 * For standard-conform serialization, we use the body field
 * resp. a TextualRepresentation of the feature.
 */
fun Feature.toTextualRepresentation(): String {
    // for Features without value we return null

    var sysml = "feature"
    // name, short name:
    if (declaredShortName != null)
        sysml += " <$declaredShortName>"
    if (declaredName != null)
        sysml += " $declaredName"
    type.forEach {
        sysml += ": ${it.qualifiedName}"
    }
    val constraint = (typeConstraint.map {it.trimStart('[').trimEnd(']')}).toString().trimStart('[').trimEnd(']')
    if (constraint.isNotEmpty()) {
        sysml += "($constraint)"
    }

    if (expression?.isNotBlank() == true) {
        when {
            isDefaultValue && isInitialValue -> sysml += " default := $expression"
            isDefaultValue -> sysml += " default $expression"
            isInitialValue -> sysml += " := $expression"
            else -> sysml += " = $expression"
        }
    }

    sysml +=";"
    return sysml
}