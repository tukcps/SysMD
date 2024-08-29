package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.cspsolver.valuefeatures.RelatedValueFeature

abstract class SolvingStrategy(private val solver: DiscreteSolver) {

    abstract val input: Set<Variable>

    abstract val output: Set<RelatedValueFeature>

    abstract fun solve()
}