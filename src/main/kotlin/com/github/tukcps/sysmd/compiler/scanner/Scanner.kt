package com.github.tukcps.sysmd.compiler.scanner

import com.github.tukcps.sysmd.compiler.scanner.Token.Definitions.charTokens
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import kotlin.math.pow


/**
 * The scanner gets as input a string and recognizes the main keywords of SysML v2 textual
 * as well as those of SysMD. The input is translated to a stream of tokens that can be
 * read sequentially. The fields
 *  - token
 *  - previousToken
 *  - nextToken
 * contain the current token, the previous, and the next token.
 * The function
 * - nextToken(): advances one token forward.
 * - nextTokenIs(Set of TokenKind): checks if a token is present and
 *   advances if the token is in the set of token passed as a parameter.
 *  @param indices Indices in the input string that shall be scanned.
 *  @param skip Tokens that shall be skipped
 *  @param keywords a map of keywords
 */
open class Scanner(
    var indices: IntRange?=null,
    val skip: Set<Token.Kind> = setOf(WHITESPACE, NOTE),
    val keywords: Map<String, Token.Kind>
) {
    /**
     * The input as a String. Setting it will reset i, lineNo, columnNo, token, etc.:
     * however, it will not change the mode.
     **/
    var input: CharSequence = ""           // Giving it a new input resets all states.
        set(it) {
            position = indices?.first?:0
            currLineNo = 1
            columnNo = 1
            nextToken = Token(EOF, "")
            token = Token(EOF, "")
            consumedToken = Token(EOF, "")
            field = it
            nextToken() // char
            nextToken() // nextChar
            nextToken()
        }

    /** The index of the character where the current token began. */
    private var startPosition = 0

    /** The index of the current character in the overall input string. */
    private var position = 0

    /** The index of the current character in the line. */
    private var columnNo: Int = 1

    /** The current line number of the scanner including lookahead */
    private var currLineNo: Int = 1
    private var nextLineNo: Int = 1

    /** the previously consumed token */
    var consumedToken: Token = Token(EOF, "", lineNo = 0, indices = 0 .. 0)

    /** The current token */
    var token = Token(EOF, "", lineNo = 0, indices = 0..0)

    /** the next token */
    var nextToken = Token(EOF, "", lineNo = 0, indices = 0..0)

    /** the token after the next token */
    var nextNextToken = Token(EOF, "", lineNo = 0, indices = 0..0)

    init {
        position = 0 // indices?.first?:0 // Only if we could also persist string
        nextToken()
        nextToken()
        nextToken()
    }

    /** gets the current character */
    private inline val curChar: Char
        get() = input.getOrElse(position) { 0.toChar() }

    /** gets the next character */
    private inline val nextChar: Char
        get() = input.getOrElse(position+1) { 0.toChar() }

    /** moves forward to the next character */
    private fun nextChar(): Char {
        position++
        columnNo++
        return if (position in input.indices) {
            if (curChar == '\n') {
                nextLineNo++ //only nextLineNo is incremented to avoid current Token being associated with a wrong Line Number
                columnNo = 1
            }
            curChar
        } else
            0.toChar()
    }

    /**
     * Main API function of the Scanner; reads a token from the input string and
     * modifies position, line, and column.
     * The token is saved in the variable nextToken
     */
    fun nextToken() {
        consumedToken = token
        token = nextToken
        nextToken = nextNextToken
        do {
            nextTokenOrSkip()
            // Special case: tokens in SysML v2 that are a sequence of two "tokens", typed by = TYPED_BY = DP
            if (nextToken.kind == TYPED && nextNextToken.kind == BY) {
                nextToken = buildToken(TYPED_BY)
                nextTokenOrSkip()
            }
        } while (nextNextToken.kind in skip) // Read over skip-tokens
    }

    /**
     * Gets next token including those to skip like comments or whitespace.
     * The token is saved in nextNextToken.
     */
    private fun nextTokenOrSkip() {
        var string = ""
        // remember where the token starts.
        startPosition = position

        when (curChar) {
            // note; will be dropped, starts with //
            '/' if nextChar == '/' -> {
                while (curChar != '\n' && curChar != 0.toChar()) {
                    string += curChar
                    nextChar()
                }
                nextNextToken = buildToken(NOTE, string = string)
                return
            }

            // Comment that is a part of the model starts with /* ... */
            '/' if nextChar == '*' -> {
                do {
                    string += curChar
                    if (curChar == 0.toChar()) {
                        nextNextToken = buildToken(ERROR, string = string)
                        return
                    }
                    nextChar()
                } while (!((curChar == '*') && (nextChar == '/')))
                nextChar()
                nextChar()
                nextNextToken = buildToken(REGULAR_COMMENT, string = string.removePrefix("/*"))
                return
            }
        }

        // The 'real' tokens ...
        when (curChar) {

            // NAME Literal or Keyword, begins with a letter, underscore
            in CharCategory.UPPERCASE_LETTER,
            in CharCategory.LOWERCASE_LETTER,
            in CharCategory.TITLECASE_LETTER,
            in CharCategory.MODIFIER_LETTER,
            in CharCategory.OTHER_LETTER,
            '_', '°' -> {
                string += curChar
                nextChar()
                while (curChar == '_' || curChar.isDigit() || curChar.isLetter()) {
                    string += curChar; nextChar()
                }
                val kind = keywords[string] ?: NAME_LIT
                nextNextToken = buildToken(kind, string = string)
            }

            in Token.WHITESPACE -> {
                while (curChar in Token.WHITESPACE) {
                    string += curChar
                    nextChar()
                }
                nextNextToken = buildToken(WHITESPACE, string = string)
            }

            // Number, either INTEGER or REAL Literal
            in '0'..'9' -> {
                var kind = INTEGER_LIT
                var mantissa = 0.0
                var fract = 0.0
                var exponent = 0.0
                var expSign = 1.0
                while (curChar.isDigit()) {
                    mantissa = mantissa * 10.0 + (curChar - '0').toDouble()
                    nextChar()
                }

                if ((curChar == '.') && (nextChar.isDigit())) {
                    kind = FLOAT_LIT
                    nextChar()
                    var i = 1.0
                    while (curChar in '0'..'9') {
                        i /= 10.0
                        fract += (curChar - '0').toDouble() * i
                        nextChar()
                    }
                }

                if (curChar in setOf('e', 'E')) {
                    kind = FLOAT_LIT
                    nextChar()
                    if (curChar == '-') {
                        expSign = -1.0
                        nextChar()
                    }
                    if (curChar == '+')
                        nextChar()
                    while (curChar in '0'..'9') {
                        exponent = exponent * 10.0 + (curChar - '0').toDouble()
                        nextChar()
                    }
                }
                nextNextToken = buildToken(kind, number = (mantissa + fract) * 10.0.pow(exponent * expSign))
            }


            // Name literal in quotes.
            '\'' -> {
                nextChar()
                while (curChar != '\'') {
                    string += curChar
                    if (curChar == 0.toChar())  {
                        nextToken = buildToken(ERROR, string = "\'"+string)
                        return
                    }
                    nextChar()
                }
                nextChar()
                nextNextToken = buildToken(NAME_LIT, string = string)
            }


            // String literal: " ... "
            '"' -> {
                nextChar()
                while (curChar != '"') {
                    string += curChar
                    if (curChar == 0.toChar()) {
                        nextToken=buildToken(ERROR, string="\""+string+curChar)
                        return
                    }
                    nextChar()
                }
                nextChar()
                nextNextToken = buildToken(STRING_LIT, string = string)
            }

            // ".", ".."
            '.' -> {
                nextNextToken = if (nextChar() == '.') {
                    nextChar()
                    buildToken(DOTDOT)
                } else
                    buildToken(DOT)
            }

            // "<", "<="
            '<' -> {
                nextNextToken = if (nextChar() == '=') {
                    nextChar()
                    buildToken(LE)
                } else
                    buildToken(LT)
            }

            // >=
            '>' -> {
                nextNextToken = if (nextChar() == '=') {
                    nextChar()
                    buildToken(GE)
                } else
                    buildToken(GT)
            }

            '-' -> {
                nextNextToken = if (nextChar() == '>') {
                    nextChar()
                    buildToken(ARROW)
                } else
                    buildToken(MINUS)
            }

            '=' -> {
                nextNextToken = if (nextChar() == '=') {
                    if(nextChar() == '=')
                    {
                        nextChar()
                        buildToken(EEE)
                    } else
                        buildToken(EE)
                } else
                    buildToken(EQ)
            }

            // : or := or :> or :: or :>> or ::>
            ':' -> {
                nextNextToken = when(nextChar()) {
                    // :>> (Redefines) resp. :> (Specializes)
                    '>' -> if (nextChar() == '>') { nextChar(); buildToken(REDEFINES)} else buildToken(DPGT)
                    // :=
                    '=' -> { nextChar(); buildToken(DPEQ)}
                    // ::> (references) resp. :: (DPDP)
                    ':' -> if (nextChar() == '>') { nextChar(); buildToken(REFERENCES)} else buildToken(DPDP)
                    else -> buildToken(TYPED_BY)
                }
            }

            // * or **
            '*' -> {
                nextNextToken = if (nextChar() == '*') {
                    nextChar()
                    buildToken(STARSTAR)
                } else
                    buildToken(TIMES)
            }
            '!' -> {
                nextNextToken = if (nextChar() == '=') {
                    if(nextChar() == '=')
                    {
                        nextChar()
                        buildToken(NEE)
                    } else
                        buildToken(NEQ)
                } else
                    buildToken(NOT)
            }

            '@' -> {
                nextNextToken = if (nextChar() == '@') {
                    nextChar()
                    buildToken(ATAT)
                } else
                    buildToken(ATSIGN)
            }
            '?' -> {
                nextNextToken = if (nextChar() == '?') {
                    nextChar()
                    buildToken(QQ)
                } else
                    buildToken(QUESTION)
            }

            0.toChar() -> nextNextToken = buildToken(EOF)

            else -> {
                nextNextToken = if (charTokens[curChar] == null) {
                    nextChar()
                    buildToken(ERROR, string="$curChar")
                } else {
                    val charToStr = curChar
                    nextChar()
                    buildToken(charTokens[charToStr]!!)
                }
            }
        }
    }


    /**
     * Checks if the next token is one of its arguments; if so, the function
     * consumes it and returns true; otherwise, it returns false and does not consume the token.
     * The consumed token is saved in the variable consumedToken.
     * @param accept Token kind that is consumed
     * @return true if the current token was in the set 'accept' and was consumed; else false.
     */
    fun consumeIfTokenIs(vararg accept: Token.Kind): Boolean =
        if (token.kind in accept) {
            nextToken()
            true
        } else
            false



    /**
     * Checks if the current token is in the argument.
     * @return true, if the current token is else false
     */
    fun tokenIs(t: Token.Kind): Boolean = token.kind == t

    /**
     * Checks if the current token is in the argument.
     * @return true, if the current token is else false
     */
    fun tokenIsNot(t: Token.Kind): Boolean = token.kind != t

    infix fun Token.Kind.or(kind: Token.Kind): MutableSet<Token.Kind> = mutableSetOf(this, kind)
    infix fun Token.Kind.or(kinds: MutableSet<Token.Kind>): MutableSet<Token.Kind>  { kinds.add(this); return kinds }
    infix fun MutableSet<Token.Kind>.or(kind: Token.Kind): MutableSet<Token.Kind> { this.add(kind); return this }

    /**
     * Returns the current token (token) as a string.
     */
    override fun toString() = token.toString()

    /**
     * Builds a Token object with the context information line number, column number, and string that was used.
     * @param kind Kind of the token
     * @param number value, represented as a Double (including integers)
     */
    private fun buildToken(kind: Token.Kind, number: Double = 0.0, string: String? = null): Token {

        val token = Token(
            kind = kind,
            string = string ?: input.subSequence(startPosition..<position).toString(),
            number = number,
            lineNo = currLineNo,
            indices = startPosition..<position
        )

        //Updates the current line No if necessary (is necessary to avoid applying wrong LineNo to a Token which is succeeded by a line break "/n")
        if(currLineNo != nextLineNo){
            currLineNo = nextLineNo
        }

       return token
    }
}
