package com.github.tukcps.sysmd.ui.composables

import androidx.compose.ui.graphics.Color
import com.github.tukcps.sysmd.model.datamodel.isSubclassOf

/**
 * We introduce colors based on the role of classes in the Metamodel.
 * Roles are:
 */
private enum class BadgeFamily {
    Package,
    AnnotatingElement,
    StructureDef,
    StructureUsage,
    BehaviorDef,
    BehaviorUsage,
    ComputeDef,
    ComputeUsage,
    Default
}

/**
 *
 */
object TypeBadgeColors {

    // ---------- Light -------------------------------------------------------
    // Package, Namespace
    private val PackageLight            = BadgeColors(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    private val AnnotatingElementLight  = BadgeColors(Color(0xFFF9FBE7), Color(0xFF558B2F))
    private val StructureUsageLight     = BadgeColors(Color(0xFFE3F2FD), Color(0xFF1565C0))
    private val StructureDefLight       = BadgeColors(Color(0xFFE8EAF6), Color(0xFF283593))
    private val ComputeUsageLight       = BadgeColors(Color(0xFFFCE4EC), Color(0xFF880E4F))
    private val ComputeDefLight         = BadgeColors(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
    private val BehaviorUsageLight      = BadgeColors(Color(0xFFFFF3E0), Color(0xFFE65100))
    private val BehaviorDefLight        = BadgeColors(Color(0xFFFFEBEE), Color(0xFFC62828))
    private val DefaultLight            = BadgeColors(Color(0xFFF5F5F5), Color(0xFF424242))

    // ---------- Dark --------------------------------------------------------
    private val PackageDark             = BadgeColors(Color(0xFF29392C), Color(0xFFA7E2AE))
    private val AnnotatingElementDark   = BadgeColors(Color(0xFF3A4228), Color(0xFFD8EEA6))
    private val StructureUsageDark      = BadgeColors(Color(0xFF2A3150), Color(0xFFBCCAFF))
    private val StructureDefDark        = BadgeColors(Color(0xFF26394A), Color(0xFF9FD4FF))
    private val ComputeUsageDark        = BadgeColors(Color(0xFF432732), Color(0xFFFFA8D0))
    private val ComputeDefDark          = BadgeColors(Color(0xFF372840), Color(0xFFD7B4FF))
    private val BehaviorUsageDark       = BadgeColors(Color(0xFF4A2B2B), Color(0xFFFFB3B3))
    private val BehaviorDefDark         = BadgeColors(Color(0xFF503824), Color(0xFFFFD18A))
    private val DefaultDark             = BadgeColors(Color(0xFF3A3C3F), Color(0xFFE4E4E4))

    // ---------- Classification ----------------------------------------------
    private fun family(type: String): BadgeFamily = when {

        // Pure hierarchical structure
        type == "Package" || type == "Namespace"
            -> BadgeFamily.Package
        isSubclassOf(type, "AnnotatingElement")
            -> BadgeFamily.AnnotatingElement

        isSubclassOf(type, "Behavior") || isSubclassOf(type, "Interaction")
            -> BadgeFamily.BehaviorDef
        isSubclassOf(type, "Step") || isSubclassOf(type, "Succession") || isSubclassOf(type, "Flow")
            -> BadgeFamily.BehaviorUsage

        isSubclassOf(type, "Multiplicity") || isSubclassOf(type, "Expression") || isSubclassOf(type, "AttributeUsage")
                || type == "Feature"
                    -> BadgeFamily.ComputeUsage
        isSubclassOf(type, "Function") || isSubclassOf(type, "AttributeDefinition")
            -> BadgeFamily.ComputeDef

        isSubclassOf(type, "Feature")
            -> BadgeFamily.StructureUsage
        isSubclassOf(type, "Type")
            -> BadgeFamily.StructureDef

        else ->
            BadgeFamily.Default
    }

    // ---------- Public API --------------------------------------------------
    fun colors(type: String, dark: Boolean): BadgeColors {
        return when (family(type)) {
            BadgeFamily.Package
                -> if (dark) PackageDark else PackageLight
            BadgeFamily.AnnotatingElement
                -> if (dark) AnnotatingElementDark else AnnotatingElementLight
            BadgeFamily.BehaviorDef
                -> if (dark) BehaviorDefDark else BehaviorDefLight
            BadgeFamily.BehaviorUsage
                -> if (dark) BehaviorUsageDark else BehaviorUsageLight
            BadgeFamily.ComputeUsage
                -> if (dark) ComputeUsageDark else ComputeUsageLight
            BadgeFamily.ComputeDef
                -> if (dark) ComputeDefDark else ComputeDefLight
            BadgeFamily.StructureUsage
                -> if (dark) StructureUsageDark else StructureUsageLight
            BadgeFamily.StructureDef
                -> if (dark) StructureDefDark else StructureDefLight
            BadgeFamily.Default
                -> if (dark) DefaultDark else DefaultLight
        }
    }
}