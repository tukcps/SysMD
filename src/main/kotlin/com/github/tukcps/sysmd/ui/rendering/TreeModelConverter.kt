package com.github.tukcps.sysmd.ui.rendering

import com.github.tukcps.sysmd.ui.viewmodel.AgilaInheritanceTree
import com.github.tukcps.sysmd.ui.composables.TreeViewModel
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import com.github.tukcps.sysmd.ui.viewmodel.AgilaCompositionTree
import java.util.*

var id:Int = 0

// Returns com.github.tukcps.appel.ui.rendering.TreeNodeModel of in gui selected item
fun convertSelectedToTreeNodeModel(treeViewModel : TreeViewModel) :Pair<TreeNodeViewModel, Boolean> {
    val selected = treeViewModel.items[treeViewModel.selectedItem.value]
    val id = id++
    val name = selected.name
    val children: List<TreeNodeViewModel> = convertToTreeNodeModels(selected.item.node.children())
    val elemId = treeViewModel.root.getId()
    val attributes = if(elemId!=null){ getAttributes(treeViewModel.root.search(elemId))} else { listOf()}
    return TreeNodeViewModel(id, name, attributes, children) to (treeViewModel.root !is AgilaCompositionTree)
}


fun convertToTreeNodeModels(treeViewNodeModels : List<TreeViewNodeModel>) : List<TreeNodeViewModel> {
    val children: MutableList<TreeNodeViewModel> = mutableListOf()
    for (treeViewNodeModel: TreeViewNodeModel in treeViewNodeModels){
        if(treeViewNodeModel.ignoreChildren) continue
        children.add(convertToTreeNodeModel(treeViewNodeModel))
    }
    return children
}

/**
 * function to convert the TreeViewNodeModel into a com.github.tukcps.appel.ui.rendering.TreeNodeModel
 */
fun convertToTreeNodeModel(treeViewNodeModel: TreeViewNodeModel) : TreeNodeViewModel {
    val id = id++
    val name = treeViewNodeModel.name
    val elemId = treeViewNodeModel.getId()
    val children = convertToTreeNodeModels(treeViewNodeModel.children())
    val attributes = if(elemId!=null){ getAttributes(treeViewNodeModel.search(elemId))} else { listOf()}
    return TreeNodeViewModel(id, name, attributes, children)
}

/**
 * extension function on TreeViewNodeModel to search for child nodes based on their name,
 * actually used on AgileCompositionTree
 */
fun TreeViewNodeModel.search(query: UUID):TreeViewNodeModel?{
    when (this) {
        is AgilaCompositionTree -> {
            if (this.elem.elementId == query) {
                return this
            } else {
                for (child: TreeViewNodeModel in this.children()) {
                    if (this.ignoreChildren) continue
                    val elem = child.search(query)
                    if (elem != null) {
                        return elem
                    }
                }
                return null
            }
        }
        is AgilaInheritanceTree -> {
            if (this.elem.elementId == query) {
                return this
            } else {
                for (child: TreeViewNodeModel in this.children()) {
                    if (this.ignoreChildren) continue
                    val elem = child.search(query)
                    if (elem != null) {
                        return elem
                    }
                }
                return null
            }
        }
        else -> return null
    }
}


fun TreeViewNodeModel.getId(): UUID? =
    when(this) {
        is AgilaCompositionTree -> this.elem.elementId
        is AgilaInheritanceTree -> this.getElem().elementId
        else -> null
    }

/**
 * function to get the attributes out of a node and return it as a List of Strings
 */
fun getAttributes(model : TreeViewNodeModel?):List<String>{
    val attributes : MutableList<String> = arrayListOf()
    if (model != null) {
        for(m in model.children()){
            if(m.name.isNotEmpty()&&m.name.isNotBlank()) attributes.add(m.name)
        }
    }
    return attributes
}