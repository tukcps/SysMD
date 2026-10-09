package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.util.Tolerance

/** Tolerance within which constraint propagation is considered finished */
private val tolerance = Tolerance(
    relative = 0.00001,
    ulps = 100
)

/**
 * Updates [Variable.stable] by checking if its value changed significantly from the last propagation iteration
 */
fun Variable.checkEvent()
{
    /**
    * @param old A broader approximation of [new]
    * @param new A more refined approximation of the same value as [old]
    * @return Whether the change from [old] to [new] is negligible
     */
    fun isStable(old : DD<*>, new : DD<*>) : Boolean = when {
        old is AADD && new is AADD -> {
            val oldRange = old.getRange()
            val newRange = new.getRange()

            if(newRange.isEmpty())
                return oldRange.isEmpty()

            // give some slack for floating point errors
            oldRange in tolerance.widen(newRange)
        }
        // fixme: why was previous version of this so weird?
        old is IDD && new is IDD -> old.getRange() == new.getRange()
        old is BDD && new is BDD -> old.value == new.value
        old is StrDD && new is StrDD -> old.toString() == new.toString()
        else -> throw IllegalStateException("Variable changed type across iterations?!")
    }

    fun isStable(old : VectorQuantity, new : VectorQuantity) : Boolean
    {
        return old.values.size == new.values.size && // changing size should be impossible
                old.unitSpec == new.unitSpec && old.unit == new.unit && // unit might change if it was "?" or "" previously
                (old.values zip new.values).all { (x, y) -> isStable(x, y) } // finally check for value change
    }

    val old = oldVectorQuantity
    oldVectorQuantity = vectorQuantity.clone()

    stable = old !== null && isStable(old, vectorQuantity)

    if(! stable)
        updated = true
}