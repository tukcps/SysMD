package compiler

import com.github.tukcps.sysmd.compiler.scanner.Scanner
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.services.session.SessionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.assertEmpty

class ScannerTests {

    /**
     * The comments: //, /* */
     */
    @Test
    fun commentsTest() = Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()).run {
        input = """
            // test 
            /******* */   
        """.trimIndent()
        assertEquals(REGULAR_COMMENT, token.kind)
        nextToken()
        assertEquals(EOF, token.kind)

        input = """
           // test  
           / 
        """
        assertEquals(DIV, token.kind)
        assertEmpty(status.issues)
    }


    @Test
    fun sysMLScannerTest() {
        class Parser: Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()) {
            fun setInput() {
                input = """
                package 
                name
                123.456
            """.trimIndent()
            }
            fun testToken() {
                assertEquals(PACKAGE, token.kind)
                nextToken()
                assertEquals(NAME_LIT, token.kind)
                assertEquals("name", token.string)
                nextToken()
                assertEquals(FLOAT_LIT, token.kind)
                assertEquals(true, 123.456 in token.real)
            }
        }
        Parser().run {
            setInput()
            testToken()
            assertEmpty(status.issues)
        }
    }

    /**
     * The comments: //, /* */
     */
    @Test
    fun commentsSkipTest() = Scanner(skip=emptySet(), keywords = Token.kerMLKeywords, status = SessionStatus()).run {
        input = """
            // test 
            /******* */   
        """.trimIndent()
        assertEquals(NOTE, token.kind)
        nextToken()
        assertEquals(WHITESPACE, token.kind)
        nextToken()
        assertEquals(REGULAR_COMMENT, token.kind)
        nextToken()
        assertEquals(WHITESPACE, token.kind)
        nextToken()
        assertEquals(EOF, token.kind)

        input = """
           // test  
           / 
        """
        assertEquals(WHITESPACE, token.kind)
        nextToken()
        assertEquals(NOTE, token.kind)
        nextToken()
        assertEquals(WHITESPACE, token.kind)
        nextToken()
        assertEquals(DIV, token.kind)
    }

    /**
     * The literals: String, Integer
     */
    @Test fun literalsTest() = Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()).run {
        input = """ 12.5 "string literal" 123 """
        assertEquals(FLOAT_LIT, token.kind)
        assertEquals("12.5", token.string)
        nextToken()
        assertEquals(STRING_LIT, token.kind)
        assertEquals("string literal", token.string)
        nextToken()
        assertEquals(INTEGER_LIT, token.kind)
        assertEquals("123", token.string)
        assertEquals(123L, token.integer.finiteValue)
        nextToken()
        assertEquals(EOF, token.kind)
    }

    /**
     * The literals: Real
     */
    @Test fun numLitTest() = Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()).run {
        input = "10.5e10 123.456"
        assertEquals(FLOAT_LIT, token.kind)
        assertEquals(true, 10.5e10 in token.real)
        nextToken()
        assertEquals(FLOAT_LIT, token.kind)
        assertEquals(true, 123.456 in token.real)
        nextToken()
        assertEquals(EOF, token.kind)
    }

    /**
     * Identifiers/names
     */
    @Test fun identifierTest() = Scanner(keywords = hashMapOf(), status = SessionStatus()).run {
        input = "name name_2 μr rμr"
        assertEquals(NAME_LIT, token.kind)
        assertEquals("name", token.string)
        nextToken()
        assertEquals(NAME_LIT, token.kind)
        assertEquals("name_2", token.string)
        nextToken()
        assertEquals(NAME_LIT, token.kind)
        assertEquals("μr", token.string)
        nextToken()
        assertEquals(NAME_LIT, token.kind)
        assertEquals("rμr", token.string)
    }

    @Test fun operandsTest() = Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()).run {
        input = "+ - .. <= / * &"
        assertEquals(PLUS, token.kind)
        nextToken()
        assertEquals(MINUS, token.kind)
        nextToken()
        assertEquals(DOTDOT, token.kind)
        assertEquals("..", token.string)
        nextToken()
        assertEquals(LE, token.kind)
        nextToken()
        assertEquals(DIV, token.kind)
        nextToken()
        assertEquals(TIMES, token.kind)
        nextToken()
        assertEquals(AND, token.kind)
        nextToken()
        assertEquals(EOF, token.kind)
    }

    @Test fun specialTerminals() = Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()).run {
        input = "  : typed by :> ::>  :>> "
        assertEquals(TYPED_BY, token.kind)
        nextToken()
        assertEquals(TYPED_BY, token.kind)
        nextToken()
        assertEquals(DPGT, token.kind)
        nextToken()
        assertEquals(REFERENCES, token.kind)
        nextToken()
        assertEquals(REDEFINES, token.kind)
    }

    @Test fun expressionTest() = Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()).run {
        input = " 2.0 + 3.0"
        assertEquals(FLOAT_LIT, token.kind)
        assertEquals(PLUS, nextToken.kind)
        nextToken()
        assertEquals(PLUS, token.kind)
        assertEquals(FLOAT_LIT, nextToken.kind)
        nextToken()
        assertEquals(FLOAT_LIT, token.kind)
    }

    @Test fun unrestrictedNameTest() = Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()).run {
        input = " '1.1 - This is a valid name' "
        assertEquals(NAME_LIT, token.kind)
        // val s = input.subSequence(token.indices)
        assertEquals("1.1 - This is a valid name", token.string)
    }

    /** Checks if the tokens for the if-else expression are recognized */
    @Test fun ifElseTest() = Scanner(keywords = Token.kerMLKeywords, status = SessionStatus()).run{
        input = " if ( a ) x else y"
        assertEquals(IF, token.kind)
        nextToken()
        assertEquals(LBRACE, token.kind)
        assertEquals("(", token.string)
        nextToken()
        assertEquals(NAME_LIT, token.kind)
        nextToken()
        assertEquals(RBRACE, token.kind)
        assertEquals(")", token.string)
        nextToken()
        assertEquals(NAME_LIT, token.kind)
        nextToken()
        assertEquals(ELSE, token.kind)
        nextToken()
        assertEquals(NAME_LIT, token.kind)
        assertEquals("y", token.string)
    }

    @Test fun keywordsTest() = Scanner(keywords = Token.sysMLv2Keywords, status = SessionStatus()).run {
        input = " package part specializes"
        assertEquals(PACKAGE, token.kind)
        assertEquals("package", token.string)
        nextToken()
        assertEquals(PART, token.kind)
        assertEquals("part", token.string)
        nextToken()
        assertEquals(SPECIALIZES, token.kind)
        assertEquals("specializes", token.string)
    }
}