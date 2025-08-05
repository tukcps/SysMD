@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName


/**
 * Semantic action for the definition of an association type.
 * @param context Context of semantic analysis
 * @param creator Lambda that creates an Association
 * @param specializes The qualified names of general types
 * an Association will be created; otherwise, a Classifier
 */
open class AssociationActions<T: Association>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: QualifiedName = "Links::BinaryLink",
): ClassifierActions<T>(context, creator, specializes),
    RelationshipActionsAssoc<T>



/**
 * Additional functions for an association:
 * - addSource adds references to sources
 * - addTarget adds references to targets
 */
interface RelationshipActionsAssoc<T: Association>: RelationshipActions<T>

/**
 * Additional functions for connectors
 */
interface ConnectorRelationshipActions<T: Connector>: RelationshipActions<T>

/**
 * Adds a connector to the model.
 * @param context Semantic context during compilation
 * @param defaultType Type if no type is given
 */
open class ConnectorActions<T: Connector>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: String = "Links::Link",
): FeatureActions<T>(context, creator, defaultType), ConnectorRelationshipActions<T>


/**
 * Semantic action for the definition of a Function.
 * @param context
 * @param creator lambda that creates T: Function
 * @param specializes The qualified name of the Calculation's superclass
 */
open class FunctionActions<T: Function>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: QualifiedName = "Base::Anything",
): ClassifierActions<T>(context, creator, specializes)


class MetaclassActions<T: Metaclass> (
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: String = "Base::Anything",
): ClassActions<T>(context, creator, defaultType)

/**
 * Semantic actions for the definition of a Metadata feature.
 */
class MetadataFeatureActions<T: MetadataFeature>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: QualifiedName = "Base::Anything",
): FeatureActions<T>(context, creator, defaultType) {
    var identificationOrType: Identification? = null
    var typeIfPresent: QualifiedName? = null
    override fun create(identification: Identification?) {
        if (typeIfPresent == null) {
            super.create(null)
            context.addTyping(type = identificationOrType!!.name!!)
        } else {
            super.create(identification = identificationOrType!!)
            context.addTyping(type = typeIfPresent!!)
        }
    }
}