package com.github.tukcps.sysmd.ui.tableview.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.helper.thenIf
import com.github.tukcps.sysmd.ui.styles.*
import com.github.tukcps.sysmd.ui.tableview.*
import com.github.tukcps.sysmd.ui.tableview.TableType.Companion.varColSetup
import com.github.tukcps.sysmd.ui.tableview.composables.TableTextStylePreset.*

private val lineHeight get() = AppTheme.fixedLineHeight.value.dp

//for variable value length tables with multiple value columns
@Composable
fun VL_Table(
    modifier: Modifier = Modifier,
    table: TableTreeNode,
    readOnly: Boolean,
    tableColors: TableColors,
) {
    Row(
        modifier
            .testTag("VL_Table")
            .height(Min)
            .fillMaxWidth()
    ) {
        //replace "To" with custom connecting relation if needed
        table.type.columnSetup
            ?.let { firstRowStrings ->
                if (firstRowStrings.last() != "To")
                    firstRowStrings
                else
                    table.conRel?.let { firstRowStrings.dropLast(1) + (it).replaceFirstChar(Char::uppercase) } ?: firstRowStrings
            }
            //build one column for each string in it's first row
            ?.forEachIndexed { c, firstRowString ->
                Column(
                    Modifier
                        .widthIn(80.dp)
                        .width(Max)
                ) {
                    //build column items (cells) from amount of rows
                    for(r in 0..<table.rows) {
                        //for the first row
                        if (r == 0)
                            DefaultTableCell(c, 0, firstRowString, Modifier, table, COLUMN_HEADER, readOnly, tableColors)
                        else {
                            //row to make buttons right of cell possible
                            Row(Modifier.weight(1f)) {
                                when {
                                    table.type.varColSetup?.getOrNull(c) != true && r == 1 -> DefaultTableCell(
                                        c, r, firstRowString, Modifier, table,
                                        INPUT.takeUnless { c == 0 } ?: COLUMN_HEADER,
                                        readOnly, tableColors
                                    )
                                    //invisible cell if value should not exist
                                    table.type.varColSetup?.getOrNull(c) != true
                                    || table.getVLV(r, c).isNull()                         -> CellText(
                                        Modifier.fillMaxWidth().partialBorder(
                                            tableColors.border, 1.dp,
                                            top = false,
                                            bottom = r == table.rows - 1,
                                            left = c == 0,
                                            right = c == table.type.columnSetup.size - 3 || readOnly
                                        ),
                                        background = Color.Transparent,
                                        firstRowString = firstRowString,
                                    )
                                    else                                                   -> VarTableCell(
                                        c, r, Modifier.weight(1f), table, readOnly, tableColors, firstRowString = firstRowString,
                                    )
                                }
                                //buttons right of cell
                                if (!readOnly && table.type.varColSetup?.getOrNull(c) == true) {
                                    Box(
                                        Modifier
                                            .sizeIn(lineHeight)
                                            .partialBorder(tableColors.border, 1.dp, top = false, bottom = true, left = false, right = false)
                                    ) {
                                        VerticalDivider(
                                            color = if (table.getVLV(r, c).isNull()) Color.Transparent else colorScheme.outlineVariant,
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(vertical = 2.dp).matchParentSize().align(Alignment.CenterStart)
                                        )
                                        CellText(background = Color.Transparent, minWidth = 0, firstRowString = "")
                                        RowAddButton(
                                            Modifier
                                                .align(Alignment.Center)
                                                .padding(3.dp, 1.dp, 0.dp, 1.dp), table, tableColors, r, c
                                        )
                                    }
                                    Box(
                                        Modifier
                                            .sizeIn(lineHeight + 2.dp)
                                            .partialBorder(tableColors.border, 1.dp, top = false, bottom = true, left = false, right = true)
                                    ) {
                                        CellText(Modifier, background = Color.Transparent, minWidth = 0, firstRowString = "")
                                        DeleteTableOrRow(
                                            Modifier
                                                .align(Alignment.Center)
                                                .padding(0.dp, 1.dp, 1.dp, 1.dp),
                                            table,
                                            tableColors,
                                            r,
                                            c,
                                            enabled = table.VLVlengths.let { (x, y) ->
                                                when(c) {
                                                    table.type.varColSetup?.indexOfFirst { it == true } -> x > 1
                                                    table.type.varColSetup?.indexOfLast { it == true }  -> y > 1
                                                    else                                                -> false
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }//Column
            }//forEachIndexed columnSetup
    }//Row of columns
}

//for variable value length tables with a single value row
@Composable
fun VLSR_Table(
    modifier: Modifier = Modifier,
    table: TableTreeNode,
    readOnly: Boolean,
    tableColors: TableColors,
) {
    //name, isa, etc if needed
    table.type.columnSetup?.let { firstRowStrings ->
        if (firstRowStrings.size > 1) {
            Row(modifier = modifier.testTag("VLSR_TableUpper").height(Min)) {
                firstRowStrings.forEachIndexed { c, firstRowString ->
                    Column(Modifier.width(Min)) {
                        for(r in 0..<2) {
                            val input: TableTextStylePreset =
                                if (r > 0 && c > 0) INPUT else COLUMN_HEADER
                            DefaultTableCell(c, r, firstRowString, Modifier, table, input, readOnly, tableColors)
                        }
                    }
                }
                Spacer(Modifier.fillMaxWidth().fillMaxHeight().partialBorder(tableColors.border, 1.dp, false, false, false, true))
            }
            Spacer(Modifier.fillMaxWidth().height(8.dp).partialBorder(tableColors.border, 1.dp, false, false, true, true))
        }
    }
    //var length part of values as a row
    Row(modifier.testTag("VLSR_TableLower").height(Min)) {
        for(c in -1..<table.VLVlengths.first) {
            VarTableCell(
                c, 0,
                Modifier
                    .thenIf(table.VLVlengths.first < 2 && c == 0) { fillMaxWidth().weight(1f) }
                    .partialBorder(tableColors.border, 1.dp, true, false, false, false),
                table, readOnly, tableColors, firstRowString = "Connected Elements",
            )
            //add/delete buttons
            if (!readOnly && c > -1) {
                Box(
                    Modifier
                        .sizeIn(lineHeight)
                        .partialBorder(tableColors.border, 1.dp, top = true, bottom = true, left = false, right = false)
                ) {
                    VerticalDivider(
                        color = colorScheme.outlineVariant,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 2.dp).matchParentSize().align(Alignment.CenterStart)
                    )
                    CellText(background = Color.Transparent, minWidth = 0, firstRowString = "")
                    RowAddButton(Modifier.align(Alignment.Center).padding(3.dp, 1.dp, 0.dp, 1.dp), table, tableColors, c)
                }
                Box(
                    Modifier
                        .sizeIn(lineHeight + 2.dp)
                        .partialBorder(tableColors.border, 1.dp, top = true, bottom = true, left = false, right = true)
                ) {
                    CellText(Modifier, background = Color.Transparent, minWidth = 0, firstRowString = "")
                    if (c != -1)
                        DeleteTableOrRow(
                            Modifier
                                .align(Alignment.Center).padding(0.dp, 1.dp, 1.dp, 1.dp),
                            table,
                            tableColors,
                            c,
                            enabled = table.VLVlengths.first > 1
                        )
                }
            }
            
        }
    }
}

//for grouped tables
@Composable
fun GroupTable(
    modifier: Modifier = Modifier,
    tableGroup: ReadOnlyStateList<TableTreeNode>,
    readOnly: Boolean,
    tableColors: TableColors,
) {
    val hasFlag = tableGroup.any(TableTreeNode::hasFlags)
    Row(
        modifier
            .height(Min)
            .fillMaxWidth()
    ) {
        //build one column for each string in columnSetup
        tableGroup[0].type.columnSetup?.forEachIndexed { c, firstRowString ->
            Column(
                Modifier
                    .widthIn(80.dp)
                    .width(Max)
            ) {
                //default rows for the first table
                DefaultTableCell(c, 0, firstRowString, Modifier, tableGroup[0], COLUMN_HEADER, readOnly, tableColors)
                //one row per table after that
                tableGroup.forEachIndexed { r, table ->
                    val align = if (hasFlag && c == 0) Alignment.CenterEnd else Alignment.Center
                    val input: TableTextStylePreset =
                        if ((r + 1) > 0 && c > 0) INPUT else COLUMN_HEADER
                    DefaultTableCell(c, (r + 1), firstRowString, Modifier, table, input, readOnly, tableColors, align)
                }
            }
        }//Column
        //add column of buttons if in edit mode
        if (!readOnly) {
            Column(
                Modifier.partialBorder(
                    tableColors.border,
                    1.dp,
                    right = true,
                    left = false,
                    top = false,
                    bottom = true,
                )
            ) {
                for(r in -1..tableGroup.lastIndex) {
                    Row(
                        modifier = Modifier
                            .background(color = defaultTableColorOf(tableGroup[0].type) ?: Color.Transparent)
                    ) {
                        //box with invisible cellText for consistent spacing and size
                        Box(
                            Modifier
                                //+2 to make up for the 1 padding in DeleteTableRow below
                                .widthIn(lineHeight + 2.dp)
                        ) {
                            CellText(background = Color.Transparent, minWidth = 0, firstRowString = "")
                            //not for row 0 (-1 in this case)
                            if (r != -1)
                                SubAddButton(
                                    Modifier
                                        .align(Alignment.Center).padding(1.dp),
                                    tableGroup[r.coerceAtLeast(0)],
                                    tableColors
                                )
                        }
                        Box(
                            Modifier
                                .widthIn(lineHeight + 2.dp)
                        ) {
                            CellText(background = Color.Transparent, minWidth = 0, firstRowString = "")
                            //not for row 0
                            if (r != -1)
                                DeleteTableOrRow(
                                    Modifier
                                        .align(Alignment.Center).padding(1.dp),
                                    tableGroup[r.coerceAtLeast(0)],
                                    tableColors,
                                    r
                                )
                        }
                    }
                }//for
            }//Column of buttons
        }//if(!readOnly)
    }//Row of columns
}