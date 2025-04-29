@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import SvgRenderer.renderSvgToImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.services.repositories.local.ProjectUsageData
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.rendering.TableOfContentsRenderer
import com.github.tukcps.sysmd.ui.rendering.isElementTOCElement
import com.github.tukcps.sysmd.ui.rendering.isShortTOCElement
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.Fonts
import com.github.tukcps.sysmd.ui.styles.MDTypography
import com.github.tukcps.sysmd.ui.viewmodel.InternalRefReference
import com.github.tukcps.sysmd.ui.viewmodel.TabsViewModel
import com.github.tukcps.sysmd.ui.viewmodel.imageCache
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterNode
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.image.attributes.ImageAttributes
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.ins.Ins
import org.commonmark.ext.ins.InsExtension
import org.commonmark.node.*
import org.commonmark.node.Paragraph
import org.commonmark.parser.Parser
import org.jetbrains.skia.Image.Companion.makeFromEncoded
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import java.awt.Desktop
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest
import java.util.regex.Pattern
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.text.Typography.bullet

/**
 * These functions will render a tree of Markdown nodes parsed with CommonMark.
 *
 * The following is an example of how to use this:
 * ```
 * val parser = Parser.builder().build()
 * val root = parser.parse(MIXED_MD) as Document
 * ```
 */
private const val TAG_URL = "url"
private const val TAG_IMAGE_URL = "imageUrl"

private val extensionForSingleCell: List<Extension> = listOf(
    TablesExtension.create(),
    ImageAttributesExtension.create(),
    StrikethroughExtension.create(),
    InsExtension.create()
)

private val parserForCell: Parser = Parser.builder().extensions(extensionForSingleCell).build()

private var latex: String = ""
private var latexList = ArrayList<String>()
internal lateinit var referenceModel: InternalRefReference
val showDirectoryWarning = mutableStateOf(false)
val showNoSuchFileWarning = mutableStateOf(false)
val fileURIToOpen = mutableStateOf("")
val showOpenFileExternalWarning = mutableStateOf(false)
val scrollToItem = mutableStateOf(0)


/** Configure a unique image name using hash function */
fun generateFileName(filename: String): String {
    val md5 = MessageDigest.getInstance("MD5")
    val oneline = filename.replace(Regex("[\\r\\n\\s]+"), "")
    val bytes = md5.digest(oneline.toByteArray())
    val hexString = StringBuilder()
    for (byte in bytes) {
        hexString.append(String.format("%02x", byte))
    }
    return "$hexString"
}


/**
 * Replaces LaTeX inlays before rendering a Markdown text
 * @param tabsViewModel The main window's view model
 * @param text The Markdown text that will be rendered
 */
@Composable
fun Markdown(
    tabsViewModel: TabsViewModel,
    text: String = "",
    internalRefReference: InternalRefReference
) {
    val path = tabsViewModel.sessionState.value.project?.directory
    referenceModel = internalRefReference
    val pattern = Pattern.compile(/* regex = */ "\\$(.+?)\\$", Pattern.DOTALL)
    val matcher = pattern.matcher(text)
    var pos = 0
    val output = StringBuilder()
    File(settings.dataFolder, "Cache").mkdir()

    while (matcher.find()) {
        try {
            // val mbox= ("\\mbox {\\left ${matcher.group(1)} \\right}")
            val latexContent = matcher.group(1)

            // Create a TeXFormula from the LaTeX expression
            val formula = TeXFormula(matcher.group(1))

            //Configure the image name
            val filename = generateFileName(latexContent)

            //
            output.append(text.substring(pos, matcher.start()))

            // Create a TeXIcon from the TeXFormula
            val icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 37F)

            // Create a buffered image to hold the rendered formula
            val image = BufferedImage(icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_ARGB)

            // Render the formula onto the image
            icon.paintIcon(null, image.graphics, 0, 0)

            // Cache dir there? If not, create it
            path?.resolve("Cache")?.createDirectories()

            // Save the image to a file
            ImageIO.write(image, "png", path?.resolve("Cache")?.resolve("formula$filename.png")?.toFile())

            //import the image in the Markdown
            output.append("![formula$filename](/Cache/formula$filename.png)")

            pos = matcher.end()
        } catch (error: Exception) {
            logger.error("In LaTeX: ${error.message}")
        }
    }
    output.append(text.substring(pos))

    val document: Node = parserForCell.parse(output.toString()) // Parses the text, document is the parse tree

    MDDocument(tabsViewModel, document)         // Renders the MD Document
}

