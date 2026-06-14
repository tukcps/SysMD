package com.github.tukcps.sysmd.ui.rendering

import com.github.tukcps.sysmd.ui.toUriString
import kotlinx.io.files.Path
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.jetbrains.skia.Image
import java.io.ByteArrayOutputStream

object SvgRenderer {
    fun renderSvgToImage(svgFile: Path): Image {
        val input = TranscoderInput(svgFile.toUriString())
        val outputStream = ByteArrayOutputStream()
        val output = TranscoderOutput(outputStream)

        val transcoder = PNGTranscoder()
        transcoder.transcode(input, output)

        val pngData = outputStream.toByteArray()
        return Image.makeFromEncoded(pngData)
    }
}