package parser

import kotlin.test.*

class ReturnTests {
    fun testParse(expression: String, whitespace: String = "") {
        val text = "return$whitespace $expression$whitespace"

        testParse(text, ::parseReturn) {
            assertIs<ReturnAst>(it)

            val expressionIterator = ParserIterator()
            expressionIterator.add("$expression$whitespace")
            val expressionAst = parseExpression(expressionIterator)!!
            assertEquals(expressionAst, it.expression())
        }
    }

    @Test
    fun testParse() {
        testParse("y")
        testParse("y + z")
        testParse("+y")
        testParse("(y)")
        testParse("f(y)")
        testParse("f(y, z)")

        for(whitespace in whitespaceCombos) {
            testParse("y", whitespace)
            testParse("y + z", whitespace)
            testParse("+y", whitespace)
            testParse("(y)", whitespace)
            testParse("f(y)", whitespace)
            testParse("f(y, z)", whitespace)
        }
    }

    @Test
    fun testParseMissingExpression() {
        testParse("return", ::parseReturn) {
            assertIs<ReturnAst>(it)
            assertNull(it.expression())
        }
    }

    @Test
    fun testAppendToEnd() {
        testAppendToEnd("return", " x", ::parseReturn)
        testAppendToEnd("return", " +x", ::parseReturn)
        testAppendToEnd("return (", " x)", ::parseReturn)
        testAppendToEnd("return x ", "+ y", ::parseReturn)
    }

    @Test
    fun testReplace() {
        testReplaceMiddle("return ", "x", "+y", "z", ::parseReturn)
        testReplaceMiddle("ret", "urn", " x", "urn y +", ::parseReturn)
    }
}