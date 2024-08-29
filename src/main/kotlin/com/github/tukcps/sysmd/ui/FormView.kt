package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.viewmodel.FormViewModel

// Displays a Row/Col table to get and show the data in Excel like format

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@Composable
fun FormView(
    body: MutableState<TextFieldValue>,
    readOnly: Boolean = false,
    elementEdited: MutableState<Boolean> = mutableStateOf(false)
) {
    //Default number of Rows and Cols
    var rows = 5
    var cols = 2
    // Whether a column is enabled or not
    val maskList = ArrayList<Boolean>()

    val boxWidth = 150.dp
    val boxHeight = 40.dp

    var formBody = body.value.text
    if (formBody.isNotEmpty()) {
        //To make sure whether the body has form data or not
        if (formBody.startsWith("Form")) {
            try {

                val parts = formBody.split(FormViewModel.getFormRowDelimiter())
                val rNC = parts[0].replace("SysMDForm", "").split("X")
                rows = Integer.parseInt(rNC[0].trim())
                cols = Integer.parseInt(rNC[1].trim())

                parts[2].split(FormViewModel.getFormColDelimiter()).forEach {
                    val parsedBool = it.toBoolean()
                    maskList.add(parsedBool)
                }

//                println(parts);
                val bodyString = StringBuilder()
                parts.forEachIndexed { index, s ->
//                    println(index)
//                    println(s)
                    if (index > 3) {
                        bodyString.append(s + FormViewModel.getFormRowDelimiter())
                    }
//                    println(bodyString)
                }
                formBody = bodyString.toString()

            } catch (e: Exception) {
                formBody = ""
            }
        }
    }
    val formViewModel = remember { mutableStateOf(FormViewModel(rows, cols, formBody, maskList)) }
    val rowsField = remember { mutableStateOf(rows.toString()) }
    val colsField = remember { mutableStateOf(cols.toString()) }
    body.value = TextFieldValue(formViewModel.value.getBodyString())
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
    ) {
        Column {

            if (!readOnly) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Rows:",
                        style = TextStyle(
                            fontSize = 12.sp,
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    //TextField to edit number of rows
                    BasicTextField(
                        rowsField.value,
                        onValueChange = {
                            if (it.isNotEmpty()) {
                                try {
                                    rows = it.toInt()
                                    formViewModel.value.updateRowsAndCols(rows, cols)
                                    elementEdited.value = true
                                } catch (_: Exception) {

                                }
                            }
                            rowsField.value = it
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        "x",
                        style = TextStyle(
                            fontSize = 12.sp,
                        ),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Cols:",
                        style = TextStyle(
                            fontSize = 12.sp,
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    //Textfield to edit number of cols
                    BasicTextField(
                        colsField.value,
                        onValueChange = {
                            if (it.isNotEmpty()) {
                                try {
                                    cols = it.toInt()
                                    formViewModel.value.updateRowsAndCols(rows, cols)
                                    elementEdited.value = true
                                } catch (_: Exception) {

                                }
                            }
                            colsField.value = it
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.width(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.horizontalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Hide rows and cols edit when read only
                if (!readOnly) {
                    Row {
                        formViewModel.value.formColMaskList.forEachIndexed { index, mask ->
                            Box(
                                modifier = Modifier
                                    .size(boxWidth, boxHeight)
                                    .border(BorderStroke(0.2.dp, color = Color.Gray))
//                                .padding(10.dp)
                                    .background(Color.White)
                            ) {
                                val cellEditable = remember { mutableStateOf(mask) }
                                Checkbox(
                                    checked = cellEditable.value,
                                    onCheckedChange = {
                                        cellEditable.value = it
                                        formViewModel.value.formColMaskList[index] = it
                                        elementEdited.value = true
                                    }
                                )
                            }
                        }
                    }
                }
                formViewModel.value.formRows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.cells.forEach { cell ->
                            val isEnabled = !readOnly || cell.editable
                            val cellText = remember { mutableStateOf(cell.value) }
                            BasicTextField(
                                cellText.value,
                                enabled = isEnabled,
                                onValueChange = {
                                    cellText.value = it
                                    cell.value = it
                                    elementEdited.value = true
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 18.sp
                                ),
                                modifier = Modifier
                                    .size(boxWidth, boxHeight)
                                    .border(BorderStroke(0.2.dp, color = Color.Gray))
                                    .background(if (isEnabled)  Color.White else Color.LightGray)
                                    .padding(10.dp)
                            )
                        }

                    }
                }

            }
        }

    }

}