/**
 * Builds an annotated string with annotations for font style, weight, family, etc.
 */
fun AnnotatedString.Builder.appendMarkdownChildren(
    parent: Node, colors: ColorScheme,
) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is Strikethrough -> {
                val strikethrough = SpanStyle(textDecoration = TextDecoration.LineThrough)
                pushStyle(strikethrough)
                appendMarkdownChildren(child, colors)
                pop()
            }
            is Ins -> {
                val underline = SpanStyle(textDecoration = TextDecoration.Underline)
                pushStyle(underline)
                appendMarkdownChildren(child, colors)
                pop()
            }
            is HtmlInline -> append(child.literal + " ")
            is Paragraph -> appendMarkdownChildren(child, colors)
            is Text -> append(child.literal + " ")
            is Image -> {
                if (child.destination.startsWith("/Cache/formula")) {
                    latex = child.destination
                    latexList += latex
                    appendInlineContent(latex, child.destination)
                } else {
                    appendInlineContent(TAG_IMAGE_URL, child.destination)
                }
            }
            is Emphasis -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendMarkdownChildren(child, colors)
                pop()
            }
            is StrongEmphasis -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendMarkdownChildren(child, colors)
                pop()
            }
            is Code -> {
                pushStyle(
                    TextStyle(
                        fontFamily = FontFamily.Monospace, background = colors.surfaceVariant
                    ).toSpanStyle()
                )
                append(child.literal)
                pop()
            }
            is HardLineBreak -> {
                append("\n")
            }
            is Link -> {
                val underline = SpanStyle(colors.primary, textDecoration = TextDecoration.Underline)
                pushStyle(underline)
                pushStringAnnotation(TAG_URL, child.destination)
                appendMarkdownChildren(child, colors)
                pop()
                pop()
            }
        }
        child = child.next
    }
}


@Composable
fun MDDocument(tabsViewModel: TabsViewModel, document: Node) {
    MDBlockChildren(tabsViewModel, document)
}

/**
 * Renders a heading with a style that is defined in the global object MDTypography.
 */
@Composable
fun MDHeading(tabsViewModel: TabsViewModel, heading: Heading, modifier: Modifier = Modifier) {
    val style = when (heading.level) {
        1 -> MDTypography.h1.style
        2 -> MDTypography.h2.style
        3 -> MDTypography.h3.style
        4 -> MDTypography.h4.style
        else -> {
            // Invalid header...
            MDBlockChildren(tabsViewModel, heading)
            return
        }
    }
    val spaceAbove = when (heading.level) {
        1 -> MDTypography.h1.spaceAbove
        2 -> MDTypography.h2.spaceAbove
        3 -> MDTypography.h3.spaceAbove
        4 -> MDTypography.h4.spaceAbove
        else -> 2.dp
    }
    val spaceBelow = when (heading.level) {
        1 -> MDTypography.h1.spaceBelow
        2 -> MDTypography.h2.spaceBelow
        3 -> MDTypography.h3.spaceBelow
        4 -> MDTypography.h4.spaceBelow
        else -> 2.dp
    }

    val padding = if (heading.parent is Document) 8.dp else 0.dp
    Spacer(modifier = Modifier.height(spaceAbove))
    Box(modifier = modifier.padding(bottom = padding)) {
        val text = buildAnnotatedString {
            appendMarkdownChildren(heading, MaterialTheme.colorScheme)
        }
        MarkdownText(tabsViewModel, text, style)
    }
    Spacer(modifier = Modifier.height(spaceBelow))
}

