package com.github.tukcps.sysmd.model.kerml


/**
 * 8.3.4.5.3 Connector
 * Description
 * A Connector is a usage of Associations, with links restricted according to instances of the Type in which they
 * are used (domain of the Connector).
 * The associations of the Connector restrict what kinds of things might be linked.
 * The Connector further restricts these links to be between values of Features on instances of its domain.
 * General Classes: Relationship, Feature
 *
 * Attributes:
 *  /association : Association [0..*] {redefines type, ordered}
 *   The Associations that type the Connector.
 *
 * /connectorEnd : Feature [0..*] {redefines endFeature, ordered}
 * The endFeatures of a Connector, which redefine the endFeatures of the associations of the Connector.
 * The connectorEnds determine via ReferenceSubsetting Relationships which Features are related by the
 * Connector.
 *
 * /relatedFeature : Feature [0..*] {redefines relatedElement, ordered, nonunique}
 * The Features that are related by this Connector considered as a Relationship and that restrict the links it
 * identifies, given by the referenced Features of the connectorEnds of the Connector.
 *
 * /sourceFeature : Feature [0..1] {subsets relatedFeature, redefines source, ordered}
 * The source relatedFeature for this Connector. It is the first relatedFeature.
 *
 * /targetFeature : Feature [0..*] {subsets relatedFeature, redefines target, ordered}
 * The target relatedFeatures for this Connector. This includes all the relatedFeatures other than the
 * sourceFeature.
 * Operations: None.
 *
 * Constraints
 * checkConnectorBinaryObjectSpecialization
 * A binary Connector for an AssociationStructure must directly or indirectly specialize the base Connector
 * Objects::binaryLinkObjects from the Kernel Semantic Library.
 * connectorEnds->size() = 2 and
 * association->exists(oclIsKindOf(AssocationStructure)) implies
 * specializesFromLibrary('Objects::binaryLinkObjects')
 * checkConnectorBinarySpecialization
 * Kernel Modeling Language (KerML) v1.0 Beta 1 177
 * A binary Connector must directly or indirectly specialize the base Connector Links::binaryLinks from the
 * Kernel Semantic Library.
 * connectorEnd->size() = 2 implies
 * specializesFromLibrary('Links::binaryLinks')
 * checkConnectorObjectSpecialization
 * A Connector for an AssociationStructure must directly or indirectly specialize the base Connector
 * Objects::linkObjects from the Kernel Semantic Library.
 * association->exists(oclIsKindOf(AssociationStructure)) implies
 * specializesFromLibrary('Objects::linkObjects')
 * checkConnectorSpecialization
 * A Connector must directly or indirectly specialize the base Connector Links::links from the Kernel Semantic
 * Library.
 * specializesFromLibrary('Links::links')
 * checkConnectorTypeFeaturing
 * Each relatedFeature of a Connector must have each featuringType of the Connector as a direct or indirect
 * featuringType (where a Feature with no featuringType is treated as if the Classifier Base::Anything
 * was its featuringType).
 * relatedFeature->forAll(f |
 * if featuringType->isEmpty() then f.isFeaturedWithin(null)
 * else featuringType->forAll(t | f.isFeaturedWithin(t))
 * endif)
 * deriveConnectorRelatedFeature
 * The relatedFeatures of a Connector are the referenced Features of its connectorEnds.
 * relatedFeature = connectorEnd.ownedReferenceSubsetting->
 * select(s | s <> null).subsettedFeature
 * deriveConnectorSourceFeature
 * The sourceFeature of a Connector is its first relatedFeature (if any).
 * sourceFeature =
 * if relatedFeature->isEmpty() then null
 * else relatedFeature->first()
 * endif
 * deriveConnectorTargetFeature
 * The targetFeatures of a Connector are the relatedFeatures other than the sourceFeature.
 * targetFeature =
 * if relatedFeature->size() < 2 then OrderedSet{}
 * else
 * relatedFeature->
 * 178 Kernel Modeling Language (KerML) v1.0 Beta 1
 * subSequence(2, relatedFeature->size())->
 * asOrderedSet()
 * endif
 * validateConnectorBinarySpecialization
 * If a Connector has more than two connectorEnds, then it must not specialize, directly or indirectly, the
 * Association BinaryLink from the Kernel Semantic Library.
 * connectorEnds->size() > 2 implies
 * not specializesFromLibrary('Links::BinaryLink')
 * validateConnectorRelatedFeatures
 * If a Connector is concrete (not abstract), then it must have at least two relatedFeatures.
 * not isAbstract implies relatedFeature->size() >= 2
 *
 */
interface Connector: Relationship, Feature {
    val association: Association
    var from: MutableList<Element>
    var to: MutableList<Element>
    var isDirected: Boolean
    override fun clone(): Connector
    override fun updateFrom(template: Element)

    var sourceFeature: Feature?
        get() = source.firstOrNull() as Feature?
        set(value) { source = if(value == null) mutableListOf() else mutableListOf(value) }


    @Suppress("UNCHECKED_CAST")
    var targetFeature: MutableList<Feature>
        get() = target as MutableList<Feature>
        set(value) { target = value as MutableList<Element> }
}