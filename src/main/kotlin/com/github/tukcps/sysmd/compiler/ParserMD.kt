package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.exceptions.SysMDInfo
import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.repositories.local.ProjectUsageData
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterNode
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.*
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import java.net.URI
import java.util.*


/**
 * Walks over the MD AST that is a tree of the syntactical elements;
 * it does NOT have a hierarchy leaning towards headings etc.
 * However, we create a hierarchical level whenever a Heading is read.
 * This function gets a source span between Heading/CodeBlock a separate Textual elements.
 * @param node
 * @param inputLines A list with all input lines.
 * @return
 */
private fun getMdSource(node: Node, inputLines: List<String>): String {
    // Str collects the processed textual representation from MD.
    var str = ""
    val firstLineIndex = node.sourceSpans.first().lineIndex

    if (node is YamlFrontMatterBlock) {
        val lineIndex = node.sourceSpans.first().lineIndex
        val lastLineIndex = node.sourceSpans.last().lineIndex
        for (i in lineIndex .. lastLineIndex)
            str += inputLines[i] + "\n"
        return str
    }

    var iter: Node? = node.next
    var last: Node = node
    while(iter != null) {
        when (iter) {
            // Overall document; top-level.
            // We just iterate over the different syntactical elements of an MD document.
            is Heading, is FencedCodeBlock -> {
                val lineIndex = iter.sourceSpans.first().lineIndex-1
                for (i in firstLineIndex .. lineIndex)
                    str += inputLines[i] + "\n"
                return str
            }

            // other Blocks, we just collect lines of its sourceSpans and add it to str ...
            else -> {}
        }
        last = iter
        iter = iter.next
    }
    val lineIndex = last.sourceSpans.last().lineIndex
    for (i in firstLineIndex .. lineIndex)
        str += inputLines[i] + "\n"
    return str
}


/**
 * Splits an MD Document, in particular, read from a file, into
 * separate source cells including its language annotation.
 * @param markdownString string with the Markdown text
 * @return List of TextualRepresentation for Notebook
 */
fun splitMarkdown(markdownString: String): List<AnnotatingElement> {
    val markdownStringLines = markdownString.lines()
    val cells: MutableList<TextualRepresentation> = mutableListOf()
    val extensions: List<Extension> = listOf(TablesExtension.create(), YamlFrontMatterExtension.create())

    // We use the Commonmark Markdown-Parser and include SourceSpans.
    val parser: Parser = Parser.builder()
        .extensions(extensions)
        .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build()

    val markdownTree: Node = parser.parse(markdownString)
    require(markdownTree is Document)

    var node = markdownTree.firstChild
    var afterCodeBlock = false
    var beforeFirstHeading = true

    if (node is YamlFrontMatterBlock) {
        // FencedCodeBlock and Heading include all respective
        val str = getMdSource(node, markdownStringLines)
        cells.add(TextualRepresentationImplementation(language = Language.YAML.toString(), body=str))
        node = node.next
    }

    while(node != null) {
        when(node) {
            is ThematicBreak -> { beforeFirstHeading = false}
            is FencedCodeBlock -> {
                val language = node.info.ifEmpty { "SysMD" }
                cells.add(TextualRepresentationImplementation(language = language, body = node.literal.trim('\n')))
                afterCodeBlock = true
            }
            is Heading -> {
                afterCodeBlock = false
                beforeFirstHeading = false
                val str = getMdSource(node, markdownStringLines)
                cells.add(TextualRepresentationImplementation(language = Language.MARKDOWN.toString(), body = str))
            }
            is Block -> {
                // FencedCodeBlock and Heading include all respective
                if (afterCodeBlock || beforeFirstHeading) {
                    val str = getMdSource(node, markdownStringLines)
                    cells.add(TextualRepresentationImplementation(language = Language.MARKDOWN.toString(), body=str))
                    afterCodeBlock = false
                }
            }
        }
        node = node.next
    }
    return cells
}


