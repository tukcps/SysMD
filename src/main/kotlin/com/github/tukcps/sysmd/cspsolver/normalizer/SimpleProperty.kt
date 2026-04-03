package com.github.tukcps.sysmd.cspsolver.normalizer

import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.Range
import io.github.tukcps.aadd.values.XBool

/**
 * Class to store a simple property.
 */
data class SimpleProperty<T : Any>(
    val name: String,
    val expression : String,
    val dd : DD<T>,
    val simpleAst : SimpleAstRoot? = null, // store a simpler version of the AST
    var unitSpec:   String = "",        // Specified unit as string
    var valueSpecs: MutableList<XBool> = mutableListOf(),
) {
    /** access methods for the valueSpec field; returns different types */
    @Suppress("UNCHECKED_CAST")
    val rangeSpec: MutableList<Range>
        get() = if(valueSpecs.size>0) valueSpecs as MutableList<Range> else mutableListOf(Range.Reals)

    @Suppress("UNCHECKED_CAST")
    val boolSpec: MutableList<XBool>
        get() = valueSpecs

    @Suppress("UNCHECKED_CAST")
    val intSpec: MutableList<IntegerRange>
        get() = if(valueSpecs.size>0) valueSpecs as MutableList<IntegerRange> else mutableListOf( IntegerRange.Integers)

    @Suppress("UNCHECKED_CAST")
    val stringSoec: MutableList<String>
        get() = if(valueSpecs.size>0) valueSpecs as MutableList<String> else mutableListOf("")

    override fun toString(): String =
        "${this.javaClass.simpleName} { name = $name, " +
                when(valueSpecs.first()) {
                    else -> {
                        "type = Boolean, boolSpec = $boolSpec" +
                                if (expression.isNotEmpty()) ", expression = \"$expression\"" else ""
                    }
                } + ", DD<*> = ${dd.toIteString()} }"

}