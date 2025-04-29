package com.github.tukcps.sysmd.services

import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.ite
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session


/**
 * Searches for a feature in a type and, if not found in its subclasses.
 * The value of the feature is computed using the ITE function.
 */
fun Session.estimateFeature(type: Type, qualifiedName: QualifiedName): VectorQuantity {
    // It is already known from this or a superclass.
    val p = type.resolveVar(qualifiedName)

    if (p != null) return p.vectorQuantity

    val subtypes = type.subtypes
        // getSubtypes(type)
    var quantity: VectorQuantity? = null
    for (subclass in subtypes) {
        quantity = if (subclass == subtypes.first())
            estimateFeature(subclass, qualifiedName)
        else {
            val alternative = builder.variable("select_" + subclass.elementId, qualifiedName, true)
            alternative.ite(estimateFeature(subclass, qualifiedName), quantity!!)
        }
    }
    return quantity!!
}