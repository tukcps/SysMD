@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui.paneleft.projectlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.composables.InputField
import com.github.tukcps.sysmd.ui.composables.SysMDTooltipArea
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.viewmodel.TabsViewModel
import java.io.File


/**
 * Shows a list of projects
 * @param tabsViewModel the model of all tabs
 * @param reset method callback for reset
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectList(
    sessionState: MutableState<Session>,
    tabsViewModel: TabsViewModel,
    reset: () -> Unit
) {
    val dataFolder           = remember { mutableStateOf(settings.dataFolder) }
    val projectListViewModel = remember { ProjectListViewModel(sessionState, tabsViewModel, reset = reset) }

    if (projectListViewModel.showNewProjectDialog.value)
        UpdateProjectDialog(projectListViewModel)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(0.2.dp),
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.fitMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = "Local Projects",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                },
                modifier = Modifier.height(30.dp),
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(0.2.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },

        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(30.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.secondary,
            ) {
                Column {
                    Row {
                        Text("Folder: ", style = MaterialTheme.typography.bodyMedium)
                        InputField(
                            modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            value = dataFolder.value,
                            onValueChange = { dataFolder.value = it },
                            singleLine = true,
                            check = { File(dataFolder.value).isDirectory }
                        )
                        Column(Modifier.requiredWidth(40.dp)) {
                            Icon(
                                modifier = Modifier.padding(all = 4.dp).height(16.dp).clickable {
                                    settings.dataFolder = dataFolder.value
                                    projectListViewModel.getProjects()
                                },
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        },

        floatingActionButton = {
            SmallFloatingActionButton(
                modifier = Modifier.scale(0.8F),
                shape = CircleShape,
                onClick = { projectListViewModel.showNewProjectDialog.value = true }
            ) {
                SysMDTooltipArea(tooltipText = "Create new project") {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) {
        innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LazyColumn {
                items(projectListViewModel.viewModelsOfProjects.value) { project ->
                    Project(project, projectListViewModel)
                }
            }
        }
    }
}
