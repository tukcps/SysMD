@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.composables.SysMDTooltipArea
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.MyIcons
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel

/**
 * Composable that shows a list of information texts if activated
 * @param model View model of textual representation cell with information to be shown
 */
@Composable
fun AnnotationsView(model: TextualRepresentationViewModel) {
    Row {
        val showInfo: MutableState<Boolean> = remember { mutableStateOf(true) }

        if (model.displayItems.isNotEmpty()) {
            Column(modifier = Modifier.width(48.dp).padding(1.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                val action = CellAction(
                    icon = MyIcons.Info,
                    label = if (showInfo.value) "Hide Info" else "Show Info",
                    action = { showInfo.value = !showInfo.value },
                    // tint = if (showInfo.value) AppTheme.colors.iconYellow else MaterialTheme.colorScheme.outlineVariant
                    tint = AppTheme.colors.iconYellow
                )

                SysMDTooltipArea(tooltipText = action.label) {
                    IconButton(
                        modifier = Modifier.size(18.dp),
                        onClick = action.action,
                        enabled = action.enabled
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            tint = action.tint ?: MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showInfo.value && model.displayItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.background(AppTheme.colors.infoContainer.copy(0.5f))
                            .fillMaxWidth()
                            .padding(start = 70.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = "Consistent values resp. created elements: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.info
                        )
                    }
                    for (item in model.displayItems) {
                        when {
                            item.text.startsWith("ERROR") -> {
                                Row(
                                    modifier = Modifier.background(
                                        MaterialTheme.colorScheme.errorContainer.copy(0.5f)
                                    ).fillMaxWidth().padding(start = 70.dp)
                                ) {
                                    Text(
                                        "   ${item.text}",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            item.text.startsWith("INFO") ->
                                Row(
                                    modifier = Modifier.background(
                                        AppTheme.colors.warningContainer.copy(0.5f)
                                    ).fillMaxWidth().padding(start = 70.dp)
                                ) {
                                    Text(
                                        "   ${item.text}",
                                        color = AppTheme.colors.warning,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                            else ->
                                Row(
                                    modifier = Modifier.background(AppTheme.colors.infoContainer.copy(0.5f))
                                        .fillMaxWidth().padding(start = 70.dp)
                                ) {
                                    Text(
                                        "   ${item.text}",
                                        color = AppTheme.colors.info,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}