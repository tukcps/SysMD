@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.QualifiedName

/**
 * Adds a connector to the model.
 * @param context Semantic context during compilation
 * @param isImplicit Type if no type is given
 */
open class ConnectorAction(
    context: ActionsContext,
    type: ElementType = ElementType.Connector,
    isImplicit: String = "Links::Link",
): FeatureAction(context = context, type = type, isImplicit = isImplicit)

/** Semantic action for the definition of a Metaclass. */
class MetaclassAction (
    context: ActionsContext,
    type: ElementType = ElementType.Metaclass,
    isImplicit: String = "Metaobjects::Metaobject",
): ClassAction(context, type, isImplicit)

/** Semantic action for the definition of a Metadata feature. */
open class MetadataFeatureAction(
    context: ActionsContext,
    type: ElementType = ElementType.MetadataFeature,
    isImplicit: QualifiedName = "Base::Anything",
): FeatureAction(context, type, isImplicit) {
    var identificationOrType: Identification? = null
    var typeIfPresent: QualifiedName? = null
    override fun afterProduction() {
        if (typeIfPresent == null) {
            if (identificationOrType?.name != null)
                context.addTyping(type = identificationOrType!!.name!!)
        } else {
            context.addTyping(type = typeIfPresent!!)
        }
        super.afterProduction()
    }
}