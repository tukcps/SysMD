package com.github.tukcps.sysmd.cspsolver.normalizer

import io.github.tukcps.aadd.values.bool.XBool
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.XBoolImpl

/**
 * Class to store the normalized properties produced by the [CNNormalizer]
 */
data class NormalizedProperties(

    /**
     * Normalized properties
     */
    val properties: MutableList<SimpleProperty<XBoolImpl>>,

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
