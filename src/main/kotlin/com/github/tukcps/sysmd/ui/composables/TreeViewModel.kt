package com.github.tukcps.sysmd.ui.composables

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector


/**
 * Interface class that must be implemented by the class that is displayed.
 * The view model TreeViewModel uses it to set up a displayable tree with additional
 * states that model the state of the displayed tree.
 * @property hasChildren true if node has children
 * @property ignoreChildren if set, no children of node will be displayed
 * @property name Name of the node that will be displayed
 * @property children Method that returns a list of the same type's children (TreeViewNodeModel)
 */
interface TreeViewNodeModel {
    val hasChildren: Boolean
    val ignoreChildren: Boolean
    val name: String
    fun children(): List<TreeViewNodeModel>
    fun icon(): ImageVector
}


/**
 * Filesystem tree from which the function open describes the action on selection of a file.
 * @param root specifies the start of the file tree. It must implement the interface
 * TreeViewNodeModel.
 * @param onOpen the action for selection of a file (via click on a file)
 * @param onCreate the action for the creation of a file (via + icon right of folder)
 */
class TreeViewModel(
    var root: TreeViewNodeModel,
    val onOpen: ((TreeViewNodeModel) -> Unit)? = null,
    val onCreate: ((TreeViewNodeModel) -> Unit)? = null,
    val onDisplay: ((TreeViewNodeModel) -> Unit)? = null,
    val sort: Boolean = true,
    val filter: (Item) -> Boolean = { true }
) {
    private var expandableRoot = ExpandableItem(root, 0, sort).apply { toggleExpanded() }

    // The list of items that are currently displayed; from expandableRoot
    val items: List<Item> get() = expandableRoot.toItems().filter { filter(it) }

    // The last selected item that will be highlighted.
    var selectedItem = mutableStateOf(-1)

    inner class Item(val item: ExpandableItem) {
        val name: String get() = item.node.name
        val level: Int get() = item.level
        val node: ExpandableItem = item

        val type: ItemType
            get() = if (item.node.hasChildren) {
                ItemType.Folder(isExpanded = item.children.isNotEmpty(), canExpand = item.canExpand)
            } else {
                ItemType.Item(name = item.node.name)
            }

        fun open(index: Int) = when (type) {
            is ItemType.Folder -> {
                item.toggleExpanded()
                selectedItem.value = index
            }
            is ItemType.Item -> {
                onOpen?.let { it(item.node) }
                selectedItem.value = index
            }
        }
        fun select(index: Int){
            selectedItem.value = index
        }
        fun display(index: Int){
            onDisplay?.let { it(item.node) }
            selectedItem.value = index
        }
    }

    sealed class ItemType {
        class Folder(val isExpanded: Boolean, val canExpand: Boolean) : ItemType()
        class Item(val name: String) : ItemType()
    }

    private fun ExpandableItem.toItems(): List<Item> {
        fun ExpandableItem.addTo(list: MutableList<Item>) {
            list.add(Item(this))
            for (child in children) {
                child.addTo(list)
            }
        }

        val list = mutableListOf<Item>()
        addTo(list)
        return list
    }
}


/**
 * An expandable item that can have children.
 * The state is either just itself (no children), or the children displayed.
 * The latter can be toggled.
 * @param node view-model of the node
 * @param level depth of the node in the tree (counting from root)
 * @param sort whether children shall be sorted or not
 */
class ExpandableItem(
    val node: TreeViewNodeModel,
    val level: Int,
    val sort: Boolean
) {
    var children: List<ExpandableItem> by mutableStateOf(emptyList())
    val canExpand: Boolean get() = node.hasChildren

    fun toggleExpanded() {
        children = if (children.isEmpty()) {
            if (sort)
                node.children().map { ExpandableItem(it, level + 1, sort) }
                    .sortedWith(compareBy({ it.node.hasChildren }, { it.node.name }))
                    .sortedBy { !it.node.hasChildren }
            else
                node.children().map { ExpandableItem(it, level + 1, sort) }

        } else {
            emptyList()
        }
    }
}
