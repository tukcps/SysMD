@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.rest.AgilaRepository
import com.github.tukcps.sysmd.ui.rendering.convertSelectedToTreeNodeModel
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.*

data class MenuState(
    val menuClicked: MutableState<Boolean>,
    val renderClicked: MutableState<Boolean>,
    val createFileClicked:MutableState<Boolean>,
    val deleteFileClicked:MutableState<Boolean>,
    val createBranchClicked:MutableState<Boolean>,
    val deleteProjectClicked:MutableState<Boolean>,
    val systemCexportClicked: MutableState<Boolean>,
    val connectToBackendClicked:MutableState<Boolean>,
    val importResultsClicked: MutableState<Boolean>,
    )

val menuState = MenuState(
    menuClicked = mutableStateOf(false),
    renderClicked = mutableStateOf(false),
    createFileClicked = mutableStateOf(false),
    deleteFileClicked = mutableStateOf(false),
    createBranchClicked = mutableStateOf(false),
    deleteProjectClicked = mutableStateOf(false),
    systemCexportClicked = mutableStateOf(false),
    connectToBackendClicked = mutableStateOf(false),
    importResultsClicked = mutableStateOf(false),
)


lateinit var modelToUpdate:MutableState<TreeViewModel>
var elementToSystemC: MutableState<Element>? = null
var selectedElement: MutableState<Element>? = null

/**
 * The hierarchical overall TreeView.
 */
@Composable
fun TreeViewPlus(
    model: MutableState<TreeViewModel>,
    tabsModel : TabsModel,
    filter: (TreeViewModel.Item) -> Boolean = { true }
) = Box(modifier = Modifier.fillMaxSize()) {
        with(LocalDensity.current) {
            Column {
                val scrollState = rememberLazyListState()
                val fontSize = AppTheme.fontSize
                val lineHeight = fontSize.toDp() * 1.5f

                Row {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = scrollState,
                    ) {
                        items(model.value.items.size) {
                            if (filter(model.value.items[it] ))
                                TreeItemView(fontSize, lineHeight, model.value, it, tabsModel)
                        }
                    }
                }
            }
        }
    }

