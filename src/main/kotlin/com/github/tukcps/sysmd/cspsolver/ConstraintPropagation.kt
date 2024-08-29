package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.model.kerml.Element

interface ConstraintPropagation {
    /**
     * Method that updates fields of this element from another element.
     */
    fun updateFrom(template: Element)

    /**
     * is true if the element has been updated in evaluate/update cycles of constraint propagation
     * mechanisms etc.; prior to its use, it shall be set to false.
     */
    var updated: Boolean

    /** Is true if the element has been changed w.r.t. the last commit and needs to be included in a new commit */
    var hasBeenChanged: Boolean
}