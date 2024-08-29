package com.github.tukcps.sysmd.exports.systemCElements

import com.github.tukcps.sysmd.model.kerml.Feature
import java.io.PrintWriter
import kotlin.math.roundToInt

class Variable(
    expression: Feature,
    dataType: DataType,
    dependencyStringToMinMax: (String) -> Pair<Double, Double>
) {
    val name : String = expression.declaredName.toString()
    val min : String?
    val max : String?
    val center : String?

    /** Used for Booleans and Strings*/
    private val singleValue : String?

    val unit : String
    private val dataType : DataType

    init {
        require(expression !is com.github.tukcps.sysmd.cspsolver.Variable)
        require(expression.variable != null)
        unit = expression.variable!!.unitSpec
        this.dataType = dataType

        when(dataType){
            DataType.REAL -> {
                if(expression.variable!!.rangeSpecs[0].isFinite()){
                    //If it is a Real or Int, we can actually access the min and max values
                    min = expression.variable!!.rangeSpecs[0].min.toString()
                    max = expression.variable!!.rangeSpecs[0].max.toString()
                    center = (expression.variable!!.rangeSpecs[0].max - ((expression.variable!!.rangeSpecs[0].max-expression.variable!!.rangeSpecs[0].min)/2)).toString()
                }else{
                    //Access values via dependency String
                    dependencyStringToMinMax(expression.variable!!.dependency).let {
                        min = it.first.toString()
                        max = it.second.toString()
                        center = (it.second - ((it.second-it.first)/2)).toString()
                    }
                }

                singleValue = null
            }

            DataType.INT -> {
                if(!expression.variable!!.intSpecs[0].toString().contains("MAX")){
                    //If it is a Real or Int, we can actually access the min and max values
                    min = expression.variable!!.intSpecs[0].min.toInt().toString()
                    max = expression.variable!!.intSpecs[0].max.toInt().toString()
                    center = (expression.variable!!.intSpecs[0].max - ((expression.variable!!.intSpecs[0].max-expression.variable!!.intSpecs[0].min)/2)).toDouble().roundToInt().toString()
                }else{
                    //Access values via dependency String
                    dependencyStringToMinMax(expression.variable!!.dependency).let {
                        min = it.first.toInt().toString()
                        max = it.second.toInt().toString()
                        center = (it.second - ((it.second-it.first)/2)).roundToInt().toString()
                    }
                }

                singleValue = null
            }


            else -> {
                //If it is a String or Boolean, we cannot use min/max values.
                //Therefor we access the value via the dependency.
                singleValue = if(dataType == DataType.STRING) {
                    expression.variable!!.dependency //Take String as is
                }else{
                    expression.variable!!.dependency.lowercase()
                // Make String to lowercase to match C++ datatype (False -> false, True -> true)
                }
                min = null
                max = null
                center = null
            }

        }
    }
    /**Writes the Variable as a String into the PrintWriter, for use in a C++ Header file*/
    fun writForHeader(
        out: PrintWriter,
        printRangeOrValue: (String?, String?, String?, String, DataType) -> String
    ) {
        out.print(
            "\n\t${this.dataType.toCPPDataType().padEnd(7)} " +
                    "${
                        ("${this.name};").padEnd(40)
                    }     ${printRangeOrValue(this.min, this.max, this.singleValue, this.unit, this.dataType)}"
        )
    }

    /**Writes the Variable as a String into the PrintWriter, for use in a C++ Source file*/
    fun writeForSource(out: PrintWriter) {
        out.print(
            "\n\t${
                ("${this.name} = ${
                    when (this.dataType) {
                        DataType.REAL, DataType.INT -> this.center
                        DataType.STRING, DataType.BOOLEAN -> this.singleValue
                    }
                };").padEnd(35)
            }"
        )
    }

    /**Writes the Variable as a String into the PrintWriter, for use in a C++ Source file*/
    fun writeForMain(out: PrintWriter, printRangeOrValue: (String?, String?, String?, String, DataType) -> String) {
        out.print(
            "\n\t${this.dataType.toCPPDataType().padEnd(7)}" +
                ("${this.name} = ${
                    when (this.dataType) {
                        DataType.REAL, DataType.INT -> this.center
                        DataType.STRING, DataType.BOOLEAN -> this.singleValue
                    }
                };").padEnd(40) + printRangeOrValue(this.min, this.max, this.singleValue, this.unit, this.dataType)
        )
    }
}