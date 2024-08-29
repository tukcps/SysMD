package com.github.tukcps.sysmd.cspsolver.valuefeatures


/*
class HasAValueFeature(
    id: UUID = UUID.randomUUID(),
    name: String? = null,
    shortName: String? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    dependency: String,
    valueSpec: MutableList<Any?>,
    basetype: BaseType,
    private val createdBy: Variable,
    val reason: String = "",
    private val src: Variable,
    private val dst: Variable
): ExpressionOld(
    elementId=id, declaredName=name, declaredShortName = shortName,
    direction = direction, dependency = dependency, valueSpecs = valueSpec, basetype = basetype) {

    init {
        isTransient = true
        require(boolSpecs == XBool.True)
    }

    override fun clone(): HasAValueFeature {
        return HasAValueFeature(
            name = declaredName,
            shortName = declaredShortName,
            dependency = dependency.plus(""),
            valueSpec = valueSpecs,
            basetype = BaseType.Bool,
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
} */