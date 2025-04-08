package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.model.sysml.AcceptActionUsage
import com.github.tukcps.sysmd.model.sysml.SuccessionAsUsage
import com.github.tukcps.sysmd.model.sysml.TransitionUsage
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.MicrochipSolid


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
        // Icons from: https://icons8.com/line-awesome
        return LineAwesomeIcons.MicrochipSolid
    }
    fun getElem(): Element {return element}
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
                val elem = it.ref
                if (elem != null)
                    result.add(HasATree(mutableStateOf(elem)))
            }
            catch (_: Exception) { }
        }
        /*
        if (elem is Relationship) {
            (elem as Relationship).source.forEach { if (it.ref != null) result.add(AgilaCompositionTree(it.ref!!, true)) }
            (elem as Relationship).target.forEach { if (it.ref != null) result.add(AgilaCompositionTree(it.ref!!, true)) }
        } */
        return result
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return when (element) {
            is Variable -> Icons.Default.Functions
            is PackageImplementation -> Icons.Default.Folder
            is TextualRepresentation -> Icons.Default.TextFields
            else -> LineAwesomeIcons.MicrochipSolid
        }
    }
}

/**
 * Function that generates the name for display in a UI.
 */
fun Element.generateName(): String {
    val nameStr = (if (declaredShortName!=null) "<$declaredShortName>" else "") +
                  (if (declaredName!=null) declaredName else "")
    try {
        return when(this) {
            is Anything        -> "Base::Anything"
            is Import          -> "Import '$target'"
            is Multiplicity    -> "$elementType ${variable?.vectorQuantity}"
            is Association     -> "$elementType $nameStr :> ${allSupertypes().first().qualifiedName} $source -> $target"
            is Annotation      -> "$elementType $nameStr: $target"
            is Specialization  -> "$elementType $target"
            is AcceptActionUsage -> "$elementType '${payloadParameter?.type?.get(0)?.ref?.escapedName()}'"
            is TransitionUsage   -> "$elementType '${source.ref?.escapedName()}' -> '${target.ref?.escapedName()}'"
            is SuccessionAsUsage -> "$elementType '${source[0].ref?.escapedName()}' -> '${target.firstOrNull()?.ref?.escapedName()}'"
            is Relationship    -> "$elementType $nameStr $source -> $target"
            is TextualRepresentation -> "TextualRepresentation $language"
            is Feature         -> { "$elementType $nameStr ${if (this.isEnd) "(end)" else ""}" +
                if (variable != null) " = ${variable!!.vectorQuantity}" else ""
            }
            else -> if (owningNamespace == null) "Root" else "$elementType $nameStr ${if (this.isLibraryElement) "(library)" else ""}"
        }
    } catch (issue: Exception) {
        return "( problem with '${escapedName()}': $issue)"
    }
}
