package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.sysml.implementation.*
import java.util.*


/**
 * Contains extensions to the SysMDSemantics for SysML v2
 * The SysML v2 semantic actions instantiate classes that are defined in the respective libraries.
 * For example, the 'Port Definition' of SysML instantiates subclasses of 'Port' which is defined in
 * the library 'Ports'.
 *
 * @param semantics semantic actions interface implementation of KerML
 */
class SysMLv2Semantics(semantics: ActionsContext): ActionsContext by semantics  {

    /**
     * Semantic actions of a concrete Feature Implementation
     */
    abstract inner class ConcreteConnectorSemantics<T: ConnectorImplementation>(
        var identification: Identification? = null,
        var source: List<QualifiedName> = mutableListOf(),
        var association: QualifiedName = "Links::Link",
        var target: List<QualifiedName> = mutableListOf(),
        var created: T? = null,
        var creator : () -> T
    ) {
        open fun create() {
            created = creator(). also {
                it.owner = owners.peek()
                it.declaredName = identification?.name
                it.declaredShortName = identification?.shortName
                it.from = source.toIdentityList()
                it.to = target.toIdentityList()
            }

            val featureTyping = FeatureTypingImplementation(
                typedFeature = Resolved(ref = created!!),
                type = Resolved(str = association)
            )
            model.addUnownedElement(created!!, ownerName())
            model.addUnownedElement(featureTyping, startOfOwnerPath = created!!)
            if (source.isNotEmpty()) {
                addReferenceSubsetting(
                    owner = created!!,
                    pathFromOwnerToReferencingFeature = "source",
                    referencedFeature =   source.first()
                )
            }
            if (target.isNotEmpty()) {
                addReferenceSubsetting(
                    owner = created!!,
                    pathFromOwnerToReferencingFeature = "target",
                    referencedFeature = target.first())
            }
            if (generateAnnotations && textualRepresentation != null)
                addAnnotation(textualRepresentation!!, created!!)
        }
    }

    inner class StateUsageSemantics: FeatureActions<StateUsageImplementation>(
        this,
        creator = ::StateUsageImplementation,
        defaultType = mutableListOf("States::StateAction")
    )

    inner class ActionUsageSemantics: FeatureActions<ActionUsageImplementation>(
        this,
        defaultType = mutableListOf("Occurrences::Occurrence"),
        creator = ::ActionUsageImplementation
    )

    inner class AcceptActionUsageSemantics(
        var payloadParameter : ReferenceUsage? = null
    ) :
        FeatureActions<AcceptActionUsageImplementation>(
            this,
            creator = ::AcceptActionUsageImplementation,
            defaultType = mutableListOf("Occurrences::Occurrence"),
        ) {
            fun create() {
                super.create(Identification(name = "accept_" + UUID.randomUUID().toString()))
                payloadParameter?.let {
                    model.addUnownedElement(it, startOfOwnerPath = created!!)
                }
            }
        }

    abstract inner class ReferenceUsageSemantics :
        FeatureActions<ReferenceUsageImplementation>(
            this,
            creator = ::ReferenceUsageImplementation,
            mutableListOf("Base::Anything")
        )

    inner class PayloadParameterSemantics : ReferenceUsageSemantics()

    inner class GuardConditionSemantics: FeatureActions<FeatureImplementation>(
        this,
        creator = ::FeatureImplementation,
        mutableListOf("ScalarValues::Boolean")
    )

    inner class TransitionUsageSemantics: FeatureActions<TransitionUsageImplementation>(
            this,
            creator = ::TransitionUsageImplementation,
            defaultType = mutableListOf("Occurrences::Occurrence")
        )


    inner class SuccessionAsUsageSemantics(
        source: List<QualifiedName> = mutableListOf(),
        association: QualifiedName = "Occurrences::HappensBefore",
        target: List<QualifiedName> = mutableListOf(),
    ): ConcreteConnectorSemantics<SuccessionAsUsageImplementation>(
        identification = Identification(name = "succ_" + UUID.randomUUID().toString()),
        source = source,
        association = association,
        target = target,
        creator = ::SuccessionAsUsageImplementation
    )
}