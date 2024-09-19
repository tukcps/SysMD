package ui.tableview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.services.session.SessionImplementation
import com.github.tukcps.sysmd.ui.styles.LightColors
import com.github.tukcps.sysmd.ui.tableview.TableType.PACKAGE
import com.github.tukcps.sysmd.ui.tableview.TableViewModel
import com.github.tukcps.sysmd.ui.tableview.composables.TableView
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language.TABLE
import kotlin.test.Ignore
import kotlin.test.Test

class TableViewTest {
    
    @OptIn(ExperimentalTestApi::class)
    @Test @Ignore("causes error when executed by gitlab pipeline, but works when tested locally - maybe because it is run with 'kotlin.test.Test' instead of 'org.junit.jupiter.api.Test' ?")
    fun uiTest() = runComposeUiTest {
        setContent {
            val tvm = TableViewModel(
                TextualRepresentationViewModel(
                    kerMlModel = mutableStateOf(SessionImplementation()),
                    refreshTrees = {},
                    textualRepresentation = TextualRepresentationImplementation(),
                    language = mutableStateOf(TABLE),
                    body = mutableStateOf(
                        TextFieldValue(
                            ""
                        )
                    )
                )
            )
            MaterialTheme(colorScheme = LightColors) {
                TableView(tvm, readOnly = false)
            }
        }
        /*
        * known compose node tags:
        *   Table:${table.type}
        *   DeleteTableOrRow
        *   DeleteTableAlert
        *   RowAddButton
        *   InputCell:$firstRowString
        *   Cell:vlsr$c
        *   Cell:vl${r - 1}
        *   SubAddDD
        *   UnrecommendedDD
        *   FirstColumnDD
        *   DDitem:setType.$childType
        *   IsaDD
        *   VL_Table
        *   VLSR_TableUpper
        *   VLSR_TableLower
        *   TableView
        * */
        onNodeWithTag("Table:$PACKAGE")
            //a new table should display a package table with an input cell for its name
            .onChildWithTag("InputCell:Name").assertIsDisplayed()
            //click to focus it for text entry
            .performClick().assertIsFocused()
        
        //after using performTextInput, any attempt to evaluate any nodes (including: fetchSemanticNode, asserts, print) causes an endless loop
        //enter text
        //onNodeWithTag("InputCell:Name").performTextInput("pck1")
        //onNodeWithTag("TableView").printToLog("TableView")
    }
    
    
}

fun SemanticsNodeInteraction.onChildWithTag(testTag: String): SemanticsNodeInteraction {
    return this.onChildren().filterToOne(hasTestTag(testTag = testTag))
}

