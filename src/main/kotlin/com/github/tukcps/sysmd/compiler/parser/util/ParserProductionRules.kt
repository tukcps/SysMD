package com.github.tukcps.sysmd.compiler.parser.util

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Scanner
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.exceptions.LexicalError


/**
 * Helper between Scanner and Parser implementation.
 * It provides a simple DSL for building a recursive descent parser.
 *
 * Production rules are supported by the following functions (with production as lambda):
 *
 *     [ production ] --> optional (start=TOKs) { production }
 *
 *     (production)* --> noOrMore (start=TOKs) { production }
 *
 *     (production)+ --> oneOrMore (end=TOKs) { production }
 *
 *        production1 --> alternatives {
 *      | production2          start(TOK)       { production1 }
 *      | production3          start(TOK, TOK)  { production2 }
 *      | ...                  start(TOK + TOK + TOK) { production3 }
 *                          }
 *
 *  production rules can use the following API to the Scanner:
 *  - token , the current token
 *  - consumedToken, the previous token
 *  - nextToken, lookahead of one token
 *  - consume(), advances one token
 *  - consume(Kind 'or' Kind 'or' ... ), advances if a token has one of the kind in parameters; else, throws error
 */
@Suppress("ClassName")
abstract class ParserProductionRules(
    indices: IntRange?=null,
    keywords: Map<String, Token.Kind>
): Scanner(indices = indices, keywords = keywords) {

    open var error: (message: String) -> Unit = fun (message: String){ throw Exception(message) }

    /**
     * Just consumes a token and moves forward.
     */
    fun consume() {
        nextToken()
    }

    @JvmName("consumeInfix")
    fun Token.Kind.consume(): Token.Kind = consume(this)

    @JvmName("consumeInfix")
    fun Set<Token.Kind>.consume(): Token.Kind = consume(this)

    @JvmName("optionalInfixNoLambda")
    fun Token.Kind.optional() {
        if (token.kind == this)
            nextToken()
    }

    fun Token.Kind.isNext(): Boolean = nextToken.kind == this

    /**
     * Checks if the current token in the token stream is equal to this, and if so,
     * consumes the token and then executes the lambda given as an argument.
     * @param ifAccepted lambda that is executed after consuming the current token
     */
    fun Token.Kind.optional(ifAccepted: () -> Unit) {
        if (token.kind == this) {
            nextToken()
            ifAccepted()
        }
    }


    /**
     * Checks for an expected token.
     * If the token is there, it is consumed and the next token is read.
     * If not, it is a syntax error.
     * @param accept expected tokens
     * @return token, if the current token is in expected tokens and was consumed
     */
    fun consume(accept: Token.Kind): Token.Kind {
        if (token.kind == accept) {
            nextToken()
            return accept
        }
        throw LexicalError(this, "after '$consumedToken': expected '$accept' but read '$token' ")
    }

    /**
     * Checks for a set of acceptable tokens; if one of the tokens given in 'accept' is the current token,
     * it will be consumed.
     * @param accept, a set of token kind that will be consumed
     */
    fun consume(accept: Set<Token.Kind>): Token.Kind {
        if (token.kind in accept) {
            nextToken()
            return token.kind
        }
        var acceptStr: String? = null
        accept.forEach { acceptStr = "${acceptStr?:""} '$it'"  }
        throw LexicalError(this, "after '$consumedToken': expected $acceptStr but read '$token' ")
    }


    /**
     * Checks if the current token is this; if so, returns true.
     * The function is for use in when statements in recursive descent parsing.
     * e.g., in a when statement: IF.starts() -> { IF.consume() ... } would check if there is
     * a token IF and then, when statement can execute the respective production.
     */
    fun Token.Kind.starts(): Boolean = token.kind == this

    /**
     * Checks if the current token is this; if so, returns true.
     * The function is for use in when statements in recursive descent parsing.
     * e.g., in a when statement: IF.starts() -> { IF.consume() ... } would check if there is
     * a token IF and then, when statement can execute the respective production.
     */
    fun Set<Token.Kind>.starts(): Boolean = token.kind in this

    /**
     * Checks if the current token is this; if so, returns true.
     * The function is for use in when statements in recursive descent parsing.
     * e.g., in a when statement: IF.then() -> { ... } would check if there is
     * a token IF and then, consume it, then the 'when' statement can execute the respective production.
     */
    fun Token.Kind.then(): Boolean {
        val result = token.kind == this
        if (result) nextToken()
        return result
    }

    /**
     * A template for alternatives that is called via its constructor; like, in the end,
     * a function call.
     * Each alternative case is registered by a local function (starts, then).
     * 'starts' registers a case by its leading terminal symbols.
     * then registers a case by its leading terminal symbols and consumes them in case of a match.
     * @param cases lambda in which different cases are registered:
     *  - the different options of the alternative by a leading token kind or set thereof
     *  - others, in case there is no match
     *  - failed, postprocessing method in case a match failed
     */
    inner class alternatives(cases: alternatives.() -> Unit) {
        private var t1: Token = token
        private var t2: Token = nextToken
        private var match1: (() -> Unit)? = null
        private var consume1: Boolean? = false
        private var match2: (() -> Unit)? = null
        private var consume2: Boolean? = false
        private var others: (() -> Unit)? = null
        val expected = mutableListOf<String>()

        init {
            cases()     // calls the lambda parameter which registers cases
            when {
                match2 != null -> {     // if there is a match also with lookahead terminals ...
                    try {
                        if (consume2 == true) {
                            nextToken(); nextToken()
                        } else {
                            token = t1; nextToken = t2
                        }
                        match2?.let { it() }
                    } catch (_: Exception) {
                        error("error in production after $t1 $t2")
                    }
                }
                match1 != null -> {     // match with only the current token ...
                    try {
                        if (consume1 == true) {
                            nextToken(); } else token = t1
                        match1?.let { it() }
                    } catch(le : LexicalError) {
                        error(le.message)
                    } catch (_: Exception) {
                        error("error in production after $t1")
                    }
                }
                others != null -> others?.let { it() } // no match ...

                else -> error("expected $expected, but read '$t1' ") // no others registered & no match ...
            }
        }

        /**
         * Infix function that registers a production by its left-side operands.
         */
        infix fun Set<Token.Kind>.starts(production: () -> Unit) {
            if (t1.kind in this) { match1 = production }
            else expected.add( toString() )
        }

        /**
         * Infix function that registers a production that consumes a recognized token.
         */
        infix fun Set<Token.Kind>.then(production: () -> Unit) {
            if (t1.kind in this) { match1 = production }
            else expected.add( toString() )
            consume1 = true
        }

        /**
         * Infix function that registers a production by a single left-side operand. Does not consume token.
         */
        infix fun Token.Kind.starts(production: () -> Unit) = start(this, false, production)

        /**
         * Infix function that registers a production by a single left-side operand.Consumes token
         */
        infix fun Token.Kind.then(production: () -> Unit) = start(this, true, production)
        fun start(kind: Token.Kind, consume: Boolean=false, production: () -> Unit) {
            if (t1.kind == kind) {
                match1 = production
                consume1 = consume
            }
            else expected.add( kind.toString() )
        }

        /**
         * Infix function that registers a lookahead of one token and returns a required pair of tokens.
         */
        infix fun Token.Kind.then(next: Token.Kind): Pair<Token.Kind, Token.Kind> = Pair(this, next)

        /**
         * Infix function that registers a production rule that follows a
         * matching pair of a token and the lookahead token.
         */
        infix fun Pair<Token.Kind, Token.Kind>.starts(production: () -> Unit) {
            if (t1.kind == this.first && t2.kind == this.second) {
                match2 = production
                consume2 = false
            }
        }

        fun others( production: () -> Unit) {
            others = production
        }
    }

    /**
     * (production)+
     * Guided by token required at start resp. at the end (a lambda with boolean results).
     * @param start token that is required at start, or null
     * @param stop lambda that stops when true
     * @param production a production implementation as lambda
     */
    inline fun oneOrMore(start: Token.Kind? = null, stop: (() -> Boolean), production: () -> Unit ) {
        do {
            production()
        } while ( (start != null && token.kind == start) || !stop())
    }


    /**
     * (production)+
     * Guided by tokens that are required at start resp. at the end (a lambda with boolean results).
     * @param start tokens that are required at start, or null; use e.g., Token.Kind or Token.Kind infix function.
     * @param production a production implementation as lambda
     */
    inline fun oneOrMore(start: Set<Token.Kind>, production: () -> Unit ) {
        do {
            production()
        } while (token.kind in start)
    }


    /**
     *  (production)*
     *
     *  Guided by a token after which the production follows, or/or a token after which it does not follow.
     *  where
     *      @param start is a token kind which must be the current token before the production
     *      @param stop is a token kind after which the production cannot follow
     *      @param consume the first token (start) is consumed
     *      @param production the production rule
     */
    inline fun noOrMore(start: Token.Kind? = null, stop: Token.Kind? = null, consume: Boolean = false, production: () -> Unit ) {
        while (token.kind == start || (stop != null && token.kind != stop)) {
            if (token.kind == Token.Kind.EOF) return
            if (consume) consume()
            production()
        }
    }


    /**
     *  (production)*
     *  where
     *  Guided by a token after which the production follows, or/or a token after which it does not follow.
     *             Note that this set can be built by the overloaded or infix function.
     *      @param consume the first token (start) is consumed
     *      @param production the production rule
     */
    inline fun noOrMore(start: Set<Token.Kind>, stop: Set<Token.Kind>? = null, consume: Boolean = false, production: () -> Unit) {
        while (token.kind in start) {
            if (consume) consume()
            production()
            if (stop != null && token.kind in stop) break
        }
    }


    /**
     *  (production)*
     *  Repeats the evaluation of the lambda expression while start evaluates to true and stop evaluation to false.
     *  The function also includes methods for error recovery that, if production throws an exception, consumes token until a recover token is found.
     *  where
     *
     *  @param start is a lambda that must hold before the production.
     *  @param end is a lambda that must hold after the production before the next production.
     *  @param production the production rule.
     */
    inline fun noOrMore(
        noinline start: (() -> Boolean)? = null,
        noinline end: (() -> Boolean)? = null,
        recover: Set<Token.Kind> = setOf(Token.Kind.SEMICOLON),
        production: () -> Unit
    ) {
        require ( end != null || start != null )
        while (start == null || start()) {
            try {
                production()
            } catch (e: Exception) {
                error(e.message ?: "no message")
                while (token.kind !in recover && token.kind != Token.Kind.EOF) {
                    if (end?.invoke() == true) break
                    nextToken()
                }
                nextToken() // Skip SEMICOLON ...
            }
            if (end?.invoke() == true || token.kind == Token.Kind.EOF) break
        }
    }

    /**
     * Function that checks whether current and next tokes match a pattern.
     * @param token current token
     * @param nextToken next token; if not relevant, null (default)
     * @param nextNextToken next token after next token, null is default for irrelevant
     */
    fun match(token: Token.Kind, nextToken: Token.Kind?=null, nextNextToken: Token.Kind?=null): Boolean =
        when {
            (token == this.token.kind && nextToken == null && nextNextToken == null) -> true
            (token == this.token.kind && nextToken == this.nextToken.kind && nextNextToken == null) -> true
            (token == this.token.kind && nextToken == this.nextToken.kind && nextNextToken == this.nextNextToken.kind) -> true
            else -> false
        }


    /**
     * Helper function that checks if the token is start, and if so executes production rule,
     * otherwise it returns default
     */
    fun <T> KerML.optional(
        start: Token.Kind,
        consume: Boolean = false,
        noMatch: T,
        rule: (KerML.() -> T)? = null
    ): T? =
        if (token.kind == start) {
            if (consume) consume()
            rule?.let { it() }
        } else
            noMatch

    /**
     * Helper function that checks if the token is start, and if so executes production rule,
     * otherwise it returns the default result of type T.
     */
    inline fun <T> KerML.optional(
        startToken: Set<Token.Kind>,
        consume: Boolean = false,
        noMatch: () -> Unit = {},
        production: KerML.() -> T? = { null }
    ) =
        if (token.kind in startToken) {
            if (consume) consume()
            production()
        } else
            noMatch()

    /**
     * Helper function that checks if the token is start, and if so executes production rule
     */
    inline fun KerML.optional(
        start: Token.Kind,
        consume: Boolean = false,
        rule: KerML.() -> Unit = { }
    ) {
        if (token.kind == start) {
            if (consume) consume()
            rule()
        }
    }

    /**
     * Helper function that checks if the token is start, and if so executes production rule
     */
    inline fun KerML.optional(
        matchingCondition: () -> Boolean,
        consume: Boolean = false,
        production: KerML.() -> Unit = { }
    ) {
        if (matchingCondition()) {
            if (consume) consume()
            production()
        }
    }

    /**
     * Helper function that checks if the token is start, and if so executes production rule
     */
    inline fun KerML.optional(
        start: Set<Token.Kind>,
        consume: Boolean = false,
        rule: KerML.() -> Unit = { }
    ) {
        if (token.kind in start) {
            if (consume) consume()
            rule()
        }
    }
}

