package com.github.tukcps.sysmd.model.expression

interface LiteralString: LiteralExpression
{
	/** SysMD extension.
	 * If true, this literal is actually a name literal.
	 * Only affects SysMD code which knows to dynamically convert these to feature(chain) references.
	 */
	val isNameLiteral : Boolean

	override val value: String?

	override fun clone(): LiteralString
}