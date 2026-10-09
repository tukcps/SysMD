package com.github.tukcps.sysmd.model.util

import io.github.tukcps.aadd.values.bounds.LongBound
import io.github.tukcps.aadd.values.integer.IntegerRange

/**
 * Default multiplicity of usages (e.g. `part`, `attribute`, `item`):
 * exactly one (`[1]`).
 */
val DEFAULT_USAGE_MULTIPLICITY: IntegerRange = IntegerRange.One

/**
 * Default multiplicity of features, types and classifiers:
 * zero or more (`[0..*]`).
 */
val DEFAULT_TYPE_MULTIPLICITY: IntegerRange =
    IntegerRange(LongBound.Finite(0), LongBound.PositiveInfinity)
