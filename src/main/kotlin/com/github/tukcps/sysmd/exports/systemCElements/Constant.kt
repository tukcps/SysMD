package com.github.tukcps.sysmd.exports.systemCElements

import com.github.tukcps.sysmd.exceptions.SysMDInternalError
import com.github.tukcps.sysmd.model.kerml.Feature
import java.io.PrintWriter


/**
 * Writes a feature with a simple constant value to the SystemC file.
 * @param expression the feature
 * @param dataType the type of the feature
 * @param dependencyStringToMinMax ?
 */
class Constant(expression: Feature, dataType: DataType, dependencyStringToMinMax: (String) -> Pair<Double, Double>) {

    val name : String = expression.declaredName!!
    val value : String
    val unit : String
    val dataType : String

    init {
        require((expression !is com.github.tukcps.sysmd.cspsolver.Variable) && (expression.variable != null))

        when(dataType){
            DataType.REAL -> {
                value = if(expression.variable!!.rangeSpecs[0].isFinite()){
                    expression.variable!!.rangeSpecs[0].max.toString()
                }else{
                    dependencyStringToMinMax(expression.expression!!).second.toString()
                }
            }

            DataType.INT -> {
                value = if(!expression.variable!!.intSpecs[0].toString().contains("MAX")){
                    expression.variable!!.intSpecs[0].max.toInt().toString()
                }else{
                    dependencyStringToMinMax(expression.expression!!).second.toInt().toString()
                }
            }

            else -> throw SysMDInternalError("The Expression uses a DataType ($dataType) which is not eligible to be used for in a Constant.")
        }

        unit = expression.variable!!.unitSpec
        this.dataType = dataType.toCPPDataType()
    }

    /**Writes the Constant as a String into the PrintWriter, for use in a C++ file*/
    fun write(out: PrintWriter) {
        out.print(
            "\n\tconst ${this.dataType.padEnd(7)} " +
                    "${
                        ("${this.name} = ${this.value};").padEnd(35)
                    }   // Unit: ${this.unit}"
        )
    }
}