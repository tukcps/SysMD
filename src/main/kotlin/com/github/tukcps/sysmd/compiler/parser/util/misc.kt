package com.github.tukcps.sysmd.compiler.parser.util

import io.github.tukcps.aadd.values.bounds.LongBound
import io.github.tukcps.aadd.values.bounds.unaryMinus
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.unaryMinus

// for working with lexed literals
internal fun LongBound.negateIf(negate : Boolean) : LongBound = if(negate) -this else this
internal fun RealRange.negateIf(negate : Boolean) : RealRange = if(negate) -this else this