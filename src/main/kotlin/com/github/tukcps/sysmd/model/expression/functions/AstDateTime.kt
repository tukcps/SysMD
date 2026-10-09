package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.real.ia.RealRange
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

/**
 * Create Date with timestamp. Documentation can be found under doc/Tutorial/Quantities/Time.md
 */
internal class AstDateTime(model: Session, args: ArrayList<AstNode>) :
    AstDateFunction("DateTime", model, args)
{
    override fun format(x: RealRange): String = Representer.default.representAsDateTime(x)

    /**
     * Converts a datetime string to a unix timestamp
     */
    override fun parse(datetime: String): Double {
        if (!datetime.contains("T")) {
            throw SemanticError("DateTime must contain a time")
        }
        return try {
            OffsetDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toEpochSecond().toDouble()
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(datetime, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(UTC).toEpochSecond().toDouble()
            } catch (_: Exception) {
                throw SemanticError("Invalid DateTime format: $datetime")
            }
        }
    }

    override fun clone() = AstDateTime(model, cloneParameters())
}
