package com.github.tukcps.sysmd.ui.tableview.composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.services.session.SessionImplementation
import com.github.tukcps.sysmd.ui.styles.getTableColors
import com.github.tukcps.sysmd.ui.tableview.TableViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language.TABLE

@Preview
@Composable
fun TableViewPreview() {
    val tvm = TableViewModel(
        TextualRepresentationViewModel(
            kerMlModel = mutableStateOf(SessionImplementation()),
            refreshTrees = {},
            textualRepresentation = TextualRepresentationImplementation(),
            language = mutableStateOf(TABLE),
            body = mutableStateOf(
                TextFieldValue(
                    """
       package x {
       connection c connect (a,b,c) to d
       }
                    """.trimIndent()
                )
            )
        )
    )
    Box(Modifier.fillMaxSize()) {
        TableView(tvm, modifier = Modifier.align(Alignment.TopStart), readOnly =  false, debug = false)
        TableView(tvm, modifier = Modifier.align(Alignment.BottomEnd), readOnly =  true, debug = false)
    }
}
//TODO attach compiler errors to tables
@Composable
fun TableView(
    tvm: TableViewModel,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
    debug: Boolean = false
) {
    val horizontalState = rememberScrollState(0)
    
    //colors
    val tableColors = getTableColors()
    
    tvm.setVisible
    //box for scrollbar
    Box(modifier.testTag("TableView")) {
        //column to allow spacer for scrollbar below content column
        Column {
            //Column containing all actual content
            Column(
                modifier
                    /*.then(
                    //border around package content
                    if (!tvm.tableTreeRoot.isChildless)
                        Modifier
                            .width(Max)
                    //allows workaround borders for import row delete buttons to show properly when there are no elements but package and imports yet
                    else Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                )*/
                    .horizontalScroll(horizontalState)
            ) {
                //package
                Table(modifier, readOnly, tvm.tableTreeRoot, tableColors, debug = debug)
                if (readOnly) Spacer(modifier.height(16.dp))
            }
            //spacer for scrollbar
            Spacer(Modifier.height(8.dp))
        }
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(horizontalState),
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}