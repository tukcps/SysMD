@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.helper.hoverIndicator
import com.github.tukcps.sysmd.ui.helper.thenIf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 * Renders a list of tabs.
 * @param tabs a list of strings that are the titles of the tabs.
 * @param selectedIndex the index of the selected tab
 * @param onSelection a callback that will be called on selection of a tab
 * @param onClose a callback that will be called on closing a tab
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Tabs(
    tabs: List<String>,
    selectedIndex: MutableState<Int>,
    onSelection: (Int) -> Unit = { selectedIndex.value = it },
    onClose: ((Int) -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val closed: MutableState<Boolean> = mutableStateOf(false)

    // selected index must be accessed to cause re-rendering.
    val dummyRead = selectedIndex.value
    BoxWithConstraints(modifier = Modifier.fitMaxWidth()) {
        val swipeState = rememberSwipeableState(0, confirmStateChange = { selectedIndex.value = it; true })

        LaunchedEffect(selectedIndex.value) {
            swipeState.animateTo(selectedIndex.value)
        }

        Column(modifier = Modifier.fitMaxWidth()) {
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
                                    .clickable { onSelection(i); selectedIndex.value = i}
                                    .onSizeChanged { widthList[i] = it.width.toFloat() }
                                    .hoverIndicator()
                                    .height(24.dp)
                                    .thenIf(selectedIndex.value == i) { background(color = Color(0f, 0f, 0f, 0.1f)) },
                                Alignment.CenterStart
                            ) {
                                Row {
                                    Text(item,
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                                    )
                                    if (onClose != null)
                                        TooltipForIcons(tooltipText = "Close this tab") {
                                            Icon(
                                                Icons.Default.Close,
                                                tint = MaterialTheme.colorScheme.primary,
                                                contentDescription = "Close",
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .padding(2.dp)
                                                    .clickable {
                                                        onClose(i)
                                                        closed.value = !closed.value
                                                    }
                                            )
                                        }
                                }
                            }
                        }
                    }

                    val xAnimator = remember { Animatable(0f) }
                    val widthAnimator = remember(widthList.first()) { Animatable(widthList.first()) }

                    LaunchedEffect(closed) {
                        coroutineScope {
                            launch { xAnimator.animateTo(widthList.take(selectedIndex.value).sum()) }
                            launch { widthAnimator.animateTo(widthList[selectedIndex.value]) }
                        }
                    }

                    Canvas(Modifier.height(24.dp)) {
                        val preSize = widthAnimator.value
                        val x = xAnimator.value
                        drawPath(Path().apply {
                            moveTo(x+3, size.height - 2.dp.roundToPx())
                            lineTo(x+3, size.height)
                            lineTo(x-3 + preSize, size.height)
                            lineTo(x-3 + preSize, size.height - 2.dp.roundToPx())
                            close()
                        },
                            style = Stroke(width = 6f),
                                    color = primaryColor,
                        )
                    }
                }
            }
        }
    }
}