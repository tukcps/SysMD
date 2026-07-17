package com.github.tukcps.sysmd.ui.rendering

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.DisplayState
import com.github.tukcps.sysmd.ui.styles.Fonts
import kotlin.math.max
import kotlin.math.roundToInt

const val borderStrokeWidth = 2f
const val boxPadding = 5
const val charHeight = 14F

private const val xPadding: Int = 10 //xPadding between nodes
private const val yPadding: Int = 60 //yPadding between nodes
private const val relationWidth: Int = 7 //e.g. half width of composition symbol
private val lineStrokeWidth = 1.dp
private val diamondShape: GenericShape = createDiamondShape()
private val triangleShape: GenericShape = createTriangleShape()

var dpstate: DisplayState? = null

//returns a diamond path used for composition symbol etc.
private fun createDiamondShape(): GenericShape {
    return GenericShape {_, _ ->
        moveTo(relationWidth.toFloat(), 0f)
        lineTo((2*relationWidth).toFloat(), relationWidth.toFloat())
        lineTo(relationWidth.toFloat(), (2*relationWidth).toFloat())
        lineTo(0f, relationWidth.toFloat())
    }
}

private fun createTriangleShape(): GenericShape {
    return GenericShape {_, _ ->
        moveTo(relationWidth.toFloat(), 0f)
        lineTo((2*relationWidth).toFloat(), relationWidth.toFloat())
        lineTo(0f, relationWidth.toFloat())
    }
}

@Composable
private fun drawCompositionSymbol(x: Int, y: Int) {
    Box(modifier = Modifier.size((2*relationWidth).dp, (2*relationWidth).dp)
        .offset(x.dp, y.dp)
        .clip(diamondShape)
        .background(Color.Black)
        .border(lineStrokeWidth, Color.Black, diamondShape))
}

@Composable
private fun drawAggregationSymbol(x: Int, y: Int) {
    Box(modifier = Modifier.size((2*relationWidth).dp, (2*relationWidth).dp)
        .offset(x.dp, y.dp)
        .clip(diamondShape)
        .background(Color.White)
        .border(lineStrokeWidth, Color.Black, diamondShape))
}

@Composable
private fun drawInheritanceSymbol(x: Int, y: Int) {
    Box(modifier = Modifier.size((2*relationWidth).dp, (2*relationWidth).dp)
        .offset(x.dp, y.dp)
        .clip(triangleShape)
        .background(Color.White)
        .border(lineStrokeWidth, Color.Black, triangleShape))
}

fun getTreeViewWidth(treeNodeViewModel: TreeNodeViewModel): Float{
    return (treeNodeViewModel.childsMaxWidth + 2*xPadding).toFloat()
}
fun getTreeViewHeight(treeNodeViewModel: TreeNodeViewModel): Float{
    return (getTotalHeight(treeNodeViewModel) + 2*yPadding).toFloat()
}

//return the total height of a node (own height, yPadding to his childs, height of tallest child).
//Used for root element to determine tree Height
private fun getTotalHeight(treeNodeViewModel: TreeNodeViewModel): Int {
    if (treeNodeViewModel.children.isEmpty()) {
        return (treeNodeViewModel.height).roundToInt()
    }
    //else
    var max = 0
    for (child in treeNodeViewModel.children) {
        val totalHeight = getTotalHeight(child)
        max = max(max, totalHeight)
    }
    return (max + treeNodeViewModel.height + yPadding).roundToInt()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun treeRenderView(model: TreeNodeViewModel, width: Float?, height: Float?, state: DisplayState, isA:Boolean){
    dpstate = state

    //val nodesLevel2 = getNodesOfLevel(model, 2)
    val interactionSource = remember { MutableInteractionSource() }

    var outerBoxModifier = if (width != null && height != null){
        Modifier.size(width.dp, height.dp)
    } else if (width != null) {
        Modifier.width(width.dp).fillMaxHeight()
    } else if (height != null){
        Modifier.height(height.dp).fillMaxWidth()
    } else {
        Modifier.fillMaxSize()
    }

    outerBoxModifier = outerBoxModifier.then(Modifier
        .border(2f.dp, Color.Black, RectangleShape)
        .scrollable(
            orientation = Orientation.Vertical,
            // Scrollable state: describes how to consume
            // scrolling delta and update offset
            state = rememberScrollableState { delta ->
                if (delta > 0f){
                    state.scaleState.value *= 1.1f
                } else if (delta < 0f){
                    state.scaleState.value *= 0.9f
                }
                delta
            }
        )
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                moveOffset(dragAmount)
            }
        }
        .combinedClickable(
            onClick = {},
            onDoubleClick = {
                state.scaleState.value = 1f
                state.offsetState.value = Offset(0f,0f)
            },
            interactionSource = interactionSource,
            indication = null,
        )
        .clipToBounds()
        .wrapContentSize(unbounded = true))

    //This is the outerbox
    Box(modifier = outerBoxModifier){
        val modifier = Modifier
            .offset(state.offsetState.value.x.dp, state.offsetState.value.y.dp)
            .scale(state.scaleState.value)
        treeView(modifier, model, isA)

    }
}

