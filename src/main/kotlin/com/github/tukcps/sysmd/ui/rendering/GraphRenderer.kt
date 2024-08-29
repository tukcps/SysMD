package com.github.tukcps.sysmd.ui.rendering

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.DisplayState
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.viewmodel.DisplayTabModel

@Composable
fun renderGraph(displayTabModel: DisplayTabModel, width: Float? = null, height: Float? = null, state: DisplayState) {
    treeRenderView(displayTabModel.treeModel, width = width, height= height, state = state, displayTabModel.isIsA())
}