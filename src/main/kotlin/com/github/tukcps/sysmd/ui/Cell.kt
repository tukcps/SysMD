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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.ui.composables.SysMDTooltipArea
import com.github.tukcps.sysmd.ui.rendering.Markdown
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.CellViewModel
import com.github.tukcps.sysmd.ui.viewmodel.InternalRefReference
import com.github.tukcps.sysmd.ui.viewmodel.MyIcons

data class CellAction(
    val icon: ImageVector,
    val label: String,
    val action: () -> Unit,
    val enabled: Boolean = true,
    val tint: Color? = null,
    val isDivider: Boolean = false
)

@Composable
fun CellActionMenu(
    actions: List<CellAction>,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        SysMDTooltipArea(tooltipText = "More options") {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More actions",
                    tint = if (isSelected) AppTheme.colors.iconGreen else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            actions.forEachIndexed { index, action ->
                if (action.isDivider) {
                    if (index > 0) { // Don't add divider at the beginning
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                } else {
                    DropdownMenuItem(
                        onClick = {
                            action.action()
                            expanded = false
                        },
                        enabled = action.enabled,
                        text = { Text(action.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.label,
                                tint = action.tint ?: MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CellToolbar(
    model: CellViewModel,
    isSelected: Boolean,
    collapsed: MutableState<Boolean>,
    onEditToggle: () -> Unit,
    onCollapseToggle: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryActions = buildList {
        // Edit action - always visible
        add(CellAction(
            icon = MyIcons.ModeEditOutline,
            label = if (isSelected) "Stop Editing" else "Edit Cell",
            action = onEditToggle,
            tint = if (isSelected) AppTheme.colors.iconGreen else MaterialTheme.colorScheme.outlineVariant
        ))
    }

    val menuActions = buildList {
        // Collapse/Expand action
        if (model.language.value != Language.YAML) {
            add(CellAction(
                icon = if (collapsed.value) MyIcons.Add else MyIcons.Remove,
                label = if (collapsed.value) "Expand Cell" else "Collapse Cell",
                action = onCollapseToggle,
                tint = MaterialTheme.colorScheme.onSurface
            ))
        }

        // Compile action (only for compilable languages)
        if (model.language.value.isCompilable()) {
            // Add separator if we have previous actions
            if (isNotEmpty()) {
                add(CellAction(
                    icon = Icons.Default.Menu, // Fake icon that won't be used
                    label = "",
                    action = {},
                    isDivider = true
                ))
            }

            add(CellAction(
                icon = Icons.Filled.Calculate,
                label = "Compile and Solve",
                action = model.onCompile,
                tint = AppTheme.colors.iconGreen
            ))
        }

        // Separator before move actions
        add(CellAction(
            icon = Icons.Default.Menu, // Fake icon that won't be used
            label = "",
            action = {},
            isDivider = true
        ))

        // Move actions
        add(CellAction(
            icon = MyIcons.ArrowCircleUp,
            label = "Move Up",
            action = onMoveUp
        ))

        add(CellAction(
            icon = MyIcons.ArrowCircleDown,
            label = "Move Down",
            action = onMoveDown
        ))

        // Separator before delete action
        add(CellAction(
            icon = Icons.Default.Menu, // Fake icon that won't be used
            label = "",
            action = {},
            isDivider = true
        ))

        // Delete action
        add(CellAction(
            icon = MyIcons.DeleteForever,
            label = "Delete Cell",
            action = onDelete,
            tint = Color.Red
        ))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Primary actions (always visible)
        primaryActions.forEach { action ->
            SysMDTooltipArea(tooltipText = action.label) {
                IconButton(
                    modifier = Modifier.size(18.dp),
                    onClick = action.action,
                    enabled = action.enabled
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = action.tint ?: MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Menu for additional actions
        if (menuActions.isNotEmpty()) {
            CellActionMenu(
                actions = menuActions,
                isSelected = isSelected,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 *  Displays a single notebook cell and dispatches the cell to editor or other render methods.
 *  An element has an index that must be selected to be editable, and a description, code, and info section.
 */
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@Composable
fun Cell(
    model: CellViewModel,
    index: Int,
    selectedIndex: MutableState<Int>,
    selectedItem: MutableState<Boolean>,
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
    var mainRowWidth by remember { mutableStateOf(0.dp) }

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Box {
        if (!collapsed.value && !hidden.value) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // The body part of the element
                Row(
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
                    // Toolbar with actions
                    CellToolbar(
                        model = model,
                        isSelected = isSelected(),
                        collapsed = collapsed,
                        onEditToggle = changeEditStatusDescription,
                        onCollapseToggle = onCollapseExpand,
                        onDelete = {
                            onDeleteRequest(index)
                            cellWasChanged.value = true
                        },
                        onMoveUp = {
                            onMoveRequest(index, MoveRequest.Up)
                            cellWasChanged.value = true
                        },
                        onMoveDown = {
                            onMoveRequest(index, MoveRequest.Down)
                            cellWasChanged.value = true
                        },
                        modifier = Modifier.padding(4.dp)
                    )

                    Column(
                        Modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        // Content area
                        if (index == selectedIndex.value && selectedItem.value) {
                            Column {
                                LanguageDropdown(model.language, model.namespace, ::onLanguageChange)

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
                                        useHighlighting = model.language.value.isCompilable(),
                                        cellWasChanged,
                                        enableElementListScrolling
                                    )
                                }
                            }
                        } else {
                            when(model.language.value) {
                                Language.YAML -> Frontmatter(model.cellListViewModel.editorTabsViewModel, model.bodyState)
                                in setOf(Language.KerML, Language.SYS_MD, Language.SYS_ML) -> {
                                    Column {
                                        Row(Modifier.background(MaterialTheme.colorScheme.background)
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                mainRowWidth = with(density) { coordinates.size.width.toDp() }
                                            }) {
                                            if ( (model.language.value == Language.SYS_MD || model.language.value == Language.SYS_ML)
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
                                            useHighlighting = model.language.value.isCompilable(),
                                            cellWasChanged,
                                            enableElementListScrolling
                                        )
                                    }
                                }
                                else -> {
                                    Column(Modifier.padding(start = 6.dp)) {
                                        Markdown(
                                            model.cellListViewModel.editorTabsViewModel,
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
                if (model.language.value.isCompilable())
                    AnnotationsView(model)
            }
        } else {
            if (collapsed.value && !hidden.value) {
                // Collapsed view
                Column(modifier = Modifier.fillMaxWidth(0.97f)) {
                    Row(
                        modifier = Modifier
                            .pointerInput(changeEditStatusDescription) {
                                detectTapGestures(onDoubleTap = { changeEditStatusDescription() })
                            }
                            .hoverable(interactionSource = interactionSource, true)
                            .background(if (hovered) MaterialTheme.colorScheme.primaryContainer.copy(0.5f) else MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                    ) {
                        // Simplified toolbar for collapsed view
                        CellToolbar(
                            model = model,
                            isSelected = isSelected(),
                            collapsed = collapsed,
                            onEditToggle = changeEditStatusDescription,
                            onCollapseToggle = onCollapseExpand,
                            onDelete = {
                                onDeleteRequest(index)
                                cellWasChanged.value = true
                            },
                            onMoveUp = {
                                onMoveRequest(index, MoveRequest.Up)
                                cellWasChanged.value = true
                            },
                            onMoveDown = {
                                onMoveRequest(index, MoveRequest.Down)
                                cellWasChanged.value = true
                            },
                            modifier = Modifier.padding(4.dp)
                        )

                        Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxWidth()) {
                            when (model.language.value) {
                                Language.MARKDOWN, Language.YAML -> {
                                    Markdown(
                                        model.cellListViewModel.editorTabsViewModel,
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
                                            useHighlighting = model.language.value.isCompilable(),
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
    }
}