@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.composables.Tabs
import com.github.tukcps.sysmd.ui.composables.TreeViewPlus
import com.github.tukcps.sysmd.ui.helper.fitMaxSize
import com.github.tukcps.sysmd.ui.projectlist.ProjectList
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel

/**
 * The navigation panel left with the tabs for navigation in projects, files,
 * and the model.
 * @param sysMDViewModel the view model of the overall model; needed for navigation and actions
 */
@Composable
fun NavigationPanel(
    sysMDViewModel: SysMDViewModel,
) {
    Surface(
        modifier = Modifier.fitMaxSize(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.2.dp)
    ) {
        val selected = remember { mutableStateOf(0) }
        Column {
            Tabs(listOf(mutableStateOf(" Projects "), mutableStateOf(" Has-A "), mutableStateOf(" Is-A ")), selected)
            Box(Modifier.fillMaxHeight().weight(1F)){
                when (selected.value) {
                    0 -> ProjectList(sysMDViewModel.kerMlModel, sysMDViewModel.editorTabsViewModel, sysMDViewModel::reset)
                    1 -> DecompositionTree(sysMDViewModel.composition)
                    2 -> TreeViewPlus(sysMDViewModel.inheritance)
                }
            }
        }
    }
}