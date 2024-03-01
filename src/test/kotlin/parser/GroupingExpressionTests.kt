package parser

import kotlin.test.*

class GroupingExpressionTests {
    fun testParse(expressionText: String) {
        val text = "($expressionText)"
        testParse(text, ::parseExpression) {
            assertIs<GroupingExpression>(it)
            val expression = it.expression()
            assertIs<AtomicExpression>(expression)
            assertEquals(expressionText, expression.atom().value)
        }
    }

    @Test
    fun testParse() {
        testParse("a")
        testParse("1")
        testParse("1.1")
        testParse("\"hello world\"")
        testParse("'a'")
    }

    fun testParseWhitespace(expressionText: String, whitespace: String) {
        val text = "($whitespace$expressionText$whitespace)$whitespace"
        testParse(text, ::parseExpression) {
            assertIs<GroupingExpression>(it)
            val expression = it.expression()
            assertIs<AtomicExpression>(expression)
            assertEquals(expressionText, expression.atom().value)
        }
    }

    @Test
    fun testParseWhitespace() {
        for(whitespace in whitespaceCombos) {
            testParseWhitespace("a", whitespace)
            testParseWhitespace("1", whitespace)
            testParseWhitespace("1.1", whitespace)
            testParseWhitespace("\"hello world\"", whitespace)
            testParseWhitespace("'a'", whitespace)
        }
    }

    @Test
    fun testParseMissingExpression() {
        testParse("(", ::parseExpression) {
            assertIs<GroupingExpression>(it)
            assertFails { it.expression() }
        }
    }

    fun testParseMissingClosingParen(expressionText: String) {
        val text = "($expressionText"
        testParse(text, ::parseExpression) {
            assertIs<GroupingExpression>(it)
            val expression = it.expression()
            assertIs<AtomicExpression>(expression)
            assertEquals(expressionText, expression.atom().value)
        }
    }

    @Test
    fun testParseMissingClosingParen() {
        testParseMissingClosingParen("a")
        testParseMissingClosingParen("1")
        testParseMissingClosingParen("1.1")
        testParseMissingClosingParen("\"hello world\"")
        testParseMissingClosingParen("'a'")
    }

    @Test
    fun testReplaceExpression() {
        testReplaceMiddle("(", "a", ")", "b") { parseExpression(it)!! }
        testReplaceMiddle("(", "a", ")", "1.1") { parseExpression(it)!! }
        testReplaceMiddle("(", "1.1", ")", "a") { parseExpression(it)!! }
        testReplaceMiddle("(", "\"hello world\"", ")", "a") { parseExpression(it)!! }
        testReplaceMiddle("(a", "", ")", "b") { parseExpression(it)!! }
    }

    @Test
    fun testAppendToEnd() {
        testAppendToEnd("(", "a") { parseExpression(it)!! }
        testAppendToEnd("(a", ")") { parseExpression(it)!! }
        testAppendToEnd("(", "a)") { parseExpression(it)!! }
    }

    @Test
    fun testAppendToBeginning() {
        testAppendToBeginning("a", "(")
    }
}