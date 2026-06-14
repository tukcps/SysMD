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
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import kotlin.uuid.Uuid
import kotlin.uuid.Uuid.Companion.NIL
import kotlin.uuid.toKotlinUuid


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
        get() = element?.generateName() ?: "(no element)"

    override fun children(): List<TreeViewNodeModel> =
        mutableListOf<TreeViewNodeModel>().also { result ->
        SessionManager.sessionService
            .getSubtypes(sessionId, element?.elementId?.toKotlinUuid() ?: NIL)
            ?.forEach { result.add(HasATree(sessionIdState, mutableStateOf(it))) }
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return when (element?.type) {
            "Package" -> Icons.Default.Folder
            "TextualRepresentation" -> Icons.Default.TextFields
            "Feature", "AttributeUsage" -> Icons.AutoMirrored.Filled.FeaturedPlayList
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
                .getOwnedElements(sessionId, element?.elementId?.toKotlinUuid() ?: NIL)
                ?.isNotEmpty()
                ?:false

    override val name: String
        get() = element?.generateName() ?: "(no element)"

    override fun children(): List<TreeViewNodeModel> {
        val result = mutableListOf<TreeViewNodeModel>()
        SessionManager.sessionService
            .getOwnedElements(sessionId, element?.elementId?.toKotlinUuid() ?: NIL)
            ?.forEach { owned ->
                result.add(HasATree(sessionIdState, mutableStateOf(owned)))
            }
        return result
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return when (element?.type) {
            "Package" -> Icons.Default.Folder
            "TextualRepresentation" -> Icons.Default.TextFields
            "Feature", "AttributeUsage" -> Icons.AutoMirrored.Filled.FeaturedPlayList
            else -> Icons.Outlined.AddIcCall
        }
    }
    override fun element() = element
}

/**
 * Function that generates the name for display in a UI.
 */
fun ElementData.generateName(): String = try {
    "[$type] ${declaredName?:declaredShortName?:""}"
} catch (_: Exception) { "(?)"}
