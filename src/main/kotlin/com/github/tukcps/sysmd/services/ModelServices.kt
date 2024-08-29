package com.github.tukcps.sysmd.services

import com.github.tukcps.sysmd.cspsolver.ConstraintPropagation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.check.CheckMetamodel
import com.github.tukcps.sysmd.services.session.Session

/**
 * API for
 * - Methods needed for verification of metamodel constraints
 * - Methods needed by constraint propagation
 */
interface ModelServices:
    Cloneable,
    ConstraintPropagation,
    CheckMetamodel {
    /**
     * The session and model to which the element belongs.
     */
    var model: Session?

    /**
     * Resolves the names and UUID used in Identity to references and UUID.
     * It returns 'true' if a field was updated; the function is overridden
     * by other classes and used during initialization.
     * @return true if a field was updated
     */
    fun resolveNames(): Boolean

    /**
     * Avoid use of clone - it creates a NEW, independent element that has the same class and
     * fields as this, but a different elementId.
     */
    public override fun clone(): Element
}
