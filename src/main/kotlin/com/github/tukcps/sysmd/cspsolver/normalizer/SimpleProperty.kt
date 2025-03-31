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
    var valueSpecs: MutableList<Any?> = mutableListOf(),
) {

    /** access methods for the valueSpec field; returns different types */
    @Suppress("UNCHECKED_CAST")
    val rangeSpec: MutableList<Range>
        get() = if(valueSpecs.size>0 && valueSpecs[0]!=null) valueSpecs as MutableList<Range> else mutableListOf(Range.Reals)

    @Suppress("UNCHECKED_CAST")
    val boolSpec: MutableList<XBool>
        get() {
            if (valueSpecs[0] is XBool)
                return if (valueSpecs[0] != null) valueSpecs as MutableList<XBool> else mutableListOf(XBool.X)
            else
                throw Exception("Invalid bool spec")
        }

    @Suppress("UNCHECKED_CAST")
    val intSpec: MutableList<IntegerRange>
        get() = if(valueSpecs.size>0 && valueSpecs[0]!=null) valueSpecs as MutableList<IntegerRange> else mutableListOf( IntegerRange.Integers)

    @Suppress("UNCHECKED_CAST")
    val stringSoec: MutableList<String>
        get() = if(valueSpecs.size>0 && valueSpecs[0]!=null) valueSpecs as MutableList<String> else mutableListOf("")

    override fun toString(): String =
        "${this.javaClass.simpleName} { name = $name, " +
                when(valueSpecs.first()) {
                     is XBool -> {
                         "type = Boolean, boolSpec = $boolSpec" +
                                 if (expression.isNotEmpty()) ", expression = \"$expression\"" else ""
                    }
                    is IntegerRange -> {
                        "type = Integer, intSpec = $intSpec" +
                                if (expression.isNotEmpty()) ", expression = \"$expression\"" else ""
                    }
                    is Range -> {
                        "type = Real, rangeSpec = $rangeSpec" +
                                (if (unitSpec.isNotEmpty()) ", unitSpec=$unitSpec" else "")+
                                if (expression.isNotEmpty()) ", expression = \"$expression\"" else ""
                    }
                    is String -> {
                        "type = String, dependency=\"$expression, stringSpec = $stringSoec"
                    }

                    else -> {"Unidentified type: $valueSpecs"}
                } + ", DD<*> = ${dd.toIteString()} }"

}