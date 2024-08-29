package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


val showCommitTree = mutableStateOf(false)

@ExperimentalAnimationApi
@Composable
fun Node(
    modifier: Modifier = Modifier,
    nodeModel: NodeModel
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isChildrenShown = remember { mutableStateOf(true) }

        NodeBox(
            modifier = modifier.clickable(onClick = {
                isChildrenShown.value = !isChildrenShown.value
            }),
            isExpanded = isChildrenShown.value
        )

        Spacer(modifier = Modifier.size(8.dp))

        AnimatedVisibility(visible = isChildrenShown.value) {
            Column (
                modifier = modifier,
                verticalArrangement = Arrangement.Center
            ) {
                nodeModel.children.forEachIndexed { index, model ->
                    Node(nodeModel = model)
                    if (index != nodeModel.children.size - 1) {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NodeBox(modifier: Modifier = Modifier, isExpanded: Boolean) {
    Canvas(modifier = Modifier.size(8.dp)){
        drawCircle(
            color = Color.Green,
            radius = 4.dp.toPx()
        )
    }
}


data class NodeModel(val children: List<NodeModel> = emptyList())

private fun rootModel(): NodeModel {
    return NodeModel(
        listOf(NodeModel(listOf(NodeModel())), NodeModel(listOf(NodeModel())), NodeModel(listOf(NodeModel())), NodeModel(listOf(NodeModel())))
    )
}