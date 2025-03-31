package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterNode
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.*
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import java.io.File
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
fun getMdSource(node: Node, inputLines: List<String>): String {
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
 * Parses the MD input string and adds textual representation elements.
 * After reading the Markdown input, there will be Annotation Elements that carry
 * SysMD-Code or Documentation in Markdown format.
 * @return a list of Cells, each an ElementData object
 */
fun File.getCells(): List<ElementData> {
    val input = this.inputStream().bufferedReader().readText()
    val inputLines = input.lines()
    val cells = mutableListOf<ElementData>()

    // We use the Commonmark Markdown-Parser and include SourceSpans.
    val extensions: List<Extension> = listOf(TablesExtension.create(), YamlFrontMatterExtension.create())
    val parser: Parser = Parser.builder()
        .extensions(extensions)
        .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build()

    val document: Node = parser.parse(input)

    // The currently edited heading level. As a hashmap Level -> Namespace.
    require(document is Document)
    var node = document.firstChild
    var afterCodeBlock = false
    var beforeFirstHeading = true
    while(node != null) {
        when(node) {
            is ThematicBreak -> { beforeFirstHeading = false }
            is FencedCodeBlock -> {
                val language = node.info
                cells.add(ElementData(UUID.randomUUID(), type = "TextualRepresentation", language = language, body = node.literal.trim('\n')))
                afterCodeBlock = true
            }
            is Heading -> {
                afterCodeBlock = false
                beforeFirstHeading = false
                val str = getMdSource(node, inputLines)
                cells.add(ElementData(UUID.randomUUID(), type = "TextualRepresentation", language = "Markdown", body = str))
            }
            is YamlFrontMatterBlock -> {
                if (afterCodeBlock || beforeFirstHeading) {
                    val str = getMdSource(node, inputLines)
                    cells.add(ElementData(UUID.randomUUID(), type = "TextualRepresentation", language = Language.YAML.toString(), body=str))
                    afterCodeBlock = false
                }
                var yaml = node.firstChild as YamlFrontMatterNode?
                while (yaml != null) {
                    when (yaml.key) { else -> {} }
                    yaml = yaml.next as YamlFrontMatterNode?
                }
            }
            is Block -> {
                // FencedCodeBlock and Heading include all respective
                if (afterCodeBlock || beforeFirstHeading) {
                    val str = getMdSource(node, inputLines)
                    cells.add(ElementData(UUID.randomUUID(), type = "TextualRepresentation", language = Language.MARKDOWN.toString(), body=str))
                    afterCodeBlock = false
                }
            }
        }
        node = node.next
    }
    return cells
}
