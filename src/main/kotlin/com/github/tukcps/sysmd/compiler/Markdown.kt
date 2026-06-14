package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.repositories.local.ProjectUsageData
import com.github.tukcps.sysmd.services.repositories.local.getMdSource
import com.github.tukcps.sysmd.services.session.ProjectSession
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterNode
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.*
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import java.net.URI


/**
 * Parses the MD input string and adds textual representation elements.
 * After reading the Markdown input, there will be Annotation Elements that carry
 * SysMD-Code or Documentation in Markdown format.
 * @param input The input in Markdown format.
 * @param createTextualRepresentationIn The namespace in the KerML model into which Textual Representations and
 * Documentation elements will be added.
 */
fun ProjectSession.importMD(input: String, createTextualRepresentationIn: Namespace?) {
    val inputLines = input.lines()

    val extensions: List<Extension> = listOf(TablesExtension.create(), YamlFrontMatterExtension.create())

    // We use the Commonmark Markdown-Parser and include SourceSpans.
    val mdParser: Parser = Parser.builder()
        .extensions(extensions)
        .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build()

    val document: Node = mdParser.parse(input)

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
                    addOwnedMember(TextualRepresentationImplementation(language = language, body = node.literal.trim('\n')), createTextualRepresentationIn)
                afterCodeBlock = true
            }
            is Heading -> {
                afterCodeBlock = false
                beforeFirstHeading = false
                val str = getMdSource(node, inputLines)
                if (createTextualRepresentationIn != null)
                    addOwnedMember(TextualRepresentationImplementation(language = "Markdown", body = str), createTextualRepresentationIn)
            }
            is YamlFrontMatterBlock -> {
                if (afterCodeBlock || beforeFirstHeading) {
                    val str = getMdSource(node, inputLines)
                    if (createTextualRepresentationIn != null)
                        addOwnedMember(TextualRepresentationImplementation(language = Language.YAML.toString(), body=str), createTextualRepresentationIn)
                    afterCodeBlock = false
                }
                var yaml = node.firstChild as YamlFrontMatterNode?
                while (yaml != null) {
                    try {
                        when (yaml.key) {
                            "usage"      -> yaml.values.firstOrNull()?.let { it.split(",").forEach { str -> project.addUsage(ProjectUsageData(URI(str.trim()))) } }
                        }
                    } catch (e: Exception) {
                        status.fatal(message = "Error while parsing YAML", cause = e)
                    }
                    yaml = yaml.next as YamlFrontMatterNode?
                }
            }
            is Block -> {
                // FencedCodeBlock and Heading include all respective
                if (afterCodeBlock || beforeFirstHeading) {
                    val str = getMdSource(node, inputLines)
                    if (createTextualRepresentationIn != null)
                        addOwnedMember(TextualRepresentationImplementation(language = Language.MARKDOWN.toString(), body=str), createTextualRepresentationIn)
                    afterCodeBlock = false
                }
            }
        }
        node = node.next
    }
}
