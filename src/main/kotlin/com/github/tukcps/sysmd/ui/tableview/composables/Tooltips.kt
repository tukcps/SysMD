package com.github.tukcps.sysmd.ui.tableview.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.Fonts
import com.github.tukcps.sysmd.ui.tableview.*


@Composable
fun CellTTtext(
    modifier: Modifier = Modifier,
    table: TableTreeNode?,
    firstRowString: String,
    setLines: (Int) -> Unit,
) {
    table?.let { cellTTmap[firstRowString] }?.let { tooltip ->
        Text(
            text = buildAnnotatedString {
                tooltip(table.type).let {
                    setLines(it.count { char -> char == '\n' } )
                    it.fold("") { acc, char ->
                        when {
                            char in "!\'" && acc.first() == char -> {
                                pop()
                                ""
                            }
                            char == '!'                          -> {
                                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                "!"
                            }
                            char == '\''                         -> {
                                pushStyle(
                                    SpanStyle(
                                        background = Color(0.5f, 0.5f, 0.5f, 0.35f),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = Fonts.jetbrainsMono,
                                        fontStyle = FontStyle.Italic,
                                    )
                                )
                                "'"
                            }
                            char == '\n'                         -> {
                                appendLine()
                                acc + char
                            }
                            char == '|'                          -> {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.background)) { append(char) }
                                acc + char
                            }
                            else                                 -> {
                                append(char)
                                acc + char
                            }
                        }
                    }
                }
            },
            modifier = modifier.padding(5.dp),
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        )
    }
}

//stores default tooltips for cells
private val cellTTmap: Map<String, (TableType) -> String> = mapOf(
    "Name" to { type -> "Name/Identifier of this ${type.rlName}\n  - Required" },
    "is a" to { type -> "Dependency/Definition/Supertype of this ${type.rlName}\n  - Optional\n  - Example: !Name!: ' fork ' !is a!: ' cutlery '  |" },
    "Assertion" to { type -> "Expression to be asserted\n  - Required\n  - Example: ' x > 7 '  |" },
    "Assumption" to { type -> "Expression to be assumed\n  - Required\n  - Example: ' x > 7 '  |" },
    "Requirement" to { type -> "Expression represented by this requirement\n  - Required\n  - Example: ' x > 7 '  |" },
    "Amount" to { type -> "Amount of ${type.rlName}s represented by this row\n  - Optional\n  - accepts either a simple number e.g. ' 17 ' |\n    or a range e.g. ' 0 - 15 ' |\n    with ' 1 - * ' for an open ended range" },
    "Value" to { type -> "Value/Expression represented by this attribute\n  - Optional\n  - Examples: ' -15.8 ', ' x > 7 '  |" },
    "Value Type" to { type -> "Type of value this attribute represents\n  - Optional\n  - Example: ' Integer '  |" },//TODO Required or Optional?
    "Unit" to { type -> "Unit of this attributes value\n  - Optional\n  - Examples: ' m ', ' dB '  |" },
    "Min Value" to { type -> "Lower constraint of this range\n  - Required\n  - Example: ' 3.7 '  |" }, //TODO * allowed?
    "From" to { type -> "Name(s)/Identifier(s) of the element(s) this connection originates from\n  - Required\n  - Using multiple values for !From! or !To! represents a single connection from the whole set of !From!-values to the whole set of !To!-values, NOT multiple row-wise connections" }, //TODO correct?
    "To" to { type -> "Name(s)/Identifier(s) of the element(s) this connection ends at\n  - Required\n  - Using multiple values for !From! or !To! represents a single connection from the whole set of !From!-values to the whole set of !To!-values, NOT multiple row-wise connections" }, //TODO correct?
    "Connected Elements" to { type -> "Names/Identifiers of the elements connected by this ${type.rlName}\n  - Required" },
    "Max Value" to { type -> "Upper constraint of this range\n  - Required\n  - Use ' * ' for range without upper constraint\n  - Examples: ' 52.91 ', ' * '  |" },
)