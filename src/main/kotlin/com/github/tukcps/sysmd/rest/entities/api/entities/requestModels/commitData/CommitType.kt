package io.github.tukcps.sysmlv2.api.entities.requestModels.commitData

/**
 * Commit types of the elements.
 * Note: currently incomplete.
 */
class CommitType {
    companion object {
        const val ASSOCIATION: String = "Association"
        const val ANNOTATING_ELEMENT: String = "AnnotatingElement"
        const val ANNOTATION: String = "Annotation"
        const val CLASSIFIER: String = "Classifier"
        const val CLASS: String = "Class"
        const val COMMENT: String = "Comment"
        const val CONNECTOR: String = "Connector"
        const val DOCUMENTATION: String = "Documentation"
        const val ELEMENT: String = "Element"
        const val FEATURE: String = "Feature"
        const val FEATURE_TYPING: String = "FeatureTyping"
        const val FEATURING: String = "Featuring"
        const val RELATIONSHIP: String = "Relationship"
        const val PACKAGE: String = "Package"
        const val NAMESPACE_IMPORT: String = "NamespaceImport"
        const val MEMBERSHIP_IMPORT: String = "MembershipImport"
        const val MULTIPLICITY: String = "Multiplicity"
        const val OWNING_MEMBERSHIP: String = "OwningMembership"
        const val SPECIALIZATION: String = "Specialization"
        const val TYPE_FEATURING: String = "TypeFeaturing"
        const val REFERENCING: String = "Referencing"
        const val TYPE: String = "Type"
        const val VALUE_FEATURE: String = "ValueFeature"
        const val NAMESPACE: String = "Namespace"
        const val TEXTUAL_REPRESENTATION: String = "TextualRepresentation"
    }
}