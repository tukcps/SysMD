package com.github.tukcps.sysmd.cspsolver.valuefeatures

/*
class IsAValueFeature(
    id: UUID = UUID.randomUUID(),
    name: String? = null,
    shortName: String? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    dependency: String,
    valueSpec: MutableList<Any?>,
    basetype: BaseType,
    val createdBy: Variable,
    val reason: String = "",
    val src: Variable,
    val dst: Variable
): ExpressionOld(
    elementId=id,
    declaredName=name,
    declaredShortName = shortName,
    direction = direction,
    dependency = dependency,
    valueSpecs = valueSpec,
    basetype = basetype) {

    init {
        isTransient = true
        require(boolSpecs == XBool.True)
    }

    override fun clone(): IsAValueFeature {
        return IsAValueFeature(
            id = elementId,
            name = declaredName,
            shortName = declaredShortName,
            dependency = dependency.plus(""),
            valueSpec = valueSpecs,
            basetype = basetype,
            createdBy = createdBy,
            reason = reason.plus(""),
            src = src,
            dst = dst
        ).also {
            it.unitSpec = unitSpec.plus("")
            it.owner.ref = owner.ref
            it.updated = updated
            it.isTransient = true
        }
    }

}
*/