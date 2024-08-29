@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.styles.AppTheme


/**
 * The hierarchical overall TreeView.
 */
@Composable
fun TreeView(model: TreeViewModel) = Surface(modifier = Modifier.fillMaxSize()) {
    with(LocalDensity.current) {
        Box {
            val scrollState = rememberLazyListState()
            val fontSize = 14.sp
            val lineHeight = fontSize.toDp() * 1.5f

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState,
            ) {
                if (model.items.isNotEmpty()){
                    items(model.items.size) {
                        TreeItemView(fontSize, lineHeight, model, it)
                    }
                } else {
                    throw Exception("Database not connected")
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
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TreeItemView(fontSize: TextUnit, height: Dp, model: TreeViewModel, index: Int) = Row(
    modifier = Modifier
        .wrapContentHeight()
        .clickable { model.items[index].open(index) }
        .height(height)
        .fillMaxWidth()
        .background(if (index == model.selectedItem.value) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundLightGray)
) {
    val active = remember { mutableStateOf(false) }
    @Suppress("UNUSED_VARIABLE")
    var isDialogOpen = remember { mutableStateOf(false) }

    TreeItemIcon(
        Modifier.align(Alignment.CenterVertically)
            .padding(start = 24.dp * model.items[index].level),
        model.items[index])

    Text(
        text = model.items[index].name,
        color = if (active.value) LocalContentColor.current.copy(alpha = 0.60f) else LocalContentColor.current,
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .clipToBounds()
            .onPointerEvent(PointerEventType.Move) {}
            .onPointerEvent(PointerEventType.Enter) { active.value = true }
            .onPointerEvent(PointerEventType.Exit) { active.value = false },
        softWrap = true,
        fontSize = fontSize,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}


/**
 * The item in the TreeView
 */
@Composable
internal fun TreeItemIcon(modifier: Modifier, model: TreeViewModel.Item) = Box(modifier.size(24.dp).padding(4.dp)) {
    when (val type = model.type) {
        is TreeViewModel.ItemType.Folder -> when {
            !type.canExpand -> Unit
            type.isExpanded -> Icon(
                Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LocalContentColor.current
            )
            else -> Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = LocalContentColor.current
            )
        }
        is TreeViewModel.ItemType.Item -> when (type.name) {
            "md" -> Icon(Icons.Default.Edit, "Markdown file", tint = AppTheme.colors.iconGreen)
            else -> Icon(Icons.Default.Calculate, contentDescription = null, tint = AppTheme.colors.backgroundDark)
        }
    }
}
