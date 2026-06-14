package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.github.tukcps.sysmd.exports.Exporter
import com.github.tukcps.sysmd.exports.Requirement
import com.github.tukcps.sysmd.exports.UcbDataPack
import com.github.tukcps.sysmd.exports.systemCElements.Channel
import com.github.tukcps.sysmd.exports.systemCElements.ChannelType
import com.github.tukcps.sysmd.exports.systemCElements.Module
import com.github.tukcps.sysmd.exports.systemCElements.ModuleType
import com.github.tukcps.sysmd.generated.resources.Res
import com.github.tukcps.sysmd.generated.resources.logo
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.settingsViewModel
import org.jetbrains.compose.resources.painterResource
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

/**Name of the Folder in which all generated SystemC files should be placed*/
private const val exportFolderName = "systemC_Exports"

/**The Folder in which the library of existing Test-Benches is located*/
private val referenceTestBenchFolder = Paths.get("").toAbsolutePath().toString() + "/src/test/resources/toSystemC/TestBenches"
@Composable
fun UserControlBoard(ucbData: UcbDataPack, sysCExporter: Exporter, openDialog: MutableState<Boolean>, openUCB: MutableState<Boolean>){

    //For Modules
    val checkedStateAllDisc = remember { mutableStateOf(false) }
    val checkedStateAllTDF = remember { mutableStateOf(false) }

    //For Channels
    val checkedStateAllTrace = remember { mutableStateOf(false) }
    val checkedStateNoTrace = remember { mutableStateOf(false) }

    //For Macros
    val checkedStateAllMacros = remember { mutableStateOf(false) }
    val checkedStateNoMacros = remember { mutableStateOf(false) }

    //For Requirements
    val checkedStateAllRequirements = remember { mutableStateOf(false) }
    val checkedStateNoRequirements = remember { mutableStateOf(false) }

    DialogWindow(
        onCloseRequest = { openDialog.value = false; openUCB.value = false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(1280.dp, 800.dp)),
        title = "User Control Board",
        resizable = true,
        icon = painterResource(Res.drawable.logo)
    ) {
        Box(
            modifier = Modifier.background(AppTheme.colors.backgroundLight)
                .fillMaxSize()
        ) {

            val stateVerticalModuleBox = rememberScrollState(0)
            val stateVerticalChannelBox = rememberScrollState(0)
            val stateVerticalRequirementsBox = rememberScrollState(0)
            val stateVerticalMacrosBox = rememberScrollState(0)

            Column(modifier = Modifier.fillMaxSize()){
                    Row(modifier = Modifier.fillMaxHeight(0.45f).fillMaxWidth()){
                        //Module Type Selection

                            Column(modifier = Modifier.fillMaxWidth(0.33f).fillMaxHeight()){

                                TitleBox("Select module type")

                                ClickerBoxModules(checkedStateAllDisc, checkedStateAllTDF)

                                Box( modifier = Modifier.fillMaxSize()){
                                    Column(modifier = Modifier
                                        .verticalScroll(stateVerticalModuleBox)
                                    ) {
                                        ucbData.modules.forEach { mod ->
                                            Spacer(modifier = Modifier.width(100.dp))
                                            ModuleTextBox(mod, checkedStateAllDisc, checkedStateAllTDF)
                                        }
                                    }
                                    VerticalScrollbar(
                                        modifier = Modifier.align(Alignment.CenterEnd),
                                        adapter = rememberScrollbarAdapter(stateVerticalModuleBox)
                                    )
                                }

                            }



                            Column(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight()) {

                                TitleBox("Select signals to trace")

                                ClickerBoxChannels(checkedStateAllTrace, checkedStateNoTrace)

                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .verticalScroll(stateVerticalChannelBox)
                                    ) {
                                        ucbData.channels.forEach { ch ->
                                            if(ch.channelType == ChannelType.PRIMITIVE) {
                                                Spacer(modifier = Modifier.width(100.dp))
                                                ChannelTextBox(ch, checkedStateAllTrace, checkedStateNoTrace)
                                            }
                                        }
                                    }
                                    VerticalScrollbar(
                                        modifier = Modifier.align(Alignment.CenterEnd),
                                        adapter = rememberScrollbarAdapter(stateVerticalChannelBox)
                                    )
                                }

                            }



                            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {

                                TitleBox("Select TDF Functions")

                                ClickerBoxMacros(checkedStateAllMacros, checkedStateNoMacros)

                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .verticalScroll(stateVerticalMacrosBox)
                                    ) {
                                        MacroTextBox("set_attributes", ucbData.macros, 0, checkedStateAllMacros,checkedStateNoMacros)
                                        MacroTextBox("change_attributes", ucbData.macros, 1, checkedStateAllMacros,checkedStateNoMacros)
                                        MacroTextBox("initialize", ucbData.macros, 2, checkedStateAllMacros,checkedStateNoMacros)
                                        MacroTextBox("reinitialize", ucbData.macros, 3, checkedStateAllMacros,checkedStateNoMacros)
                                        MacroTextBoxAlwaysEnabled("processing", ucbData.macros, 4)
                                        MacroTextBox("ac_processing", ucbData.macros, 5, checkedStateAllMacros,checkedStateNoMacros)
                                    }
                                    VerticalScrollbar(
                                        modifier = Modifier.align(Alignment.CenterEnd),
                                        adapter = rememberScrollbarAdapter(stateVerticalMacrosBox)
                                    )
                                }

                            }
                    }//Row End



                Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {

                    TitleBox("Select Requirements for which Test-Benches should be instantiated")

                    ClickerBoxRequirements(checkedStateAllRequirements, checkedStateNoRequirements)

                    Box(modifier = Modifier) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(stateVerticalRequirementsBox)
                        ) {

                            ucbData.requirements.forEach { requirement ->

                                Spacer(modifier = Modifier.width(150.dp))
                                RequiremenTextBox(requirement, existsTestBench(requirement), checkedStateAllRequirements, checkedStateNoRequirements)

                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            adapter = rememberScrollbarAdapter(stateVerticalChannelBox)
                        )
                    }

                }


                //Contains the Export Button
                ExportBox(sysCExporter,openDialog,openUCB)

            }//BigColumnScope End
        }
    }
}




