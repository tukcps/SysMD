import org.jetbrains.skia.Image
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.ByteArrayOutputStream
import java.io.File

object SvgRenderer {
    fun renderSvgToImage(svgFile: File): Image {
        val input = TranscoderInput(svgFile.toURI().toString())
        val outputStream = ByteArrayOutputStream()
        val output = TranscoderOutput(outputStream)

        val transcoder = PNGTranscoder()
        transcoder.transcode(input, output)

        val pngData = outputStream.toByteArray()
        return Image.makeFromEncoded(pngData)
    }
}