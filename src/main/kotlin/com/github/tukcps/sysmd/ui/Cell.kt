@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.composables.SysMDTooltipArea
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.InternalRefReference
import com.github.tukcps.sysmd.ui.viewmodel.MyIcons
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language.*
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.compilableLanguages


/**
 *  Displays a single notebook cell and dispatches the cell to editor or other render methods.
 *  An element has an index that must be selected to be editable, and a description, code, and info section.
 */
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@Composable
fun Cell(
    model: TextualRepresentationViewModel,
    index: Int,
    selectedIndex: MutableState<Int>,
    selectedItem: MutableState<Boolean>,
    showInfo: MutableState<Boolean>,
    collapsedElementIds: MutableMap<Int, Boolean>,
    hiddenElementIds: MutableMap<Int, Boolean>,
    onDeleteRequest: (Int) -> Unit,
    onMoveRequest: (Int, MoveRequest) -> Unit,
    cellWasChanged: MutableState<Boolean>,
    internalRefReference: InternalRefReference,
    enableElementListScrolling: MutableState<Boolean>,
) {
    val collapsed = mutableStateOf(collapsedElementIds[index] == true)
    val hidden = mutableStateOf(hiddenElementIds[index] == true)

    fun isSelected(): Boolean = (index == selectedIndex.value && selectedItem.value)

    fun onLanguageChange() {}

    // Lambda that is called upon edit icon
    val changeEditStatusDescription = {
        val changedIndex = selectedIndex.value != index
        selectedIndex.value = index
        selectedItem.value = if (changedIndex) true else !selectedItem.value
        collapsedElementIds[index] = false
        if (!selectedItem.value)
            internalRefReference.updateTOC()
    }
    
    val onCollapseExpand = {
        selectedIndex.value = -1
        selectedItem.value = false
        collapsed.value = !collapsed.value
        collapsedElementIds[index] = collapsed.value
    }
    
    val density = LocalDensity.current
    var mainRowWidth by remember { mutableStateOf(0.dp) }//Holds the Width of the very top Row of this Composable
    
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
        if (!collapsed.value && !hidden.value) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // The body part of the element
                Row(
                    // the whole row is clickable; double click selects/deselects it.
                    modifier = Modifier
                        .pointerInput(changeEditStatusDescription) {
                            detectTapGestures(onDoubleTap = { changeEditStatusDescription() })
                        }
                        .hoverable(interactionSource = interactionSource, true)
                        .background(
                            when {
                                isSelected() -> MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
                                hovered      -> MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
                                else         -> MaterialTheme.colorScheme.background
                            }
                        )
                ) {
                    Column {
                        // Pencil ... edit the element
                        SysMDTooltipArea(tooltipText = "Enable/Disable editing of the cell") {
                            IconButton(
                                modifier = Modifier.height(18.dp).width(18.dp).padding(1.dp),
                                onClick = changeEditStatusDescription
                            ) {
                                Icon(
                                    MyIcons.ModeEditOutline,
                                    "Edit the cell",
                                    tint = if (index == selectedIndex.value && selectedItem.value)
                                        AppTheme.colors.iconGreen else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        
                        // Icon to select display of the optional info section.
                        if (model.language.value in compilableLanguages) {
                            SysMDTooltipArea(tooltipText = "Display/Hide additional information section\nNote: If 'Analyze' was never clicked this might be empty") {
                                IconButton(
                                    modifier = Modifier.height(18.dp).width(18.dp).padding(1.dp),
                                    onClick = { showInfo.value = !showInfo.value }) {
                                    Icon(
                                        modifier = Modifier.height(16.dp),
                                        imageVector = MyIcons.Info,
                                        contentDescription = "",
                                        tint = if (showInfo.value) AppTheme.colors.iconYellow else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                    Column(Modifier.width(18.dp)) {
                        // + or - for collapsing the cell.
                        if (model.language.value != YAML)
                            SysMDTooltipArea(tooltipText = "Collapse / Expand this cell") {
                                IconButton(
                                    modifier = Modifier.width(18.dp).height(18.dp).padding(1.dp),
                                    onClick = onCollapseExpand
                                ) {
                                    Icon(
                                        imageVector = if (collapsed.value) MyIcons.Add else MyIcons.Remove,
                                        contentDescription = if (collapsed.value) "Expand this part" else "Collapse this part",
                                        tint = if (index == selectedIndex.value && selectedItem.value) Color.Black else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        if (model.language.value in compilableLanguages) {
                            SysMDTooltipArea(tooltipText = "Compile and solve this cell") {
                                IconButton(
                                    modifier = Modifier.height(18.dp).width(18.dp).padding(1.dp),
                                    onClick = model.onCompile
                                ) {
                                    Icon(
                                        Icons.Filled.Calculate,
                                        "Compile and solve",
                                        tint = if (index == selectedIndex.value && selectedItem.value)
                                            AppTheme.colors.iconGreen else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                        // Trash ... delete the cell
                        SysMDTooltipArea(tooltipText = "Delete this cell") {
                            IconButton(
                                modifier = Modifier.height(18.dp).width(18.dp).padding(1.dp),
                                onClick = {
                                    onDeleteRequest(index)
                                    cellWasChanged.value = true
                                }
                            ) {
                                Icon(
                                    MyIcons.DeleteForever,
                                    "Delete the cell",
                                    tint = if (index == selectedIndex.value && selectedItem.value)
                                        Color.Red else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                    
                    Column(
                        Modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        // 2nd column with the headline menu only in selected mode:
                        // Spacer(modifier = Modifier.width(18.dp))
                        if (index == selectedIndex.value && selectedItem.value) {
                            Column {
                                LanguageDropdown(model.language, model.namespace, model.bodyState, ::onLanguageChange)

                                Row(modifier = Modifier.onGloballyPositioned { coordinates ->
                                    mainRowWidth = with(density) { coordinates.size.width.toDp() }
                                })
                                {
                                    Editor(
                                        mainRowWidth,
                                        model.bodyState,
                                        model.annotations,
                                        model.resultsAnnotations,
                                        readOnly = false,
                                        useHighlighting = model.language.value in compilableLanguages,
                                        cellWasChanged,
                                        enableElementListScrolling
                                    )
                                }
                            }
                        } else {
                            if (model.sessionState.value.project?.directory != null)
                            when(model.language.value) {
                                YAML -> Frontmatter(model.tabViewModel.tabsViewModel, model.bodyState)
                                in setOf(KerML, SYS_MD, SYS_ML) -> {
                                    Column {
                                        Row(Modifier.background(MaterialTheme.colorScheme.background)
                                                .fillMaxWidth()
                                                .onGloballyPositioned { coordinates ->
                                                    mainRowWidth = with(density) { coordinates.size.width.toDp() }
                                                }) {
                                            if ( (model.language.value == SYS_MD || model.language.value == SYS_ML)
                                                && model.namespace.value !in setOf("Global", "")
                                            )
                                                Text(
                                                    " package ${model.namespace.value} owns ",
                                                    fontSize = 12.sp,
                                                    lineHeight = 14.sp
                                                )
                                        }
                                        Editor(
                                            mainRowWidth,
                                            model.bodyState,
                                            model.annotations,
                                            model.resultsAnnotations,
                                            readOnly = true,
                                            useHighlighting = model.language.value in compilableLanguages,
                                            cellWasChanged,
                                            enableElementListScrolling
                                        )
                                    }
                                }
                                //else if (model.language.value == MARKDOWN)
                                else -> {
                                    Column(Modifier.padding(start = 6.dp)) {
                                        Markdown(
                                            model.tabViewModel.tabsViewModel,
                                            model.body.text,
                                            internalRefReference
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Display annotations if selected
                if (model.language.value in compilableLanguages)
                    AnnotationsView(showInfo, model)
            }
        } else {
            if (collapsed.value && !hidden.value) {
                // the section is collapsed but not hidden.
                Column(modifier = Modifier.fillMaxWidth(0.97f)) {

                    // The body part of the cell
                    Row(// whole row; double click selects/deselects it.
                        modifier = Modifier
                            .pointerInput(changeEditStatusDescription) {
                                detectTapGestures(onDoubleTap = { changeEditStatusDescription() })
                            }
                            .hoverable(interactionSource = interactionSource, true)
                            .background(if (hovered) MaterialTheme.colorScheme.primaryContainer.copy(0.5f) else MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                    ) {
                        Column {
                            // Pencil ... edit the cell
                            SysMDTooltipArea(tooltipText = "Enable/Disable editing of the cell") {
                                IconButton(
                                    modifier = Modifier.height(18.dp).width(18.dp).padding(1.dp),
                                    onClick = changeEditStatusDescription
                                ) {
                                    Icon(
                                        MyIcons.ModeEditOutline,
                                        "Edit the cell",
                                        tint = if (index == selectedIndex.value && selectedItem.value)
                                            AppTheme.colors.iconGreen else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                        Column {
                            // + or - for collapsing the cell.
                            SysMDTooltipArea(tooltipText = "Collapse / Expand this cell") {
                                IconButton(
                                    modifier = Modifier.width(18.dp).height(18.dp).padding(1.dp),
                                    onClick = onCollapseExpand
                                ) {
                                    Icon(
                                        imageVector = if (collapsed.value) MyIcons.Add else MyIcons.Remove,
                                        contentDescription = if (collapsed.value) "Expand this part" else "Collapse this part",
                                        tint = if (index == selectedIndex.value && selectedItem.value) Color.Black else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                        Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxWidth()) {
                            when (model.language.value) {
                                MARKDOWN, YAML -> {
                                    Markdown(
                                        model.tabViewModel.tabsViewModel,
                                        model.body.text.trim().lines()[0] + " (...)",
                                        internalRefReference
                                    )
                                }
                                else -> {
                                    Row(modifier = Modifier.onGloballyPositioned { coordinates ->
                                        mainRowWidth = with(density) { coordinates.size.width.toDp() }
                                    })
                                    {
                                        Editor(
                                            mainRowWidth,
                                            mutableStateOf(
                                                TextFieldValue(model.body.text.trim().lines()[0] + " (...)")
                                            ),
                                            model.annotations,
                                            model.resultsAnnotations,
                                            readOnly = true,
                                            useHighlighting = model.language.value in compilableLanguages,
                                            cellWasChanged,
                                            enableElementListScrolling
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //Arrow icons to move the elements/cells with in the file
            SysMDTooltipArea(tooltipText ="Move cell up",
                modifier = Modifier.padding(end = 10.dp).width(18.dp).height(18.dp).align(Alignment.TopEnd)) {
                IconButton(
                    onClick = {
                        onMoveRequest(index, MoveRequest.Up)
                        cellWasChanged.value = true
                    }
                ) {
                    Icon(
                        imageVector = MyIcons.ArrowCircleUp,
                        contentDescription = "Move this cell up",
                        tint = if (index == selectedIndex.value && selectedItem.value) Color.Black else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            SysMDTooltipArea(tooltipText ="Move cell down",
                modifier = Modifier.padding(end = 10.dp).width(18.dp).height(18.dp).align(Alignment.BottomEnd)) {
                IconButton(
                    onClick = {
                        onMoveRequest(index, MoveRequest.Down)
                        cellWasChanged.value = true
                    }
                ) {
                    Icon(
                        imageVector = MyIcons.ArrowCircleDown,
                        contentDescription = "Move this cell down",
                        tint = if (index == selectedIndex.value && selectedItem.value) Color.Black else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}