/**
 * Parses the MD input string and adds textual representation elements.
 * After reading the Markdown input, there will be Annotation Elements that carry
 * SysMD-Code or Documentation in Markdown format.
 * @param input The input in Markdown format.
 * @param createTextualRepresentationIn The annotation in the KerML model into which Textual Representations and
 * Documentation elements will be added.
 */
fun Session.importMD(input: String, createTextualRepresentationIn: AnnotatingElement?) {
    val inputLines = input.lines()

    val extensions: List<Extension> = listOf(TablesExtension.create(), YamlFrontMatterExtension.create())

    // We use the Commonmark Markdown-Parser and include SourceSpans.
    val parser: Parser = Parser.builder()
        .extensions(extensions)
        .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build()

    val document: Node = parser.parse(input)

    // The currently edited heading level. As a hashmap Level -> Namespace.
    // val headings = HeadingMgr(owningAnnotation)

    require(document is Document)
    var node = document.firstChild
    var afterCodeBlock = false
    var beforeFirstHeading = true
    while(node != null) {
        when(node) {
            is ThematicBreak -> { beforeFirstHeading = false }
            is FencedCodeBlock -> {
                val language = node.info.ifEmpty { "SysMD" }
                if (createTextualRepresentationIn != null)
                    create(TextualRepresentationImplementation(language = language, body = node.literal.trim('\n')), createTextualRepresentationIn)
                afterCodeBlock = true
            }
            is Heading -> {
                afterCodeBlock = false
                beforeFirstHeading = false
                val str = getMdSource(node, inputLines)
                if (createTextualRepresentationIn != null)
                    create(TextualRepresentationImplementation(language = "Markdown", body = str), createTextualRepresentationIn)
            }
            is YamlFrontMatterBlock -> {
                if (afterCodeBlock || beforeFirstHeading) {
                    val str = getMdSource(node, inputLines)
                    if (createTextualRepresentationIn != null)
                        create(TextualRepresentationImplementation(language = TextualRepresentationViewModel.Companion.Language.YAML.toString(), body=str), createTextualRepresentationIn)
                    afterCodeBlock = false
                }
                var yaml = node.firstChild as YamlFrontMatterNode?
                while (yaml != null) {
                    try {
                        when (yaml.key) {
                            "name"       -> yaml.values.firstOrNull()?.let { project.name = it }
                            "id"         -> yaml.values.firstOrNull()?.let { project.id = UUID.fromString(it) }
                            "title"      -> yaml.values.firstOrNull()?.let { project.topic.add(it)}
                            "maintainer" -> yaml.values.firstOrNull()?.let { project.maintainer.addAll(it.split(","))}
                            "description"-> yaml.values.firstOrNull()?.let { project.description = it }
                            "version"    -> project.version = yaml.values.firstOrNull()?:"0"
                            "usage"      -> yaml.values.firstOrNull()?.let { it.split(",").forEach { str -> project.addUsage(
                                ProjectUsageData(URI(str.trim()))
                            ) } }
                            "files"      -> yaml.values.firstOrNull()?.let { it.split(",").forEach { str -> files.add(str.trim()) }}
                            "license"    -> project.license = yaml.values.firstOrNull()
                            "website"    -> yaml.values.firstOrNull()?.let { project.website = URI.create(it) }
                            "logo"       -> {}  // Handled in UI
                            else         -> report(SysMDInfo("Unknown YaML key: ${yaml.key}"))
                        }
                    } catch (e: Exception) {
                        report(null, message = "Error while parsing YAML", cause = e)
                    }
                    yaml = yaml.next as YamlFrontMatterNode?
                }
            }
            is Block -> {
                // FencedCodeBlock and Heading include all respective
                if (afterCodeBlock || beforeFirstHeading) {
                    val str = getMdSource(node, inputLines)
                    if (createTextualRepresentationIn != null)
                        create(TextualRepresentationImplementation(language = Language.MARKDOWN.toString(), body=str), createTextualRepresentationIn)
                    afterCodeBlock = false
                }
            }
        }
        node = node.next
    }
}
