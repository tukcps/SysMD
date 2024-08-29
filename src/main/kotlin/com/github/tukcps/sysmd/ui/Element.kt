@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.composables.TooltipForIcons
import com.github.tukcps.sysmd.ui.composables.TreeViewModel
import com.github.tukcps.sysmd.ui.rendering.*
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.InternalRefReference
import com.github.tukcps.sysmd.ui.viewmodel.MyIcons
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language.*
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.compilableLanguages
import org.jetbrains.skia.Image
import kotlin.math.roundToInt


/**
 *  Displays a single cell, and dispatches the cell to editor or render methods.
 *  An element has an index that must be selected to be editable, and a description, code, and info section.
 */
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@Composable
fun Element(
    model: TextualRepresentationViewModel,
    index: Int,
    selectedIndex: MutableState<Int>,
    selectedItem: MutableState<Boolean>,
    showInfo: MutableState<Boolean>,
    collapsedElementIds: MutableMap<Int, Boolean>,
    hiddenElementIds: MutableMap<Int, Boolean>,
    onDeleteRequest: (Int) -> Unit,
    onMoveRequest: (Int, MoveRequest) -> Unit,
    elementEdited: MutableState<Boolean>,
    internalRefReference: InternalRefReference,
    enableElementListScrolling: MutableState<Boolean>,
    sysMDViewModel: MutableState<SysMDViewModel>
) {
    val collapsed = mutableStateOf(collapsedElementIds[index] ?: false)
    val hidden = mutableStateOf(hiddenElementIds[index] ?: false)
    val imageChecked = remember { mutableStateOf(false) }
    val treeViewModel = remember { mutableStateOf<TreeViewModel.Item?>(null) }
    val image = remember { mutableStateOf<Image?>(null) }

    fun isSelected(): Boolean = (index == selectedIndex.value && selectedItem.value)

    fun createImage(className: String, relationshipType: String) {
        imageChecked.value = true
        val isA = relationshipType == "isA"
        val items =
            if (isA) sysMDViewModel.value.inheritance.value.items else sysMDViewModel.value.composition.value.items
        treeViewModel.value = items.firstOrNull {
            it.name == className
        }
        if (treeViewModel.value == null && className.isNotEmpty()) {
            treeViewModel.value = items.firstOrNull {
                it.name.contains(className)
            }
        }
        if (treeViewModel.value != null) {
            val treeNodeModel = convertToTreeNodeModel(treeViewModel.value!!.item.node)
            determineChildsWidth(treeNodeModel)

            image.value = ImageComposeScene(
                content = { treeView(Modifier, treeNodeModel, true) },
                width = getTreeViewWidth(treeNodeModel).roundToInt(),
                height = getTreeViewHeight(treeNodeModel).roundToInt()
            ).render()
        } else {
            image.value = null
        }
    }

    fun onLanguageChange() {
        if (model.language.value == VIEW) {
            elementEdited.value = true
            createImage(model.namespace.value, model.body.value.text)
        }
    }

    fun resetImageView() {
        imageChecked.value = false
        image.value = null
        sysMDViewModel.value = SysMDViewModel(session = model.kerMlModel.value)
    }


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

    if (model.language.value == VIEW && !imageChecked.value && image.value == null) {
        createImage(model.namespace.value, model.body.value.text)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!collapsed.value && !hidden.value) {
            Column(modifier = Modifier.fillMaxWidth(0.97f)) {
                // The body part of the element
                Row(
                    // the whole row is clickable; double click selects/deselects it.
                    modifier = Modifier
                        .pointerInput(changeEditStatusDescription) {
                            detectTapGestures(onDoubleTap = { changeEditStatusDescription() })
                        }
                        .hoverable(interactionSource = interactionSource, true)
                        .background(
                            if (isSelected()) MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
                            else if (hovered) MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
                            else MaterialTheme.colorScheme.background
                        )
                ) {
                    Column {
                        // Pencil ... edit the element
                        TooltipForIcons(tooltipText = "Enable/Disable editing of the cell") {
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
                            TooltipForIcons(tooltipText = "Display/Hide additional information section\nNote: If 'Analyze' was never clicked this might be empty") {
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
                            TooltipForIcons(tooltipText = "Collapse / Expand this cell") {
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
                            TooltipForIcons(tooltipText = "Compile and analyze only this cell") {
                                IconButton(
                                    modifier = Modifier.height(18.dp).width(18.dp).padding(1.dp),
                                    onClick = {
                                        model.compile()
                                        resetImageView()
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Calculate,
                                        "Compile and analyze",
                                        tint = if (index == selectedIndex.value && selectedItem.value)
                                            AppTheme.colors.iconGreen else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                        // Trash ... delete the cell
                        if (model.language.value != YAML)
                            TooltipForIcons(tooltipText = "Delete this cell") {
                                IconButton(
                                    modifier = Modifier.height(18.dp).width(18.dp).padding(1.dp),
                                    onClick = {
                                        onDeleteRequest(index)
                                        elementEdited.value = true
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
                        // 2nd column with the headline menu only if selected mode:
                        // Spacer(modifier = Modifier.width(18.dp))
                        if (index == selectedIndex.value && selectedItem.value) {
                            Column {
                                if (model.language.value != YAML)
                                    LanguageDropdown(model.language, model.namespace, model.body, ::onLanguageChange)

                                if (model.language.value == YAML)
                                    Frontmatter(model.body, readOnly = false)
                                else if (model.language.value == FORM)
                                    FormView(model.body, elementEdited = elementEdited, readOnly = false)
                                else if (model.language.value == VIEW) {
                                    if (image.value != null) {
                                        Image(
                                            bitmap = image.value!!.toComposeImageBitmap(),
                                            contentDescription = "",
                                            modifier = Modifier.fillMaxSize()
                                                .horizontalScroll(state = rememberScrollState()),
                                        )
                                    }
                                } else {
                                    Row(modifier = Modifier.onGloballyPositioned { coordinates ->
                                        mainRowWidth = with(density) { coordinates.size.width.toDp() }
                                    })
                                    {
                                        Editor(
                                            mainRowWidth,
                                            model.body,
                                            model.annotations,
                                            model.resultsAnnotations,
                                            readOnly = false,
                                            useHighlighting = model.language.value in compilableLanguages,
                                            elementEdited,
                                            enableElementListScrolling
                                        )
                                    }
                                }
                            }
                        } else
                            if (model.language.value == YAML)
                                Frontmatter(model.body, readOnly = true)
                            else if (model.language.value == FORM)
                                FormView(model.body, elementEdited = elementEdited, readOnly = true)
                            else if (model.language.value == VIEW) {
                                if (image.value != null) {
                                    Image(
                                        bitmap = image.value!!.toComposeImageBitmap(),
                                        contentDescription = "",
                                        modifier = Modifier.fillMaxSize()
                                            .horizontalScroll(state = rememberScrollState()),
                                    )
                                }
                            } else if (model.language.value in setOf(SYS_MD, SYS_ML)) {
                                Column {
                                    Row(Modifier.background(MaterialTheme.colorScheme.background)
                                        .fillMaxWidth()
                                        .onGloballyPositioned { coordinates ->
                                            mainRowWidth = with(density) { coordinates.size.width.toDp() }
                                        }) {
                                        if (model.language.value == SYS_MD
                                            && model.namespace.value !in setOf("Global", "")
                                        )
                                            Text(
                                                " Namespace ${model.namespace.value} hasA",
                                                fontSize = 12.sp,
                                                lineHeight = 14.sp
                                            )
                                    }
                                    Editor(
                                        mainRowWidth,
                                        model.body,
                                        model.annotations,
                                        model.resultsAnnotations,
                                        readOnly = true,
                                        useHighlighting = model.language.value == SYS_MD,
                                        elementEdited,
                                        enableElementListScrolling
                                    )
                                }
                            } else // if (model.language.value == MARKDOWN)
                                Column(Modifier.padding(start = 6.dp)) {
                                    markdownRendering(
                                        model.body.value.text,
                                        internalRefReference
                                    )
                                }
                    }
                }
                // Display annotations if selected
                if (model.language.value in compilableLanguages)
                    AnnotationsView(showInfo, model)
            }
        } else if (collapsed.value && !hidden.value) {
            // the section is collapsed, but not hidden.
            Column(modifier = Modifier.fillMaxWidth(0.97f)) {

                // The body part of the cell
                Row(// whole row ; double click selects/deselects it.
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
                        TooltipForIcons(tooltipText = "Enable/Disable editing of the cell") {
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
                        TooltipForIcons(tooltipText = "Collapse / Expand this cell") {
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
                            MARKDOWN, YAML ->
                                markdownRendering(
                                    model.body.value.text.trim().lines()[0] + " (...)",
                                    internalRefReference
                                )

                            FORM
                            -> FormView(model.body, elementEdited = elementEdited, readOnly = true)

                            else -> {
                                Row(modifier = Modifier.onGloballyPositioned { coordinates ->
                                    mainRowWidth = with(density) { coordinates.size.width.toDp() }
                                })
                                {
                                    Editor(
                                        mainRowWidth,
                                        mutableStateOf(
                                            TextFieldValue(model.body.value.text.trim().lines()[0] + " (...)")
                                        ),
                                        model.annotations,
                                        model.resultsAnnotations,
                                        readOnly = true,
                                        useHighlighting = model.language.value in compilableLanguages,
                                        elementEdited,
                                        enableElementListScrolling
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        //Arrow icons to move the elements/cells with in the file
        Column {
            TooltipForIcons(tooltipText = "Move up") {
                IconButton(
                    modifier = Modifier.width(18.dp).height(18.dp).padding(1.dp),
                    onClick = {
                        onMoveRequest(index, MoveRequest.Up)
                        elementEdited.value = true
                    }
                ) {
                    Icon(
                        imageVector = MyIcons.ArrowCircleUp,
                        contentDescription = "Move this cell up",
                        tint = if (index == selectedIndex.value && selectedItem.value) Color.Black else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
            TooltipForIcons(tooltipText = "Move down") {
                IconButton(
                    modifier = Modifier.width(18.dp).height(18.dp).padding(1.dp),
                    onClick = {
                        onMoveRequest(index, MoveRequest.Down)
                        elementEdited.value = true
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