package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AddIcCall
import androidx.compose.material.icons.outlined.Token
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel


/**
 * NOTE: TreeView is a composable that visualizes and edits trees.
 * The composable is in the package "composables".
 * It only needs a tree as a parameter that implements the TreeViewNode interface.
 * The class AgilaInheritanceTree sets up such a tree for a given Agila model.
 */
class IsATree(
    elementState: MutableState<Type>,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {
    var element: Type by elementState
    override val hasChildren: Boolean
        get() = element.subtypes.isNotEmpty()
    override val name: String
        get() = element.path()
    override fun children(): List<TreeViewNodeModel> {
        val elems =  element.subtypes
        val result = mutableListOf<TreeViewNodeModel>()
        elems.forEach {
            try {
                result.add(IsATree(mutableStateOf(it)))
            } catch (_: Exception) {}
        }
        return result
    }
    override fun icon(): ImageVector {
        return Icons.Outlined.Token
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
    elementState: MutableState<Element>,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    var element: Element by elementState

    override val hasChildren: Boolean
        get() = if (ignoreChildren)  false
                else  element.ownedElement.isNotEmpty()

    override val name: String
        get() = element.generateName()

    override fun children(): List<TreeViewNodeModel> {
        val result = mutableListOf<TreeViewNodeModel>()
        element.ownedElement.forEach {
            try {
                result.add(HasATree(mutableStateOf(it)))
            }
            catch (_: Exception) { }
        }
        return result
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return when (element) {
            is Variable -> Icons.Default.Functions
            is PackageImplementation -> Icons.Default.Folder
            is TextualRepresentation -> Icons.Default.TextFields
            else -> Icons.Outlined.AddIcCall
        }
    }
    override fun element() = element
}

/**
 * Function that generates the name for display in a UI.
 */
fun Element.generateName(): String = try {
    toString() } catch (_: Exception) { "(?)"}
