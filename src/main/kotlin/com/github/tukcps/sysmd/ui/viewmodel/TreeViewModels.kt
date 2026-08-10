package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AddIcCall
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import kotlin.uuid.Uuid
import kotlin.uuid.Uuid.Companion.NIL


/**
 * NOTE: TreeView is a composable that visualizes and edits trees.
 * The composable is in the package "composables".
 * It only needs a tree as a parameter that implements the TreeViewNode interface.
 * The class AgilaInheritanceTree sets up such a tree for a given Agila model.
 */
class IsATree(
    val sessionIdState: MutableState<Uuid>,
    elementState: MutableState<ElementData?>,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {
    var element: ElementData? by elementState
    var sessionId: Uuid by sessionIdState

    override val hasChildren: Boolean
        get() = children().isNotEmpty()

    override val name: String
        get() = element?.generateName(sessionId) ?: "(no element)"

    override fun children(): List<TreeViewNodeModel> =
        mutableListOf<TreeViewNodeModel>().also { result ->
        SessionManager.sessionService
            .getSubtypes(sessionId, element?.elementId ?: NIL)
            ?.forEach { result.add(HasATree(sessionIdState, mutableStateOf(it))) }
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return when (element?.type) {
            ElementType.Package
                -> Icons.Default.Folder
            ElementType.TextualRepresentation
                -> Icons.Default.TextFields
            ElementType.Feature, ElementType.AttributeUsage
                -> Icons.AutoMirrored.Filled.FeaturedPlayList
            else -> Icons.Outlined.AddIcCall
        }
    }
    override fun element() = element
}


/**
 * NOTE: TreeView is a generic composable that visualizes and edits trees.
 * The composable is in the package composables.
 * It only needs a tree as a parameter that implements the TreeViewNode interface.
 * The class AgilaCompositionTree sets up such a tree for a given model.
 */
class HasATree(
    val sessionIdState: MutableState<Uuid>,
    elementState: MutableState<ElementData?>,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    var element: ElementData? by elementState
    var sessionId: Uuid by sessionIdState

    override val hasChildren: Boolean
        get() = if (ignoreChildren)  false
        else
            SessionManager.sessionService
                .getOwnedElements(sessionId, element?.elementId ?: NIL)
                ?.isNotEmpty()
                ?:false

    override val name: String
        get() = element?.generateName(sessionId) ?: "(no element)"

    override fun children(): List<TreeViewNodeModel> {
        val result = mutableListOf<TreeViewNodeModel>()
        SessionManager.sessionService
            .getOwnedElements(sessionId, element?.elementId ?: NIL)
            ?.forEach { owned ->
                result.add(HasATree(sessionIdState, mutableStateOf(owned)))
            }
        return result
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return when (element?.type) {
            ElementType.Package -> Icons.Default.Folder
            ElementType.TextualRepresentation -> Icons.Default.TextFields
            ElementType.Feature,
            ElementType.AttributeUsage -> Icons.AutoMirrored.Filled.FeaturedPlayList
            else -> Icons.Outlined.AddIcCall
        }
    }
    override fun element() = element
}

/**
 * Function that generates the name for display in a UI.
 */
fun ElementData.generateName(sessionId: Uuid? = null): String = try {
    var displayName = "[${type.name}] ${declaredName?:declaredShortName?:""}"
    if (sessionId != null) {
        val session = SessionManager.getSession(sessionId)
        val kermlElement = session?.get(elementId)
        if (kermlElement is Feature) {
            val variable = kermlElement.variable
            if (variable != null) {
                displayName += if (kermlElement is Multiplicity) {
                    " ${variable.vectorQuantity}"
                } else {
                    " = ${variable.vectorQuantity}"
                }
            }
        }
        if (kermlElement is Relationship) {
            val targetName = kermlElement.target.firstOrNull()?.let {
                it.qualifiedName ?: it.escapedName() ?: "[${it.elementType()}]"
            }
            if (targetName != null) {
                displayName += " $targetName"
            }
        }
    }
    displayName
} catch (_: Exception) { "(?)"}
