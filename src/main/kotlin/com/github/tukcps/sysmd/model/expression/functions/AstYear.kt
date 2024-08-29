package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.StrDD
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
internal class AstYear(model: Session, args: ArrayList<AstNode>) :
    AstFunction("Year", model, 1, args) {
    init {
        if (args.size != 1)
            throw SemanticError("Year function expects one parameter of type String")
    }

    override fun initialize() {
        if (getParam(0).upQuantity.values.size != 1)
            throw VectorDimensionError("Year function is not possible with Vectors")
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is StrDD -> Quantity(model.builder.Reals, Unit("s"), "Year")
            else -> throw SemanticError("Year function must have a String as parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        if (!getParam(0).isString) {
            throw SemanticError("Year should be given as a String")
        }
        val string = getParam(0).upQuantity.values[0] as StrDD
        val timestamp = toUnix(string.toIteString())
        upQuantity = Quantity(model.builder.scalar(timestamp), Unit("s"), "Year")
    }

    override fun evalDown() { //Transforms unix timestamp back to the String
        if (downQuantity.values[0].asAadd().getRange().max > 0) {
            val timeString = getParam(0).downQuantity.yearToString(downQuantity.values[0].asAadd().getRange().max)
            getParam(0).downQuantity = Quantity(StrDD.Leaf(this.model.builder, timeString))
        }
    }

    /**
     * Converts a year string to a timestamp
     */
    private fun toUnix(month: String): Double {
        return if (month.contains("T")) { //contains a explicit time
            throw SemanticError("Year should contain no time")
        } else { //contains no explicit time
            if (month.length >= 5)
                throw SemanticError("Year should contain no explicit date or month")
            val date = "$month-01-01" //append first day of year, so that it can be parsed by DateTimeFormatter
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(UTC).toEpochSecond().toDouble()
        }
    }
}
