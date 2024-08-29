@file:Suppress("MemberVisibilityCanBePrivate", "FunctionName")

package com.github.tukcps.sysmd.compiler

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.compiler.parser.*
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.sysmlv2.*
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.compiler.semantics.SemanticActionsImplementation
import com.github.tukcps.sysmd.compiler.semantics.SysMLv2Semantics
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.report
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
 * @param textualRepresentation: the textual representation of the source code as a KerML element
 * @param generateAnnotations: if true, the semantics will generate annotations between the textual
 * representation and the generated elements.
 */
class KerML(
    val model: Session,                            // model in which the results will be returned.
    val textualRepresentation: TextualRepresentation,   // the textual representation in which parsing is done.
    indices: IntRange? = null,                          // allows us to select a subset to be parsed, i.e., an expression.
    val generateAnnotations: Boolean = false
) : ParserProductionRules(textualRepresentation.body, indices) {

    // A class that implements the Semantic Actions on the KerML model.
    var semantics: SemanticActions = SemanticActionsImplementation(model, textualRepresentation, generateAnnotations = generateAnnotations)

    // A class that implements the semantic actions of SysML v2 productions
    var sysMLSemantics = SysMLv2Semantics(semantics)

    // Re-definition of the Parser template's error message function
    override var error = fun(message: String) { throw SyntaxError(this, message = message) }
    
    /**
     *    RootNamespace :- ( Triple | Element )* EOF
     */
    fun parseSysMD() {
        noOrMore(stop = EOF) {
            try {
                alternatives {
                    t1 = token.considerMetaKeywords()
                    NAME_LIT starts { Triple() }        // SysMD
                    others { NamespaceBodyElement() }   // KerML or SysML v2 textual
                }
            } catch (exception: Exception) {
                if (model.settings.catchExceptions) {
                    handleError(exception)
                    semantics.initOwners()
                } else throw exception
            }
        }
        try {
            EOF.consume()
        } catch (exception: Exception) {
            handleError(exception)
            semantics.initOwners()
        }
    }


    /**
     * Prefixes :- ("abstract", "in", "out", "inout", "end")*
     */
    fun FeaturePrefixes() {
        val featurePrefixes = setOf(ABSTRACT, COMPOSITE, DERIVED, END, IN, OUT, INOUT, PORTION, READONLY)
        while (token.kind in featurePrefixes) {
            semantics.prefixes.add(token.kind)
            consume()
        }
    }


    /**
     * MemberPrefix :- ("public" | "private" | "protected") "abstract"?
     */
    fun MemberPrefix() {
        alternatives {
            PUBLIC    starts { PUBLIC.consume();    semantics.visibilityKind = PUBLIC }
            PRIVATE   starts { PRIVATE.consume();   semantics.visibilityKind = PUBLIC }
            PROTECTED starts { PROTECTED.consume(); semantics.visibilityKind = PROTECTED }
            others           {                      semantics.visibilityKind = PUBLIC }
        }
        ABSTRACT.optional { semantics.prefixes.add(ABSTRACT) }
    }


    /**
     * KerML Elements; if it cannot be parsed properly as KerML, try SysML v2
     */
    fun NamespaceBodyElement() {
        MemberPrefix()
        var noKerML = false
        alternatives {
            NON_FEATURE_ELEMENT_TOKENS starts   { NonFeatureElement() }
            FEATURE_ELEMENT_TOKENS starts       { FeatureElement().also { noKerML = it } }
            ALIAS starts                        { AliasMember() }
            IMPORT starts                       { Import() }
            SEMICOLON then                      { /* Empty statement */ }
            // Else, we have a SysMLv2 Statement
            others                              { noKerML = true }
        }
        if (noKerML)
            PackageBodyElement()

        semantics.prefixes.clear()
    }


    /**
     * SysMD productions lean towards triples that give additional facts on existing elements.
     * The existing element is the subject and is identified by its qualified name.
     *
     * Triple :-
     *            QualifiedName hasA                FeatureList
     *          | QualifiedName imports             QualifiedNameList
     *          | QualifiedName defines             DefinitionList
     *
     * For implementation, we consider the special relations IS_A, HAS_A, IMPORTS, DEFINES separately.
     */
    fun Triple() {

        QualifiedName().also {
            semantics.pushOwner(Resolved(it))
        }

        alternatives {
            HAS_A then { ElementList() }
            DEFINES then { DefinitionList() }
            others {
                semantics.initOwners()
                throw SyntaxError(
                    this@KerML,
                    "Expecting a SysMD triple (isA, hasA, uses, imports, defines, user-defined, but read $consumedToken"
                )
            }
        }
        semantics.popOwner()
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
     * DefinitionList :- (Definition)*
     * Definition :- Class | Association
     */
    private fun DefinitionList() {
        noOrMore(end = { consumedToken.kind == DOT }) {
            alternatives {
                DEF starts { Class() }
                CLASS starts { Class() }
                DATATYPE starts { Class() }
                ASSOC starts { Association() }
                others { Class() }
            }
        }
    }


    /**
     * Parses an optional Multiplicity; if it is not present, the result is [1, 1]
     * Multiplicity :-
     *      ["[" (IntegerLiteral | "*") [".." (IntegerLiteral | "*" ] "]"]
     */
    fun Multiplicity(): IntegerRange {
        var multiplicity = IntegerRange(1, 1)
        optional(LCBRACE, consume = true) {
            parseIntegerRange().also { multiplicity = it }
            RCBRACE.consume()
        }
        return multiplicity
    }


    /**
     * IntegerRange :- ConstInt [".." ConstInt]
     */
    fun parseIntegerRange(): IntegerRange {
        val result = IntegerRange(IntegerRange.Integers)
        ConstInt().also { result.min = it; result.max = it }
        optional(DOTDOT, consume = true) {
            ConstInt().also { result.max = it }
        }
        if (result.min > result.max)
            throw SyntaxError(this, message = "max of range must be larger or equal min")
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
            ConstInt().also { result = Quantity(model.builder.range(if (minus) -it else it, if (minus) -it else it)) }
        } else if (tokenIs(FLOAT_LIT)) {
            ConstReal().also { result = Quantity(model.builder.range(if (minus) -it else it, if (minus) -it else it),"?") }
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
                            is AADD -> Quantity(model.builder.range(result.aadd().getRange().min, if (minusUb) -it.toDouble() else it.toDouble()), "?")
                            is IDD -> Quantity(model.builder.range(result.idd().getRange().min, if (minusUb) -it else it))
                            else -> throw SyntaxError(this@KerML, "expect range of form [number .. number]")
                        }
                    }
                }
                FLOAT_LIT starts {
                    ConstReal().also {
                        result = when (result.value) {
                            is AADD -> Quantity(model.builder.range(result.aadd().getRange().min, if (minusUb) -it else it), "?")
                            is IDD -> Quantity(model.builder.range(result.idd().getRange().min.toDouble(), if (minusUb) -it else it), "?")
                            else -> throw SyntaxError(this@KerML, "expect range of form [number .. number]")
                        }
                    }
                }
            }
            return result
        }
    }


    /**
     * Handles "keywords" for identification of profile classes.
     * (currently hard-wired, not nice, lacks leading #)
     */
    fun Token.considerMetaKeywords(): Token =
        if (kind == NAME_LIT) Token(
            kind = when(string) {
                in setOf("Class") -> CLASS
                in setOf("Component", "Function", "Part", "System", "Software", "Processor", "Feature") -> PART
                in setOf("Package", "package") -> PACKAGE
                in setOf("Value", "Quantity") -> ATTRIBUTE
                in setOf("Relationship", "Relation", "Link") -> ASSOC
                in setOf("Calc") -> CALCULATION
                else -> NAME_LIT
            },
            string = string,
            lineNo = lineNo,
            indices = indices )
        else this

    /**
     * Enters an error message in the status and tries to re-sync with stream of token.
     * It does so by reading until reaching a DOT which marks the end of a triple.
     * @param exception Exception that was thrown and caught prior to starting error handling
     */
    private fun handleError(exception: Exception) {

        // report error.
        if (exception is SysMDException) {
            model.report(textualRepresentation, exception.message, exception)
        } else
            model.report(SysMDError(textualRepresentation = textualRepresentation, message = "Exception: ${exception.message}"))
        // Skip input until we get the next DOT (=end of triple) or RCURBRACE or EOF.
        while (token.kind != DOT && token.kind != EOF && token.kind != RCURBRACE)
            consume()
        consume()
    }

    override fun toString(): String {
        return "Parser at token '${token.string}' in line ${token.lineNo}; exceptions: ${model.status.exceptions.size}"
    }
}
