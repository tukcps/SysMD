package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.services.repositories.local.Language.Companion.toLanguage
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterNode
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.*
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import kotlin.uuid.Uuid


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
fun Path.getCells(): List<ElementData> {
    val input: String = if (SystemFileSystem.exists(this)) {
        SystemFileSystem.source(this).buffered().use { it.readString() }
    } else ""

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
                cells.add(
                    ElementData(
                        Uuid.random(),
                        type = ElementType.TextualRepresentation,
                        language = language,
                        body = node.literal.trim('\n')
                    )
                )
                afterCodeBlock = true
            }
            is Heading -> {
                afterCodeBlock = false
                beforeFirstHeading = false
                val str = getMdSource(node, inputLines)
                cells.add(
                    ElementData(
                        Uuid.random(),
                        type = ElementType.TextualRepresentation,
                        language = "Markdown",
                        body = str
                    )
                )
            }
            is YamlFrontMatterBlock -> {
                if (afterCodeBlock || beforeFirstHeading) {
                    val str = getMdSource(node, inputLines)
                    cells.add(
                        ElementData(
                            Uuid.random(),
                            type = ElementType.TextualRepresentation,
                            language = Language.YAML.toString(),
                            body = str
                        )
                    )
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
                    cells.add(
                        ElementData(
                            Uuid.random(),
                            type = ElementType.TextualRepresentation,
                            language = Language.MARKDOWN.toString(),
                            body = str
                        )
                    )
                    afterCodeBlock = false
                }
            }
        }
        node = node.next
    }
    return cells
}

/**
 * Transform a list of textual representations in different languages,
 * or documentations to a Markdown string.
 * @param cells list of reps or docs,
 */
fun toMarkdownString(cells: List<ElementData>): String {
    val str = StringBuilder()
    for (e in cells) {
        // The lines of the description section.
        val languageStr = e.language!!
        if (toLanguage(e.language!!) !in setOf(Language.MARKDOWN, Language.YAML))
            str.append("```$languageStr\n")
        str.append(e.body?.trimEnd('\n') + "\n")
        if (toLanguage(e.language!!) !in setOf(Language.MARKDOWN, Language.YAML))
            str.append("```\n")
    }
    return str.toString()
}

/** The languages handled in SysMD Notebook. */
enum class Language {
    MARKDOWN { override fun toString() = "Markdown" },
    KerML    { override fun toString() = "KerML" },
    SYS_MD   { override fun toString() = "SysMD" },
    SYS_ML   { override fun toString() = "SysML" },
    YAML     { override fun toString() = "YAML" }
    ;

    fun isCompilable() = this in setOf(KerML, SYS_MD, SYS_ML)

    companion object {
        val allLanguages = Language.entries
        val language: Map<String, Language> = allLanguages.associate { (it.toString() to it) }
        fun toLanguage(langPath: String): Language? = language[langPath.split("::", limit = 2).firstOrNull()]
        fun toNamespace(langPath: String): String? = langPath.split("::", limit = 2).getOrNull(1)
        fun languageWithNamespace(language: Language, namespace: String?): String =
            if (namespace.isNullOrBlank()) language.toString()
            else "$language::$namespace"
    }
}