package com.github.tukcps.sysmd.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File

/**
 * Adds a listener for drag-and-drop events, which covers the whole screen. For each dropped file the provided callback
 * function is called.
 * The DropBox can be disabled, if needed.
 */
@Composable
fun DropBox(window: ComposeWindow, callback: (parent: String, file: String) -> Unit, disabled: Boolean = false) {
    if (disabled) {
        window.contentPane.dropTarget = null
    } else {
        window.contentPane.dropTarget = object : DropTarget() {
            @Synchronized
            override fun drop(evt: DropTargetDropEvent) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY)
                    val droppedFiles = evt.transferable.getTransferData(DataFlavor.javaFileListFlavor)
                    if (droppedFiles is List<*>) {
                        for (file in droppedFiles) {
                            if (file is File) {
                                val itParent: String = file.parent
                                val itName: String = file.name
                                // println("dropped: $itParent $itName")
                                callback(itParent, itName)
                            }
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
}
