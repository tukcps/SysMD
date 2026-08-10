package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ParameterMembership
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.UnresolvedExpression

interface InstantiationExpression: Expression, Type {
    @Deprecated("Not well-defined by standard. Do not use.", ReplaceWith("positionalArguments"))
    val argument: List<Expression>

    // Generated from MOF
    val instantiatedType: Type
        get() = instantiatedType()!!

    fun instantiatedType(): Type?
    /** Non-Standard. Name of the invoked function */
    var functionName : QualifiedName?

    override fun clone(): InstantiationExpression

    /** The supplied positional arguments, in order. Empty when named arguments were used instead.
     * Feature-indirected arguments are unpacked to the expressions they reference (which may in turn be [FeatureReferenceExpression]s)
     * @see namedArguments
     */
    val positionalArguments : List<Feature> get() = membership
        .filterIsInstance<ParameterMembership>()
        .filter { it.parameterDirection == Feature.FeatureDirectionKind.IN } // TODO
        .sortedBy { it.parameterIndex } // TODO: remove, no longer needed
        .map { it.ownedMemberParameter }

    /** The supplied named arguments, with the parameter features they redefine.
     * Empty when positional arguments were used instead.
     * @see positionalArguments
     */
    val namedArguments : List<Pair<Feature, Expression>> get() = feature.mapNotNull {
        it.redefining?.to(it.featureValue ?: UnresolvedExpression(model))
    }

}