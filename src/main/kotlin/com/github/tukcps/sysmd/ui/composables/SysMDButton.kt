package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.AppTheme

/**
 * Displays a rounded button in SysMD style with optional icon and
 * mandatory tooltip text.
 * @param icon Optional icon
 * @param text Optional text
 * @param tooltipText Mandatory tooltip
 * @param onClick lambda that is called on click
 */
@Composable
fun SysMDButton(
    icon: ImageVector?,
    iconTint: Color,
    text: String?,
    tooltipText: String,
    onClick: () -> Unit
) {
    SysMDTooltipArea(
        tooltipText = tooltipText,
    ) {
        Box(
            modifier = Modifier
                .padding(all = 3.dp)
                .clip(RoundedCornerShape(30.dp))
                .clickable { onClick() },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                    Spacer(Modifier.width(4.dp))
                }
                if (text != null) {
                    Box(
                        modifier = Modifier.padding(2.dp).offset(y = (-2).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            fontSize = AppTheme.fontSize,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}