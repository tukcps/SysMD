@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.paneleft

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.ui.composables.ButtonSelection
import com.github.tukcps.sysmd.ui.composables.TreeViewModel
import com.github.tukcps.sysmd.ui.composables.TreeViewPlus
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.viewmodel.MyIcons

/**
 * The tree view with the ownership (de-)composition, i.e., starting with the root namespace
 * @param composition view model of the tree
 */
@Composable
fun DecompositionTree(
    composition: MutableState<TreeViewModel>,
) {
    Column(Modifier.padding(start = 5.dp)) {
        var collapsed by remember { mutableStateOf(true) }
        val standards = remember { mutableStateOf(false) }
        val annotations = remember { mutableStateOf(false) }
        Row(Modifier.padding(start = 5.dp).height(24.dp).fillMaxWidth().clickable { collapsed = !collapsed }) {
            Icon(imageVector = MyIcons.Menu, "Filters", modifier = Modifier.size(16.dp).align(Alignment.CenterVertically))
            Text(" Filters",
                modifier = Modifier.padding(bottom = 2.dp).align(Alignment.CenterVertically),
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }

        if (!collapsed) {
            Column(modifier = Modifier.padding(start=10.dp)) {
                ButtonSelection(standards, "Libraries")
                ButtonSelection(annotations, "Metadata")
            }
        }

        Row {
            Spacer(modifier = Modifier.padding(8.dp).height(1.dp).fitMaxWidth().background(Color.DarkGray))
        }

        TreeViewPlus(composition) {
            !   (
                    ((!standards.value) && (it.element as Element).isLibraryElement)  ||
                    ((!annotations.value) && it.name.startsWith("[MetadataFeature]"))
                )
        }
    }
}