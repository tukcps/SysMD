@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.helper.hoverIndicator
import com.github.tukcps.sysmd.ui.helper.thenIf
import com.github.tukcps.sysmd.ui.helper.toPx
import com.github.tukcps.sysmd.ui.viewmodel.storeSettings
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 * For the composable for the settings; renders the settings tabs on top of the window.
 */
@ExperimentalMaterialApi
@Composable
fun SettingsTabMenu(
    tabs: List<SettingsTabItem>,
    selectedIndex: MutableState<Int>,
    fillFullSpace: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    onSelectionChange: (Int) -> Unit = { selectedIndex.value = it }
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    BoxWithConstraints(
        modifier = Modifier.fitMaxWidth()
    ) {
        val constraints = this
        val width = constraints.maxWidth.toPx()
        Column(modifier = Modifier.fitMaxWidth()) {
            Row(
                Modifier.fitMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    val widthList: SnapshotStateList<Float> =
                        remember { mutableStateListOf(*(Array(tabs.size) { 0f })) }
                    val canExpand = fillFullSpace && widthList.sum() <= width
                    Row(
                        Modifier.thenIf(canExpand) { width(constraints.maxWidth) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { i, item ->
                            Box(Modifier
                                .clickable {
                                    storeSettings()
                                    onSelectionChange(i)
                                }
                                .onSizeChanged { widthList[i] = it.width.toFloat() }
                                .run { if (canExpand) weight(1f) else this }
                                .hoverIndicator()
                                .height(30.dp)
                                .thenIf(selectedIndex.value == i) { background(color = Color(0f, 0f, 0f, 0.1f)) },
                                Alignment.Center
                            ) {
                                Text(item.title, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }

                    val xAnimator = remember { Animatable(0f) }
                    val widthAnimator = remember(widthList.first()) { Animatable(widthList.first()) }
                    LaunchedEffect(selectedIndex.value) {
                        coroutineScope {
                            launch { xAnimator.animateTo(widthList.take(selectedIndex.value).sum()) }
                            launch { widthAnimator.animateTo(widthList[selectedIndex.value]) }
                        }
                    }

                    Canvas(Modifier.height(30.dp)) {
                        val preSize = widthAnimator.value
                        val x = xAnimator.value
                        drawPath(Path().apply {
                            moveTo(x, size.height - 2.dp.roundToPx())
                            lineTo(x, size.height)
                            lineTo(x + preSize, size.height)
                            lineTo(x + preSize, size.height - 2.dp.roundToPx())
                            close()
                        }, color = primaryColor)
                    }
                }
            }
        }
    }
}