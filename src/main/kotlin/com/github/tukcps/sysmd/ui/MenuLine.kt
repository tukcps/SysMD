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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.generated.resources.Res
import com.github.tukcps.sysmd.generated.resources.sysmd
import com.github.tukcps.sysmd.rest.RESTRepository
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.composables.SysMDButton
import com.github.tukcps.sysmd.ui.dialogs.NewCommitDialog
import com.github.tukcps.sysmd.ui.dialogs.NoProjectSelectedDialog
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.CompileProgress
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


/**
 * This Composable displays the menu line on top.
 * It consists of
 * - filename
 * - Icons and text for different actions.
 */
val isCommitDialogOpen = mutableStateOf(false)
val isNoProjectDialogOpen = mutableStateOf(false)

@Suppress("FunctionName") @Composable
fun MenuLine(sysMDViewModel: SysMDViewModel) {
    val hasProject = SessionManager.getSession(sysMDViewModel.sessionIdState.value)?.project != null

    // While the project is compiled or solved in the background, the actions on the whole project are not available.
    val compileProgress by sysMDViewModel.compileProgress.collectAsState()
    val busy = compileProgress != null
    val busyTooltip = "Not available while the project is compiled or solved"

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
                    painter = painterResource(Res.drawable.sysmd),
                    contentDescription = "SysMD Notebook"
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Project session: ${sysMDViewModel.projectListViewModel.selectedProjectState.value?.name?:" ./. "} ",
                    modifier = Modifier.padding(3.dp),
                    fontSize = AppTheme.fontSize
                )
            }

            Row(Modifier.fillMaxWidth().fillMaxHeight(), horizontalArrangement = Arrangement.End) {
                Spacer(Modifier.width(8.dp))
                val composableScope = rememberCoroutineScope()

                // What a running compile or solve is doing; it takes the room that the buttons leave.
                compileProgress?.let { CompileProgressIndicator(it, Modifier.weight(1f, fill = false).fillMaxHeight()) }

                SysMDButton(
                    icon = Icons.Filled.Transform,
                    iconTint = if (hasProject && !busy) AppTheme.colors.iconGreen else AppTheme.colors.iconGray,
                    text = "Compile",
                    tooltipText = when {
                        busy -> busyTooltip
                        hasProject -> "Compiles all files in project session"
                        else -> "No project session"
                    },
                    onClick = {
                        if (busy) return@SysMDButton
                        if (!hasProject) {
                            isNoProjectDialogOpen.value = true
                            return@SysMDButton
                        }
                        composableScope.launch { sysMDViewModel.compileProject(Runlevel.MODEL) }
                    }
                )

                Spacer(Modifier.width(8.dp))

                SysMDButton(
                    icon = Icons.Filled.Calculate,
                    iconTint = if (hasProject && !busy) AppTheme.colors.iconGreen else AppTheme.colors.iconGray,
                    text = "Solve",
                    tooltipText = when {
                        busy -> busyTooltip
                        hasProject -> "Compiles and then computes constraint propagation in all files of project session"
                        else -> "No project selected"
                    },
                    onClick = {
                        if (busy) return@SysMDButton
                        if (!hasProject) {
                            isNoProjectDialogOpen.value = true
                            return@SysMDButton
                        }
                        composableScope.launch { sysMDViewModel.compileProject(Runlevel.ALL) }
                    }
                )

                Spacer(Modifier.width(8.dp))

                SysMDButton(
                    icon = Icons.Filled.Save,
                    iconTint = if (hasProject && !busy) AppTheme.colors.iconGreen else AppTheme.colors.iconGray,
                    text = if(RESTRepository.onlineState.value) "Commit" else "Save",
                    tooltipText = when {
                        busy -> busyTooltip
                        hasProject -> "Saves the project to repository or interchange project files."
                        else -> "No project selected"
                    },
                    onClick = {
                        if (busy) return@SysMDButton
                        if (!hasProject) {
                            isNoProjectDialogOpen.value = true
                            return@SysMDButton
                        }
                        sysMDViewModel.projectListViewModel.selectedProjectState.value?.saveProjectToRepository()
                    }
                )

                SysMDButton(
                    icon = Icons.Filled.RestartAlt,
                    iconTint = if (hasProject && !busy) AppTheme.colors.iconRed else AppTheme.colors.iconGray,
                    text = "Reset",
                    tooltipText = when {
                        busy -> busyTooltip
                        hasProject -> "Resets the model, error messages, and results."
                        else -> "No project selected"
                    },
                    onClick = {
                        if (busy) return@SysMDButton
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

    if (isCommitDialogOpen.value) { NewCommitDialog(sysMDViewModel) }

    NoProjectSelectedDialog(isNoProjectDialogOpen, onDismiss = { isNoProjectDialogOpen.value = false })
}

/**
 * Shows what a running compile or solve is doing: a short text and a progress bar.
 * If there is not enough room, the text is shortened.
 */
@Suppress("FunctionName") @Composable
fun CompileProgressIndicator(progress: CompileProgress, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = progress.message,
            modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp),
            fontSize = AppTheme.fontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        LinearProgressIndicator(
            progress = { progress.fraction },
            modifier = Modifier.width(120.dp),
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.width(8.dp))
    }
}
