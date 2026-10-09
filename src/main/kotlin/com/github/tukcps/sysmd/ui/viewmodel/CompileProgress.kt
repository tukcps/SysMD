package com.github.tukcps.sysmd.ui.viewmodel

import com.github.tukcps.sysmd.services.Runlevel

/**
 * What a running compile or solve of a project currently does.
 * @param message A short text for the user.
 * @param fraction A rough estimate of how much is done, 0.0 .. 1.0.
 */
data class CompileProgress(
    val message: String,
    val fraction: Float
) {
    companion object {
        /**
         * Share of the overall work that is done when a stage of the analysis starts; the cells are compiled before.
         * The numbers are rough: they were measured with a large model (some 12 000 variables).
         */
        private val stageStart = linkedMapOf(
            Runlevel.NAMES_RESOLVED to 0.15f,
            Runlevel.TYPES_INHERITED to 0.19f,
            Runlevel.FEATURE_CHAINS_RESOLVED to 0.34f,
            Runlevel.MODEL to 0.36f,
            Runlevel.VARIABLES to 0.44f,
            Runlevel.VARIANCE_CHECKED to 0.70f,
            Runlevel.SOLVED to 0.76f,
        )

        private val stageMessage = mapOf(
            Runlevel.NAMES_RESOLVED to "Resolving names",
            Runlevel.TYPES_INHERITED to "Inheriting features",
            Runlevel.FEATURE_CHAINS_RESOLVED to "Resolving feature chains",
            Runlevel.MODEL to "Checking model",
            Runlevel.VARIABLES to "Creating variables",
            Runlevel.VARIANCE_CHECKED to "Checking consistency",
            Runlevel.SOLVED to "Solving",
        )

        /** Share of the overall work at which a run up to the given runlevel is complete. */
        private fun end(target: Runlevel): Float =
            stageStart.entries.firstOrNull { it.key > target }?.value ?: 1f

        val preparing = CompileProgress("Preparing", 0f)
        val showingResults = CompileProgress("Showing results", 1f)

        /** Progress while cell number [index] (from 0) of [count] is compiled, in a run up to [target]. */
        fun compiling(index: Int, count: Int, target: Runlevel) = CompileProgress(
            message = "Compiling cell ${index + 1} of $count",
            fraction = stageStart.getValue(Runlevel.NAMES_RESOLVED) * index / count / end(target)
        )

        /** Progress when [stage] of the analysis starts, in a run up to [target]. */
        fun analyzing(stage: Runlevel, target: Runlevel) = CompileProgress(
            message = stageMessage[stage] ?: "Analyzing model",
            fraction = ((stageStart[stage] ?: 0f) / end(target)).coerceAtMost(1f)
        )
    }
}
