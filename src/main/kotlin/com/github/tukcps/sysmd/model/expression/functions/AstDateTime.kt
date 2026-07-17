package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.StrDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.services.session.Session
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

/**
 * Create Date with timestamp. Documentation can be found under doc/Tutorial/Quantities/Time.md
 */
internal class AstDateTime(model: Session, args: ArrayList<AstNode>) :
    AstFunction("DateTime", model, 1, args) {
    init {
        if (args.size != 1)
            throw SemanticError("DateTime expects 1 parameter of type String")
    }

    override fun initialize() {
        if (getParam(0).upQuantity.values.size != 1)
            throw VectorDimensionError("DateTime is not possible with Vectors")
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is StrDD -> Quantity(model.builder.Reals, Unit("s"), "DateTime")
            else -> throw SemanticError("DateTime must have a String as parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        if (!getParam(0).isString)
            throw SemanticError("Datetime should be given as a String")
        val string = getParam(0).upQuantity.values[0] as StrDD
        val timestamp = toUnix(string.toIteString())
        upQuantity = Quantity(model.builder.real(timestamp), Unit("s"), "DateTime")
    }

    override fun evalDown() { //Transforms unix timestamp back to the String
        if (downQuantity.values[0].asAadd().getRange().max > 0) {
            val timeString = this.downQuantity.timeToString(downQuantity.values[0].asAadd().getRange().max)
            getParam(0).downQuantity = Quantity(StrDD.Leaf(this.model.builder, timeString))
        }
    }

    /**
     * Converts a datetime string to a unix timestamp
     */
    private fun toUnix(datetime: String): Double {
        if (!datetime.contains("T")) {
            throw SemanticError("DateTime must contain a time")
        }
        return try {
            OffsetDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toEpochSecond().toDouble()
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(datetime, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(UTC).toEpochSecond().toDouble()
            } catch (e2: Exception) {
                throw SemanticError("Invalid DateTime format: $datetime")
            }
        }
    }

    override fun clone(): AstDateTime {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstDateTime(model, parClone)
    }
}
