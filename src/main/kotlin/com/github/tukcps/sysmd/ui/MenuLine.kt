@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LiveHelp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.rest.AgilaRepository
import com.github.tukcps.sysmd.ui.composables.NewDigitalTwinDialog
import com.github.tukcps.sysmd.ui.composables.TooltipForIcons
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import com.github.tukcps.sysmd.ui.viewmodel.DisplayTabModel
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel
import kotlinx.coroutines.launch


/**
 * This Composable displays the menu line on top.
 * It consists of
 * - filename
 * - Icons and text for different actions.
 */
var inCompile: Boolean = false
val isCommitDialogOpen = mutableStateOf(false)
val isDigitalTwinDialogOpen = mutableStateOf(false)
@Suppress("FunctionName") @Composable
fun MenuLine(sysMDViewModel: SysMDViewModel) {
    val activeTab = sysMDViewModel.tabsModel.active
    val newActiveTab =  sysMDViewModel.tabsModel.active as? EditorTabModel
    // val editorTabsModel = agilaViewModel.tabsModel

    // var showUploadPopup by remember { mutableStateOf(false) }
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
                    painter = painterResource("SysMD-Logo.png"),
                    contentDescription = "SysMD Notebook"
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Project: "
                            + (if (activeTab != null) {(if( activeTab is EditorTabModel){ activeTab.tabTitle.value} else {activeTab as DisplayTabModel; activeTab.name})} else "/"),
                    modifier = Modifier.padding(3.dp),
                    fontSize = AppTheme.fontSize
                )
            }

            Row(Modifier.fillMaxWidth().fillMaxHeight(), horizontalArrangement = Arrangement.End) {
                Spacer(Modifier.width(8.dp))
                val composableScope = rememberCoroutineScope()
                TooltipForIcons(tooltipText = "Analyze Model") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable {
                                composableScope.launch {
                                    if (!inCompile) {
                                        inCompile = true
                                        // agilaViewModel.reset() // Always reset before compile?? because new Added Projects in the repo are not started in a session
                                        sysMDViewModel.compile()
                                    }
                                    inCompile = false
                                }
                            }
                    )
                    {
                        Icon(Icons.Filled.Calculate, "Analyze", tint = AppTheme.colors.iconGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze", fontSize = AppTheme.fontSize)
                    }

                    Spacer(Modifier.width(8.dp))
                }

                Spacer(Modifier.width(8.dp))

                TooltipForIcons(tooltipText = "Recommender coming soon") {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { println("Model method for recommender not wired.") })
                    {
                        Icon(Icons.AutoMirrored.Filled.LiveHelp, "Recommend", tint = AppTheme.colors.iconGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Recommend", fontSize = AppTheme.fontSize)
                    }
                }

                Spacer(Modifier.width(8.dp))

                TooltipForIcons(tooltipText = "Saves the currently open tab into its file.") {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable {
                                activeTab?.save()
                                if(newActiveTab != null)
                                    newActiveTab.elementEdited.value = false
                            }
                    ) {
                        if(AgilaRepository.onlineState.value)
                            Icon(Icons.Filled.Save, "Commit", tint = AppTheme.colors.iconGreen)
                        else
                            Icon(Icons.Filled.Save, "Save", tint = AppTheme.colors.iconGreen)
                        Spacer(Modifier.width(8.dp))
                        if(AgilaRepository.onlineState.value)
                            Text("Commit", fontSize = AppTheme.fontSize)
                        else
                            Text("Save", fontSize = AppTheme.fontSize)
                    }
                }
                Spacer(Modifier.width(8.dp))

                if(AgilaRepository.onlineState.value) {
                    // commit: only when we have file from the repository directory: Done
                    TooltipForIcons(tooltipText = "Pushes all modifications to the AGILA backend.") {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable {
                                    try {
                                        // TODO: here, we need more.
                                        // 1) Start session button
                                        // 2) Commit session button
                                        // 3) Close session button

                                        // Open Popup to retrieve user  input
                                        isCommitDialogOpen.value = true
                                    } catch (ignore: Exception) {
                                        println("No repository connected to the AGILA frontend.")
                                    }
                                    // I would put the session settings so some settings, e.g., in JSON!
                                    // And have some form for all settings ...
                                    // As it is, now, it will commit all modifications into a transaction to the DB.
                                    // showUploadPopup = true
                                }
                        ) {
                            Icon(Icons.Filled.Upload, "Push", tint = AppTheme.colors.iconGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("Push", fontSize = AppTheme.fontSize)
                        }
                    }
                    Spacer(Modifier.width(8.dp))

                    // commit : only when we have file from the repository directory: Done
                    TooltipForIcons(tooltipText = "Pulls all modifications from the AGILA backend.") {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable {
                                    sysMDViewModel.pull()
                                }
                        ) {
                            Icon(Icons.Filled.Download, "Pull", tint = AppTheme.colors.iconGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("Pull", fontSize = AppTheme.fontSize)
                        }
                    }
                    Spacer(Modifier.width(30.dp))
                    TooltipForIcons(tooltipText = "Creates a Digital Twin") {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable {
                                    try {
                                        isDigitalTwinDialogOpen.value = true
                                    } catch (ignore: Exception) {
                                        println("No repository connected to the AGILA frontend.")
                                    }
                                    // I would put the session settings so some settings e.g. in JSON !
                                    // And have some form for all settings ...
                                    // As it is now, it will commit all modifications into a transation to the DB.
                                    // showUploadPopup = true
                                }
                        ) {
                            Icon(Icons.Filled.Computer, "New Digital Twin", tint = AppTheme.colors.iconGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("Create Digital Twin", fontSize = AppTheme.fontSize)
                        }
                    }
                    Spacer(Modifier.width(30.dp))
                }

                TooltipForIcons(tooltipText = "Resets the internal Agila model. This also affects the error messages and results.") {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { sysMDViewModel.reset() })
                    {
                        Icon(Icons.Filled.Delete, "Reset session", tint = AppTheme.colors.iconRed)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset session", fontSize = AppTheme.fontSize)
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    }

    if (isCommitDialogOpen.value){
        NewCommitDialog(sysMDViewModel)
    }

    if(isDigitalTwinDialogOpen.value){
        NewDigitalTwinDialog(isDigitalTwinDialogOpen,sysMDViewModel)
    }
}
