@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.helper.hoverIndicator
import com.github.tukcps.sysmd.ui.helper.thenIf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


fun isValidFilename(filename: String): Boolean {
    val invalidChars = Regex("[\\\\/:*?\"<>|]")
    return filename.isNotEmpty() && !invalidChars.containsMatchIn(filename)
}

/**
 * General-purpose composable that renders a list of tabs.
 * Optionally, tabs can be added, renamed or closed.
 * @param tabs a list of strings that are the titles of the tabs.
 * @param selectedIndex the index of the selected tab
 * @param onSelection a callback that will be called on selection of a tab
 * @param onHide a callback that will be called on closing a tab
 * @param onShow a callback that will be called on adding a tab
 */
@Composable
fun Tabs(
    tabs: List<MutableState<String>>,
    selectedIndex: MutableState<Int>,
    onSelection: (Int) -> Unit = { selectedIndex.value = it; },
    onHide: ((Int) -> Unit)? = null,
    onShow: (() -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    /** Triggers the animation on closing a tab */
    val closed: MutableState<Boolean> = mutableStateOf(false)
    val editNameIndex = remember { mutableStateOf<Int?>(null) }

    // selected index must be accessed to cause re-rendering.
    @Suppress("unused", "UnusedVariable") val renderRead = selectedIndex.value
    BoxWithConstraints(modifier = Modifier.fitMaxWidth()) {
        // The index of a tab whose name can be edited
        Column {
            Row(
                Modifier
                    .fitMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    val widthList: SnapshotStateList<Float> = mutableStateListOf(*(Array(tabs.size) { 0f }))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        tabs.forEachIndexed { i, item ->
                            Box(
                                Modifier
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                if (event.type == PointerEventType.Press) {
                                                    if (event.buttons.isPrimaryPressed) {
                                                        if (i != selectedIndex.value) editNameIndex.value = null; onSelection(i); selectedIndex.value = i;  }
                                                    if (event.buttons.isSecondaryPressed) { editNameIndex.value = i; selectedIndex.value = i }
                                                }
                                            }
                                        }
                                    }
                                    .onSizeChanged { widthList[i] = it.width.toFloat() }
                                    .hoverIndicator()
                                    .height(26.dp)
                                    .thenIf(selectedIndex.value == i) { background(color = Color(0f, 0f, 0f, 0.1f)) },
                                Alignment.CenterStart
                            ) {
                                Row {
                                    if (editNameIndex.value != i)
                                        BasicTextField(
                                            modifier = Modifier.padding(start = 5.dp, end = 3.dp, top = 1.dp, bottom = 7.dp)
                                                .width(IntrinsicSize.Min)
                                                .height(IntrinsicSize.Min)
                                            ,
                                            // color = MaterialTheme.colorScheme.onBackground,
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                            ),
                                            value = item.value,
                                            onValueChange = {  },
                                            readOnly = true,
                                            singleLine = true
                                        )
                                    else
                                        BasicTextField(
                                            modifier = Modifier.padding(start = 3.dp, end = 3.dp, top = 1.dp, bottom = 7.dp)
                                                .width(IntrinsicSize.Min)
                                                .height(IntrinsicSize.Min)
                                                .background(color = if(isValidFilename(item.value)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
                                                .onKeyEvent { keyEvent ->
                                                    val buffer = selectedIndex.value // Enforce redrawing of the whole tabs list
                                                    selectedIndex.value = 0
                                                    selectedIndex.value = buffer
                                                    true
                                                }
                                            ,
                                            // color = MaterialTheme.colorScheme.onBackground,
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                            ),
                                            value = item.value, onValueChange = { item.value = it }, readOnly = false, singleLine = true
                                        )
                                    if (onHide != null)
                                        SysMDTooltipArea(tooltipText = "Close this tab") {
                                            Icon(
                                                Icons.Default.Close,
                                                tint = MaterialTheme.colorScheme.primary,
                                                contentDescription = "Close",
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .padding(2.dp)
                                                    .clickable {
                                                        onHide(i)
                                                        closed.value = !closed.value
                                                    }
                                            )
                                        }
                                }
                            }
                        }
                        if (onShow != null) {
                            Spacer(
                                modifier = Modifier
                                    .padding(horizontal = 5.dp, vertical = 3.dp)
                                    .width(1.dp)
                                    .height(18.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            SysMDTooltipArea(tooltipText = "Add Tab") {
                                Icon(
                                    Icons.Default.Add,
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = "Add",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(1.dp)
                                        .clickable {
                                            onShow()
                                            editNameIndex.value = tabs.size
                                        }
                                )
                            }
                        }
                    }

                    if (widthList.isNotEmpty()) {
                        val xAnimator = remember { Animatable(0f) }
                        val widthAnimator = remember(widthList.first()) { Animatable(widthList.first()) }

                        LaunchedEffect(closed) {
                            coroutineScope {
                                launch { xAnimator.animateTo(widthList.take(selectedIndex.value).sum()) }
                                launch { widthAnimator.animateTo(widthList[selectedIndex.value]) }
                            }
                        }

                        // The bar below the selected tab ...
                        Canvas(Modifier.height(24.dp)) {
                            val preSize = widthAnimator.value
                            val x = xAnimator.value
                            drawPath(
                                Path().apply {
                                    moveTo(x + 2, size.height - 1.dp.roundToPx())
                                    lineTo(x + 2, size.height)
                                    lineTo(x - 2 + preSize, size.height)
                                    lineTo(x - 2 + preSize, size.height - 1.dp.roundToPx())
                                    close()
                                },
                                style = Stroke(width = 3f), color = primaryColor,
                            )
                        }
                    }
                }
            }
        }
    }
}