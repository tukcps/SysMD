@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.tukcps.sysmd.ui.tableview.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.*
import com.github.tukcps.sysmd.ui.composables.TooltipForIcons
import com.github.tukcps.sysmd.ui.styles.*
import com.github.tukcps.sysmd.ui.tableview.*
import com.github.tukcps.sysmd.ui.tableview.TableType.Companion.recommendedChildren

private val lineHeight = AppTheme.fixedLineHeight.value.dp

private data class CustomViewConfiguration(
    override val doubleTapMinTimeMillis: Long,
    override val doubleTapTimeoutMillis: Long,
    override val longPressTimeoutMillis: Long,
    override val touchSlop: Float,
    override val minimumTouchTargetSize: DpSize,
): ViewConfiguration {
    companion object {
        fun buttonSizeFix(localViewConfiguration: ViewConfiguration): ViewConfiguration = CustomViewConfiguration(
            doubleTapMinTimeMillis = localViewConfiguration.doubleTapMinTimeMillis,
            doubleTapTimeoutMillis = localViewConfiguration.doubleTapTimeoutMillis,
            longPressTimeoutMillis = localViewConfiguration.longPressTimeoutMillis,
            touchSlop = localViewConfiguration.touchSlop,
            minimumTouchTargetSize = DpSize(lineHeight, lineHeight),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DeleteTableOrRow(
    modifier: Modifier,
    table: TableTreeNode,
    tableColors: TableColors,
    row: Int = -1,
    col: Int = 0,
    enabled: Boolean = true,
) {
    var openAlert by remember { mutableStateOf(false) }
    var hovered by remember { mutableStateOf(false) }
    val buttonSizeFix = CustomViewConfiguration.buttonSizeFix(LocalViewConfiguration.current)
    
    CompositionLocalProvider(LocalViewConfiguration provides buttonSizeFix, LocalMinimumInteractiveComponentEnforcement provides false) {
        with(table) {
            if (openAlert)
                DeleteTableAlert(table, { openAlert = false }, tableColors)
            TooltipForIcons(
                tooltipText = when {
                    row < 0 -> "Delete this table"
                    else    -> "Delete this value"
                },
                modifier = modifier
                    .size(lineHeight)
                    .focusProperties { canFocus = false }) {
                IconButton(
                    enabled = enabled,
                    modifier = Modifier
                        .testTag("DeleteTableOrRow")
                        .focusProperties { canFocus = false },
                    onClick = fun() = when {
                        !isChildless && row < 0 -> openAlert = true
                        !isChildless && !table.isVarLength -> openAlert = true
                        row < 0 || !table.isVarLength -> deleteTable()
                        else -> delVLVfield(row, col)
                    }
                ) {
                    Icon(
                        imageVector = when {
                            row < 0 -> Filled.Cancel
                            else    -> Filled.DoNotDisturbOn
                        },
                        contentDescription = when {
                            row < 0 -> "Delete this table"
                            else    -> "Delete this row"
                        },
                        tint = when {
                            !enabled -> MaterialTheme.colorScheme.outlineVariant
                            hovered  -> tableColors.delete
                            else     -> tableColors.button
                        },
                        modifier = Modifier
                            .onPointerEvent(PointerEventType.Enter) { hovered = true }
                            .onPointerEvent(PointerEventType.Exit) { hovered = false }
                            .focusProperties { canFocus = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTableAlert(
    table: TableTreeNode,
    closeAlert: () -> Unit,
    tableColors: TableColors,
    modifier: Modifier = Modifier,
) {
    val buttonSizeFix = CustomViewConfiguration.buttonSizeFix(LocalViewConfiguration.current)
    
    CompositionLocalProvider(LocalViewConfiguration provides buttonSizeFix, LocalMinimumInteractiveComponentEnforcement provides false) {
        AlertDialog(
            modifier = modifier
                .testTag("DeleteTableAlert"),
            onDismissRequest = {},
            confirmButton = {
                Row {
                    Button(
                        modifier = Modifier,
                        onClick = { closeAlert(); table.deleteTable() },
                        colors = ButtonDefaults.buttonColors(containerColor = tableColors.delete)
                    ) { Text(text = "Delete Children") }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        modifier = Modifier,
                        onClick = { closeAlert(); table.unwrap() },
                        colors = ButtonDefaults.buttonColors(containerColor = tableColors.delete)
                    ) { Text(text = "Unwrap Children") }
                }
            },
            dismissButton = {
                Button(
                    modifier = Modifier,
                    onClick = closeAlert
                ) { Text(text = "Cancel") }
            },
            text = { Text(text = "Do you want to delete this table and all its children or unwrap its children?") },
        )
    }
}


//button for adding rows
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RowAddButton(
    modifier: Modifier,
    table: TableTreeNode,
    tableColors: TableColors,
    row: Int,
    col: Int = 0,
) {
    val buttonSizeFix = CustomViewConfiguration.buttonSizeFix(LocalViewConfiguration.current)
    CompositionLocalProvider(LocalViewConfiguration provides buttonSizeFix, LocalMinimumInteractiveComponentEnforcement provides false) {
        var hovered by remember { mutableStateOf(false) }
        with(table) {
            TooltipForIcons(
                tooltipText = "Add a row",
                modifier = modifier.testTag("RowAddButton")
                    .size(lineHeight)
                    .focusProperties { canFocus = false }
            ) {
                IconButton(
                    onClick = { addVLVfield(row, col) },
                    modifier = Modifier
                        .focusProperties { canFocus = false },
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add a row",
                        tint = when {
                            hovered -> tableColors.buttonHovered
                            else    -> tableColors.button
                        },
                        modifier = Modifier
                            .onPointerEvent(PointerEventType.Enter) { hovered = true }
                            .onPointerEvent(PointerEventType.Exit) { hovered = false }
                            .focusProperties { canFocus = false }
                    )
                }
            }
        }
    }
}

//button for adding subordinate tables
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SubAddButton(
    modifier: Modifier,
    table: TableTreeNode,
    tableColors: TableColors,
    icon: ImageVector = Icons.Default.ArrowDropDownCircle,
) {
    val buttonSizeFix = CustomViewConfiguration.buttonSizeFix(LocalViewConfiguration.current)
    CompositionLocalProvider(LocalViewConfiguration provides buttonSizeFix, LocalMinimumInteractiveComponentEnforcement provides false) {
        var expanded by remember { mutableStateOf(false) }
        var hovered by remember { mutableStateOf(false) }
        var tooltipLines by remember { mutableStateOf(1) }
        with(table) {
            TooltipArea(
                tooltip = {
                    Surface(
                        modifier = modifier
                            .shadow(4.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column {
                            if (type.recommendedChildren.isNullOrEmpty()) Icon(
                                Icons.Default.Warning,
                                null,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                tint = Color(1f, .4f, 0f)
                            )
                            Text(
                                text = buildAnnotatedString {
                                    if (type.recommendedChildren.isNullOrEmpty()) {
                                        pushStyle(SpanStyle(color = Color(1f, .4f, 0f)))
                                        append("Not recommended for this type of table")
                                        appendLine()
                                        pop()
                                        tooltipLines = 4
                                    } else tooltipLines = 1
                                    append("Add a subordinate Table to this ${type.rlName}")
                                },
                                modifier = Modifier.padding(5.dp),
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        }
                    }
                },
                modifier = modifier
                    .size(lineHeight)
                    .focusProperties { canFocus = false },
                delayMillis = if (type.recommendedChildren.isNullOrEmpty()) 0 else 800,
                tooltipPlacement = TooltipPlacement.ComponentRect(anchor = Alignment.TopEnd, offset = 4.dp X -4.dp, alignment = Alignment.TopEnd),
            ) {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .focusProperties { canFocus = false },
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Add a subordinate table to this table",
                        tint = when {
                            (type.recommendedChildren?.size ?: 0) == 0 -> tableColors.buttonDisabled
                            hovered                                    -> tableColors.buttonHovered
                            else                                       -> tableColors.button
                        },
                        modifier = Modifier
                            .onPointerEvent(PointerEventType.Enter) { hovered = true }
                            .onPointerEvent(PointerEventType.Exit) { hovered = false }
                            .focusProperties { canFocus = false }
                    )
                    SubAddDD(expansionState = expanded, closeDropdown = { expanded = false }, table)
                }
            }
        }
    }
}