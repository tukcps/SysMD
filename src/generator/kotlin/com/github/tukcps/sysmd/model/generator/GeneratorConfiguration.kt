package com.github.tukcps.sysmd.model.generator

import java.nio.file.Path
import java.util.*

/**
 * Common configuration shared by all generators.
 */
object GeneratorConfiguration {
    /**
     * OMG metamodel release.
     */
    const val VERSION = "20250201"
    const val KERML_SOURCE = "https://www.omg.org/spec/KerML/$VERSION/KerML.xmi"
    const val SYSML_SOURCE = "https://www.omg.org/spec/SysML/$VERSION/SysML.xmi"

    /** Local KerML XMI file. */
    val KERML_XMI: Path = Path.of("src", "main", "resources", "metamodel", "KerML.xmi")

    /** Local SysML XMI file. */
    val SYSML_XMI: Path = Path.of("src", "main", "resources", "metamodel", "SysML.xmi")

    /** Root directory of the model sources. */
    val MODEL: Path = Path.of("src", "main", "kotlin", "com", "github", "tukcps", "sysmd", "model")

    /** Expression model sources. */
    val EXPRESSION_MODEL: Path = MODEL.resolve("expression")

    /** KerML model sources. */
    val KERML_MODEL: Path = MODEL.resolve("kerml")

    /** SysML model sources. */
    val SYSML_MODEL: Path = MODEL.resolve("sysml")

    /** Source directories containing metamodel interfaces and implementations. */
    val MODEL_DIRECTORIES = listOf(
        EXPRESSION_MODEL,
        KERML_MODEL,
        SYSML_MODEL
    )

    /** KerML metaclasses implemented in the expression package. */
    val EXPRESSION_CLASSES = setOf(
        "BooleanExpression",
        "CollectExpression",
        "ConstructorExpression",
        "Expression",
        "FeatureChainExpression",
        "FeatureReferenceExpression",
        "IndexExpression",
        "InstantiationExpression",
        "Invariant",
        "InvocationExpression",
        "LiteralBoolean",
        "LiteralExpression",
        "LiteralInfinity",
        "LiteralInteger",
        "LiteralRational",
        "LiteralString",
        "MetadataAccessExpression",
        "NullExpression",
        "OperatorExpression",
        "SelectExpression",
        // already existing in KerML
        // "Multiplicity",
        // "MultiplicityRange"
    )

    const val KERML_PACKAGE = "com.github.tukcps.sysmd.model.kerml"
    const val SYSML_PACKAGE = "com.github.tukcps.sysmd.model.sysml"
    const val EXPRESSION_PACKAGE = "com.github.tukcps.sysmd.model.expression"

    /**
     * Additional imports for the generated element data interface.
     */
    val ELEMENT_DATA_IMPORTS = setOf(
        "com.github.tukcps.sysmd.model.generated.ElementType",
        "kotlin.uuid.Uuid",
        "com.github.tukcps.sysmd.rest.entities.api.entities.Identified",
        "com.github.tukcps.sysmd.model.kerml.Feature.FeatureDirectionKind",
        "com.github.tukcps.sysmd.model.kerml.Import.VisibilityKind",
        "com.github.tukcps.sysmd.model.sysml.OccurrenceUsage.PortionKind",
        "com.github.tukcps.sysmd.model.sysml.RequirementConstraintMembership.RequirementConstraintKind",
        "com.github.tukcps.sysmd.model.sysml.StateSubactionMembership.StateSubactionKind",
        "com.github.tukcps.sysmd.model.sysml.TransitionFeatureMembership.TransitionFeatureKind",
        "com.github.tukcps.sysmd.model.sysml.TriggerInvocationExpression.TriggerKind",
        "com.github.tukcps.sysmd.model.sysml.implementation.TransitionFeatureMembershipImplementation"
    )

    /**
     * Kotlin type overrides for generated element data properties.
     */
    val ELEMENT_DATA_TYPE_OVERRIDES = mapOf(
        "elementId" to "Uuid",
        "importedMemberName" to "String?", // For transition ... should be in relationship
        "importedNamespace" to "String?",  // s.above.
        "isNameLiteral" to "Boolean?", // for StringLiteral. Required for legacy functions.
    )

    /**
     * Element data properties that are always required.
     */
    val REQUIRED_ELEMENT_DATA_PROPERTIES = setOf(
        "elementId"
    )

    /**
     * Properties to be added. Not part of Metamodel, but might help implementation.
     * Configuration for SysMD contains these synthetic properties to (temporarily) persist information.
     * Goes later, e.g., to relationships.
     */
    val ELEMENT_DATA_SYNTHETIC_PROPERTIES = listOf(
        // for transition until import fixed
        SyntheticProperty(className = "Import", name = "importedMemberName", type = "String?"),
        SyntheticProperty(className = "Import", name = "importedNamespace", type = "String?"),
        SyntheticProperty(className = "Conjugation", name = "isConjugated", type = "Boolean?"),
        // for Compiler, tracking of relation to input
        SyntheticProperty(className = "./.", name = "input", type = "String?"),
        SyntheticProperty(className = "./.", name = "indices", type = "IntRange?"),
    )

    /**
     * Structural element references to be included in the element data model.
     * Typically, elements that are, e.g., derived, and hence would not be added by default.
     */
    val ELEMENT_DATA_REFERENCES = mapOf(
        "Element" to setOf(
            "ownedElement",
            "owner",
            "owningMembership", // Drop?
            "owningNamespace",  // Drop?
            "owningRelationship",
            "ownedRelationship"
    ),
        "Relationship" to setOf(
            "source",
            "target",
            "owningRelatedElement",
            "ownedRelatedElement"
        )
    )

    /**
     * A set of classes for which the factory either cannot call a constructor, or should call a different constructor.
     * e.g. because they are sealed or abstract in the target implementation.
     */
    val REMAPPED_CONSTRUCTORS = mapOf(
        "Expression" to Optional.of("BodyExpressionImplementation"), // SysMD has a specific subtype for these expressions
        "LiteralExpression" to Optional.empty(), // Do unspecified literals even make sense?
        "FeatureChaining" to Optional.empty() // we don't implement these.
    )

    /**
     * Derived properties included in the generated element data model.
     *
     * Derived properties are excluded by default because they can be recomputed
     * from the semantic model. Only add properties here if they are required
     * during parsing, compilation, or model exchange.
     */
    val ELEMENT_DATA_DERIVED_PROPERTIES = setOf(
        /* On Relationship. Needed for properly tracking ownership.
        * Is derived as `subsets relatedElement`, but which element is picked cannot be reconstructed.
        */
        "owningRelatedElement",
        /* Also on relationship. Per standard, ownedElement only contains indirectly owned elements. */
        "ownedRelatedElement"
    )
}


/**
 * Properties that are introduced artificially by user.
 * Not part of Metamodel ... just for implementation.
 */
data class SyntheticProperty(
    val className: String,
    val name: String,
    val type: String,
)