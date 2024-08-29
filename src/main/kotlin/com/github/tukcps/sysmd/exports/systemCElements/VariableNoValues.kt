package com.github.tukcps.sysmd.exports.systemCElements

import java.io.PrintWriter
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Feature

class VariableNoValues(expression: Feature, val dataType: DataType) {

    val name : String = expression.declaredName.toString()
    val unit : String = expression.variable!!.unitSpec

    /**Writes the Variable as a String into the PrintWriter, for use in a C++ Header file*/
    fun writeForHeader(out: PrintWriter) {
        out.print("\n\t${this.dataType.toCPPDataType()} ${this.name};")
    }

    /**Writes the Variable as a String into the PrintWriter, for use in a C++ Source file*/
    fun writeForSource(out: PrintWriter) {
        out.print("\n\t${this.name} = ${this.name}_;")
    }

    fun writForMain(out: PrintWriter) {
        out.print(
            "\n\t${this.dataType.toCPPDataType().padEnd(7)}" +
                    ("${this.name} = SPECIFY_VALUE").padEnd(35)
        )
    }
}