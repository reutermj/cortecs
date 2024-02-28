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
        testReplaceMiddle("(", "a", ")", "b")
        testReplaceMiddle("(", "a", ")", "1.1")
        testReplaceMiddle("(", "1.1", ")", "a")
        testReplaceMiddle("(", "\"hello world\"", ")", "a")
        testReplaceMiddle("(a", "", ")", "b")
    }

    @Test
    fun testAppendToEnd() {
        testAppendToEnd("(", "a")
        testAppendToEnd("(a", ")")
        testAppendToEnd("(", "a)")
    }

    @Test
    fun testAppendToBeginning() {
        testAppendToBeginning("a", "(")
    }
}