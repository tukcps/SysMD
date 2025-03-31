package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.cspsolver.Variable.BaseType.Unknown
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.expression.checkEvent
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.report
import com.github.tukcps.sysmd.services.session.reportInfo
import com.github.tukcps.sysmd.services.session.Session


/**
 * Most simple constraint propagation; just until Jack is finished.
 * Requires calling initialize if ast is not yet initialized, e.g., if it comes from database or REST.
 * Or as a benchmark to demonstrate the benefit of his method.
 */
fun Session.propagate() {
    try {
        if (repo.schedule.isEmpty())
            initialize()
        if (!dSolver.isInitialized())
            dSolver.initialize(this)
        // We use the inv { ... } syntax from standard SysMLv2 / KerML hence this is no longer needed:
        // else
        //    dSolver.processRequirements(get().filterIsInstance<Expression>().filter { it.type?.str?.contains("Requirement") == true })

        var modelIsStable: Boolean
        repo.schedule.forEach {
            it.stable = false
            it.updated = false
        }
        status.numberOfPropagateIterations = 1
        var instable: Set<Variable>
        do {
            instable = mutableSetOf()
            modelIsStable = true
            repo.schedule.forEach { value ->
                try {
                    assert(value.baseType != Unknown)
                    if ( value.baseType != Variable.BaseType.String ) {
                        modelIsStable = modelIsStable and value.stable
                        if (value.ast != null) {
                            value.ast!!.evalUpRec()
                            value.ast!!.evalDownRec()
                            value.checkEvent()     // Sets property.stable to false,
                            // if changed in an iteration step, and property.updated iff changed in a 'propagate' call
                            if ( value.baseType == Variable.BaseType.Bool && value.updated ) {
                                dSolver.update(value)
                            }
                        } else
                            value.stable = true
                        if (!value.stable) instable.add(value)
                    }
                } catch (e: Exception) {
                    value.stable = true
                    report(value.feature, e.message ?: "(issue in constraint propagation)", e)
                }
            }
            dSolver.advanceState()
            dSolver.assertConstraints()

            // ----- For debugging ---
            // val inStables = repo.schedule.filter { !it.stable }
            // val stables = repo.schedule.filter { it.stable }
            status.numberOfPropagateIterations += 1
        } while (!modelIsStable && status.numberOfPropagateIterations < 100)
        if (!modelIsStable)
            reportInfo(global, "Number of constraint propagation iterations exceeded; issue with: $instable. Increase it if needed.")

        // Copy updated entries into the status map, check consistency.
        repo.schedule.forEach {
            if (it.updated)
                status.updates[it.elementId!!] = it.valueStr
        }
    } catch (error: Exception) {
        report(SysMDError("During propagation: ${error.message}", cause = error))
    }
}