/**
 * A single line in the tree view.
 * It is clickable, and if selected opens a new editor tab.
 * The last icon "+" allows to create a new file.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class) //TODO rewrite pointerMoveFilter to be stable
@Composable
private fun TreeItemView(
    fontSize: TextUnit,
    height: Dp,
    model: TreeViewModel,
    index: Int,
    tabsModel: TabsModel?
) {
    modelToUpdate= mutableStateOf(model)
    Modifier.wrapContentHeight()
    Row(
        modifier = Modifier
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary)) {
                model.items[index].open(index)
            }
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary)) {
                menuState.menuClicked.value = true
                model.items[index].select(index)
            }
            .height(height)
            .fillMaxWidth()
            .background(if (index == model.selectedItem.value) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
    ) {
        val active = remember { mutableStateOf(false) }
        val isDialogOpen = remember { mutableStateOf(false) }
        if(menuState.menuClicked.value){
            if (tabsModel != null) {
                Contextmenu(model, tabsModel)
            }
        }

        // Icon left of the line.
        try { model.items[index] }
        catch (e: java.lang.IndexOutOfBoundsException) { null }?.let {
            TreeItemIcon(
                Modifier.align(Alignment.CenterVertically).padding(
                    start = 24.dp * try { model.items[index].level } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                        1
                    }
                ),
                it,
            )
        }

        // Text right of icon.
        Text(
            text = try {
                model.items[index].name
            } catch (e: java.lang.IndexOutOfBoundsException) {
                "NOT LOADED"
            },
            color = if (active.value) LocalContentColor.current.copy(alpha = 0.60f) else LocalContentColor.current,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clipToBounds()
                .onPointerEvent(PointerEventType.Move) {}
                .onPointerEvent(PointerEventType.Enter) {
                    active.value = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    active.value = false
                },
            softWrap = true,
            fontSize = fontSize,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        // DEPRECATED:
        // Icon "+" to create a new file. Arranged to the right end.
        if ( (model.items.getOrNull(index)?.type is TreeViewModel.ItemType.Folder)
                && (model.selectedItem.value == index) && (model.root is AgilaFileTree || model.root is AgilaProjectsTree)) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                TooltipForIcons(tooltipText = "Create a new file in ${model.items[index].name}/...") {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        tint = LocalContentColor.current,
                        modifier = Modifier.padding(5.dp).clickable {
                            //TODO(Implement PostProject : Get name on the Window POPUP -> see create new File)
                            model.onCreate(model.items[index].item.node)
                            isDialogOpen.value = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Contextmenu(model: TreeViewModel, tabsModel: TabsModel) {
    CursorDropdownMenu(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)),
        expanded = true,
        onDismissRequest = { menuState.menuClicked.value = false }
    ) {
        when (model.root) {
            is AgilaProjectsTree -> {
                DropdownMenuItem(
                    text = { Text("Create new Project") },
                    onClick = {
                        menuState.menuClicked.value = false
                        model.onCreate(model.root)
                })

                DropdownMenuItem(
                    text =  { Text("Create Branch") },
                    onClick = {
                    menuState.menuClicked.value = false
                    menuState.createBranchClicked.value=true
                })
                DropdownMenuItem(
                    text = { Text("Delete Project") },
                    onClick = {
                    menuState.menuClicked.value = false
                    model.onDelete(
                        model.root,
                        (modelToUpdate.value.root as AgilaProjectsTree).projects[model.selectedItem.value - 1].id
                            .toString()
                    )
                })
            }


            is AgilaFileTree -> {
                DropdownMenuItem(
                    { Text("Create new File") },
                    { menuState.menuClicked.value = false; model.onCreate(model.root) }
                )
                DropdownMenuItem(
                    { Text("Delete File") },
                    { menuState.menuClicked.value = false; model.onDelete(model.root, model.items[model.selectedItem.value].name) }
                )
                DropdownMenuItem(
                    { Text("Import Results") },
                    { menuState.importResultsClicked.value = true; menuState.menuClicked.value = false })
                if(AgilaRepository.onlineState.value){
                    DropdownMenuItem(
                        { Text("Upload File to Backend") },
                        { menuState.menuClicked.value = false; model.onUpload(model.root,model.items[model.selectedItem.value].name) }
                    )
                }
            }

            is AgilaCompositionTree -> {
                selectedElement =
                    mutableStateOf((model.items[model.selectedItem.value].item.node as AgilaCompositionTree).elem)
                if (selectedElement?.value is StateUsage) {
                    DropdownMenuItem(
                        { Text("Render diagram") },
                        {
                            menuState.renderClicked.value = true

                        }
                    )
                }

                DropdownMenuItem(
                    { Text("Export SystemC") },
                    { menuState.systemCexportClicked.value = true
                        elementToSystemC = mutableStateOf((model.items[model.selectedItem.value].item.node as AgilaCompositionTree).elem) }
                )
            }

            is AgilaInheritanceTree -> {
                DropdownMenuItem(
                    { Text("Render diagram") },
                    { menuState.renderClicked.value = true })
            }
        }
    }

    //Closing Menu
    if(menuState.renderClicked.value || menuState.createFileClicked.value || menuState.deleteFileClicked.value)
        menuState.menuClicked.value = false
}

@Composable
// Former implementation for rendering diagrams, currently not in use
fun renderNonStateDiagram(model: TreeViewModel, tabsModel: TabsModel) {
    val selectedElementValue = selectedElement?.value
    if(selectedElementValue !is StateUsage){
        val (model1,isA) = convertSelectedToTreeNodeModel(model)
        val displayTabModel = DisplayTabModel(tabsModel.kerMlModelState, model1, isA)
        tabsModel.addDisplayTabModel(displayTabModel)
        menuState.renderClicked.value = false
    }
}





