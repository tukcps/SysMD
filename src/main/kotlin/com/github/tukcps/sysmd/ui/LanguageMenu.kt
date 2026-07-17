@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.repositories.local.Language.Companion.allLanguages
import com.github.tukcps.sysmd.ui.composables.TooltipInstant
import com.github.tukcps.sysmd.ui.composables.TypeBadgeColors
import com.github.tukcps.sysmd.ui.styles.DarkColors
import com.github.tukcps.sysmd.ui.styles.Fonts
import com.github.tukcps.sysmd.ui.viewmodel.colorMode

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
    onLanguageChange: () -> Unit
) {
    var languageExpanded by remember { mutableStateOf(false) }

    TooltipInstant(tooltipText = "Click on the language name to select the syntax this element should follow",{}) {
        Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).fillMaxWidth()) {
            Text(" Language: ", style = MaterialTheme.typography.bodyMedium)
            Box(modifier = Modifier.fillMaxHeight()) {
                val items = allLanguages.toList()

                Surface(
                    modifier = Modifier.clickable { languageExpanded = true },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 0.dp, shadowElevation = 0.dp
                ) {
                    Text(
                        text = "${items[selectedLanguage.value.ordinal]} ▼",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }

                DropdownMenu(
                    offset = DpOffset(0.dp , 3.dp),
                    expanded = languageExpanded,
                    onDismissRequest = { languageExpanded = false },
                ) {
                    items.forEachIndexed { index, s ->
                        DropdownMenuItem(
                            modifier = Modifier.height(20.dp),
                            onClick = {
                                selectedLanguage.value = Language.entries[index]
                                languageExpanded = false
                                onLanguageChange()
                            },
                            text = { Text(text = s.toString(), style = MaterialTheme.typography.bodyMedium) },
                            contentPadding = PaddingValues(1.dp)
                        )
                    }
                }
            }

            if (selectedLanguage.value in setOf(Language.SYS_MD, Language.KerML, Language.SYS_ML)) {
                Spacer(Modifier.width(10.dp)) // Same as width of line numbers of Editor Composable
                val badge = TypeBadgeColors.colors("Package", colorMode() == DarkColors)

                Text("/   ", style = MaterialTheme.typography.bodyMedium)

                Text(
                    text = "package",
                    style = MaterialTheme.typography.bodySmall.copy(color = badge.foreground, fontFamily = Fonts.jetbrainsMono),
                    color = badge.foreground,
                    modifier = Modifier
                        .background(badge.background)
                        .padding(horizontal = 3.dp, vertical = 3.dp)
                )
                BasicTextField(
                    value = selectedNamespace.value,
                    maxLines = 1,
                    onValueChange = { selectedNamespace.value = it },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = badge.foreground,
                        fontFamily = Fonts.jetbrainsMono),
                    modifier = Modifier
                        .background(color = badge.background.copy(alpha = 0.65f))
                        .border(1.dp, badge.foreground.copy(alpha = 0.45f))
                        .padding(horizontal = 3.dp, vertical = 3.dp)
                )
                Text(" owns ", style = MaterialTheme.typography.bodyMedium)

            }
        }
    }
}