@file:Suppress("FunctionName", "SpellCheckingInspection")
package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.compiler.getYaml
import com.github.tukcps.sysmd.ui.viewmodel.TabsViewModel
import kotlin.io.path.exists


/**
 * Renders the title page of a project or file.
 * @param tabsViewModel the Tab's view model .
 * @param body the body of the notebook cell; it must be in Frontmatter YAML Markdown syntax.
 */
@Composable
fun Frontmatter(
    tabsViewModel: TabsViewModel,
    body: MutableState<TextFieldValue>
) {

    // The entries of the key/value YAML header as states
    var yaml = getYaml(body.value.text)

    // After each re-rendering: also synchronize YAML with data inputs
    SideEffect { yaml = getYaml(body.value.text) }

    // The Composable itself
    Column  {
        if (yaml?.get("title") != null) {
            Spacer(Modifier.height(50.dp))
            Text(
                text = yaml?.get("title")?:"",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth().padding(horizontal = 20.dp),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (yaml?.get("subtitle") != null) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = yaml?.get("subtitle")?:"",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth().padding(horizontal = 20.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (yaml?.get("title") != null) {
            Spacer(Modifier.height(20.dp))
            if (tabsViewModel.sessionState.value.project?.directory?.resolve("Files")?.resolve("icon.png")?.exists() == true)
                Image(
                    bitmap = loadFullImage(tabsViewModel = tabsViewModel, "Files/icon.png").image, contentDescription = "icon",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )
        }

        if (yaml?.get("author") != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = yaml?.get("author")?:"",
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 28.dp, vertical = 3.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}