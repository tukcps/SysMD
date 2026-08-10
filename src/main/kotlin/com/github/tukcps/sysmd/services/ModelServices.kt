package com.github.tukcps.sysmd.services

import com.github.tukcps.sysmd.cspsolver.ConstraintPropagation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.check.CheckSemanticConstraints
import com.github.tukcps.sysmd.services.session.Session

/**
 * API of the model elements for
 * - Methods needed for verification of metamodel constraints
 * - Methods needed by constraint propagation
 */
interface ModelServices:
    Cloneable, ConstraintPropagation, CheckSemanticConstraints
{
   /**
    * The session and model to which the element belongs.
    */
    val model: Session

    /**
     * The indices in the input string during a parse run.
     * Start is the first token of the production, last one the start of a body.
     */
    var indices: IntRange?

    var input: CharSequence?

    /**
     * Avoid use of clone - it creates a NEW, independent element that has the same class and
     * fields as this, but a different elementId.
     */
    public override fun clone(): Element
}
