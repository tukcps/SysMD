package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.SimpleName
import io.github.tukcps.aadd.values.IntegerRange

/**
 * A feature definition including Multiplicity, as in KerML (mostly).
 */
open class FeatureImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: String? = null,
    elementType: String = "Feature",
    override var typeConstraint: MutableList<String> = mutableListOf(),
    override var expression: String? = null,
    override var isDefaultValue: Boolean = false,
    override var isInitialValue: Boolean = false,
): Feature, TypeImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
){
    final override var direction: Feature.FeatureDirectionKind? = null
    final override var isEnd: Boolean = false
    final override var isComposite: Boolean = true
    final override var isPortion: Boolean = false
    final override var isSufficient: Boolean = false
    final override var isUnique: Boolean = false
    final override var isOrdered: Boolean = false
    final override var isDerived: Boolean = false
    override var isReadOnly: Boolean = false

    override val type: List<Type>
        get() = generalization

    override val unitConstraint: String?
        get() = resolveLocal("unit")?.member<Feature>()?.expression?.trim('"')?:""

    override val typing: List<FeatureTyping>
        get() = getOwnedElementsOfType()

    override val ownedTypeFeaturing: List<FeatureTyping>
        get() = getOwnedElementsOfType()


    /** Getter and setter for the specified multiplicity. */
    override var multiplicityRange: IntegerRange
        get() = IntegerRange(multiplicity()?.typeConstraint?.firstOrNull()?:"1..1")
        set(value) { multiplicity()?.variable?.intSpecs = mutableListOf(value)}

    override val name: String?
        get() = declaredName?: referencedFeature?.name

    override fun clone(): Feature {
        val klon = FeatureImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            klon.model = model
            klon.updated = updated
            klon.typeConstraint = typeConstraint.toMutableList()
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
        return klon
    }

    final override val referencedFeature: Feature?
        get() = getOwnedElementOfType<ReferenceSubsetting>()?.referencedFeature

    override fun updateFrom(template: Element) {
        if (template is Feature) {
            model = template.model
            updated = template.updated
            expression = template.expression
            typeConstraint = template.typeConstraint.toMutableList()
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
        get() = model!!.solver.getVariable(this.path())

    override fun toString(): String = super.toString() +
            (if (model?.solver?.getVariable(path()) !== null) " = " +
                    try { model!!.solver.getVariable(path())!!.vectorQuantity.toString() }
                    catch (_: Exception) {"(?)"} else "") +
            (if (isEnd) " end" else "") +
            (if (isComposite) " composite" else "") +
            (if (isPortion) " portion" else "") +
            (if (isAbstract) " abstract" else "") +
            (if (isOrdered) "ordered" else "")
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
    if (typeConstraint.isNotEmpty()) {
        sysml += "($typeConstraint)"
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
