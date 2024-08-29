package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.sysml.implementation.*
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
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
    abstract inner class ConcreteAssociationSemantics<T: AssociationImplementation>(
        var owner: QualifiedName = ownerName(),
        var identification: Identification? = null,
        var specialization: QualifiedName = "Links::Link",
        var subsetting: MutableList<QualifiedName> = mutableListOf(),
        var created: T? = null,
        var creator: () -> T
    ) {
        fun create() {
            created = creator().also {
                it.declaredName = identification?.name
                it.declaredShortName = identification?.shortName
                it.owner = Resolved<Namespace>(owner)
            }
            model.addUnownedElement(created!!, owner)
            val specialization = SpecializationImplementation(owner = Resolved(ref=created!!), specific = Resolved(ref = created!!), general = Resolved(specialization))
            model.addUnownedElement(specialization, startOfOwnerPath = created!!)
            if (generateAnnotations) addAnnotation(textualRepresentation, created!!)
        }
    }

    /**
     * Semantic actions of a concrete Feature Implementation
     */
    abstract inner class ConcreteConnectorSemantics<T: ConnectorImplementation>(
        var owner: QualifiedName = ownerName(),
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
                owner = Resolved(str = null, ref = created, id = created!!.elementId),
                typedFeature = Resolved(ref = created!!),
                type = Resolved(str = association)
            )
            model.addUnownedElement(created!!, owner)
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
            if (generateAnnotations) addAnnotation(textualRepresentation, created!!)
        }
    }

    /**
     * Semantic actions of a concrete Feature Implementation
     * @param owner the owner; optional
     * @param identification the identification; usually required (at least in SysMD!)
     * @param multiplicity the multiplicity, optional
     * @param className the name of the class in the SysMLv2 or KerML semantic libraries
     * @param creator lambda that creates the respective instance of KerML classes
     */
    abstract inner class ConcreteFeatureSemantics<T: FeatureImplementation>(
        var owner: QualifiedName = ownerName(),
        var identification: Identification? = null,
        var multiplicity: IntegerRange = IntegerRange(1,1),
        var className: QualifiedName = "Base::Anything",
        var types: List<QualifiedName> = mutableListOf(),
        var references: QualifiedName? = null,
        var redefines: QualifiedName? = null,
        var subsetting: MutableList<QualifiedName> = mutableListOf(),
        var created: T? = null,
        var creator : () -> T
    ) {
        open fun create() {
            val isEnd = END in prefixes
            val direction = when {
                (INOUT in prefixes) -> Feature.FeatureDirectionKind.INOUT
                (OUT in prefixes) -> Feature.FeatureDirectionKind.OUT
                else -> { Feature.FeatureDirectionKind.IN}
            }

            created = creator().also {
                it.owner = owners.peek()
                it.declaredName = identification?.name
                it.declaredShortName
                it.isEnd = isEnd
                it.direction = direction
            }

            val multiplicity = MultiplicityImplementation(
                owner = Resolved(created!!),
                multiplicity = multiplicity.toString()
            )
            val specialization = FeatureTypingImplementation(
                owner = Resolved(ref = created!!),
                typedFeature = Resolved(ref = created!!),
                type = Resolved(str = className)
            )
            val types2 = SpecializationImplementation(
                owner = Resolved(ref = created!!),
                specific = Resolved(ref = created!!),
                general = Resolved(str = types.firstOrNull()?:"")
            )

            // removed references -- not needed if concrete?
            model.addUnownedElement(created!!, toEffectiveName(owner))
            model.addUnownedElement(multiplicity, toEffectiveName(owner + "::${identification!!.toName()}"))
            model.addUnownedElement(specialization, toEffectiveName(owner + "::${identification!!.toName()}"))
            if (types.isNotEmpty())
                model.addUnownedElement(types2, toEffectiveName(owner + "::${identification!!.toName()}"))
            if (generateAnnotations) addAnnotation(textualRepresentation, created!!)
        }
    }

    /**
     * Semantic action for the declaration of a DataType.
     * @param owner The identification of the owner
     * @param identification The name of the DataType
     * @param specialization The qualified name of the Type's superclass
     */
    abstract inner class ConcreteTypeSemantics<T: TypeImplementation>(
        var owner: QualifiedName = ownerName(),
        var identification: Identification = Identification(),
        var specialization: QualifiedName = "Base::Anything",
        var created: T? = null,
        var creator: () -> T
    ) {
        fun create() {
            created = creator().also {
                it.declaredName = identification.name
                it.declaredShortName = identification.shortName
                it.owner = Resolved<Namespace>(owner)
            }
            created?.textualRepresentation = mutableListOf(textualRepresentation)
            model.addUnownedElement(created!!, owner)
            val specialization = SpecializationImplementation(owner = Resolved(ref=created!!), specific = Resolved(ref = created!!), general = Resolved(specialization))
            model.addUnownedElement(specialization, startOfOwnerPath =  created!!)
            if (generateAnnotations) addAnnotation(textualRepresentation, created!!)
        }
    }

    inner class PartDefinitionSemantics(
        identification: Identification? = null,
        specialization: QualifiedName = "Parts::Part",
    ): ConcreteTypeSemantics<PartDefinitionImplementation>(
        identification = identification?: Identification(),
        specialization = specialization,
        creator = ::PartDefinitionImplementation
    )

    inner class ItemDefinitionSemantics(
        identification: Identification? = null,
        specialization: QualifiedName = "Items::Item"
    ): ConcreteTypeSemantics<ItemDefinitionImplementation>(
        identification = identification?: Identification(),
        specialization = specialization,
        creator = ::ItemDefinitionImplementation
    )

    inner class OccurrenceDefinitionSemantics(
        identification: Identification? = null,
        specialization: QualifiedName = "Occurrences::Occurrence"
    ): ConcreteTypeSemantics<OccurrenceDefinitionImplementation>(
        identification = identification?: Identification(),
        specialization = specialization,
        creator = ::OccurrenceDefinitionImplementation
    )

    inner class PortDefinitionSemantics(
        identification: Identification? = null,
        specialization: QualifiedName = "Ports::Port",
    ): ConcreteTypeSemantics<PortDefinitionImplementation>(
        identification = identification?: Identification(),
        specialization = specialization,
        creator = ::PortDefinitionImplementation
    )

    inner class ConnectionDefinitionSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification? = null,
        specialization: QualifiedName = "Connections::Connection",
        creator: ()->ConnectionDefinitionImplementation = ::ConnectionDefinitionImplementation
    ): ConcreteAssociationSemantics<ConnectionDefinitionImplementation>(
        owner = owner,
        identification = identification,
        specialization = specialization,
        creator = creator
    )

    inner class AllocationDefinitionSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification? = null,
        specialization: QualifiedName = "Allocations::Allocation",
    ): ConcreteAssociationSemantics<AllocationDefinitionImplementation>(
        owner = owner,
        identification = identification,
        specialization = specialization,
        creator = ::AllocationDefinitionImplementation
    )

    inner class AttributeDefinitionSemantics(
        var owner: QualifiedName = ownerName(),
        var identification: Identification = Identification(),
        var superclass: QualifiedName = "Base::Anything",
        var created: DataType? = null
    ) {
        fun create() {
            // Call constructor depending on the type
            created = AttributeDefinitionImplementation(declaredName = identification.name, declaredShortName = identification.shortName, owner = owners.peek())
            model.addUnownedElement(created!!, path=owner)
            val specialization = SpecializationImplementation(owner = Resolved(ref=created!!), specific = Resolved(ref = created!!), general = Resolved(superclass))
            model.addUnownedElement(specialization, startOfOwnerPath = created!!)
            if (generateAnnotations) addAnnotation(textualRepresentation, created!!)
        }
    }

    inner class PartUsageSemantics(
        typeName: QualifiedName = "Parts::Part",
        identification: Identification? = null
    ) : ConcreteFeatureSemantics<PartUsageImplementation>(
        className = typeName,
        identification = identification,
        creator = ::PartUsageImplementation
    )

    inner class ItemUsageSemantics(
        typeName: QualifiedName = "Items::Item",
        identification: Identification? = null
    ) : ConcreteFeatureSemantics<ItemUsageImplementation>(
        className = typeName,
        identification = identification,
        creator = ::ItemUsageImplementation
    )


    inner class OccurrenceUsageSemantics(
        typeName: QualifiedName = "Occurrences::Occurrence",
        identification: Identification? = null
    ) : ConcreteFeatureSemantics<OccurrenceUsageImplementation>(
        className = typeName,
        identification = identification,
        creator = ::OccurrenceUsageImplementation
    )

    inner class StateUsageSemantics(
        typeName: QualifiedName = "States::StateAction") :
        ConcreteFeatureSemantics<StateUsageImplementation>(
            className = typeName,
            creator = ::StateUsageImplementation
        )

    inner class ActionUsageSemantics(
        typeName: QualifiedName = "Occurrences::Occurrence") :
        ConcreteFeatureSemantics<ActionUsageImplementation>(
            className = typeName,
            creator = ::ActionUsageImplementation
        )

    inner class AcceptActionUsageSemantics(
        typeName: QualifiedName = "Occurrences::Occurrence",
        var payloadParameter : ReferenceUsage? = null
    ) :
        ConcreteFeatureSemantics<AcceptActionUsageImplementation>(
            className = typeName,
            creator = ::AcceptActionUsageImplementation,
            identification = Identification(name = "accept_" + UUID.randomUUID().toString())
        ) {
            override fun create() {
                super.create()
                payloadParameter?.let {
                    it.owner = Resolved(created!!)
                    created!!.payloadParameter = Resolved(ref = it)
                }
            }
        }

    abstract inner class ReferenceUsageSemantics(
        identification: Identification? = null,
        typeName: QualifiedName) :
        ConcreteFeatureSemantics<ReferenceUsageImplementation>(
            className = typeName,
            identification = identification,
            creator = ::ReferenceUsageImplementation
        )

    inner class PayloadParameterSemantics(
        typeName: QualifiedName
    ) : ReferenceUsageSemantics(
        typeName = typeName,
        identification = Identification(name = "payload")
    )

    inner class TransitionUsageSemantics(
        var source : QualifiedName? = null,
        var target: QualifiedName? = null,
        typeName: QualifiedName = "Occurrences::Occurrence"
    ):
        ConcreteFeatureSemantics<TransitionUsageImplementation>(
            className = typeName,
            identification = Identification(name = "trans_" + UUID.randomUUID().toString()),
            creator = ::TransitionUsageImplementation
        )

    inner class PortUsageSemantics(
        identification: Identification? = null,
        className: QualifiedName = "Ports::Port",
        multiplicity: IntegerRange = IntegerRange(1,1),
    ): ConcreteFeatureSemantics<PortUsageImplementation>(
        identification = identification?: Identification(),
        multiplicity = multiplicity,
        className = className,
        creator = ::PortUsageImplementation
    )

    inner class ConnectionUsageSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification? = null,
        source: List<QualifiedName> = mutableListOf(),
        association: QualifiedName = "Connections::Connection",
        target: List<QualifiedName> = mutableListOf(),
        creator: ()->ConnectionUsageImplementation = ::ConnectionUsageImplementation
    ): ConcreteConnectorSemantics<ConnectionUsageImplementation>(
        owner = owner,
        identification = identification,
        source = source,
        association = association,
        target = target,
        creator = creator
    )

    inner class AllocationUsageSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification? = null,
        source: List<QualifiedName> = mutableListOf(),
        association: QualifiedName = "Connections::Connection",
        target: List<QualifiedName> = mutableListOf(),
    ): ConcreteConnectorSemantics<ConnectionUsageImplementation>(
        owner = owner,
        identification = identification,
        source = source,
        association = association,
        target = target,
        creator = ::AllocationUsageImplementation
    )

    inner class SuccessionAsUsageSemantics(
        owner: QualifiedName = ownerName(),
        source: List<QualifiedName> = mutableListOf(),
        association: QualifiedName = "Occurrences::HappensBefore",
        target: List<QualifiedName> = mutableListOf(),
    ): ConcreteConnectorSemantics<SuccessionAsUsageImplementation>(
        owner = owner,
        identification = Identification(name = "succ_" + UUID.randomUUID().toString()),
        source = source,
        association = association,
        target = target,
        creator = ::SuccessionAsUsageImplementation
    )

    inner class RequirementUsageSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification = Identification(),
        superclass: QualifiedName = "Requirements::RequirementUsage",
        subject: QualifiedName? = null,
    ): ConcreteFeatureSemantics<RequirementUsageImplementation>(
        owner = owner,
        identification = identification,
        className = superclass,
        creator = ::RequirementUsageImplementation,
        references = subject
    )

    inner class RequirementDefinitionSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification = Identification(),
        specialization: QualifiedName = "Requirements::RequirementDefinition",
        creator: ()-> RequirementDefinitionImplementation = ::RequirementDefinitionImplementation
    ): ConcreteTypeSemantics<RequirementDefinitionImplementation>(
        owner = owner,
        identification = identification,
        specialization = specialization,
        creator = creator
    )

    inner class RequirementConstraintUsageSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification = Identification(),
        superclass: QualifiedName = "ScalarValues::Boolean",
        var kindRequire: Boolean = true,
        creator: ()-> FeatureImplementation = { FeatureImplementation() }
    ): ConcreteFeatureSemantics<FeatureImplementation>(
        owner = owner,
        types =  mutableListOf("Requirements::RequirementUsage"),
        identification = identification,
        className = superclass,
        creator = creator
    )

    inner class RequirementAssumeUsageSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification = Identification(),
        superclass: QualifiedName = "ScalarValues::Boolean",
        creator: ()-> FeatureImplementation = { FeatureImplementation() }
    ): ConcreteFeatureSemantics<FeatureImplementation>(
        owner = owner,
        types =  mutableListOf("Requirements::SatisfyRequirementUsage"),
        identification = identification,
        className = superclass,
        creator = creator
    )

    inner class AssertSemantics(
        owner: QualifiedName = ownerName(),
        identification: Identification = Identification(),
        superclass: QualifiedName = "ScalarValues::Boolean",
        creator: ()-> InvariantImplementation = { InvariantImplementation() }
    ): ConcreteFeatureSemantics<InvariantImplementation>(
        owner = owner,
        identification = identification,
        className = superclass,
        creator = creator
    )
}