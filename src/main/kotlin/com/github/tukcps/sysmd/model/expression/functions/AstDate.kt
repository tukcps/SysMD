package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.StrDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.services.session.Session
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

/**
 * Create Date with timestamp. Documentation can be found under doc/Tutorial/Quantities/Time.md
 */
internal class AstDate(model: Session, args: ArrayList<AstNode>) :
    AstFunction("Date", model, 1, args) {
    init {
        if (args.size != 1)
            throw SemanticError("Date expects 1 parameter of type String")
    }

    override fun initialize() {
        if (getParam(0).upQuantity.values.size != 1)
            throw VectorDimensionError("Date is not possible with Vectors")
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is StrDD -> Quantity(model.builder.Reals, Unit("s"), "Date")
            else -> throw SemanticError("Date must have a String as parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        if (!getParam(0).isString)
            throw SemanticError("Date should be given as a String")
        val string = getParam(0).upQuantity.values[0] as StrDD
        val timestamp = toUnix(string.toIteString())
        upQuantity = Quantity(model.builder.real(timestamp), Unit("s"), "Date")
    }

    override fun evalDown() { //Transforms unix timestamp back to the String
        if (downQuantity.values[0].asAadd().getRange().max > 0) {
            val timeString = this.downQuantity.dateToString(downQuantity.values[0].asAadd().getRange().max)
            getParam(0).downQuantity = Quantity(StrDD.Leaf(this.model.builder, timeString))
        }
    }

    /**
     * Converts a date string to a unix timestamp
     */
    private fun toUnix(date: String): Double {
        return if (date.contains("T")) { //contains an explicit time
            throw SemanticError("Date should not contain a time")
        } else { //contains no explicit time
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(UTC).toEpochSecond().toDouble()
        }
    }

    override fun clone(): AstDate {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstDate(model, parClone)
    }
}
