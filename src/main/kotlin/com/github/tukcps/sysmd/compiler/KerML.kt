@file:Suppress("MemberVisibilityCanBePrivate", "FunctionName")

package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.compiler.parser.kerml.NamespaceBodyElement
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.kerml.legacy.ConstInt
import com.github.tukcps.sysmd.compiler.parser.kerml.legacy.ConstReal
import com.github.tukcps.sysmd.compiler.parser.util.ParserProductionRules
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicy
import com.github.tukcps.sysmd.compiler.semantics.fixIDs
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.session.*
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD

/**
 * This class provides a parser for the language SysML v2 and SysMD.
 * The parser uses the recursive descent method.
 * It inherits infrastructure from ParserProductionRules, in particular DSL functions
 * for modeling production rules (lambda parameters for functions), with the help of current and lookahead
 * token tok:
 *
 *   - [ production ]       -> optional(tok)  { production }
 *   - [ production ]+      -> oneOrMore(tok) { production }
 *   - [ production ]*      -> noOrMore(tok)  { production }
 *   - (p1|p2|p3...|others) -> alternatives { start tok1 {p1} tok2 {p2} ... }
 *   - consume()/consume(to) allow consuming a specific or arbitrary token.
 *
 * Details for these functions are given in the ParserProductionRules class.
 * Examples for the use of the parser are in the unit test.
 * Parameters are:
 * @param status: an object in which issues are reported, and results are given, including
 * the generated ElementData.
 * @param keywords the keywords for the ParserProductionRules
 */
open class KerML(
    @Deprecated("dont use ")
    val model: Session = SessionImplementation(), // Just for transition.
    val status: SessionStatus = model.status,
    val settings: SessionSettings = model.settings,
    val uuidPolicy : UuidPolicy = UuidPolicies.NewSysMD,
    keywords: Map<String, Token.Kind> = Token.kerMLKeywords
) : ParserProductionRules(keywords = keywords) {

    /**
     *  For setting options by .settings { ... }
     */
    fun settings(block: SessionSettings.() -> Unit): KerML {
        settings.block()
        return this
    }

    /**
     * If true, don't resolve any qualified names and produce `RawNameExpression` instead.
     * Hack to support some legacy syntax.
     */
    var unresolvedNamesMode = false
        private set

    val semantics = ActionsContext(this)

    /**
     * Parses an input directly given as a CharSequence.
     * @param input the char sequence that is parsed
     * @param ownerQualifiedName the qualified name of the package that gives the scope.
     */
    fun parse(
        input: String,
        ownerQualifiedName: QualifiedName? = null,
    ): List<ElementData>{
        this.input = input
        semantics.initOwningNamespaces(ownerQualifiedName)
        parse()
        uuidPolicy.fixIDs(semantics.elementsBuilt)
        status.elementsBuilt.addAll(semantics.elementsBuilt)
        return semantics.elementsBuilt
    }

    /**
     * Re-definition of the Parser template's error message function.
     * error is a lambda that is used for error reporting.
     */
    override fun error(message: String, exception: Exception)
    {
        nextToken()
        status.error(
            kind = Issue.Kind.ERROR_SYNTACTICAL,
            message = message,
            element = semantics.element,
            cause = exception
        )
    }

    /**
     * Starting rule for KerML models.
     *
     *    RootNamespace :- ( NamespaceBodyElement )* EOF
     */
    open fun parse()  {
        noOrMore(stop = EOF) {
            try {
                NamespaceBodyElement()   // KerML textual
            } catch (exception: Exception) {
                handleError(exception)
                semantics.initOwningNamespaces()
            }
        }
        try {
            EOF.consume()
        } catch (exception: Exception) {
            handleError(exception)
        }
    }

    /**
     *          QualifiedNameList :- QualifiedName ("," QualifiedName )*
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
     * Enters an error message in the status and tries to re-sync with a stream of token.
     * It does so by reading until reaching a semicolon or curly brace.
     * @param exception Exception that was thrown and caught prior to starting error handling
     */
    internal fun handleError(exception: Exception) {

        // report error.
        if (exception is SysMDException) {
            status.error(exception.message, semantics.element, cause = exception, kind = exception.kind)
        } else
            status.fatal(exception.message?: "Unknown error",  this, cause = exception)
        // Skip input until we get the next DOT (=end of triple) or RCURBRACE or EOF.
        var nested = 0
        while (
            (token.kind != SEMICOLON || nested > 0)
            && token.kind != EOF
            && (token.kind != RCURBRACE || nested <= 0))  {
            if (token.kind == LCURBRACE) nested += 1
            if (token.kind == RCURBRACE) nested -= 1
            consume()
        }
        // If parser skips right curly brace, we need to also pop one from the owner stack.
        // if (token.kind == RCURBRACE)
        //    semantics.popOwner()
        consume()
        if (token .kind == EOF)
            status.error( "Unexpected end of input", semantics.element, cause = exception)
    }

    internal fun handleSyntaxError(message: String) {
        status.error(message, element = semantics.element, kind=Issue.Kind.ERROR_SYNTACTICAL)
        // Skip input until we get the next DOT (=end of triple) or RCURBRACE or EOF.
        var nested = 0
        while (
            (token.kind != SEMICOLON || nested > 0)
            && token.kind != EOF
            && (token.kind != RCURBRACE || nested <= 0)
        )  {
            if (token.kind == LCURBRACE) nested += 1
            if (token.kind == RCURBRACE) nested -= 1
            consume()
        }
        // If parser skips right curly brace, we need to also pop one from the owner stack.
        // if (token.kind == RCURBRACE)
        // semantics.popOwner()
        consume()
        if (token .kind == EOF)
            status.error(kind = Issue.Kind.ERROR_SYNTACTICAL, message = "Unexpected end of input", element = semantics.element)
    }

    override fun toString(): String {
        return "Parser at '${token.string}', line ${token.lineNo}, #issues: ${status.issues.size}"
    }

    /**
     * Utility function
     */
    fun Set<Token.Kind>.optional(rule: KerML.() -> Unit = { }) {
        if (token.kind in this) {
            this@KerML.rule()
        }
    }

    /** Executes a production in which names should not be resolved.
     * Only toggles `unresolvedNamedMode` flag, the used production rule has to respect that setting.
     */
    fun<R> withUnresolvedNames(condition : Boolean = true, body : KerML.() -> R) : R
    = if(!condition || this.unresolvedNamesMode) body() else {
        this.unresolvedNamesMode = true
        try { body() } finally { this.unresolvedNamesMode = false }
    }

    /**
     * Executes semantic action in the context ActionsContext.
     */
    inline fun <T> T.semantics(
        block: ActionsContext.(T) -> Unit
    ): T {
        semantics.block(this)
        return this
    }
}
