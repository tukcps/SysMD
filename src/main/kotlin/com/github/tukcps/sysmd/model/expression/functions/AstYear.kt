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
internal class AstYear(model: Session, args: ArrayList<AstNode>) : AstDateFunction("Year", model, args)
{
    override fun format(x: RealRange): String = Representer.default.representAsYear(x)

    override fun parse(month: String): Double = if (month.contains("T")) { //contains an explicit time
        throw SemanticError("Year should contain no time")
    } else { //contains no explicit time
        if (month.length >= 5)
            throw SemanticError("Year should contain no explicit date or month")
        val date = "$month-01-01" //append first day of year, so that it can be parsed by DateTimeFormatter
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(UTC).toEpochSecond().toDouble()
    }

    override fun clone() = AstYear(model, cloneParameters())
}
