package com.github.tukcps.sysmd.cspsolver.normalizer

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.services.session.Session

/**
 * Class to store the normalized properties produced by the [CNNormalizer]
 */
data class NormalizedProperties(

    /**
     * Normalized properties
     */
    val properties: MutableList<SimpleProperty<XBool>>,

    /**
     * The model for which the properties were normalized
     */
    val model : Session
    ) : Cloneable {

    //override fun copy()
    public override fun clone(): NormalizedProperties {
        return NormalizedProperties(this.properties.toMutableList(), this.model)
    }
}