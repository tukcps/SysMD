package com.github.tukcps.sysmd.ui.rendering
import kotlin.math.max

data class TreeNodeViewModel(
    val id: Int,
    val name: String,
    val attributes: List<String>,
    val children: List<TreeNodeViewModel> = emptyList(),
) {
    var maxNumOfChars = max(name.length, attributes.maxByOrNull { it.length }?.length ?: 0)
    var width: Int = 9 * maxNumOfChars + 20
    var height: Float = 17f * attributes.size + 19f
    var childsMaxWidth: Int = 0
    var minXPos = 0
    var largerThanChilds = false
    var x:Int = 0 //top left corner
    var y:Float = 0f //top left corner
}