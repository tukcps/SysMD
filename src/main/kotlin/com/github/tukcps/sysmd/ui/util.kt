package com.github.tukcps.sysmd.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.io.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import org.jetbrains.skia.Image
import kotlin.random.Random

/**
 * Reads the entire content of a file as a string.
 *
 * @return The complete content of the file as a [String].
 */
fun Path.readText(): String {
    return SystemFileSystem.source(this).buffered().use { buffer ->
        buffer.readString()
    }
}

/**
 * Writes the given text to a file. Overwrites any existing content.
 * Automatically creates all missing parent directories if they do not exist.
 *
 * @param text The string content to write into the file.
 */
fun Path.writeText(text: String) {
    val parentDir = this.parent
    if (parentDir != null) {
        SystemFileSystem.createDirectories(parentDir)
    }

    SystemFileSystem.sink(this).buffered().use { buffer ->
        buffer.writeString(text)
    }
}

/**
 * Reads the entire content of the file as a byte array.
 *
 * @return A [ByteArray] containing all bytes from the file.
 */
fun Path.readBytes(): ByteArray {
    return try {
        SystemFileSystem.source(this).buffered().use { buffer ->
            buffer.readByteArray()
        }
    } catch (_: Exception) {
        ByteArray(0)
    }
}


/**
 * Writes the given byte array to a file. Overwrites any existing content.
 * Automatically creates all missing parent directories if they do not exist.
 *
 * @param bytes The raw byte array content to write into the file.
 * @throws IOException If the directories cannot be created or the file cannot be written to.
 */
fun Path.writeBytes(bytes: ByteArray) {
    // 1. Get the parent directory structure
    val parentDir = this.parent

    // 2. Automatically create all missing folders in the chain
    if (parentDir != null) {
        SystemFileSystem.createDirectories(parentDir)
    }

    // 3. Open the target sink, buffer it, and write the raw bytes
    // SystemFileSystem.sink() overwrites any existing file by default
    SystemFileSystem.sink(this).buffered().use { buffer ->
        buffer.write(bytes)
    } // .use {} guarantees the stream is securely flushed and closed
}

/**
 * Lists the names of all direct children (files and folders) inside this directory.
 *
 * @return A list containing the [String] names of the children, or an empty list if
 *         the path is not a directory or an error occurs.
 */
fun Path.listChildNames(): List<String> {
    return try {
        // SystemFileSystem.list(this) returns a list of Path objects
        SystemFileSystem.list(this).map { it.name }
    } catch (_: Exception) {
        emptyList()
    }
}


/**
 * Converts this Path into a standardized file:// URI string.
 * Automatically handles absolute resolution and URL encoding for spaces/special characters.
 *
 * @return A [String] representing the absolute file URI.
 */
fun Path.toUriString(): String {
    // 1. Resolve to an absolute path first
    val absolutePath = try {
        SystemFileSystem.resolve(this).toString()
    } catch (_: Exception) {
        this.toString() // Fallback if file doesn't exist yet
    }

    // 2. Normalize Windows backslashes to forward slashes if necessary
    val normalizedPath = absolutePath.replace("\\", "/")

    // 3. Ensure it starts with a leading slash
    val leadingSlash = if (normalizedPath.startsWith("/")) "" else "/"

    // 4. URL encode spaces and special characters safely without reflection
    val encodedPath = normalizedPath.split("/").joinToString("/") { segment ->
        segment.encodeUrlPathSegment()
    }

    return "file://$leadingSlash$encodedPath"
}

/**
 * Simple extension helper to encode URI path segments manually in commonMain
 */
private fun String.encodeUrlPathSegment(): String {
    return this.map { char ->
        when (char) {
            in 'a'..'z', in 'A'..'Z', in '0'..'9', '-', '_', '.', '!' -> char.toString()
            ' ' -> "%20"
            else -> {
                // Quick hex conversion fallback for special symbols
                "%" + char.code.toString(16).uppercase()
            }
        }
    }.joinToString("")
}


/**
 * Copies all data from the given source into this path, replacing any existing file.
 * Automatically creates missing parent directories.
 *
 * @param source The input stream data source to copy from.
 */
fun Path.copyFrom(source: Source) {
    // 1. Create parent directories if they don't exist yet
    val parentDir = this.parent
    if (parentDir != null) {
        SystemFileSystem.createDirectories(parentDir)
    }

    // 2. Open the target sink. SystemFileSystem.sink() overwrites existing files by default
    // (This is the KMP equivalent to StandardCopyOption.REPLACE_EXISTING)
    SystemFileSystem.sink(this).buffered().use { targetSink ->
        source.transferTo(targetSink)
    }
}


/**
 * Checks whether the path denotes a directory.
 *
 * @return true if the path exists and is a directory, false otherwise.
 */
fun Path.isDirectory(): Boolean {
    // metadataOrNull returns null if the path does not exist
    val metadata = SystemFileSystem.metadataOrNull(this)
    return metadata?.isDirectory == true
}


/**
 * Checks whether the path denotes a regular file.
 *
 * @return true if the path exists and is a regular file, false otherwise.
 */
fun Path.isFile(): Boolean {
    // metadataOrNull returns null if the path does not exist
    val metadata = SystemFileSystem.metadataOrNull(this)
    return metadata?.isRegularFile == true
}


/**
 * Moves or renames this file/directory to a target destination path atomically.
 * Automatically creates missing parent directories for the target destination.
 *
 * @param destination The target path where the file/directory should be moved to.
 * @throws UnsupportedOperationException If the underlying filesystem does not support atomic moves.
 */
fun Path.moveTo(destination: Path) {
    // 1. Ensure target parent directories exist before moving
    val targetParent = destination.parent
    if (targetParent != null) {
        SystemFileSystem.createDirectories(targetParent)
    }

    // 2. Perform the atomic move/rename operation
    SystemFileSystem.atomicMove(source = this, destination = destination)
}



val latexCache = hashMapOf<String, ImageBitmap>()
fun saveComposeBitmapToCache(filename: String, bitmap: ImageBitmap) {
    val skiaBitmap = bitmap.asSkiaBitmap()
    val imageBitmap = Image.makeFromBitmap(skiaBitmap).toComposeImageBitmap()
    latexCache[filename] = imageBitmap
}


fun createTempFile(prefix: String = "tmp", suffix: String = ".tmp"): Path {
    val tempDir = SystemTemporaryDirectory

    val randomId = Random.nextLong(0, Long.MAX_VALUE)
    val fileName = "$prefix$randomId$suffix"
    val fullPath = Path(tempDir, fileName)

    SystemFileSystem.sink(fullPath).close()

    return fullPath
}