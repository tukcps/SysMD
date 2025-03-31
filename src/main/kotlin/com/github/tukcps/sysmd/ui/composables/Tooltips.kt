@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp


/**
 * A SysMD - specific tooltip area
 * @param tooltipText The text that appears after 800 ms
 * @param modifier a modifier that is passed to components
 * @param content the content of the area with the tooltip
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SysMDTooltipArea(
    tooltipText: String = "Place tooltip text here",
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipArea(
        tooltip = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = tooltipText,
                    modifier = Modifier.padding(5.dp),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                )
            }
        },
        modifier = modifier,
        delayMillis = 800, // in milliseconds
        tooltipPlacement = TooltipPlacement.CursorPoint(
            offset = DpOffset((-20).dp, (7).dp) // tooltip offset
        ),
        content = content
    )
}

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TooltipInstant(
    tooltipText: String = "Place instant tooltip text here",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipArea(
        tooltip = {// composable tooltip content
            Surface(
                modifier = Modifier.shadow(4.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = tooltipText,
                    modifier = Modifier.clickable(onClick = onClick).padding(5.dp),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            }
        },
        modifier = modifier,
        delayMillis = 400, // in milliseconds
        tooltipPlacement = TooltipPlacement.CursorPoint(
            offset = DpOffset((-20).dp, (7).dp) // tooltip offset
        ),
        content = content
    )
}