@Composable
fun MDParagraph(tabsViewModel: TabsViewModel, paragraph: Paragraph, modifier: Modifier = Modifier) {
    val tocRenderer = TableOfContentsRenderer(referenceModel)
    if (paragraph.firstChild is Image && paragraph.firstChild == paragraph.lastChild) {
        // Paragraph with single image
        if (paragraph.firstChild.lastChild is ImageAttributes) {
            val width = (paragraph.firstChild.lastChild as ImageAttributes).attributes["width"]?.toIntOrNull()
            val height = (paragraph.firstChild.lastChild as ImageAttributes).attributes["height"]?.toIntOrNull()
            if (width != null && height != null) {
                MDImage(tabsViewModel, paragraph.firstChild as Image, modifier, maxWidth = width, maxHeight = height, hasAttributes = true)
            } else {
                MDImage(tabsViewModel, paragraph.firstChild as Image, modifier, hasAttributes = false)
            }
        } else {
            MDImage(tabsViewModel, paragraph.firstChild as Image, modifier, hasAttributes = false)
        }
    } else if (isShortTOCElement(paragraph)) {
        // normal Paragraph
        val padding = if (paragraph.parent is Document) 8.dp else 30.dp
        val newParagraph = tocRenderer.generateSmallTOC()
        Box(modifier = modifier.padding(bottom = padding)) {
            val styledText = buildAnnotatedString {
                pushStyle(MDTypography.bodyMedium.style.toParagraphStyle())
                appendMarkdownChildren(newParagraph, MaterialTheme.colorScheme)
                pop()
            }
            MarkdownText(tabsViewModel, styledText, MDTypography.bodyMedium.style)
        }
    } else if (isElementTOCElement(paragraph)){
        //normal Paragraph
        val padding = if (paragraph.parent is Document) 8.dp else 30.dp
        val newParagraph = tocRenderer.generateTOCAsParagraphElement()
        Box(modifier = modifier.padding(bottom = padding)) {
            val styledText = buildAnnotatedString {
                pushStyle(MDTypography.bodyMedium.style.toParagraphStyle())
                appendMarkdownChildren(newParagraph, MaterialTheme.colorScheme)
                pop()
            }
            MarkdownText(tabsViewModel, styledText, MDTypography.bodyMedium.style)
        }
    }
    else {
        // regular Paragraph
        val padding = if (paragraph.parent is Document) 8.dp else 30.dp
        Box(modifier = modifier.padding(bottom = padding)) {
            val styledText = buildAnnotatedString {
                pushStyle(MDTypography.bodyMedium.style.toSpanStyle())
                appendMarkdownChildren(paragraph, MaterialTheme.colorScheme)
                pop()
            }
            MarkdownText(tabsViewModel, styledText, MDTypography.bodyMedium.style)
        }
    }
}

/**
 * @Composable to display an image of either local or online source
 * Note that the Image will be downscaled to maxsize 700.dp X 700.dp
 */