@Composable
fun TitleBox(text: String) {
    Box(
        modifier = Modifier
            .height(35.dp)
            .fillMaxWidth()
            .background(Color(150,150,150, 100)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}


@Composable
fun ModuleTextBox(module: Module, checkedStateDisc: MutableState<Boolean>, checkedStateTDF: MutableState<Boolean>) {
    Box(
        modifier = Modifier
            .height(35.dp)
            .padding(start = 10.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {

        Row(
            modifier = Modifier.padding(5.dp),
        ) {
            Text(text = module.moduleName, modifier = Modifier.fillMaxWidth(0.45f))
            RadioButtonsModuleType(module,checkedStateDisc,checkedStateTDF)
        }
    }
}

@Composable
fun RadioButtonsModuleType(module : Module, checkedStateDisc: MutableState<Boolean>, checkedStateTDF: MutableState<Boolean>) {
    val radioOptions = if(module.subModules.isEmpty()) listOf("Discrete", "TDF") else listOf("Discrete")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0] ) }

    if(module.subModules.isEmpty()){
        if(checkedStateDisc.value) {onOptionSelected("Discrete"); module.moduleType = ModuleType.Discrete }
        if(checkedStateTDF.value) {onOptionSelected("TDF"); module.moduleType = ModuleType.TDF }
    }

    if (selectedOption == "Discrete") { module.moduleType = ModuleType.Discrete; println("module ${module.moduleName} is now DISC") }
    if (selectedOption == "TDF") { module.moduleType = ModuleType.TDF; println("module ${module.moduleName} is now TDF") }


        radioOptions.forEach { text ->
                RadioButton(
                    selected = if (module.subModules.isEmpty()) (text == selectedOption) else true,
                    onClick = {
                        onOptionSelected(text)
                        checkedStateDisc.value = false
                        checkedStateTDF.value = false
                    }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.merge(),
                    modifier = Modifier.padding(start = 2.dp)
                )

        }

}

@Composable
fun ChannelTextBox(ch: Channel, checkedStateAllTrace: MutableState<Boolean>, checkedStateNoTrace: MutableState<Boolean>) {

    val checkedState = remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .height(35.dp)
            .padding(start = 10.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {


        Row(
            modifier = Modifier.padding(5.dp),
        ) {
            Text(text = ch.channelName, modifier = Modifier.fillMaxWidth(.7f))
            Checkbox(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it
                                    checkedStateAllTrace.value = false
                                    checkedStateNoTrace.value = false}
            )

            if(checkedStateAllTrace.value) checkedState.value = true
            if(checkedStateNoTrace.value) checkedState.value = false
            ch.trace = checkedState.value
            println("${ch.channelName} ${ch.trace}")
        }
    }
}

@Composable
fun MacroTextBox(macroName: String, macros: BooleanArray, pos : Int, checkedStateAllMacros: MutableState<Boolean>, checkedStateNoMacros: MutableState<Boolean>) {

    val checkedState = remember { mutableStateOf(true) }

    Row(
        modifier = Modifier.padding(start = 10.dp).height(35.dp),
    ) {
        Text(text = macroName, modifier = Modifier.fillMaxWidth(0.5f))
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = { checkedState.value = it
                                checkedStateNoMacros.value = false
                                checkedStateAllMacros.value = false}
        )
        if(checkedStateAllMacros.value) checkedState.value = true
        if(checkedStateNoMacros.value) checkedState.value = false
        macros[pos] = checkedState.value
        println(checkedState.value)
    }

}

