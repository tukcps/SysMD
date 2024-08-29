@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import java.util.*

@Composable
fun NewDigitalTwinDialog(openDialog: MutableState<Boolean>, agilaViewModel: SysMDViewModel){
    val projectID = agilaViewModel.tabsModel.active?.tabTitle?.value !!
    val allElementsOfProject = agilaViewModel.getAllElementsProxy(projectID)
    DialogWindow(onCloseRequest = {openDialog.value = false},
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(400.dp, 500.dp)),
        title = "Create Digital Twin",
        resizable = false,
        content =  {
            val listState = rememberLazyListState()
            Column(modifier = Modifier.fillMaxSize())
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Text(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        textAlign = TextAlign.Center, text = "Creation of a new Digital Twin"
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextField(
                        value = agilaViewModel.chosenNameDigitalTwin.value,
                        placeholder = { Text(text = "Enter Digital Twin name") },
                        leadingIcon = { Icon(Icons.Default.Create, null) },
                        onValueChange = { agilaViewModel.chosenNameDigitalTwin.value = it; },
                        singleLine = true,
                        textStyle = TextStyle(lineHeight = 24.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
                            items(allElementsOfProject) { it ->
                                if (it.language == "SysMD") {
                                    it.body?.let { it1 ->
                                        if (!agilaViewModel.chosenModelsForDigitalTwin.containsKey(it.elementId))
                                            agilaViewModel.chosenModelsForDigitalTwin[it.elementId] =
                                                mutableStateOf(false)
                                        agilaViewModel.chosenModelsForDigitalTwin[it.elementId]?.let { it2 ->
                                            Item(
                                                item = it1,
                                                onItemChecked = {
                                                    agilaViewModel.chosenModelsForDigitalTwin[it]?.value =
                                                        !(agilaViewModel.chosenModelsForDigitalTwin[it]?.value!!)
                                                },
                                                id = it.elementId,
                                                checkState = it2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {

                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = {
                            openDialog.value = false
                        }) { Text("Cancel") }


                    TextButton(
                        modifier = Modifier.padding(start = 15.dp, end = 20.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                        onClick = {
                            openDialog.value = false
                            agilaViewModel.createDigitalTwin()
                        }

                    ) { Text("Create Digital Twin") }
                }
            }
        })
}

@Composable
private fun Item(
    item:String,
    onItemChecked:(id:UUID)->Unit,
    id: UUID,
    checkState:MutableState<Boolean>
){
    Row(modifier = Modifier.clickable { onItemChecked(id)  }.fillMaxWidth()) {
        Checkbox(
            checked = checkState.value,
            modifier = Modifier.align(Alignment.CenterVertically),
            onCheckedChange = {onItemChecked(id) }
        )

        Text(
            text = item
        )

        Spacer(modifier = Modifier.width(8.dp))
    }
}