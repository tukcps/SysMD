package com.github.tukcps.sysmd.ui.composables

import androidx.compose.ui.graphics.Color

/**
 * We introduce colors based on the role of classes in the Metamodel.
 */
private enum class BadgeFamily {
    Classification,
    Feature,
    Structure,
    Constraint,
    Connector,
    Behavior,
    Invariant,
    Metadata,
    Default
}

object TypeBadgeColors {

    // ---------- Light -------------------------------------------------------
    private val StructureLight   = BadgeColors(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    private val ClassificationLight  = BadgeColors(Color(0xFFE3F2FD), Color(0xFF1565C0))
    private val FeatureLight     = BadgeColors(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
    private val ConstraintLight  = BadgeColors(Color(0xFFFCE4EC), Color(0xFF880E4F))
    private val ConnectionLight  = BadgeColors(Color(0xFFE8EAF6), Color(0xFF283593))
    private val RequirementLight = BadgeColors(Color(0xFFFFF3E0), Color(0xFFE65100))
    private val BehaviorLight    = BadgeColors(Color(0xFFFFEBEE), Color(0xFFC62828))
    private val MetadataLight    = BadgeColors(Color(0xFFF9FBE7), Color(0xFF558B2F))
    private val DefaultLight     = BadgeColors(Color(0xFFF5F5F5), Color(0xFF424242))

    // ---------- Dark --------------------------------------------------------
    private val StructureDark   = BadgeColors(Color(0xFF29392C), Color(0xFFA7E2AE))
    private val ClassificationDark  = BadgeColors(Color(0xFF26394A), Color(0xFF9FD4FF))
    private val FeatureDark     = BadgeColors(Color(0xFF372840), Color(0xFFD7B4FF))
    private val ConstraintDark  = BadgeColors(Color(0xFF432732), Color(0xFFFFA8D0))
    private val ConnectionDark  = BadgeColors(Color(0xFF2A3150), Color(0xFFBCCAFF))
    private val RequirementDark = BadgeColors(Color(0xFF503824), Color(0xFFFFD18A))
    private val BehaviorDark    = BadgeColors(Color(0xFF4A2B2B), Color(0xFFFFB3B3))
    private val MetadataDark    = BadgeColors(Color(0xFF3A4228), Color(0xFFD8EEA6))
    private val DefaultDark     = BadgeColors(Color(0xFF3A3C3F), Color(0xFFE4E4E4))

    // ---------- Classification ----------------------------------------------
    private fun family(type: String): BadgeFamily = when (type) {

        // Pure hierarchical structure
        "Package",
        "Namespace" ->
            BadgeFamily.Structure

        // Definitions / Types
        "Type", "Classifier", "Class", "DataType", "Function",
        "Association",
        "ItemDefinition", "PartDefinition", "PortDefinition",
        "AttributeDefinition",
        "ConnectionDefinition", "InterfaceDefinition",
        "RequirementDefinition",
        "CalculationDefinition" ->
            BadgeFamily.Classification

        // Features / Usages
        "Feature",
        "AttributeUsage",
        "ItemUsage",
        "PartUsage",
        "PortUsage",
        "ReferenceUsage" ->
            BadgeFamily.Feature

        // Constraints
        "Invariant",
        "ConstraintUsage" ->
            BadgeFamily.Constraint

        // Connections
        "Connection",
        "Connector",
        "ConnectionUsage",
        "InterfaceUsage" ->
            BadgeFamily.Connector

        // Behaviors
        "Behavior",
        "BehaviorUsage",
        "ActionDefinition",
        "ActionUsage",
        "StateDefinition",
        "StateUsage",
        "TransitionUsage" ->
            BadgeFamily.Behavior

        // Requirements
        "RequirementUsage" ->
            BadgeFamily.Invariant

        // Metadata
        "Metadata" ->
            BadgeFamily.Metadata

        else ->
            BadgeFamily.Default
    }

    // ---------- Public API --------------------------------------------------
    fun colors(type: String, dark: Boolean): BadgeColors {
        return when (family(type)) {
            BadgeFamily.Classification  -> if (dark) ClassificationDark else ClassificationLight
            BadgeFamily.Feature     -> if (dark) FeatureDark else FeatureLight
            BadgeFamily.Structure   -> if (dark) StructureDark else StructureLight
            BadgeFamily.Constraint  -> if (dark) ConstraintDark else ConstraintLight
            BadgeFamily.Connector  -> if (dark) ConnectionDark else ConnectionLight
            BadgeFamily.Behavior    -> if (dark) BehaviorDark else BehaviorLight
            BadgeFamily.Invariant -> if (dark) RequirementDark else RequirementLight
            BadgeFamily.Metadata    -> if (dark) MetadataDark else MetadataLight
            BadgeFamily.Default     -> if (dark) DefaultDark else DefaultLight
        }
    }
}