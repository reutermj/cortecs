package parser

import kotlin.test.*

class FunctionCallExpressionTests {
    fun validate(text: String, functionNameText: String, argTexts: List<String>) {
        testParse(text, ::parseExpression) {
            assertIs<FunctionCallExpression>(it)
            val functionName = it.function()
            assertIs<AtomicExpression>(functionName)
            assertEquals(functionNameText, functionName.atom().value)

            var i = 0
            it.arguments().inOrder {
                val argument = it.expression()
                assertIs<AtomicExpression>(argument)
                assertEquals(argTexts[i], argument.atom().value)
                i++
            }

            assertEquals(argTexts.size, i)
        }
    }

    fun testParse(functionNameText: String, argTexts: List<String>) {
        val text = "$functionNameText(${argTexts.joinToString(separator = ",")})"
        validate(text, functionNameText, argTexts)
    }
    @Test
    fun testParse() {
        testParse("f", listOf())
        testParse("f", listOf("a"))
        testParse("f", listOf("a", "b"))
        testParse("f", listOf("a", "b", "c"))
        testParse("\"hello world\"", listOf("1", "1.1", "'a'"))
    }

    fun testParseTrailingComma(functionNameText: String, argTexts: List<String>) {
        val text = "$functionNameText(${argTexts.joinToString(separator = ",")},)"
        validate(text, functionNameText, argTexts)
    }

    @Test
    fun testParseTrailingComma() {
        testParseTrailingComma("f", listOf("a"))
        testParseTrailingComma("f", listOf("a", "b"))
        testParseTrailingComma("f", listOf("a", "b", "c"))
        testParseTrailingComma("\"hello world\"", listOf("1", "1.1", "'a'"))
    }

    fun testParseWhitespace(functionNameText: String, argTexts: List<String>, whitespace: String) {
        val text = "$functionNameText$whitespace($whitespace${argTexts.joinToString(separator = ",$whitespace")}$whitespace)$whitespace"
        validate(text, functionNameText, argTexts)
    }
    @Test
    fun testParseWhitespace() {
        for(whitespace in whitespaceCombos) {
            testParseWhitespace("f", listOf(), whitespace)
            testParseWhitespace("f", listOf("a"), whitespace)
            testParseWhitespace("f", listOf("a", "b"), whitespace)
            testParseWhitespace("f", listOf("a", "b", "c"), whitespace)
            testParseWhitespace("\"hello world\"", listOf("1", "1.1", "'a'"), whitespace)
        }
    }

    fun testParseMissingCloseParen(functionNameText: String, argTexts: List<String>) {
        val text = "$functionNameText(${argTexts.joinToString(separator = ",")}"
        validate(text, functionNameText, argTexts)
    }

    @Test
    fun testParseMissingCloseParen() {
        testParseMissingCloseParen("f", listOf())
        testParseMissingCloseParen("f", listOf("a"))
        testParseMissingCloseParen("f", listOf("a", "b"))
        testParseMissingCloseParen("f", listOf("a", "b", "c"))
        testParseMissingCloseParen("\"hello world\"", listOf("1", "1.1", "'a'"))
    }

    @Test
    fun testReplaceExpression() {
        testReplaceMiddle("f(", "a", ")", "b") { parseExpression(it)!! }
        testReplaceMiddle("f(", "a", ")", "b,") { parseExpression(it)!! }
        testReplaceMiddle("f(a", "", ")", ", b") { parseExpression(it)!! }
        testReplaceMiddle("f(a", "", ")", ", b,") { parseExpression(it)!! }
        testReplaceMiddle("f(a", "", ")", ", b, c") { parseExpression(it)!! }
        testReplaceMiddle("f(a", ", b, c", ")", "") { parseExpression(it)!! }
    }

    @Test
    fun testAppendToBeginning() {
        testAppendToBeginning("(a)", "f")
        testAppendToBeginning("(a", "f")
        testAppendToBeginning("a", "f(")
    }

    @Test
    fun testAppendToEnd() {
        testAppendToEnd("f", "(a)") { parseExpression(it)!! }
        testAppendToEnd("f(a", ")") { parseExpression(it)!! }
        testAppendToEnd("f(a", ", b") { parseExpression(it)!! }
        testAppendToEnd("f(a", ", b)") { parseExpression(it)!! }
        testAppendToEnd("f(a, b", ", c") { parseExpression(it)!! }
        testAppendToEnd("f(a, b", ", c)") { parseExpression(it)!! }
        testAppendToEnd("f(a", ", b, c") { parseExpression(it)!! }
        testAppendToEnd("f(a", ", b, c)") { parseExpression(it)!! }
    }
}