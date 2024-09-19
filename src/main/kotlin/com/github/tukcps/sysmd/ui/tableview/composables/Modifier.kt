package com.github.tukcps.sysmd.ui.tableview.composables

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.*
import com.github.tukcps.sysmd.ui.tableview.X

fun Modifier.width(dpOrIntrinsicSize: Comparable<*>) = this.then(
    if (dpOrIntrinsicSize is Dp) width(width = (dpOrIntrinsicSize))
    else width(intrinsicSize = dpOrIntrinsicSize as IntrinsicSize)
)

//draw borders around element according to specifications (e.g. leave right side open)
private class PartialBorder(
    val color: Color,
    val thickness: Dp,
    val top: Boolean = true,
    val bottom: Boolean = true,
    val left: Boolean = true,
    val right: Boolean = true,
    val offset: Offset = Offset.Zero,
    val customSize: Offset? = null,
): DrawModifier {
    override fun ContentDrawScope.draw() {
        val width = customSize?.x ?: size.width
        val height = customSize?.y ?: size.height
        this@draw.drawContent()
        //+-thickness/2 to stop half of line being outside the drawing area
        translate(offset.x, offset.y) {
            if (top)
                drawLine(
                    color = color,
                    start = 0f X (0f + thickness.toPx() / 2),
                    end = width X (0f + thickness.toPx() / 2),
                    strokeWidth = thickness.toPx()
                )
            if (bottom)
                drawLine(
                    color = color,
                    start = 0f X (height - thickness.toPx() / 2),
                    end = width X (height - thickness.toPx() / 2),
                    strokeWidth = thickness.toPx()
                )
            if (left)
                drawLine(
                    color = color,
                    start = (0f + thickness.toPx() / 2) X 0f,
                    end = (0f + thickness.toPx() / 2) X height,
                    strokeWidth = thickness.toPx()
                )
            if (right)
                drawLine(
                    color = color,
                    start = (width - thickness.toPx() / 2) X 0f,
                    end = (width - thickness.toPx() / 2) X height,
                    strokeWidth = thickness.toPx()
                )
        }
    }
}

fun Modifier.partialBorder(
    color: Color,
    thickness: Dp = 1.dp,
    top: Boolean = true,
    bottom: Boolean = true,
    left: Boolean = true,
    right: Boolean = true,
    offset: Offset = Offset.Zero,
    customSize: Offset? = null
) = this.then(PartialBorder(color, thickness, top, bottom, left, right, offset, customSize))