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
    specializes: MutableList<QualifiedName> = mutableListOf("Links::BinaryLink"),
): ClassifierActions<T>(context, creator, specializes), RelationshipActionsAssoc<T>


/**
 * Additional functions for an association:
 * - addSource adds references to sources
 * - addTarget adds references to targets
 */
interface RelationshipActionsAssoc<T: Association>: RelationshipActions<T> {

    override fun addSource(source: List<QualifiedName>) {
        super.addSource(source)
        if (source.isNotEmpty()) {
            context.addReferenceSubsetting(
                owner = created!!,
                pathFromOwnerToReferencingFeature = "source",
                referencedFeature = source.first()
            )
        }
    }

    override fun addTarget(target: List<QualifiedName>) {
        super.addTarget(target)
        if (target.isNotEmpty()) {
            context.addReferenceSubsetting(
                owner = created!!,
                pathFromOwnerToReferencingFeature = "target",
                referencedFeature = target.first())
        }
    }
}

/**
 * Additional functions for connectors
 */
interface ConnectorRelationshipActions<T: Connector>: RelationshipActions<T> {
    override var created: T?
    override val context: ActionsContext

    override fun addSource(source: List<QualifiedName>) {
        created?.source = source.toIdentityList()
        if (source.isNotEmpty()) {
            context.addReferenceSubsetting(
                owner = created!!,
                pathFromOwnerToReferencingFeature = "source",
                referencedFeature = source.first())
        }
    }

    override fun addTarget(target: List<QualifiedName>) {
        created?.target = target.toIdentityList()

        if (target.isNotEmpty()) {
            context.addReferenceSubsetting(
                owner = created!!,
                pathFromOwnerToReferencingFeature = "target",
                referencedFeature = target.first())
        }
    }
}

/**
 * Adds a connector to the model.
 * @param context Semantic context during compilation
 * @param defaultType Type if no type is given
 */
open class ConnectorActions<T: Connector>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: MutableList<String> = mutableListOf("Links::Link")
): FeatureActions<T>(context, creator, defaultType),
    ConnectorRelationshipActions<T>


/**
 * Semantic action for the definition of a Function.
 * @param context
 * @param creator lambda that creates T: Function
 * @param specializes The qualified name of the Calculation's superclass
 */
open class FunctionActions<T: Function>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<QualifiedName> = mutableListOf("Base::Anything")
): ClassifierActions<T>(context, creator, specializes)


class MetaclassActions<T: Metaclass> (
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: MutableList<String> = mutableListOf("Base::Anything")
): ClassActions<T>(context, creator, defaultType)

/**
 * Semantic actions for the definition of a Metadata feature.
 */
class MetadataFeatureActions<T: Feature>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: MutableList<QualifiedName> = mutableListOf("Base::Anything")
): FeatureActions<T>(context, creator, defaultType) {
    var identificationOrType: Identification? = null
    var typeIfPresent: QualifiedName? = null
    fun create() {
        if (typeIfPresent == null) {
            super.create(identification = Identification())
            addTyping(type = mutableListOf(identificationOrType!!.name!!))
        } else {
            super.create(identification = identificationOrType!!)
            addTyping(type = mutableListOf(typeIfPresent!!))
        }
    }
}