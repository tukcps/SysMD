@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.composables.TooltipInstant
import com.github.tukcps.sysmd.ui.styles.Fonts
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.allLanguages

/**
 * The composable that creates the drop-down menu line on top of each editable element.
 * It permits choice of
 * - Language (SysMD, SysML, Markdown)
 * - Scope (Global, Level 1 Headings, Level 2 Headings)
 */
@Composable
fun LanguageDropdown(
    selectedLanguage: MutableState<Language>,
    selectedNamespace: MutableState<String>,
    selectedRelationshipType: MutableState<TextFieldValue>,
    onLanguageChange: () -> Unit,
    ) {
    var languageExpanded by remember { mutableStateOf(false) }
    var typeOfRelationshipExpanded by remember { mutableStateOf(false) }

    TooltipInstant(tooltipText = "Click on the language name to select the syntax this element should follow",{}) {
        Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).fillMaxWidth()) {
            Text(" Language: ", fontSize = 12.sp, lineHeight = 14.sp)
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                val items = allLanguages.toList()
                Text(
                    text = items[selectedLanguage.value.ordinal].toString(),
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.clickable(onClick = { languageExpanded = true }).background(MaterialTheme.colorScheme.background.copy(0.3f))
                )
                DropdownMenu(
                    expanded = languageExpanded,
                    onDismissRequest = { languageExpanded = false },
                ) {
                    items.forEachIndexed { index, s ->
                        DropdownMenuItem(
                            modifier = Modifier.height(18.dp),
                            onClick = {
                                selectedLanguage.value = TextualRepresentationViewModel.Companion.Language.entries[index]
                                languageExpanded = false
                            },
                            text = { Text(text = s.toString(), fontSize = 12.sp, lineHeight = 14.sp) },
                            contentPadding = PaddingValues(1.dp)
                        )
                    }
                }
            }
            if (selectedLanguage.value == TextualRepresentationViewModel.Companion.Language.SYS_MD) {
                Spacer(Modifier.width(20.dp)) // Same as width of line numbers of Editor Composable
                Text(" Namespace ", fontSize = 12.sp, lineHeight = 14.sp)
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    BasicTextField(
                        value = selectedNamespace.value,
                        maxLines = 1,
                        onValueChange = { selectedNamespace.value = it },
                        textStyle = TextStyle(
                                color = Color.Magenta,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontFamily = Fonts.jetbrainsMono
                        )
                    )
                }
                Text(" hasA ", fontSize = 12.sp, lineHeight = 14.sp)
            }

            if (selectedLanguage.value == TextualRepresentationViewModel.Companion.Language.VIEW) {
                Spacer(Modifier.width(20.dp)) // Same as width of line numbers of Editor Composable

                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    // val items = allLanguages.toList()
                    Text(
                        text = if (selectedRelationshipType.value.text.isBlank()) "hasA" else selectedRelationshipType.value.text,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.clickable(onClick = { typeOfRelationshipExpanded = true })
                            .background(MaterialTheme.colorScheme.background.copy(0.3f))
                    )
                    DropdownMenu(
                        expanded = typeOfRelationshipExpanded,
                        onDismissRequest = { typeOfRelationshipExpanded = false },
                    ) {
                        arrayOf("hasA", "isA").forEachIndexed { _, s ->
                            DropdownMenuItem(
                                modifier = Modifier.height(18.dp),
                                onClick = {
                                    selectedRelationshipType.value = TextFieldValue(s)
                                    typeOfRelationshipExpanded = false
                                    onLanguageChange()
                                },
                                text = { Text(text = s.toString(), fontSize = 12.sp, lineHeight = 14.sp) },
                                contentPadding = PaddingValues(1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(20.dp))
                Text(" Namespace ", fontSize = 12.sp, lineHeight = 14.sp)
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    BasicTextField(
                        value = selectedNamespace.value,
                        maxLines = 1,
                        onValueChange = {
                            selectedNamespace.value = it
                            onLanguageChange()
                        },
                        textStyle = TextStyle(
                            color = Color.Magenta,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontFamily = Fonts.jetbrainsMono
                        )
                    )


                }

            }

        }
    }
}