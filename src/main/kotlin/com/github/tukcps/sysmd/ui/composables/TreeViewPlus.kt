@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
                                TreeItemView(fontSize, lineHeight, model.value, it)
                        }
                    }
                }
            }
        }
    }

/**
 * A single line in the tree view.
 * It is clickable, and if selected opens a new editor tab.
 * The last icon "+" allows creating a new file.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class) //TODO rewrite pointerMoveFilter to be stable
@Composable
private fun TreeItemView(
    fontSize: TextUnit,
    height: Dp,
    model: TreeViewModel,
    index: Int,
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
        if(menuState.menuClicked.value){
            Contextmenu(model)
        }

        // Icon left of the line.
        try { model.items[index] }
        catch (_: java.lang.IndexOutOfBoundsException) { null }?.let {
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
            } catch (_: java.lang.IndexOutOfBoundsException) {
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
    }
}

@Composable
fun Contextmenu(model: TreeViewModel) {
    CursorDropdownMenu(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)),
        expanded = true,
        onDismissRequest = { menuState.menuClicked.value = false }
    ) {
        when (model.root) {

            is HasATree -> {
                selectedElement =
                    mutableStateOf((model.items[model.selectedItem.value].item.node as HasATree).element)
                if (selectedElement?.value is StateUsage) {
                    DropdownMenuItem(
                        { Text("Render diagram") },
                        { menuState.renderClicked.value = true }
                    )
                }

                DropdownMenuItem(
                    { Text("Export SystemC") },
                    { menuState.systemCexportClicked.value = true
                        elementToSystemC = mutableStateOf((model.items[model.selectedItem.value].item.node as HasATree).element) }
                )
            }

            is IsATree -> {
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
