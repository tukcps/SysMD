@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.ui.styles.AppTheme


/**
 * The item in the TreeView
 */
@Composable
internal fun TreeItemIcon(modifier: Modifier, model: TreeViewModel.Item) = Box(modifier.size(24.dp).padding(4.dp)) {
    when (val type = model.type) {
        is TreeViewModel.ItemType.Folder -> when {
            !type.canExpand -> Unit
            type.isExpanded -> Icon(
                Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LocalContentColor.current
            )
            else -> Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = LocalContentColor.current
            )
        }
        is TreeViewModel.ItemType.Item -> when ( (model.element as? ElementData)?.type) {
            ElementType.TextualRepresentation -> Icon(Icons.Default.Edit, "Markdown file", tint = AppTheme.colors.iconGreen)
            else -> Icon(Icons.Default.Calculate, contentDescription = null, tint = AppTheme.colors.backgroundDark)
        }
    }
}
