package com.github.tukcps.sysmd.ui.diagram

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.tukcps.sysmd.model.sysml.StateUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

@Composable
fun showStateDiagramImagePopup(statemachine: StateUsage, diagramPath: Path, renderClicked: MutableState<Boolean>) {
    val stateDiagramFile = createStateDiagramFile(statemachine, diagramPath)
    val density = LocalDensity.current
    val svgPainter = stateDiagramFile.inputStream().buffered().use { loadSvgPainter(it, density) }

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
                AsyncImage(
                    load = { svgPainter },
                    painterFor = { it },
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
private fun createStateDiagramFile(statemachine: StateUsage, diagramPath: Path): File {
    createDiagramsFolderIfNecessary(diagramPath)
    val stateDiagramImageFile = Files.createTempFile(diagramPath,"stateDiagram", ".svg").toFile()
    StateDiagram(statemachine).image?.let {
        it.writeToSvgFile(stateDiagramImageFile)
        println("State diagram image written to: ${stateDiagramImageFile.absolutePath}")
    }
    return stateDiagramImageFile
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