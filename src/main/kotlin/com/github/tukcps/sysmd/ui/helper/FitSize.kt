package com.github.tukcps.sysmd.ui.helper

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints

internal enum class Direction {
    Vertical, Horizontal, Both
}

@Stable fun Modifier.fitMaxSize() = this.then(
    FitModifier(
        Direction.Both,
        debugInspectorInfo {
            name = "fitMaxSize"
        }
    )
)
@Stable fun Modifier.fitMaxWidth() = this.then(
    FitModifier(
        Direction.Horizontal,
        debugInspectorInfo {
            name = "fitMaxWidth"
        }
    )
)
@Stable fun Modifier.fitMaxHeight() = this.then(
    FitModifier(
        Direction.Vertical,
        debugInspectorInfo {
            name = "fitMaxHeight"
        }
    )
)

private class FitModifier(
    private val direction: Direction,
    inspectorInfo: InspectorInfo.() -> Unit
) : LayoutModifier, InspectorValueInfo(inspectorInfo) {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val width = if (constraints.hasBoundedWidth && direction != Direction.Vertical)
            constraints.maxWidth else constraints.minWidth

        val height = if (constraints.hasBoundedHeight && direction != Direction.Horizontal)
            constraints.maxHeight else constraints.minHeight

        val placeable = measurable.measure(
            Constraints(width, if (width == 0) Constraints.Infinity else width, height, if (height == 0) Constraints.Infinity else height)
        )

        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}