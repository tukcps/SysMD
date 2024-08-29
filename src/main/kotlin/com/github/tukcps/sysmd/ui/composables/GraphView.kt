package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.DisplayState
import com.github.tukcps.sysmd.ui.rendering.getTreeViewHeight
import com.github.tukcps.sysmd.ui.rendering.getTreeViewWidth
import com.github.tukcps.sysmd.ui.rendering.renderGraph
import com.github.tukcps.sysmd.ui.rendering.treeView
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.Fonts
import com.github.tukcps.sysmd.ui.viewmodel.DisplayTabModel
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import javax.imageio.ImageIO
import kotlin.math.roundToInt


val exportPressed = mutableStateOf(false)
val exportPath = mutableStateOf("")

@Composable
fun graphView(model: DisplayTabModel, state: DisplayState) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Row(modifier = Modifier.wrapContentSize().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Diagram: " + model.name,
                textAlign = TextAlign.Center,
                fontFamily = Fonts.jetbrainsMono,
                fontSize = 18f.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { exportPressed.value = true },
                content = {
                    Text("Export")
                }
            )
        }

        renderGraph(model,state = state)
        if (exportPressed.value) {
            saveModel(model = model)
        }
    }
}

fun saveModel(model:DisplayTabModel) {
    //Open File Dialog
    exportPressed.value = false
    val fileDialog = FileDialog(Frame(), "Save Model", FileDialog.SAVE)
    fileDialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".png") }
    fileDialog.file = model.name + ".png"
    fileDialog.isVisible = true
    exportPath.value = fileDialog.directory + fileDialog.file
    print(exportPath.value)

    //Continue exporting
    exportModel(model)
}

fun exportModel(model: DisplayTabModel){
    //Create Image from Composable
    val treeNodeModel = model.treeModel
    val image = ImageComposeScene(
        content = { treeView(Modifier, treeNodeModel, model.isIsA())},
        width = getTreeViewWidth(treeNodeModel).roundToInt(),
        height = getTreeViewHeight(treeNodeModel).roundToInt()
    ).render()

    //Continue exporting
    saveImage(image)
}

fun saveImage(
    image: Image){
    //Create File
    val file = File(exportPath.value)
    file.createNewFile()

    //Convert image to BufferedImage
    val bufferedImage = image.toComposeImageBitmap().toAwtImage()

    //Save image to path
    ImageIO.write(bufferedImage, "PNG", file)
}