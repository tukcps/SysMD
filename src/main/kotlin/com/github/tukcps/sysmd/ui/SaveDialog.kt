@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel
import com.github.tukcps.sysmd.ui.viewmodel.TabsModel
import kotlin.system.exitProcess

@Composable
fun SaveDialog(
    openDialog: MutableState<Boolean>,
    model: EditorTabModel? = null,
    tabs: TabsModel? = null
) {

    val onSave = {
        if (model == null) {
            tabs?.editorTabs?.forEach { tab ->
                tab as EditorTabModel
                tab.save()
                openDialog.value = false
                tab.elementEdited.value = false
            }
            exitProcess(0)
        } else {
            model.save()
            model.close?.invoke()
            openDialog.value = false
            model.elementEdited.value = false
        }
    }

    val onDrop = {
        if (model == null) {
            exitProcess(0)
        } else {
            model.close?.invoke()
            openDialog.value = false
            model.elementEdited.value = false
        }
    }

    AlertDialog(
        onDismissRequest = { openDialog.value = false },
        title = { Text("Unsaved Changes") },
        text = { Text("There are unsaved changes. Save the changes?") },
        confirmButton = { Button(onClick = onSave) { Text("Save changes") } },
        dismissButton = { Button(onClick =onDrop)  { Text("Drop changes") } },
        icon = { Icon(Icons.Default.Warning,
            contentDescription = "Unsaved modifications",
            tint = AppTheme.colors.iconRed,
            modifier = Modifier.size(60.dp))},
    )
}