@Composable
fun MDImage(
    tabsViewModel: TabsViewModel,
    image: Image,
    modifier: Modifier = Modifier,
    maxWidth: Int = 700,
    maxHeight: Int = 700,
    hasAttributes: Boolean,
) {
    val picture = loadFullImage(tabsViewModel, image.destination)
    var height = picture.height.dp
    var width = picture.width.dp
    if (width < 20.dp) width = 700.dp
    if (height < 20.dp) height = 700.dp

    //the max size of a picture
    if (width > maxWidth.dp) {
        height *= maxWidth.dp / width
        width *= maxWidth.dp / width
    }
    if (height > maxHeight.dp) {
        width *= maxHeight.dp / height
        height *= maxHeight.dp / height
    }

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            bitmap = picture.image,
            contentDescription = image.title,
            modifier = modifier
                .width(if (hasAttributes) maxWidth.dp else width)
                .height(if (hasAttributes) maxHeight.dp else height)
                .padding(10.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/** inspiration from compose-jb/examples/imageviewer */
private fun toByteArray(bitmap: BufferedImage): ByteArray {
    val baos = ByteArrayOutputStream()
    ImageIO.write(bitmap, "png", baos)
    return baos.toByteArray()
}

/**
 * load image from local or remote source --
 * if source is determined to be local: first try to load inside of Model path then as an absolute path --
 * only supports JPEG, PNG, BMP, WEBMP, GIF format
 * @param source path where the Image is stored
 * @return custom Picture containing a BufferedImage and meta-data
 */
fun loadFullImage(
    tabsViewModel: TabsViewModel,
    source: String
): Picture {
    //if source is in cache
    val path = tabsViewModel.sessionState.value.project?.directory
    var picture: Picture
    val img: ImageBitmap? = imageCache[path?.name+source]
    if (img != null) {
        picture = Picture(source = source, image = img, name = getNameURL(source), width = img.width, height = img.height)
    } else {
        // source was not in cache, so try to load first from the web...
        try {
            val url = URI(source).toURL()
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 1000
            connection.addRequestProperty("User-Agent", "Mozilla/4.0")
            connection.connect()

            val input: InputStream = connection.inputStream
            val bitmap: ImageBitmap = makeFromEncoded(toByteArray(ImageIO.read(input))).toComposeImageBitmap()

            imageCache.put(path?.name+source, bitmap)
            picture = Picture(
                    source = source,
                    image = bitmap,
                    name = getNameURL(source),
                    width = bitmap.width,
                    height = bitmap.height
            )
        } catch (_: Exception) {
            try { // In File system ...
                var imagePath = "${path}/$source"
                if (!File(imagePath).exists()) {
                    imagePath = source
                }
                val bitmap: ImageBitmap = createImageFromFile(File(imagePath)).toComposeImageBitmap()
                imageCache.put(path?.name + source, bitmap)
                picture = Picture(
                    source = source,
                    image = bitmap,
                    name = getNameURL(source),
                    width = bitmap.width,
                    height = bitmap.height
                )
            } catch (_: Exception) {
                logger.error("No image '${source}' in $path.")
                val text = "No image '$source' in $path."
                val frc = FontRenderContext(null, true, true)
                val notFoundImage: ImageBitmap = makeFromEncoded(toByteArray(BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB)
                    .also { bufferedImage ->
                    bufferedImage.createGraphics().apply {
                        var x: Float
                        var y: Float
                        color = java.awt.Color.GRAY
                        font = Font("Plain", Font.PLAIN, 20).apply {
                            getStringBounds(text, frc).also {
                                x = it.x.toFloat(); y = it.y.toFloat()
                            }
                        }
                        fillRect(0, 0, 200, 100)
                        color = java.awt.Color.BLACK

                        drawString(text, x, -y)
                        dispose()
                    }
                })).toComposeImageBitmap()
                picture = Picture(image = notFoundImage, name = "Image not found", width = 200, height = 100)
            }
        }
    }
    return picture
}

private fun getNameURL(url: String): String {
    return url.substring(url.lastIndexOf('/') + 1, url.length)
}

private fun createImageFromFile(imagePath: File): org.jetbrains.skia.Image =
    if(imagePath.name.endsWith(".SVG")){
        renderSvgToImage(imagePath)
    } else{
        makeFromEncoded(
            toByteArray(
                loadNonSvgImageFromFile(imagePath)
            )
        )
    }

private fun loadNonSvgImageFromFile(file: File): BufferedImage = ImageIO.read(file)

@Composable
fun MDBulletList(tabsViewModel: TabsViewModel, bulletList: BulletList, modifier: Modifier = Modifier) {
    val marker = bullet
    Spacer(modifier.height(3.dp))
    MDListItems(tabsViewModel, bulletList, modifier = modifier) {
        Row {
            val bullet = buildAnnotatedString {
                pushStyle(MDTypography.bodyMedium.style.toSpanStyle())
                append("$marker ")
            }
            MarkdownText(tabsViewModel, bullet, MDTypography.bodyMedium.style, modifier)
            Spacer(modifier.width(10.dp))
            val text = buildAnnotatedString {
                pushStyle(MDTypography.bodyMedium.style.toSpanStyle())
                appendMarkdownChildren(it, MaterialTheme.colorScheme)
                pop()
            }
            MarkdownText(tabsViewModel, text, MDTypography.bodyMedium.style, modifier)
        }
        Spacer(modifier.height(4.dp))
    }
    Spacer(modifier.height(3.dp))
}

@Composable
fun MDOrderedList(tabsViewModel: TabsViewModel, orderedList: OrderedList, modifier: Modifier = Modifier) {
    var number = orderedList.markerStartNumber
    val delimiter = orderedList.markerDelimiter
    MDListItems(tabsViewModel, orderedList, modifier) {
        val text = buildAnnotatedString {
            pushStyle(MDTypography.bodyMedium.style.toSpanStyle())
            append("${number++}$delimiter ")
            appendMarkdownChildren(it, MaterialTheme.colorScheme)
            pop()
        }
        MarkdownText(tabsViewModel, text, MDTypography.bodyMedium.style, modifier)
        Spacer(modifier.height(5.dp))
    }
}


/**
 * Renders a list of items
 */
@Composable
fun MDListItems(
    tabsViewModel: TabsViewModel,
    listBlock: ListBlock,
    modifier: Modifier = Modifier,
    item: @Composable (node: Node) -> Unit,
) {
    val bottom = if (listBlock.parent is Document) 8.dp else 0.dp
    val start = if (listBlock.parent is Document) 20.dp else 20.dp
    Column(modifier = modifier.padding(start = start, bottom = bottom)) {
        var listItem = listBlock.firstChild
        while (listItem != null) {
            var child = listItem.firstChild
            while (child != null) {
                when (child) {
                    is BulletList -> MDBulletList(tabsViewModel, child, modifier)
                    is OrderedList -> MDOrderedList(tabsViewModel, child, modifier)
                    else -> item(child)
                    //TODO here is probably #65
                }
                child = child.next
            }
            listItem = listItem.next
        }
    }
}

@Composable
fun MDBlockQuote(blockQuote: BlockQuote, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.secondary.copy(0.5f)
    Box(modifier = modifier.padding(all = 4.dp).fitMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)).drawBehind {
        drawLine(
            color = color,
            strokeWidth = 8f,
            start = Offset(0.dp.value, 0f),
            end = Offset(0.dp.value, size.height+2)
        )
    }.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
        val text = buildAnnotatedString {
            pushStyle(MDTypography.bodyMedium.style.toSpanStyle().plus(SpanStyle(fontStyle = FontStyle.Italic)))
            appendMarkdownChildren(blockQuote, MaterialTheme.colorScheme)
            pop()
        }
        Text(text, modifier.padding(all = 4.dp))
    }
}



