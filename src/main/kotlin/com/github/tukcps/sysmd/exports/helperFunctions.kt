package com.github.tukcps.sysmd.exports

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.SysMDFatalInternalError
import com.github.tukcps.sysmd.exports.systemCElements.DataType
import com.github.tukcps.sysmd.exports.systemCElements.PortType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Specialization
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.StrDD

/**
 * Checks if the Expression maps to a Variable which HAS initial values.
 * The check is performed based on the data type of the Expression.
 * @param feature The Expression which should be checked.
 * @return A Boolean telling if this Expression maps to Variable WITH initial values.
 */
fun isVariable(feature : Feature) : Boolean {
    require (feature !is Variable)
    when(feature.variable?.vectorQuantity?.value){
        is AADD -> {
            return if(feature.variable!!.dependency.isNotEmpty()){
                feature.variable!!.dependency.contains(" .. ") && dependencyStringToMinMax(feature.variable!!.dependency).let { it.first != it.second }
            }else{
                feature.variable!!.rangeSpecs[0].max != feature.variable!!.rangeSpecs[0].min
            }
        }

        is IDD -> {
            return if(feature.variable!!.dependency.isNotEmpty()){
                feature.variable!!.dependency.contains(" .. ") && dependencyStringToMinMax(feature.variable!!.dependency).let { it.first != it.second }
            }else{
                feature.variable!!.intSpecs[0].max != feature.variable!!.intSpecs[0].min
            }
        }

        else ->  throw SysMDFatalInternalError("This check is not supported for Expressions of data type \"${feature.type.firstOrNull()}\"")
    }
}

/**
 * Checks if the Expression maps to a Variable which has NO initial values.
 * The check is performed based on the data type of the Expression.
 * @param feature The Expression which should be checked.
 * @return A Boolean telling if this Expression maps to Variable WITHOUT initial values.
 */
fun isVariableWithoutValues(feature : Feature) : Boolean {
    require(feature !is Variable)
    return when {
        feature.model!!.repo.realType in feature.allSupertypes(true) -> feature.variable!!.dependency.isEmpty() && !feature.variable!!.rangeSpecs[0].isFinite()
        feature.model!!.repo.integerType in feature.allSupertypes(true)-> feature.variable!!.dependency.isEmpty() && feature.variable!!.intSpecs[0].toString().contains("MAX")
        feature.model!!.repo.stringType in feature.allSupertypes(true)-> (feature.variable!!.vectorQuantity.value as StrDD.Leaf).value.isEmpty()
        feature.model!!.repo.booleanType in feature.allSupertypes(true)-> feature.variable!!.dependency.isEmpty()
        else -> throw SysMDFatalInternalError("Cannot perform this Variable Check on a Expression with Data Type \"${feature.type.firstOrNull()}\"")
    }
}

/**
 * Extracts the min/max values from the dependency String of an Expression.
 * @param dependency The Dependency String from which the min/max values should be extracted.
 * @return A Pair containing the min (Pair.first) and max (Pair.second) values.
 */
fun dependencyStringToMinMax(dependency: String) : Pair<Double,Double>{
    val keepChars = "[^.0-9.-]".toRegex()
    if(dependency.isNotEmpty()){
        val min = dependency.substringBefore(" .. ").removePrefix("[").replace(keepChars, "").toDouble()
        val max = dependency.substringAfter(" .. ").substringBefore("]").replace(keepChars, "").toDouble()

        return Pair(min,max)
    } else {
        throw SysMDFatalInternalError("Cannot extract min/max values from an empty dependency String!")
    }
}

/**
 * Translates a FeatureDirectionKind to a PortType as it is used inside a Port Object.
 */
val translateToPortType : (Feature.FeatureDirectionKind) -> PortType = {
    when (it){
        Feature.FeatureDirectionKind.IN ->   PortType.TARGET
        Feature.FeatureDirectionKind.OUT ->  PortType.SOURCE
        Feature.FeatureDirectionKind.INOUT ->  PortType.BIDIRECTIONAL
    }
}

/**
 * Searches in the ownedElement list for an Element that matches the defined type.
 * @param element The Element which is analyzed.
 * @param type The class of the Element that is searched.
 */
fun getElementOfType(element : Element, type : String) : Element {
    element.ownedRelationship.forEach {
        if(it.javaClass.simpleName == type) return it
    }

    throw SysMDFatalInternalError("No FeatureTyping found for Element: ${element.declaredName}")
}

/**
 * Translates the type of this FeatureTypingImplementation in to a proper DataType Enum.
 */
fun Specialization.toDataType() : DataType {
    return when (this.general) {
        model!!.repo.realType -> DataType.REAL
        model!!.repo.integerType -> DataType.INT
        model!!.repo.booleanType -> DataType.BOOLEAN
        model!!.repo.stringType -> DataType.STRING
        // else -> throw SysMDFatalInternalError("The FeatureTypingImplementation \"${this.declaredName}\" has no known and translatable data type.")
        else -> DataType.REAL
    }
}

fun MutableList<Feature>.containsByName(referenceExpression: Feature): Boolean{
    this.forEach { expression ->
        if(expression.declaredName.toString() == referenceExpression.declaredName.toString()) return true
    }
    return false
}