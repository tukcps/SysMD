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

@Suppress("FunctionName") @Composable
fun MenuLine(sysMDViewModel: SysMDViewModel) {
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
                    iconTint = AppTheme.colors.iconGreen,
                    text = "Compile",
                    tooltipText = "Compiles textual representation",
                    onClick = {
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
                    iconTint = AppTheme.colors.iconGreen,
                    text = "Solve",
                    tooltipText = "Computes constraint propagation",
                    onClick = {
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
                    iconTint = AppTheme.colors.iconGreen,
                    text = if(RESTRepository.onlineState.value) "Commit" else "Save",
                    tooltipText = "Saves the project in its files.",
                    onClick = {
                        sysMDViewModel.sessionState.value.project?.saveToInterchangeFiles()
                        sysMDViewModel.editorTabsViewModel.editorTabs.forEach { editorTabModel ->
                            editorTabModel.save()
                            editorTabModel.elementEdited.value = false
                        }
                    }
                )

                SysMDButton(
                    icon = Icons.Filled.RestartAlt, iconTint = AppTheme.colors.iconRed,
                    text = "Reset",
                    tooltipText = "Resets the model, error messages, and results.",
                    onClick = { sysMDViewModel.reset() }
                )

                Spacer(Modifier.width(8.dp))
            }
        }
    }

    if (isCommitDialogOpen.value){
        NewCommitDialog(sysMDViewModel)
    }
}
