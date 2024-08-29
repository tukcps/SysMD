package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.aadd.*
import com.github.tukcps.sysmd.cspsolver.Variable
import kotlin.math.absoluteValue
import kotlin.math.ulp


/**
 * Checks if a property value has been restricted to a smaller interval, and if so, sets the marker
 * stable to false, otherwise to true.
 */
fun Variable.checkEvent() {
    stable = vectorQuantity.values.indices.all {
        when (vectorQuantity.values[0]) {
        is AADD -> {
                // We allow here some percentage of slack to become robust against potential FP rounding errors.
                var lb = vectorQuantity.values[it].asAadd().min - 0.00001 * vectorQuantity.values[it].asAadd().min.absoluteValue
                lb -= lb.ulp*100
                var ub = vectorQuantity.values[it].asAadd().max + 0.00001 * vectorQuantity.values[it].asAadd().max.absoluteValue
                ub += ub.ulp*100
                val oldValue = if(oldVectorQuantity!!.values.size>1) oldVectorQuantity!!.values[it] else oldVectorQuantity!!.value

                (((oldValue as AADD) in lb..ub || (lb >= ub)))
                        || (lb.isNaN() || ub.isNaN())
        }
        is IDD -> {
            val lb = vectorQuantity.values[it].asIdd().getRange().min
            val ub = vectorQuantity.values[it].asIdd().getRange().max
            if (oldVectorQuantity != null) {
                val oldValue = if (oldVectorQuantity!!.values.size > 1) oldVectorQuantity!!.values[it] else oldVectorQuantity!!.value
                (oldValue as IDD).getRange().min in lb..ub && oldValue.getRange().max in lb..ub || (lb > ub)
            } else
                false
        }
        is BDD -> {
            // Only covers really serious changes. More by Alex?
            val oldValue = if(oldVectorQuantity!!.values.size>1) oldVectorQuantity!!.values[it] else oldVectorQuantity!!.value
            when (vectorQuantity.values[it]) {
                vectorQuantity.values[it].builder.True -> {
                    oldValue == vectorQuantity.values[it]
                }
                vectorQuantity.values[it].builder.False -> {
                    oldValue == vectorQuantity.values[it]
                }
                else -> true
            }
        }
        is StrDD -> {
            val oldValue = if(oldVectorQuantity!!.values.size>1) oldVectorQuantity!!.values[it] else oldVectorQuantity!!.value
            oldValue.toString() == vectorQuantity.values[it].toString()
        }
        else -> throw DDException("checkUpdated called with wrong type")
    }}
    if (!stable)
        updated = true

    oldVectorQuantity = vectorQuantity.clone()
}