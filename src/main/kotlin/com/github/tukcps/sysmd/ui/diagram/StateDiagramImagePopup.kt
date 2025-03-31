@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.diagram

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.tukcps.sysmd.model.sysml.StateUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

@OptIn(ExperimentalResourceApi::class)
@Composable
fun showStateDiagramImagePopup(statemachine: StateUsage, diagramPath: Path, renderClicked: MutableState<Boolean>) {
    val stateDiagramFilePath = createStateDiagramFile(statemachine, diagramPath)
    val stateDiagramFile = File(stateDiagramFilePath)

    Dialog(
        onDismissRequest = { renderClicked.value = false },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val imageBitmap = remember { stateDiagramFile.inputStream().buffered().readAllBytes().decodeToImageBitmap() }
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "State Diagram Image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.width(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun createStateDiagramFile(statemachine: StateUsage, diagramPath: Path): String {
    createDiagramsFolderIfNecessary(diagramPath)
    val stateDiagramImageFile = Files.createTempFile(diagramPath,"stateDiagram", ".png").toFile()
    val stateDiagramImageFilePath = stateDiagramImageFile.absolutePath
    StateDiagram(statemachine).image?.let {
        it.writeToPngFile(stateDiagramImageFile)
        println("State diagram image written to: $stateDiagramImageFilePath")
    }
    return stateDiagramImageFilePath
}
private fun createDiagramsFolderIfNecessary(diagramPath: Path) {
    if (!diagramPath.exists()) {
        diagramPath.toFile().mkdirs()
    }
}

@Composable
fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val image: T? by produceState<T?>(null) {
        value = withContext(Dispatchers.IO) {
            try {
                load()
            } catch (e: IOException) {
                // instead of printing to console, you can also write this to log,
                // or show some error placeholder
                e.printStackTrace()
                null
            }
        }
    }

    if (image != null) {
        Image(
            painter = painterFor(image!!),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}