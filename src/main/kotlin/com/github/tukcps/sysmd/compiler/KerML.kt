@file:Suppress("MemberVisibilityCanBePrivate", "FunctionName")

package com.github.tukcps.sysmd.compiler

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.compiler.parser.kerml.ConstInt
import com.github.tukcps.sysmd.compiler.parser.kerml.ConstReal
import com.github.tukcps.sysmd.compiler.parser.kerml.NamespaceBodyElement
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.util.ParserProductionRules
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.compiler.semantics.SemanticActionsImplementation
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.session.report
import com.github.tukcps.sysmd.services.session.Session


/**
 * This class provides a parser for the language SysMLv2 and SysMD.
 * The parser uses the recursive descent method.
 * It inherits some infrastructure from KParser, in particular DSL functions
 * for modeling production rules (lambda parameters for functions), with the help of current and lookahead
 * token tok:
 *
 *  [ production ]       -> optional(tok)  { production }
 *  [ production ]+      -> oneOrMore(tok) { production }
 *  [ production ]*      -> noOrMore(tok)  { production }
 *  (p1|p2|p3...|others) -> alternatives { start tok1 {p1} tok2 {p2} ... }
 *
 *  consume()/consume(to) allow consuming a specific or arbitrary token.
 *
 * Details for these functions are given in the KParser class.
 * Examples for application of the parser are in the unit test.
 * Parameters are:
 * @param model: the model in which the result will be saved; by default the memory-only model
 * @param indices: can restrict parsing of the textualRepresentation's body to a substring, if given
 * @param generateAnnotations: if true, the semantics will generate annotations that link the textual
 * representation and the generated elements.
 */
open class KerML(
    val model: Session,                                 // model in which the results will be returned.
    indices: IntRange? = null,                          // allows us to select a subset to be parsed, i.e., an expression.
    val generateAnnotations: Boolean = false
) : ParserProductionRules(indices) {

    var ownerPrefix: String? = null
    var semantics: SemanticActions = SemanticActionsImplementation(model, generateAnnotations = generateAnnotations)

    /**
     * Parses the body of the textual representation while considering the owner prefix.
     */
    fun parse(textualRepresentation: TextualRepresentation) {
        this.input = textualRepresentation.body
        ownerPrefix = textualRepresentation.getOwnerPrefix()
        semantics.initOwners(ownerPrefix!!)
        semantics.textualRepresentation = textualRepresentation
        parse()
    }

    /**
     * Parsers an input directly.
     * No TextualRepresentation is created, and no annotations, etc.
     */
    fun parse(input: CharSequence) {
        this.input = input
        parse()
    }


    // Re-definition of the Parser template's error message function
    override var error = fun(message: String) { throw SyntaxError(this, message = message) }

    /**
     *    RootNamespace :- ( NamespaceBodyElement )* EOF
     */
    open fun parse() {
        noOrMore(stop = EOF) {
            try {
                NamespaceBodyElement()   // KerML textual
            } catch (exception: Exception) {
                handleError(exception)
                semantics.initOwners(ownerPrefix?:"::Global")
            }
        }
        try {
            EOF.consume()
        } catch (exception: Exception) {
            handleError(exception)
            semantics.initOwners(ownerPrefix?:"::Global")
        }
    }


    /**
     * QualifiedNameList :- QualifiedName ( "," QualifiedName )*
     * Semantics: returns a list of identifications that have been parsed.
     */
    fun QualifiedNameList(): MutableList<QualifiedName> {
        val result = mutableListOf<QualifiedName>()
        QualifiedName().also { result.add(it) }
        noOrMore(start = COMMA, consume = true) {
            QualifiedName().also { result.add(it) }
        }
        return result
    }



    /**
     * ValueRange :- [-] ValueLiteral [.. [-] ValueLiteral]
     **/
    fun parseValueRange(): Quantity {
        var result: Quantity
        var minus = false
        optional(MINUS, consume = true) { minus = true  }
        if (tokenIs(INTEGER_LIT)) {
            ConstInt().also { result = Quantity(model.builder.integer((if (minus) -it else it)..if (minus) -it else it)) }
        } else if (tokenIs(FLOAT_LIT)) {
            ConstReal().also { result = Quantity(model.builder.real((if (minus) -it else it)..if (minus) -it else it),"?") }
        } else
            throw SyntaxError(this, "expect value-range of form number literal .. number literal")

        if (!tokenIs(DOTDOT)) {
            return result
        }
        else { // ".." [-] ValueLiteral
            DOTDOT.consume()
            var minusUb = false
            optional(MINUS, consume = true) { minusUb = true}
            alternatives {
                INTEGER_LIT starts {
                    ConstInt().also {
                        result = when (result.value) {
                            is AADD -> Quantity(model.builder.real(result.aadd().getRange().min..if (minusUb) -it.toDouble() else it.toDouble()), "?")
                            is IDD -> Quantity(model.builder.integer(result.idd().getRange().min..if (minusUb) -it else it))
                            else -> throw SyntaxError(this@KerML, "expect range of form [number .. number]")
                        }
                    }
                }
                FLOAT_LIT starts {
                    ConstReal().also {
                        result = when (result.value) {
                            is AADD -> Quantity(model.builder.real(result.aadd().getRange().min..if (minusUb) -it else it), "?")
                            is IDD -> Quantity(model.builder.real(result.idd().getRange().min.toDouble()..if (minusUb) -it else it), "?")
                            else -> throw SyntaxError(this@KerML, "expect range of form [number .. number]")
                        }
                    }
                }
            }
            return result
        }
    }

    /**
     * Enters an error message in the status and tries to re-sync with stream of token.
     * It does so by reading until reaching a DOT which marks the end of a triple.
     * @param exception Exception that was thrown and caught prior to starting error handling
     */
    internal fun handleError(exception: Exception) {

        // report error.
        if (exception is SysMDException) {
            model.report(semantics.textualRepresentation, exception.message, exception)
        } else
            model.report(SysMDError(textualRepresentation = semantics.textualRepresentation, message = "Exception: ${exception.message}", cause = exception))
        // Skip input until we get the next DOT (=end of triple) or RCURBRACE or EOF.
        while (token.kind != SEMICOLON && token.kind != EOF && token.kind != RCURBRACE)
            consume()
        consume()
    }

    override fun toString(): String {
        return "Parser at token '${token.string}' in line ${token.lineNo}; exceptions: ${model.status.exceptions.size}"
    }

    /**
     * Utility function
     */
    fun Set<Token.Kind>.optional(rule: KerML.() -> Unit = { }) {
        if (token.kind in this) {
            this@KerML.rule()
        }
    }
}
