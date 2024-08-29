package com.github.tukcps.sysmd.services

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import java.util.*

data class Outputvariable(
    val name: QualifiedName, val oldValue: VectorQuantity?, val newValue: VectorQuantity
)

/**
 * Stores all variables that have been changed since the last model computation
 */
class OutputDisplay(
    private var kerMlModel: MutableState<Session>
) {
    private val varList: MutableList<Outputvariable> = mutableStateListOf()

    /**
     * Determines whether variables assigned the first time are to be displayed
     */
    private val showNew = false

    /**
     * Adds a variable to the list
     */
    fun changedVariable(name: QualifiedName, oldValue: VectorQuantity?, newValue: VectorQuantity) {
        if (oldValue != null || showNew) varList.add(Outputvariable(name, oldValue, newValue))
    }

    /**
     * Adds a variable to the list
     */
    fun changedVariable(id: UUID) {
        val el: Element? = kerMlModel.value[id]
        if (el != null && el.elementType == "Expression") {
            try {
                varList.add(
                    Outputvariable(
                        el.qualifiedName, (el as Variable).oldVectorQuantity, el.vectorQuantity
                    )
                )
            } catch (e: Exception) {
                println(e)
            }
            //println("name: ${el.qualifiedName}, old: ${el.oldVectorQuantity}, new: ${el.vectorQuantity}")
        }
    }

    fun update() {
        kerMlModel.value.status.updates.forEach {
            changedVariable(it.key)
        }
    }

    /**
     * Returns a list of all changed variables
     */
    fun variables() = varList.toList()

    /**
     * Clears the variables list
     */
    fun clear() = varList.clear()
}
