package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.real.ia.RealRange
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

/**
 * Create Date with timestamp. Documentation can be found under doc/Tutorial/Quantities/Time.md
 */
internal class AstDate(model: Session, args: ArrayList<AstNode>) : AstDateFunction("Date", model, args) {
    override fun format(x: RealRange): String = Representer.default.representAsDate(x)

    override fun parse(date: String): Double = if (date.contains("T")) { //contains an explicit time
            throw SemanticError("Date should not contain a time")
        } else { //contains no explicit time
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(UTC).toEpochSecond().toDouble()
        }

    override fun clone() = AstDate(model, cloneParameters())
}
