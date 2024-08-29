@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.rest.AgilaRepository
import com.github.tukcps.sysmd.ui.composables.Tabs
import com.github.tukcps.sysmd.ui.composables.TreeViewPlus
import com.github.tukcps.sysmd.ui.helper.fitMaxSize
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel

/**
 * The navigation panel left with the tabs for navigation in projects, files,
 * and the model.
 * @param sysMDViewModel the view model of the overall model; needed for navigation and actions
 */
@Composable
fun NavigationPanel(
    sysMDViewModel: SysMDViewModel,
    progressBarValue: MutableState<Float>
) {
    Surface(
        modifier = Modifier.fitMaxSize(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.2.dp)
    ) {
        val selected = remember { mutableStateOf(if (AgilaRepository.onlineState.value) 0 else 1) }
        Column() {
            Tabs(mutableListOf("Web", "Project", " hasA ", " isA "), selected)
            Box(Modifier.fillMaxHeight().weight(1F)){
                when (selected.value) {
                    0 -> TreeViewPlus(sysMDViewModel.projectsTree, sysMDViewModel.tabsModel)
                    1 -> ProjectList(sysMDViewModel.tabsModel, sysMDViewModel::reset)
                    2 -> DecompositionTree(sysMDViewModel.composition, sysMDViewModel.tabsModel)
                    3 -> TreeViewPlus(sysMDViewModel.inheritance, sysMDViewModel.tabsModel)
                }
            }


            if(progressBarValue.value < 1F){
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "Building Indexes: ")
                    LinearProgressIndicator(
                        progress = { progressBarValue.value },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
            }
        }
    }
}