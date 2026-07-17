package com.github.tukcps.sysmd.model.generator.migration

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension

/**
 * Scans existing Kotlin model sources.
 */
class ExistingSourceScanner(
    private val directories: List<Path>
) {

    /**
     * Signature of an existing Kotlin operation.
     *
     * @param name Operation name.
     * @param parameterTypes Parameter types.
     */
    data class OperationSignature(
        val name: String,
        val parameterTypes: List<String>
    )

    /**
     * Information about an existing implementation class.
     *
     * @param name Implementation class name.
     * @param packageName Kotlin package name.
     * @param superTypes Direct supertypes.
     * @param properties Declared override properties.
     * @param operations Declared override operations.
     * @param file Source file.
     */
    data class Implementation(
        val name: String,
        val packageName: String,
        val superTypes: List<String>,
        val properties: Set<String>,
        val operations: Set<OperationSignature>,
        val file: Path
    )

    /**
     * Returns all existing model interfaces.
     *
     * @return Interface names.
     */
    fun interfaces(): Set<String> =
        sourceFiles()
            .flatMap { file ->
                INTERFACE.findAll(Files.readString(file))
                    .map { it.groupValues[1] }
            }
            .toSet()

    /**
     * Returns all existing implementation classes.
     *
     * @return Implementations by class name.
     */
    fun implementations(): Map<String, Implementation> =
        buildMap {
            sourceFiles().forEach { file ->
                val source = Files.readString(file)
                val packageName = packageName(source, file)

                CLASS.findAll(source).forEach { match ->
                    val name = match.groupValues[1]
                    if (!name.endsWith("Implementation")) return@forEach

                    val declarationStart = match.range.last + 1
                    val bodyStart = classBodyStart(source, declarationStart)
                    val declaration = source.substring(declarationStart, bodyStart)
                    val body = classBody(source, bodyStart)

                    put(
                        name,
                        Implementation(
                            name = name,
                            packageName = packageName,
                            superTypes = superTypes(source, declarationStart),
                            properties = properties(declaration, body),
                            operations = operations(body),
                            file = file
                        )
                    )
                }
            }
        }

    /**
     * Returns the Kotlin package name.
     *
     * @param source Kotlin source.
     * @param file Source file.
     * @return Package name.
     */
    private fun packageName(
        source: String,
        file: Path
    ): String =
        PACKAGE.find(source)
            ?.groupValues
            ?.get(1)
            ?: error("Package declaration not found in $file")

    /**
     * Returns direct supertypes following a class declaration.
     *
     * @param source Kotlin source.
     * @param start Position following the class name.
     * @return Direct supertype names.
     */
    private fun superTypes(source: String, start: Int): List<String> {
        var index = start

        while (index < source.length && source[index].isWhitespace()) index++

        if (index < source.length && source[index] == '<')
            index = skipBalanced(source, index, '<', '>')

        while (index < source.length && source[index].isWhitespace()) index++

        if (index < source.length && source[index] == '(')
            index = skipBalanced(source, index, '(', ')')

        while (index < source.length && source[index].isWhitespace()) index++

        if (index >= source.length || source[index] != ':') return emptyList()

        val end = classBodyStart(source, index)

        return splitTopLevel(source.substring(index + 1, end))
            .map { it.substringBefore('(').trim().substringAfterLast('.') }
            .filter { it.isNotEmpty() }
    }

    /**
     * Returns the start of a class body.
     *
     * @param source Kotlin source.
     * @param start Search start.
     * @return Position of the opening brace.
     */
    private fun classBodyStart(source: String, start: Int): Int {
        var parentheses = 0
        var angles = 0
        var index = start

        while (index < source.length) {
            when (source[index]) {
                '(' -> parentheses++
                ')' -> parentheses--
                '<' -> angles++
                '>' -> angles--
                '{' -> if (parentheses == 0 && angles == 0) return index
            }
            index++
        }

        return source.length
    }

    /**
     * Returns a class body.
     *
     * @param source Kotlin source.
     * @param start Opening brace position.
     * @return Class body.
     */
    private fun classBody(source: String, start: Int): String {
        if (start >= source.length) return ""

        val end = skipBalanced(source, start, '{', '}')

        return source.substring(
            start + 1,
            (end - 1).coerceAtLeast(start + 1)
        )
    }

    /**
     * Returns declared override properties.
     *
     * Properties in the primary constructor and class body are included.
     *
     * @param declaration Class declaration.
     * @param body Class body.
     * @return Property names.
     */
    private fun properties(
        declaration: String,
        body: String
    ): Set<String> =
        PROPERTY.findAll("$declaration\n$body")
            .map { it.groupValues[1] }
            .toSet()

    /**
     * Returns declared override operations.
     *
     * @param source Class body.
     * @return Operation signatures.
     */
    private fun operations(source: String): Set<OperationSignature> =
        FUNCTION.findAll(source)
            .map { match ->
                OperationSignature(
                    name = match.groupValues[1],
                    parameterTypes = parameterTypes(match.groupValues[2])
                )
            }
            .toSet()

    /**
     * Returns parameter types of a Kotlin parameter list.
     *
     * @param source Parameter list.
     * @return Parameter types.
     */
    private fun parameterTypes(source: String): List<String> =
        splitTopLevel(source)
            .mapNotNull { parameter ->
                parameter.substringAfter(':', "")
                    .substringBefore('=')
                    .trim()
                    .takeIf { it.isNotEmpty() }
            }

    /**
     * Splits Kotlin source at top-level commas.
     *
     * @param source Kotlin source.
     * @return Source parts.
     */
    private fun splitTopLevel(source: String): List<String> {
        val result = mutableListOf<String>()
        var parentheses = 0
        var angles = 0
        var brackets = 0
        var start = 0

        source.forEachIndexed { index, char ->
            when (char) {
                '(' -> parentheses++
                ')' -> parentheses--
                '<' -> angles++
                '>' -> angles--
                '[' -> brackets++
                ']' -> brackets--
                ',' -> if (
                    parentheses == 0 &&
                    angles == 0 &&
                    brackets == 0
                ) {
                    result += source.substring(start, index)
                    start = index + 1
                }
            }
        }

        result += source.substring(start)
        return result
    }

    /**
     * Skips a balanced character pair.
     *
     * @param source Kotlin source.
     * @param start Opening character position.
     * @param open Opening character.
     * @param close Closing character.
     * @return Position following the closing character.
     */
    private fun skipBalanced(
        source: String,
        start: Int,
        open: Char,
        close: Char
    ): Int {
        var depth = 0
        var index = start

        while (index < source.length) {
            when (source[index]) {
                open -> depth++
                close -> {
                    depth--
                    if (depth == 0) return index + 1
                }
            }

            index++
        }

        return source.length
    }

    /**
     * Returns all Kotlin source files.
     *
     * @return Kotlin source files.
     */
    private fun sourceFiles(): Sequence<Path> =
        directories.asSequence()
            .filter(Files::exists)
            .flatMap { directory ->
                Files.walk(directory).use { paths ->
                    paths.filter {
                        Files.isRegularFile(it) && it.extension == "kt"
                    }.toList().asSequence()
                }
            }

    private companion object {
        val PACKAGE = Regex(
            """\bpackage\s+([A-Za-z_][A-Za-z0-9_.]*)"""
        )

        val INTERFACE = Regex(
            """\binterface\s+([A-Za-z_][A-Za-z0-9_]*)"""
        )

        val CLASS = Regex(
            """\bclass\s+([A-Za-z_][A-Za-z0-9_]*)"""
        )

        val PROPERTY = Regex(
            """\boverride\s+(?:public\s+|protected\s+|internal\s+)?""" +
                    """(?:val|var)\s+([A-Za-z_][A-Za-z0-9_]*)"""
        )

        val FUNCTION = Regex(
            """\boverride\s+(?:public\s+|protected\s+|internal\s+)?""" +
                    """fun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\)"""
        )
    }
}