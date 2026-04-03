package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.util.QualifiedName

interface MetadataAccessExpression : Expression
{
	val metaclassFeature : MetadataFeature?

	val referencedElement : Element?
		get() = member.firstOrNull()

	override val isModelLevelEvaluable : Boolean get() = true

	override fun clone(): MetadataAccessExpression
}