@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.composables.InputField
import com.github.tukcps.sysmd.ui.composables.TooltipForIcons
import com.github.tukcps.sysmd.ui.helper.fitMaxSize
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.viewmodel.ProjectListViewModel
import com.github.tukcps.sysmd.ui.viewmodel.ProjectViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TabsModel
import java.io.File


/**
 * Shows a list of projects
 * @param tabsModel the model of all tabs
 */
@Composable
fun ProjectList(
    tabsModel: TabsModel,
    reset: () -> Unit
) {
    val newProjectViewModel = remember {  ProjectViewModel(null, "", tabsModel, reset = reset) }
    if (newProjectViewModel.showDialog)
        NewProjectDialog(newProjectViewModel)
    val dataFolder =  remember { mutableStateOf(settings.dataFolder) }
    val projectListViewModel = ProjectListViewModel(tabsModel, reset = reset)
    Column(modifier = Modifier.padding(all = 2.dp)) {
        Spacer(modifier = Modifier.height(6.dp).fitMaxWidth())
        Column {
            Row {
                Text(
                    modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                    text = "Projects in local files",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(Modifier.requiredWidth(40.dp)) {
                    TooltipForIcons(tooltipText = "Creates a new project based on template") {
                        Icon(
                            modifier = Modifier.padding(all = 1.dp).height(16.dp).clickable {
                                newProjectViewModel.showDialog = true
                            },
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null
                        )
                    }
                }
            }
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
                        modifier = Modifier.padding(all = 1.dp).height(16.dp).clickable {
                            settings.dataFolder = dataFolder.value
                            projectListViewModel.readProjects()
                        },
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                    )
                }
            }
            Row {
                Spacer(modifier = Modifier.padding(8.dp).height(1.dp).fitMaxWidth().background(Color.DarkGray))
            }
        }

        LazyColumn {
            items(projectListViewModel.viewModelsOfProjects.value) { project -> Project(project) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Project(
    project: ProjectViewModel
) {
    var expanded: Boolean by remember { mutableStateOf(false) }

    if (project.warnDialog.value) {
        AlertDialog({project.warnDialog.value = false} ,
            title = { Text("Unsaved changes")},
            confirmButton = { Button(onClick = {project.warnDialog.value = false}) { Text("OK") } },
            text = { Text("Some files have been modified. Save them before opening another project.")}
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth().padding(all = 5.dp).clickable { expanded = !expanded },
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 4.dp)
                .onClick { project.openProject() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                var icon: ImageBitmap? = null
                if (project.logo.value.isNotBlank())
                    try {
                        val file = if (project.directory!!.isDirectory)
                            File(project.directory?.path + "/" + project.logo.value)
                        else
                            File(project.directory?.parent!! + "/" + project.logo.value)
                        icon = loadImageBitmap(file.inputStream())
                    } catch (e: Exception) {
                        println("  In ProjectList.kt, project ${project.name}: $e")
                    }

                if (icon != null) {
                    Image(
                        modifier = Modifier.size(40.dp),
                        painter = BitmapPainter(image = icon),
                        contentScale = ContentScale.Fit,
                        contentDescription = null
                    )
                } else
                    Icon(
                        modifier = Modifier.padding(all = 7.dp),
                        imageVector = Icons.Default.FilePresent,
                        contentDescription = null,
                    )

                Column(modifier = Modifier.padding(start = 5.dp).fitMaxSize()) {
                    Column {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}