@Composable
fun RequiremenTextBox(
    requirement: Requirement,
    hasTestBench: Boolean,
    checkedStateAllRequirements: MutableState<Boolean>,
    checkedStateNoRequirements: MutableState<Boolean>
) {

    val checkedState = remember { mutableStateOf(true) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(AppTheme.colors.backgroundMedium.copy(0.5F,0.9F,0.9F,0.9F))
    ) {

        Row(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(modifier = Modifier.width(300.dp)) {
                Text(text = requirement.requirementName)
            }


            Checkbox(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it
                    checkedStateAllRequirements.value = false
                    checkedStateNoRequirements.value = false}
            )

            if(!hasTestBench) {
                Text(text = "No Test-Bench found - Will generate Test-Bench Template.", color = Color.DarkGray)
            } else {
                Text(text = "Test-Bench found!", color = AppTheme.colors.iconGreen)
            }

            if(checkedStateAllRequirements.value) checkedState.value = true
            if(checkedStateNoRequirements.value) checkedState.value = false
            requirement.generateTB = checkedState.value
            println("$requirement ${requirement.generateTB}")
        }

        //Text(requirement.requirementName, modifier = Modifier.padding(40.dp, 10.dp ,10.dp ,10.dp))

    }
}

/**Checks if a Test-Bench file is found for this Requirement.
 * It returns the Boolean value which is also directly applied to the existsTB variable of the Requirement.
 * @param requirement The Requirement for which the Test-Bench should be searched.
 * @return A Boolean that tells if a Test-Bench file (.cpp) is found for this requirement.
 */
private fun existsTestBench(requirement: Requirement): Boolean {
    val pathToTestBenchFolder = Path("$referenceTestBenchFolder/${requirement.requirementName}_TB.cpp")
    return Files.exists(pathToTestBenchFolder).also { requirement.existsTB = it }
}

/**
 * Special Text Box for the processing() macro which MUST NOT be disabled!
 */
@Composable
fun MacroTextBoxAlwaysEnabled(macroName: String, macros: BooleanArray, pos : Int) {

    val checkedState = remember { mutableStateOf(true) }


    Row(
        modifier = Modifier.padding(start = 10.dp).height(35.dp)
    ) {
        Text(text = macroName, modifier = Modifier.fillMaxWidth(0.5f))
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = { checkedState.value = true}
        )

        macros[pos] = true
        println(checkedState.value)

        Spacer(modifier = Modifier.width(5.dp))
        Text(text = "(Must be enabled)", fontSize = 10.sp, textAlign = TextAlign.Left)

    }

}



@Composable
fun ClickerBoxModules(checkedStateDisc: MutableState<Boolean>, checkedStateTDF: MutableState<Boolean>) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Color(200,200,200, 100)),
        contentAlignment = Alignment.Center
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checkedStateDisc.value,
                onCheckedChange = { checkedStateDisc.value = it; if(checkedStateDisc.value){checkedStateTDF.value = false} }
            )
            Text(
                text = "All Discrete",
            )

            Spacer(modifier = Modifier.width(30.dp))

            Checkbox(
                checked = checkedStateTDF.value,
                onCheckedChange = { checkedStateTDF.value = it; if(checkedStateTDF.value){checkedStateDisc.value = false} }
            )
            Text(
                text = "All TDF",
            )
        }
    }
}