fun moveOffset(dragAmount: Offset){
    if (dpstate != null){
        dpstate!!.offsetState.value = Offset(
            dpstate!!.offsetState.value.x + dragAmount.x,
            dpstate!!.offsetState.value.y + dragAmount.y)
    }
}

@Composable
fun treeView(innerBoxModifier: Modifier, nodeViewModel: TreeNodeViewModel, isA:Boolean){
    //some Preprocessing
    determineChildsWidth(nodeViewModel)
    //This is the innerbox
    Box(modifier = innerBoxModifier.then(Modifier.size(getTreeViewWidth(nodeViewModel).dp, getTreeViewHeight(nodeViewModel).dp))) {
        //level order traversal
        val currentLevel: MutableList<TreeNodeViewModel> = arrayListOf(nodeViewModel)
        val nextLevel: MutableList<TreeNodeViewModel> = arrayListOf()
        //offsets for nodes (dependent on previous places nodes, x -> siblings, y -> parents, uncles)
        var x: Int = xPadding
        var y: Int = yPadding
        var localYMax = 0
        while (currentLevel.isNotEmpty()) {
            val node: TreeNodeViewModel = currentLevel.removeFirst()
            node.y = y.toFloat()
            x = max(x, node.minXPos)
            node.x = x + node.childsMaxWidth/2 - node.width/2 //place node in the middle of his childs + offset x
            drawNode(node)

            if (node.children.isNotEmpty()){
                //make sure first child starts beneath this node (e.g. violated if prev uncle node has no childs)
                node.children[0].minXPos = (node.x + (node.width - node.childsMaxWidth)/2)
                //place childs centralized beneath parent, if they are smaller. As well adjusting of fst child only
                if (node.largerThanChilds) {
                    val requiredWidth = node.children.sumOf { child -> child.childsMaxWidth + xPadding} -xPadding
                    node.children[0].minXPos += (node.childsMaxWidth - requiredWidth)/2 //shift prior min x pos to the right
                }
            }

            nextLevel.addAll(node.children)
            localYMax = max(localYMax, node.height.roundToInt())
            x += node.childsMaxWidth + xPadding
            if (currentLevel.isEmpty()) {
                if (nextLevel.isEmpty()) {
                    break
                }
                currentLevel.addAll(nextLevel)
                nextLevel.clear()
                y += localYMax + yPadding
                x = xPadding
                localYMax = 0
            }
        }
        drawLines(nodeViewModel, isA)
    }
}

@Composable
fun drawNode(model: TreeNodeViewModel) {
    Box(
        modifier = Modifier
            .offset(model.x.dp, model.y.dp)
            .size(model.width.dp, model.height.dp)
            .border(borderStrokeWidth.dp, Color.Black, RectangleShape)
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = model.name,
                fontFamily = Fonts.jetbrainsMono,
                fontSize = charHeight.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(color = Color.Black, thickness = borderStrokeWidth.dp)
            for (attr in model.attributes) {
                Text(
                    text = attr,
                    fontFamily = Fonts.jetbrainsMono,
                    fontSize = charHeight.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = boxPadding.dp)
                )
            }
        }
    }

}