@Composable
fun MDIndentedCodeBlock(blockQuote: IndentedCodeBlock, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.secondary.copy(0.5f)
    Box(modifier = modifier.padding(all = 4.dp).fitMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)).drawBehind {
        drawLine(
            color = color,
            strokeWidth = 8f,
            start = Offset(0.dp.value, 0f),
            end = Offset(0.dp.value, size.height)
        )
    }.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
        Text(blockQuote.literal,
            fontSize = AppTheme.fonts.medium,
            fontFamily = Fonts.jetbrainsMono,
            lineHeight = AppTheme.fixedLineHeight,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
            modifier = Modifier.padding(0.dp),
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun MDThematicBreak() {
    Spacer(Modifier.padding(all = 12.dp).height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.onBackground))
}

@Composable
fun MDFencedCodeBlock(fencedCodeBlock: FencedCodeBlock, modifier: Modifier = Modifier) {
    val padding = if (fencedCodeBlock.parent is Document) 8.dp else 0.dp
    Box(modifier = modifier.padding(bottom = padding).fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = fencedCodeBlock.literal,
            fontFamily = FontFamily.Monospace,
            modifier = modifier
        )
    }
}

@Composable
fun MDYamlFrontMatter(
    tabsViewModel: TabsViewModel,
    yamlFrontMatterBlock: YamlFrontMatterBlock
) {
    var yaml = yamlFrontMatterBlock.firstChild as YamlFrontMatterNode?
    var name: String? = null
    var title: String? = null
    val maintainer = mutableListOf<String>()
    var version: String? = null
    var logo: String? = null
    val usage = mutableListOf<ProjectUsageData>()
    var license: String? = null
    var website: URI? = null
    var description: String? = null
    while (yaml != null) {
        try {
            when (yaml.key) {
                "title" -> yaml.values.firstOrNull()?.let { title = it }
                "logo"  -> logo = yaml.values.firstOrNull()
                "maintainer" -> yaml.values.firstOrNull()?.let { maintainer.addAll(it.split(",")) }
                "version" -> version = yaml.values.firstOrNull()
                "usage" -> yaml.values.firstOrNull()
                    ?.let { it.split(",").forEach { str -> usage.add(ProjectUsageData(URI(str.trim()))) } }
                "license" -> license = yaml.values.firstOrNull()
                "website" -> yaml.values.firstOrNull()?.let { website = URI.create(it) }
                "name" -> yaml.values.firstOrNull()?.let { name = it }
                "description" -> yaml.values.firstOrNull()?.let { description = it }
            }
            yaml = yaml.next as YamlFrontMatterNode?
        } catch (e: Exception) {
            logger.error("In Yaml cell: $e")
        }
    }
    Column   {
        Spacer(Modifier.height(50.dp))
        title?.let {
            Text(text = it, modifier = Modifier.fillMaxWidth(),
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))}
        Spacer(Modifier.height(20.dp))

        logo?.let {
            Image(bitmap = loadFullImage(tabsViewModel, logo).image, contentDescription = "logo",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            ) }
        Spacer(Modifier.height(20.dp))
        if (maintainer.isNotEmpty()) {
            Text(text = maintainer.toString().trim('[', ']'), modifier = Modifier.fillMaxWidth(),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
            )
        }
        Spacer(Modifier.height(50.dp))
        version?.let { Text(text = "Version: $version")}
        Text(text = """Usage: ${usage.toString().trim('[', ']')}""")
        license?.let { Text(text = """License: $license""") }
        website?.let { Text(text = """Website: $website""")}
    }
}