@Composable
fun ClickerBoxChannels(checkedStateAllTrace: MutableState<Boolean>, checkedStateNoTrace: MutableState<Boolean>) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Color(200,200,200, 100)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checkedStateAllTrace.value,
                onCheckedChange = { checkedStateAllTrace.value = it; if(checkedStateAllTrace.value) checkedStateNoTrace.value = false }
            )
            Text(
                text = "Select all",
            )
            Checkbox(
                checked = checkedStateNoTrace.value,
                onCheckedChange = { checkedStateNoTrace.value = it; if(checkedStateNoTrace.value) checkedStateAllTrace.value = false }
            )
            Text(
                text = "Unselect all",
            )
        }
    }
}

@Composable
fun ClickerBoxMacros(checkedStateAllMacros: MutableState<Boolean>, checkedStateNoMacros : MutableState<Boolean>) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Color(200,200,200, 100)),
        contentAlignment = Alignment.Center
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checkedStateAllMacros.value,
                onCheckedChange = { checkedStateAllMacros.value = it; if(checkedStateAllMacros.value){checkedStateNoMacros.value = false} }
            )
            Text(
                text = "Select all",
            )

            Spacer(modifier = Modifier.width(30.dp))

            Checkbox(
                checked = checkedStateNoMacros.value,
                onCheckedChange = { checkedStateNoMacros.value = it; if(checkedStateNoMacros.value){checkedStateAllMacros.value = false} }
            )
            Text(
                text = "Unselect all",
            )
        }
    }
}

@Composable
fun ClickerBoxRequirements(checkedStateAllRequirements: MutableState<Boolean>, checkedStateNoRequirements : MutableState<Boolean>) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Color(200,200,200, 100)),
        contentAlignment = Alignment.Center
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checkedStateAllRequirements.value,
                onCheckedChange = { checkedStateAllRequirements.value = it; if(checkedStateAllRequirements.value){checkedStateNoRequirements.value = false} }
            )
            Text(
                text = "Select all",
            )

            Spacer(modifier = Modifier.width(30.dp))

            Checkbox(
                checked = checkedStateNoRequirements.value,
                onCheckedChange = { checkedStateNoRequirements.value = it; if(checkedStateNoRequirements.value){checkedStateAllRequirements.value = false} }
            )
            Text(
                text = "Unselect all",
            )
        }
    }
}


@Composable
fun ExportBox(
    exporter: Exporter,
    openDialog: MutableState<Boolean>,
    openUCB: MutableState<Boolean>,
) {

    val exception : MutableState<Exception?> = remember { mutableStateOf(null) }
    val errorMessage = remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxHeight()
            .fillMaxWidth()
            .padding(start = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = {
                try {
                    exporter.toSystemC(pathIn = settingsViewModel.systemCExportFolder.value, tbLibFolder = referenceTestBenchFolder)
                }
                catch (e : SecurityException){
                    println("SecurityException: Problem accessing directory: " + e.message.toString())
                    errorMessage.value = "SecurityException: Problem accessing directory: " + e.message.toString()
                    exception.value = e
                }
                catch (e : IOException){
                    println("IOException: The directory was not found or could not be generated: " + e.message.toString())
                    errorMessage.value = "The directory was not found or could not be generated: " + e.message.toString()
                    exception.value = e
                }

                if(exception.value == null){
                    openDialog.value = false
                    openUCB.value = false
                }
            }
        ){
            Text(text = "Generate templates")
        }

        if (exception.value != null){
            displayErrorDialog("Failed accessing directory", exception, errorMessage, openDialog, openUCB)
        }
    }

}

@Composable
fun displayErrorDialog(
    title: String,
    exception: MutableState<Exception?>,
    message: MutableState<String>,
    openDialog: MutableState<Boolean>,
    openUCB: MutableState<Boolean>
) {

    DialogWindow(
        onCloseRequest = {
            exception.value = null
            openDialog.value = false
            openUCB.value = false
         },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(500.dp, 210.dp)),
        title = title,
        resizable = false,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            )
            {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = AppTheme.colors.iconRed,
                    modifier = Modifier.size(60.dp).padding(start = 10.dp)
                )
                Text(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp),
                    textAlign = TextAlign.Center, text = message.value
                )
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            )
            {
                TextButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                    border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                    onClick = { exception.value = null; openDialog.value = false; openUCB.value = false })
                {
                    Text("Ok")
                }
            }
        }

    }
}
