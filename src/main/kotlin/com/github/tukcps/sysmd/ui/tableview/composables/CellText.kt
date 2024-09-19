package com.github.tukcps.sysmd.ui.tableview.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.helper.thenIf
import com.github.tukcps.sysmd.ui.styles.*
import com.github.tukcps.sysmd.ui.tableview.*
import com.github.tukcps.sysmd.ui.tableview.TableTreeNode.Dependencies
import com.github.tukcps.sysmd.ui.tableview.TableTreeNode.Dependencies.*
import com.github.tukcps.sysmd.ui.tableview.TableType.*
import com.github.tukcps.sysmd.ui.tableview.TableType.Companion.varColSetup
import com.github.tukcps.sysmd.ui.tableview.composables.TableTextStylePreset.*


private val lineHeight get() = AppTheme.fixedLineHeight.value.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CellText(
    modifier: Modifier = Modifier,
    startingString: String? = "",
    readOnly: Boolean = true,
    stylePreset: TableTextStylePreset = COLUMN_HEADER,
    table: TableTreeNode? = null,
    index: Int = -1,
    c: Int = -1,
    r: Int = -1,
    color: Color = LocalTextStyle.current.color,
    background: Color = MaterialTheme.colorScheme.background,
    minWidth: Int = 80,
    textAlignment: Alignment = Alignment.Center,
    firstRowString: String,
) {
    //check for subordinate hidden expressions if the value in the last column is empty
    val substituteValueRef =
        if (stylePreset == INPUT
            && startingString.isNullOrBlank()
            && index == table?.values?.lastIndex
            //if the following are both true: table has hidden children and one of them is relevant
            && table.isChildless
            && table.children.any { it.type == ANON_EXP }
        ) {
            table.children.find { it.type == ANON_EXP }
        } else null
    
    //does substitute string if needed
    val string: String =
        if (textAlignment != Alignment.Center)
            startingString?.padStart(10) ?: "          "
        else {
            substituteValueRef?.values?.getOrNull(0)
            ?: startingString.e
        }
    
    //on value change functions
    val onVC: (String) -> Unit =
        when {
            index == -2                 -> { //for multi column VL tables
                { table?.changeVLV(r, c, it) }
            }
            substituteValueRef.isNull() -> { //default
                { table?.changeVal(it, index) }
            }
            else                        -> { //for val substituted tables
                { substituteValueRef?.changeVal(it, 0) }
            }
        }
    
    Box(
        modifier
            //gray background for empty input cells
            .background(
                if (stylePreset == INPUT
                    && index >= 0
                    && string.isEmpty()
                )
                    color.copy(alpha = 0.1f)
                else background
            )
            .padding(start = 4.dp, end = 4.dp, top = 1.dp, bottom = 3.dp)
    ) {
        val dep = Dependencies.mapSymbol(table?.dependencyType.e).takeIf { string == "is a" }
        var tooltipLines by remember { mutableStateOf(1) }
        BasicTextField(
            modifier = Modifier
                .thenIf(stylePreset == INPUT) { testTag("InputCell:$firstRowString") }
                .align(textAlignment)
                .width(
                    when {
                        dep?.component1() == DEFINED_BY  -> 120.dp
                        dep?.component1() == SPECIALIZES -> 140.dp
                        dep?.component1() == SUBSETS     -> 120.dp
                        dep?.component1() == REFERENCES  -> 140.dp
                        dep?.component1() == REDEFINES   -> 130.dp
                        string == "is a"                 -> 40.dp
                        string.length < 10               -> minWidth.dp
                        else                             -> Min
                    }
                )
                .height(Max)
                //allows proper cell selection with tab
                .focusProperties { canFocus = !readOnly },
            readOnly = readOnly || stylePreset != INPUT,
            textStyle = stylePreset.textStyle(color),
            value = if (string == "is a") {
                dep?.let { (_, name, symbol) ->
                    "$name [$symbol]"
                } ?: "is a"
            } else string,
            singleLine = true,
            onValueChange = onVC,
        ) {
            Box() {
                if (!readOnly) {
                    TooltipArea(
                        tooltip = {
                            Surface(
                                modifier = Modifier
                                    .width(Max)
                                    .shadow(4.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                CellTTtext(Modifier, table, firstRowString) { tooltipLines = it }
                            }
                        },
                        delayMillis = 800,
                        tooltipPlacement = TooltipPlacement.ComponentRect(anchor = Alignment.TopEnd, offset = 4.dp X -4.dp, alignment = Alignment.TopEnd),
                    ) {
                        it()
                    }
                } else it()
            }
        }
        
    }
    
}

//following are presets for CellText

@Composable
fun VarTableCell(
    c: Int,
    r: Int,
    modifier: Modifier,
    table: TableTreeNode,
    readOnly: Boolean,
    tableColors: TableColors,
    firstRowString: String,
): Unit =
    when {
        //varlength table with 2 default rows and a single data row
        table.isVLngSnglRow && (table.type.columnSetup?.size ?: 0) > 1 -> {
            CellText(
                firstRowString = firstRowString,
                color = tableColors.textInput, readOnly = readOnly,
                modifier = modifier
                    .thenIf(c > -1) { testTag("Cell:vlsr$c") }
                    .background(tableColors.back)
                    .partialBorder(tableColors.border, 1.dp, left = c == -1, right = readOnly || c == -1, top = false),
                stylePreset = if (c != -1) INPUT else COLUMN_HEADER,
                //strings for first col (in this case -1) are taken from rlName; everything else is from the TVM
                startingString = when {
                    c == -1 -> "Connected Elements"
                    else    -> table.getVLV(row = r, col = c)
                },
                table = table,
                index = -2,
                r = r,
                c = c,
                minWidth = when(table.getVLV(row = r, col = c).e.length) {
                    0, 1, 2, 3 -> 30
                    4          -> 40
                    5          -> 50
                    6          -> 60
                    7          -> 70
                    else       -> 80
                },
            )
        }
        
        //varlength table with just a single data row
        table.isVLngSnglRow                                            -> {
            CellText(
                firstRowString = firstRowString,
                color = tableColors.textInput, readOnly = readOnly,
                modifier = modifier
                    .thenIf(c > -1) { testTag("Cell:vlsr$c") }
                    .background(tableColors.back)
                    .partialBorder(tableColors.border, 1.dp, left = c == -1, right = readOnly || c == -1, top = false),
                stylePreset = if (c != -1) INPUT else COLUMN_HEADER,
                //strings for first col (in this case -1) are taken from rlName; everything else is from the TVM
                startingString = when {
                    c == -1 -> "Connected Elements"
                    else    -> table.values.getOrNull(c)
                },
                table = table,
                index = r,
                minWidth = when(table.getVLV(row = r, col = c).e.length) {
                    0, 1, 2, 3 -> 30
                    4          -> 40
                    5          -> 50
                    6          -> 60
                    7          -> 70
                    else       -> 80
                }
            )
        }
        
        //if this is this a varlength table with multiple data columns and this is a columns that's supposed to be written to
        table.type.varColSetup?.getOrNull(c) == true                   -> {
            CellText(
                color = tableColors.textInput, readOnly = readOnly,
                modifier = modifier
                    .testTag("Cell:vl${r - 1}")
                    .background(tableColors.back)
                    .partialBorder(tableColors.border, 1.dp, left = c == 0, right = readOnly, top = false),
                stylePreset = INPUT,
                //strings for first row and col are taken from columnSetup and rlName; everything else is from the TVM
                startingString = table.getVLV(r, c),
                table = table,
                index = -2,
                r = r,
                c = c,
                firstRowString = firstRowString,
            )
        }
        else                                                           -> Unit
    }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DefaultTableCell(
    c: Int,
    r: Int,
    firstRowString: String,
    modifier: Modifier,
    table: TableTreeNode,
    stylePreset: TableTextStylePreset,
    readOnly: Boolean,
    tableColors: TableColors,
    textAlignment: Alignment = Alignment.Center,
) {
    var hovered by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    fun checkLegal() =
        when(table.type) {
            ATTR_RNG -> ATTR_EXP
            ATTR_EXP -> ATTR_RNG
            INTR_USE -> INTR_USD
            INTR_USD -> INTR_USE
            CONN_USE -> CONN_USD
            CONN_USD -> CONN_USE
            PORT_USE -> PORT_USE
            else     -> null
        }
    
    var legalSubstitutions by remember {
        mutableStateOf(
            checkLegal()
        )
    }
    
    Box(
        modifier = modifier
            .wrapContentSize()
            .partialBorder(
                tableColors.border, 1.dp,
                top = false,
                //left border only for first column of cells
                left = c == 0,
            )
    ) {
        //row to include dropdown arrow if needed
        Row(
            if (!readOnly
                && legalSubstitutions.notNull()
                && c == 0
                && r != 0
                || !readOnly && r == 0 && firstRowString == "is a"
            ) {
                Modifier.clickable { expanded = true }
                    .onPointerEvent(PointerEventType.Enter) { hovered = true }
                    .onPointerEvent(PointerEventType.Exit) { hovered = false }
            } else Modifier
        ) {
            @Suppress("ReplaceNotNullAssertionWithElvisReturn")
            CellText(
                color = if (stylePreset == INPUT) tableColors.textInput else tableColors.text,
                readOnly = readOnly || stylePreset != INPUT,
                modifier = modifier
                    .weight(1f)
                    .background(
                        color = when {
                            stylePreset == INPUT -> tableColors.backInput
                            hovered              -> MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
                            else                 -> tableColors.back
                        }
                    ),
                stylePreset = stylePreset,
                //strings for first row and col are taken from columnSetup and rlName; everything else is from the TVM
                startingString = when {
                    r == 0                                    -> firstRowString
                    c == 0 && table.hasFlags                  -> table.flags + table.type.rlName
                    c == 0                                    -> table.type.rlName
                    //contingency for tables with a "from" "to" scheme to ignore "to" value
                    c == table.values.lastIndex
                    && table.type.columnSetup?.last() == "To" -> table.values.getOrNull(c)
                    else                                      -> table.values.getOrNull(c - 1)
                },
                table = table,
                index = c - 1,
                textAlignment = textAlignment,
                firstRowString = firstRowString,
            )
            if (!readOnly && legalSubstitutions.notNull() && c == 0 && r != 0
                || !readOnly && r == 0 && firstRowString == "is a"
            ) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    null,
                    modifier = Modifier
                        .background(Color.Transparent)
                        .size(lineHeight),
                    tint = tableColors.text
                )
            }
        }
        if (legalSubstitutions.notNull() && c == 0) {
            FirstColumnDD(
                expansionState = expanded,
                closeDropdown = {
                    expanded = false
                    legalSubstitutions = checkLegal()
                },
                table = table,
                getLegalSubstitution = {
                    legalSubstitutions = checkLegal()
                    legalSubstitutions
                },
                tableColors = tableColors,
            )
        } else if (r == 0 && firstRowString == "is a") {
            IsaDD(
                expansionState = expanded,
                closeDropdown = {
                    expanded = false
                    legalSubstitutions = checkLegal()
                },
                table = table,
                tableColors = tableColors,
            )
        }
    }
}

//different styles for text input by user and given text
enum class TableTextStylePreset {
    INPUT,
    TABLE_HEADER,
    COLUMN_HEADER;
    
    @Composable
    fun textStyle(color: Color, textAlign: TextAlign = TextAlign.Center) = when(this) {
        INPUT         -> TextStyle(
            color = color,
            fontSize = AppTheme.fontSize,
            lineHeight = AppTheme.fixedLineHeight,
            fontFamily = Fonts.jetbrainsMono,
            fontStyle = FontStyle.Italic,
            textAlign = textAlign,
        )
        TABLE_HEADER  -> TextStyle(
            color = color,
            fontSize = AppTheme.fontSize,
            lineHeight = AppTheme.fixedLineHeight,
            fontFamily = Fonts.jetbrainsMono,
            fontWeight = FontWeight.ExtraBold,
            textAlign = textAlign,
        )
        COLUMN_HEADER -> TextStyle(
            color = color,
            fontSize = AppTheme.fontSize,
            lineHeight = AppTheme.fixedLineHeight,
            fontFamily = Fonts.jetbrainsMono,
            fontWeight = FontWeight.SemiBold,
            textAlign = textAlign,
        )
    }
}