//add these parts if you want them to do something
//also add corresponding cases in the MDBlockChildren() Composable

/*
@Composable
fun MDIndentedCodeBlock(indentedCodeBlock: IndentedCodeBlock, modifier: Modifier = Modifier) {
    // Ignored
}
*/

/*
@Composable
fun MDThematicBreak(thematicBreak: ThematicBreak, modifier: Modifier = Modifier) {
    //Ignored
}*/

@Composable
fun MDTableBlock(tabsViewModel: TabsViewModel, tableBlock: TableBlock) {
    val header: TableRow = (tableBlock.firstChild.firstChild) as TableRow
    val bodyRow: TableRow = (tableBlock.lastChild.firstChild) as TableRow
    val headerEntry: MutableList<AnnotatedString> = mutableListOf()
    val bodyEntry: MutableList<AnnotatedString> = mutableListOf()
    val body: MutableList<List<AnnotatedString>> = mutableListOf()
    var cell: TableCell? = header.firstChild as TableCell
    while (cell != null) {
        val styledText: AnnotatedString = buildAnnotatedString {
            appendMarkdownChildren(cell, MaterialTheme.colorScheme)
        }
        headerEntry.add(styledText)
        cell = cell.next as TableCell?
    }
    var tmpTableRow: TableRow? = bodyRow
    while (tmpTableRow != null) {
        var cell2: TableCell? = tmpTableRow.firstChild as TableCell
        while (cell2 != null) {
            bodyEntry.add((buildAnnotatedString { appendMarkdownChildren(cell2, MaterialTheme.colorScheme) }))
            cell2 = cell2.next as TableCell?
        }
        body += ArrayList(bodyEntry)
        bodyEntry.clear()
        tmpTableRow = tmpTableRow.next as TableRow?
    }

    Table(tabsViewModel, header = headerEntry, data = body)
}


@Composable
fun MDBlockChildren(tabsViewModel: TabsViewModel, parent: Node) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is BlockQuote -> MDBlockQuote(child)
            is Heading -> MDHeading(tabsViewModel, child)
            is Paragraph -> MDParagraph(tabsViewModel, child)
            is FencedCodeBlock -> MDFencedCodeBlock(child)
            is YamlFrontMatterBlock -> MDYamlFrontMatter(tabsViewModel, child)
            is Image -> MDImage(tabsViewModel, child, hasAttributes = false)  //Image attributes are only use if there is a single image paragraph
            is BulletList -> MDBulletList(tabsViewModel, child)
            is OrderedList -> MDOrderedList(tabsViewModel, child)
            is TableBlock -> MDTableBlock(tabsViewModel, child)
            is ThematicBreak -> MDThematicBreak()
            is IndentedCodeBlock -> MDIndentedCodeBlock(child)
            // all other cases are skipped
            else -> { println("${child.javaClass} Block is not yet implemented") }
        }
        child = child.next
    }
}


