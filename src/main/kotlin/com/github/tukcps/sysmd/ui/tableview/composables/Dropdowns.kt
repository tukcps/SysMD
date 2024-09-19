package com.github.tukcps.sysmd.ui.tableview.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize.Min
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.github.tukcps.sysmd.ui.helper.toDp
import com.github.tukcps.sysmd.ui.styles.*
import com.github.tukcps.sysmd.ui.tableview.*
import com.github.tukcps.sysmd.ui.tableview.TableTreeNode.Dependencies
import com.github.tukcps.sysmd.ui.tableview.TableType.*
import com.github.tukcps.sysmd.ui.tableview.TableType.Companion.recommendedChildren
import com.github.tukcps.sysmd.ui.tableview.composables.TableTextStylePreset.COLUMN_HEADER
import kotlin.math.round

private val lineHeight = AppTheme.fixedLineHeight.value.dp

@Composable
fun SubAddDD(
    expansionState: Boolean,
    closeDropdown: () -> Unit,
    table: TableTreeNode,
) {
    var secondListExpanded by remember { mutableStateOf(false) }
    var width by remember { mutableStateOf(0) }
    DropdownMenu(
        modifier = Modifier
            .testTag("SubAddDD")
            .padding(horizontal = 4.dp, vertical = 0.dp)
            .onGloballyPositioned {
                width = it.size.width
            },
        expanded = expansionState,
        onDismissRequest = closeDropdown,
    ) {
        val fontSize: TextUnit = round(LocalTextStyle.current.fontSize.value * 0.8).sp
        Text("Recommended table types", fontSize = fontSize, fontWeight = FontWeight.ExtraBold)
        HorizontalDivider()
        table.type.recommendedChildren?.forEach {childType ->
            DropdownMenuItem(
                modifier = Modifier
                    .height(lineHeight + 4.dp),
                text = { Text(childType.rlName ?: "NULL", fontSize = fontSize, fontFamily = Fonts.jetbrainsMono) },
                onClick = { table.add(childType) },
                contentPadding = PaddingValues(0.dp)
            )
            if (childType == CONN_DEF)
                HorizontalDivider()
        }
        HorizontalDivider()
        Box {
            DropdownMenuItem(
                modifier = Modifier
                    .height(lineHeight + 4.dp),
                text = { Text("All table types", fontSize = fontSize, fontWeight = FontWeight.ExtraBold) },
                onClick = { secondListExpanded = !secondListExpanded },
                contentPadding = PaddingValues(0.dp),
                trailingIcon = { Icon(Icons.AutoMirrored.Filled.NavigateNext, null, Modifier.size(lineHeight)) }
            
            )
            DropdownMenu(
                modifier = Modifier
                    .testTag("UnrecommendedDD")
                    .padding(horizontal = 4.dp, vertical = 0.dp),
                expanded = secondListExpanded,
                onDismissRequest = { secondListExpanded = false },
                offset = width.toDp() X 0.dp
            ) {
                TableType.allTypes.forEach { childType ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .height(lineHeight + 4.dp),
                        text = { Text(childType.rlName ?: "NULL", fontSize = fontSize, fontFamily = Fonts.jetbrainsMono) },
                        onClick = { table.add(childType) },
                        contentPadding = PaddingValues(0.dp),
                    )
                    if (childType == ASOC_DEF)
                        HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun FirstColumnDD(
    modifier: Modifier = Modifier,
    expansionState: Boolean,
    closeDropdown: () -> Unit,
    table: TableTreeNode,
    getLegalSubstitution: () -> TableType?,
    tableColors: TableColors,
) {
    DropdownMenu(
        modifier = modifier
            .testTag("FirstColumnDD").padding(0.dp).height(Min),
        expanded = expansionState,
        onDismissRequest = closeDropdown,
    ) {
        getLegalSubstitution()?.let { childType ->
            if (childType == PORT_USE) {
                if (table.hasFlags) DropdownMenuItem(
                    modifier = Modifier
                        .height(lineHeight + 4.dp),
                    text = { Text(text = "Port", style = COLUMN_HEADER.textStyle(tableColors.text)) },
                    onClick = { table.changeFlags(null) },
                    contentPadding = PaddingValues(0.dp)
                )
                if (table.flags != "in ") DropdownMenuItem(
                    modifier = Modifier
                        .height(lineHeight + 4.dp),
                    text = { Text(text = "in Port", style = COLUMN_HEADER.textStyle(tableColors.text)) },
                    onClick = { table.changeFlags("in ") },
                    contentPadding = PaddingValues(0.dp)
                )
                if (table.flags != "out ") DropdownMenuItem(
                    modifier = Modifier
                        .height(lineHeight + 4.dp),
                    text = { Text(text = "out Port", style = COLUMN_HEADER.textStyle(tableColors.text)) },
                    onClick = { table.changeFlags("out ") },
                    contentPadding = PaddingValues(0.dp)
                )
                if (table.flags != "inout ") DropdownMenuItem(
                    modifier = Modifier
                        .height(lineHeight + 4.dp),
                    text = { Text(text = "inout Port", style = COLUMN_HEADER.textStyle(tableColors.text)) },
                    onClick = { table.changeFlags("inout ") },
                    contentPadding = PaddingValues(0.dp)
                )
            } else {
                DropdownMenuItem(
                    modifier = Modifier
                        .testTag("DDitem:setType.$childType")
                        .height(lineHeight + 4.dp),
                    text = { Text(text = childType.rlName ?: "NULL", style = COLUMN_HEADER.textStyle(tableColors.text)) },
                    onClick = {
                        table.parent?.add(
                            type = childType,
                            dependencyType = table.dependencyType,
                            flags = table.flags,
                            originalLine = table.originalLine,
                            values = table.values.filterNotNull().let { them ->
                                if(table.isVarLength)
                                    them
                                else them.take(childType.columnSetup?.lastIndex ?: 0)
                            }.toTypedArray()
                        )
                        table.deleteTable()
                    },
                    contentPadding = PaddingValues(0.dp)
                )
            }
        }
    }
}

@Composable
fun IsaDD(
    modifier: Modifier = Modifier,
    expansionState: Boolean,
    closeDropdown: () -> Unit,
    table: TableTreeNode,
    tableColors: TableColors,
) {
    val deps = Dependencies.entries
    DropdownMenu(
        modifier = modifier.testTag("IsaDD").padding(0.dp).height(Min),
        expanded = expansionState,
        onDismissRequest = closeDropdown,
    ) {
        deps.forEach { (dep, name, symbol) ->
            DropdownMenuItem(
                modifier = Modifier
                    .height(lineHeight + 4.dp),
                text = { Text(text = name, style = COLUMN_HEADER.textStyle(tableColors.text)) },
                trailingIcon = { Text(symbol, style = COLUMN_HEADER.textStyle(tableColors.text)) },
                onClick = {
                    table.changeDep(dep)
                },
                contentPadding = PaddingValues(0.dp)
            )
        }
        
    }
}