//set childsWidth of a node. Depends on max of own width and the childs childsWidth + the padding between them.
//Recursively for whole tree
fun determineChildsWidth(treeNodeViewModel: TreeNodeViewModel) {
    if (treeNodeViewModel.children.isEmpty()) {
        treeNodeViewModel.childsMaxWidth = treeNodeViewModel.width
    }
    else {
        var sum = 0
        for (child in treeNodeViewModel.children){
            determineChildsWidth(child)
            sum += child.childsMaxWidth + xPadding
        }
        sum -= xPadding //added one time to often
        if (sum < treeNodeViewModel.width) {
            treeNodeViewModel.largerThanChilds = true
        }
        treeNodeViewModel.childsMaxWidth = max(treeNodeViewModel.width, sum)
    }
}

@Composable
private fun drawLines(treeNodeViewModel: TreeNodeViewModel, isA:Boolean) {
    val childs = treeNodeViewModel.children
    if (childs.size == 1) { //child is centralized beneath
        val xCanvas: Int = (treeNodeViewModel.x + treeNodeViewModel.width/2 - relationWidth)
        val yCanvas: Int = (treeNodeViewModel.y + treeNodeViewModel.height).roundToInt()
        val heightCanvas = childs[0].y - yCanvas
        Canvas(modifier = Modifier.size((2*relationWidth).dp, heightCanvas.dp)
            .offset(xCanvas.dp, yCanvas.dp)) {
            drawLine(start = Offset(x= relationWidth.toFloat(), y= 0f),
                end = Offset(x= relationWidth.toFloat(), y= heightCanvas),
                color = Color.Black,
                strokeWidth = 1f)
        }
        if(isA){
            drawInheritanceSymbol(xCanvas, yCanvas)
        }else {
            drawCompositionSymbol(xCanvas, yCanvas)
        }
    }
    //for several childs
    else if (childs.isNotEmpty()) {
        val firstChild = childs[0]
        val lastChild = childs.last()
        val widthCanvas = (lastChild.x + lastChild.width/2) - (firstChild.x + firstChild.width/2) + 2*relationWidth
        val heightCanvas = firstChild.y - (treeNodeViewModel.y + treeNodeViewModel.height)
        val xCanvas = firstChild.x + firstChild.width/2 - relationWidth
        val yCanvas = treeNodeViewModel.y + treeNodeViewModel.height
        val yMidLine = firstChild.y - yPadding/2 - yCanvas //y pos of horizontal line inside Canvas
        val xNode = treeNodeViewModel.x + treeNodeViewModel.width/2 - xCanvas //x pos of vertical line beneath node
        Canvas(modifier = Modifier.size(widthCanvas.dp, heightCanvas.dp)
            .offset(xCanvas.dp, yCanvas.dp)) {
            //horizontal line, which connects childs to parent
            drawLine(start = Offset(relationWidth.toFloat(), yMidLine),
                end = Offset(((widthCanvas - relationWidth).toFloat()), yMidLine),
                color = Color.Black,
                strokeWidth = 1f)
            //vertical line beneath node
            drawLine(start = Offset(xNode.toFloat(), 0f),
                end = Offset(xNode.toFloat(), yMidLine),
                color = Color.Black,
                strokeWidth = 1f)
            //vertical line above childs
            for (child in childs) {
                val xChildLine = child.x + child.width/2 - xCanvas //x pos of line to child in canvas
                drawLine(start = Offset(xChildLine.toFloat(), yMidLine),
                    end = Offset(xChildLine.toFloat(), heightCanvas),
                    color = Color.Black,
                    strokeWidth = 1f)
            }
        }
        if(isA){
            drawInheritanceSymbol(treeNodeViewModel.x+ treeNodeViewModel.width/2 -relationWidth, yCanvas.roundToInt())
        }else {
            drawCompositionSymbol(treeNodeViewModel.x+ treeNodeViewModel.width/2 -relationWidth, yCanvas.roundToInt())
        }

    }
    childs.forEach {node -> drawLines(node, isA)}
}