@Composable
fun mapOfWithLatex(tabsViewModel: TabsViewModel, style: TextStyle): Map<String, InlineTextContent> {
    val pairsList = ArrayList<Pair<String, InlineTextContent>>()
    val imageUrlPair = Pair(TAG_IMAGE_URL,
        InlineTextContent(Placeholder(style.fontSize, style.fontSize, PlaceholderVerticalAlign.Bottom)) {
            val picture: Picture = loadFullImage(tabsViewModel, it)
            Image(bitmap = picture.image, contentDescription = picture.name, alignment = Alignment.Center)
        })
    pairsList.add(imageUrlPair)

    for (x in latexList) {
        /*px = em * font-size*/
        val picture: Picture = loadFullImage(tabsViewModel, x)

        val tempPair = Pair(x, InlineTextContent(
                Placeholder(
                    ((picture.width / style.fontSize.value) * 0.4).em,
                    ((picture.height / style.fontSize.value) * 0.4).em,
                    PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Image(bitmap = picture.image, contentDescription = picture.name, alignment = Alignment.Center)
            })
        pairsList.add(tempPair)
    }
    return pairsList.associate { it.first to it.second }
}


@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalComposeUiApi::class) //TODO rewrite pointerMoveFilter to be stable
@Composable
fun MarkdownText(
    tabsViewModel: TabsViewModel,
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    //val uriHandler = UriHandlerAmbient.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    var position = Int.MAX_VALUE
    //correct indexes get set later on, just check if there are any tags to process
    var annotations = text.getStringAnnotations(1, position)
    var tag = annotations.firstOrNull()?.tag
    var url: String? = annotations.firstOrNull()?.item

    Text(text = text, modifier = modifier.onPointerEvent(PointerEventType.Move) {
        //get the correct annotation depending on position set in onMove lambda
        val pos = it.changes.first().position
        //here the correct position to choose indices for clickable is set
        layoutResult.value?.let { position = it.getOffsetForPosition(pos) }
            .also { //get the correct annotation depending on position set in onMove lambda
                annotations = text.getStringAnnotations(position, position)
                tag = annotations.firstOrNull()?.tag
                url = annotations.firstOrNull()?.item
            }
        }
        .then(
            if (tag == TAG_URL) {
                Modifier.pointerInput({}){
                        detectTapGestures(onTap = { if(url != null){
                            checkAfterLinkClick(tabsViewModel, url!!) }
                        })
                    }
            } else { Modifier }
        ),
        style = style,
        inlineContent = mapOfWithLatex(tabsViewModel, style),
        onTextLayout = { layoutResult.value = it }
    )
}


@Composable
fun Table(
    tabsViewModel: TabsViewModel,
    header: List<AnnotatedString>? = null,
    data: List<List<AnnotatedString>>,
    headerModifier: Modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
    rowModifier: Modifier = Modifier,
) {
    // padding end should be (amount_of_buttons * (button_with + 2*button_padding))
    Column(Modifier.padding(start = 0.dp, top = 5.dp, end = 40.dp, bottom = 20.dp)) {
        if (!header.isNullOrEmpty()) {
            Row(modifier = headerModifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                for (e in header) {
                    Box(
                        modifier = Modifier.weight(1f / header.size).border(width = 1.dp, color = Color.Gray)
                            .fillMaxHeight()
                    ) {
                        MarkdownText(tabsViewModel, e, modifier = Modifier.padding(3.dp), style = MDTypography.h4.style)
                    }
                }
            }
        }

        for (row in data) {
            Row(modifier = rowModifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                for (e in row) {
                    Box(
                        modifier = Modifier.weight(1f / row.size).border(width = 1.dp, color = Color.Gray)
                            .fillMaxHeight()
                    ) {
                        MarkdownText(tabsViewModel, e, modifier = Modifier.padding(3.dp), style = MDTypography.bodyMedium.style)
                    }
                }
            }
        }
    }
}


/**
 * Switches to other Tab (which has file name) or opens a url.
 * @param tabsViewModel View model of the tabs
 * @param url a file name of the project or a url
 */
fun checkAfterLinkClick(tabsViewModel: TabsViewModel, url:String) {
    if(url[0]=='#') {
        scrollToItem.value = referenceModel.getIndexOfNewActiveElement(url)
    } else{
        val localFile = tabsViewModel.sessionState.value.project?.directory?.resolve(url)
        if (localFile != null && localFile.exists()) {
            val active = tabsViewModel.findTabIndexByName(localFile.toFile().name)
            if (active >= 0) { 
                tabsViewModel.selectedIndex.value = active
                scrollToItem.value = 0
            }
        } else {
            try { Desktop.getDesktop().browse(URI(url)) }
            catch (_: Exception) {
                logger.error("$url is not a URL")
            }
        }
    }
}