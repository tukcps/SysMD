@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.compiler.getYaml
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.composables.InputField
import com.github.tukcps.sysmd.ui.composables.SelectableIcon
import com.github.tukcps.sysmd.ui.viewmodel.MyIcons
import java.io.File

/**
 * Renders the title page of a project or file.
 * @param body the body of the notebook cell; it must be in Frontmatter yaml Markdown syntax.
 */
@Composable
fun Frontmatter(
    body: MutableState<TextFieldValue>,
    readOnly: Boolean = false
) {

    // The entries of the key/value Yaml header as states
    var yaml = getYaml(body.value.text)
    var name by remember { mutableStateOf(yaml?.get("name")?:"") }
    var title by remember { mutableStateOf(yaml?.get("title")?:"") }
    var logo by remember { mutableStateOf(yaml?.get("logo")?:"") }
    var description by remember { mutableStateOf(yaml?.get("description")?:"") }
    var maintainer by remember { mutableStateOf(yaml?.get("maintainer")?:"") }
    var usage by remember { mutableStateOf(yaml?.get("usage")?:"") }
    var files by remember { mutableStateOf(yaml?.get("files")?:"") }

    // After each re-rendering: also synchronize yaml with data inputs
    SideEffect {
        yaml = getYaml(body.value.text)
        name = yaml?.get("name")?:""
        title = yaml?.get("title")?:""
        logo =  yaml?.get("logo")?:""
        description =yaml?.get("description")?:""
        maintainer = yaml?.get("maintainer")?:""
        usage = yaml?.get("usage")?:""
        files = yaml?.get("files")?:""
    }

    // The update functions with some checks
    fun updateBody(key: String, value: String) {
        yaml?.set(key, value)
        var txt = ""
        yaml?.forEach { txt +="${it.key}: ${yaml!![it.key]}\n" }
        txt = "---\n$txt---\n"
        body.value = TextFieldValue(txt)
    }

    fun checkFiles(string: String): Boolean {
        val fileList = string.split(",").filter { it.isNotBlank() }
        var ok = true
        fileList.forEach {
            if (!File(settings.projectFolder, it.trim()).exists())
                ok = false
        }
        return ok
    }

    fun createFiles() {
        files.split(",").forEach {
            if ( !File(settings.projectFolder, it.trim()).exists()) {
                val file = File(settings.projectFolder, it.trim())
                file.writeText(
                    """
                ---
                
                ---
                
                # Section
                
                ```SysMD
                
                ```
            """.trimIndent()
                )
            }
        }
    }

    // The Composable itself
    Column   {
        Spacer(Modifier.height(50.dp))

        val style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
        InputField(
            value = title,
            onValueChange = { title = it; updateBody("title", it) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth().padding(horizontal = 20.dp),
            textStyle = style,
            maxLines = 3,
            singleLine = false,
            text = "Enter a title",
            readOnly = readOnly
        )

        Spacer(Modifier.height(20.dp))
        if (logo.isNotBlank()) Image(bitmap = loadFullImage(logo.trim()).image, contentDescription = "logo",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(4.dp))
        if (!readOnly) InputField(
            value = logo,
            text = "Path to the logo or icon image in the project folder",
            onValueChange = { logo = it; updateBody("logo", it)},
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 20.dp)
                .align(Alignment.CenterHorizontally)
            ,
            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground),
            check = { string: String -> if (string.isNotBlank()) File(settings.projectFolder, string.trim()).exists() else true }
        )
        Spacer(Modifier.height(8.dp))
        if (readOnly) Text(text = maintainer,
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 20.dp, vertical = 3.dp)
                .align(Alignment.CenterHorizontally),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        else InputField(
            value = maintainer,
            onValueChange = { maintainer=it; updateBody("maintainer", it) },
            text = "Author or maintainer of the project",
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 20.dp, vertical = 3.dp)
                .align(Alignment.CenterHorizontally),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
        )
        Spacer(Modifier.height(8.dp))

        if (!readOnly || name.isNotBlank())
        Row(Modifier.padding(all = 4.dp)) {
            Text(
                modifier = Modifier.width(180.dp),
                text = "Name of project: ",
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left,
                color = MaterialTheme.colorScheme.onBackground
            )
            InputField(
                value = name,
                text = "If the file is a project, give it a name and add files below",
                onValueChange = { name = it; updateBody("name", it) },
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                check = { it.isNotEmpty() },
                readOnly = readOnly
            )
        }
        if (name.isNotBlank()) {
            if (!readOnly || description.isNotBlank())
            Row(Modifier.padding(all = 4.dp)) {
                Text(
                    modifier = Modifier.width(180.dp),
                    text = "Description of project: ",
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Left,
                    color = MaterialTheme.colorScheme.onBackground
                )
                InputField(
                    value = description,
                    onValueChange = { description=it; updateBody("description", it) },
                    text = "Give a brief textual description; must not have empty lines.",
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    readOnly = readOnly
                )
            }
            if (!readOnly || files.isNotBlank())
            Row(Modifier.padding(all = 4.dp)) {
                Text(
                    modifier = Modifier.width(180.dp),
                    text = "Files of this project: ",
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Left,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = MaterialTheme.colorScheme.onBackground
                )
                InputField(
                    value = files,
                    text = "Give a comma-separated list of .md files that belong to this project",
                    onValueChange = { files = it; updateBody("files", it) },
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    check = ::checkFiles,
                    readOnly = readOnly
                )
                SelectableIcon(icon = MyIcons.Add, color = MaterialTheme.colorScheme.onBackground, tooltip = "Create files (save & restart SysMD after change!)") {
                    createFiles()
                    checkFiles(files)
                    // TODO: Reset, add and open new tab, mark project file and new tabs edited.
                }
            }
            if (!readOnly || usage.isNotBlank())
            Row(Modifier.padding(all = 4.dp)) {
                Text(
                    modifier = Modifier.width(180.dp),
                    text = "Usage of projects:",
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Left,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = MaterialTheme.colorScheme.onBackground
                )
                InputField(
                    value = usage,
                    text = "Give a comma-separated list of names of other projects that are used in this project (save & restart after change!).",
                    onValueChange = { usage = it; updateBody("usage", it) },
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    readOnly = readOnly
                )
            }
        }
    }
}