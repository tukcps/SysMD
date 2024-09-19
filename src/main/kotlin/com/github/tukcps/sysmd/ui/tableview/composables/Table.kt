package com.github.tukcps.sysmd.ui.tableview.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.*
import com.github.tukcps.sysmd.ui.tableview.*
import com.github.tukcps.sysmd.ui.tableview.TableType.Companion.ROWS_DEFAULT
import com.github.tukcps.sysmd.ui.tableview.composables.TableTextStylePreset.*

private val lineHeight get() = AppTheme.fixedLineHeight.value.dp

@Composable
fun Table(
    modifier: Modifier,
    readOnly: Boolean,
    table: TableTreeNode,
    tableColors: TableColors,
    tableGroup: ReadOnlyStateList<TableTreeNode>? = null,
    debug: Boolean = false
) {
    val defaultTableColor = defaultTableColorOf(table.type) ?: Color.Transparent
    //if no columns: dont display table
    if (table.type.columnSetup != null) Column(
        //set table borders
        modifier
            .testTag("Table:${table.type}")
            .width(Min)
            //border around children
            .partialBorder(if (!table.isChildless) tableColors.border else Color.Transparent, 1.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        //debug text
        if (debug) Text(table.toString())
        //header for tables
        Box(
            Modifier
                .height(Min)
                .background(color = defaultTableColor)
        ) {
            CellText(
                modifier = Modifier
                    .fillMaxWidth()
                    .partialBorder(tableColors.border, 1.dp)
                    .align(Alignment.Center),
                color = tableColors.text,
                startingString = (table.type.rlName ?: "NULL_NAME")
                                 + if (tableGroup.notNull()) "s" else "" //plural for grouped tables
                ,
                stylePreset = TABLE_HEADER,
                background = Color.Transparent,
                firstRowString = ""
            )
            if (!readOnly && table.parent.notNull() && tableGroup.isNull()
                || !readOnly && table.parent.notNull() && table.isVarLength
                )
                DeleteTableOrRow(Modifier.align(Alignment.CenterEnd).padding(1.dp), table, tableColors)
        }
        //column: allows bordering of table without subparts
        Column(
            modifier = Modifier
                .width(Max)
                .background(color = defaultTableColor.copy(alpha = (defaultTableColor.alpha * 0.1).toFloat()))
        ) {
            when {
                //special setup for single row tables
                table.isVLngSnglRow -> VLSR_Table(Modifier, table, readOnly, tableColors)
                //for multi row variable length tables
                table.isVarLength   -> VL_Table(Modifier, table, readOnly, tableColors)
                //for grouped tables
                tableGroup.notNull() -> GroupTable(Modifier, tableGroup, readOnly, tableColors)
                //default table as Row of Columns
                else                -> {
                    Row(
                        Modifier
                            .height(Min)
                            .fillMaxWidth()
                    ) {
                        //replace "To" with custom connecting relation if needed
                        table.type.columnSetup
                            .let { firstRowStrings ->
                                if (firstRowStrings.last() != "To")
                                    firstRowStrings
                                else
                                    table.conRel?.let { firstRowStrings.dropLast(1) + (it).replaceFirstChar(Char::uppercase) } ?: firstRowStrings
                            }
                            //build one column for each string in it's first row
                            .forEachIndexed { c, firstRowString ->
                                Column(
                                    Modifier
                                        .widthIn(80.dp)
                                        .width(Max)
                                ) {
                                    //build column items (cells) from amount of rows
                                    for(r in 0..<ROWS_DEFAULT) {
                                        //is this an input cell?
                                        val input: TableTextStylePreset =
                                            if (c != 0 && r != 0) INPUT else COLUMN_HEADER
                                        //the cell itself
                                        DefaultTableCell(c, r, firstRowString, Modifier, table, input, readOnly, tableColors)
                                    }
                                }//Column
                            }//forEachIndexed columnSetup
                        //add column of buttons if in edit mode
                        if (!readOnly) {
                            Column(
                            ) {
                                //not for first row
                                for(r in 0..<ROWS_DEFAULT) {
                                    //box with invisible cellText for consistent spacing and size
                                    Row(
                                        modifier.background(color = defaultTableColorOf(table.type) ?: Color.Transparent)
                                    ) {
                                        Box(
                                            Modifier
                                                //+2 to make up for the 1 padding in DeleteTableRow below
                                                .widthIn(lineHeight + 2.dp)
                                                .partialBorder(
                                                    tableColors.border,
                                                    1.dp,
                                                    right = true,
                                                    left = false,
                                                    top = false,
                                                    bottom = r == ROWS_DEFAULT - 1
                                                )
                                        ) {
                                            CellText(Modifier, background = Color.Transparent, minWidth = 0, firstRowString = "")
                                            //not for row 0
                                            if (r != 0)
                                                SubAddButton(Modifier.align(Alignment.Center).padding(1.dp), table, tableColors)
                                        }
                                        //not for root and not if table does not have more than 2 rows
                                        /*if (table.parent.notNull) {
                                            Box(
                                                Modifier
                                                    .widthIn(lineHeight + 2.dp)
                                                    .partialBorder(
                                                        tableColors.border,
                                                        1.dp,
                                                        left = false,
                                                        top = false,
                                                        bottom = r == ROWS_DEFAULT - 1
                                                    )
                                            ) {
                                                CellText(Modifier, background = Color.Transparent, minWidth = 0)
                                                //not for row 0 and 1
                                                when {
                                                    r > 1  -> DeleteTableOrRow(Modifier.align(Alignment.Center).padding(1.dp), table, tableColors, r)
                                                }
                                            }
                                        }*/
                                    }
                                }//for
                            }//Column of buttons
                        }
                    }//Row of columns
                }//else branch
            }//when
        }//Column
        //recursively display subordinate tables sandwiched between spacers above and below, if there are any
        if (!table.isChildless) {
            Column(Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)) {
                table.childrenInOrder.forEachIndexed { i, childGroup ->
                    //spacer between groups except for the first element
                    if (i != 0)
                        Spacer(Modifier.height(16.dp))
                    //don't generate table for types without column
                    //default call for lone tables
                    if (childGroup.size == 1 && childGroup[0].type.columnSetup != null)
                        Table(Modifier, readOnly, childGroup[0], tableColors, debug = debug)
                    //call for grouped tables
                    if (childGroup.size > 1 && childGroup[0].type.columnSetup != null)
                        Table(Modifier, readOnly, childGroup[0], tableColors, childGroup, debug = debug)
                }
                Spacer(Modifier.height(4.dp))
                if (!readOnly)
                    SubAddButton(Modifier.align(Alignment.CenterHorizontally).padding(1.dp), table, tableColors, Icons.Default.AddCircle)
                else
                    Spacer(Modifier.height(12.dp))
            }
        }
    }//Column
}
