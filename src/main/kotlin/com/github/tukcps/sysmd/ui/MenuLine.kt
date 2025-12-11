@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.generated.resources.Res
import com.github.tukcps.sysmd.generated.resources.SysMD_Icon
import com.github.tukcps.sysmd.rest.RESTRepository
import com.github.tukcps.sysmd.ui.composables.SysMDButton
import com.github.tukcps.sysmd.ui.dialogs.NewCommitDialog
import com.github.tukcps.sysmd.ui.dialogs.NoProjectSelectedDialog
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


/**
 * This Composable displays the menu line on top.
 * It consists of
 * - filename
 * - Icons and text for different actions.
 */
var inCompile: Boolean = false
val isCommitDialogOpen = mutableStateOf(false)
val isNoProjectDialogOpen = mutableStateOf(false)

@Suppress("FunctionName") @Composable
fun MenuLine(sysMDViewModel: SysMDViewModel) {
    val hasProject = sysMDViewModel.sessionState.value.project != null

    Box(
        Modifier
            .height(32.dp)
            .fillMaxWidth()
    ) {
        //if (showUploadPopup) { uploadPopup(editorTabsModel.toString(), onDismissRequest = { showUploadPopup = false }) }
        Row(
            Modifier.fillMaxHeight().fillMaxWidth()
                .align(Alignment.CenterStart)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier.padding(top = 3.dp).align(Alignment.Bottom),
                    painter = painterResource(Res.drawable.SysMD_Icon),
                    contentDescription = "SysMD Notebook"
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Project: ${sysMDViewModel.sessionState.value.project?.name?:"(no project selected)"}",
                    modifier = Modifier.padding(3.dp),
                    fontSize = AppTheme.fontSize
                )
            }

            Row(Modifier.fillMaxWidth().fillMaxHeight(), horizontalArrangement = Arrangement.End) {
                Spacer(Modifier.width(8.dp))
                val composableScope = rememberCoroutineScope()

                SysMDButton(
                    icon = Icons.Filled.Transform,
                    iconTint = if (hasProject) AppTheme.colors.iconGreen else AppTheme.colors.iconGray,
                    text = "Compile",
                    tooltipText = if (hasProject) "Compiles textual representation" else "No project selected",
                    onClick = {
                        if (!hasProject) {
                            isNoProjectDialogOpen.value = true
                            return@SysMDButton
                        }
                        composableScope.launch {
                            sysMDViewModel.reset()
                            if (!inCompile) {
                                inCompile = true
                                sysMDViewModel.compile(solve = false)
                            }
                            inCompile = false
                        }
                    }
                )

                Spacer(Modifier.width(8.dp))

                SysMDButton(
                    icon = Icons.Filled.Calculate,
                    iconTint = if (hasProject) AppTheme.colors.iconGreen else AppTheme.colors.iconGray,
                    text = "Solve",
                    tooltipText = if (hasProject) "Computes constraint propagation" else "No project selected",
                    onClick = {
                        if (!hasProject) {
                            isNoProjectDialogOpen.value = true
                            return@SysMDButton
                        }
                        composableScope.launch {
                            sysMDViewModel.reset()
                            if (!inCompile) {
                                inCompile = true
                                sysMDViewModel.compile(solve = true)
                            }
                            inCompile = false
                        }
                    }
                )

                Spacer(Modifier.width(8.dp))

                SysMDButton(
                    icon = Icons.Filled.Save,
                    iconTint = if (hasProject) AppTheme.colors.iconGreen else AppTheme.colors.iconGray,
                    text = if(RESTRepository.onlineState.value) "Commit" else "Save",
                    tooltipText = if (hasProject) "Saves the project in its files." else "No project selected",
                    onClick = {
                        if (!hasProject) {
                            isNoProjectDialogOpen.value = true
                            return@SysMDButton
                        }
                        sysMDViewModel.sessionState.value.project?.saveToInterchangeFiles()
                        sysMDViewModel.tabsViewModel.editorTabs.forEach { editorTabModel ->
                            editorTabModel.save()
                            editorTabModel.elementEdited.value = false
                        }
                    }
                )

                SysMDButton(
                    icon = Icons.Filled.RestartAlt,
                    iconTint = if (hasProject) AppTheme.colors.iconRed else AppTheme.colors.iconGray,
                    text = "Reset",
                    tooltipText = if (hasProject) "Resets the model, error messages, and results." else "No project selected",
                    onClick = {
                        if (!hasProject) {
                            isNoProjectDialogOpen.value = true
                            return@SysMDButton
                        }
                        sysMDViewModel.reset()
                    }
                )

                Spacer(Modifier.width(8.dp))
            }
        }
    }

    if (isCommitDialogOpen.value) {
        NewCommitDialog(sysMDViewModel)
    }

    if (isNoProjectDialogOpen.value) {
        NoProjectSelectedDialog(
            onDismiss = { isNoProjectDialogOpen.value = false }
        )
    }
}