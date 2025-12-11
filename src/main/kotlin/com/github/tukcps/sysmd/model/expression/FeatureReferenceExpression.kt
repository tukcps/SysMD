package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.QualifiedName

interface FeatureReferenceExpression: Expression
{
	val referent : Feature?
	var identifier : QualifiedName?
}