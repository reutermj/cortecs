package parser

import kotlin.test.*

class UnaryExpressionTests {
    fun validate(text: String, atomText: String) {
        testParse(text, ::parseExpression) {
            assertIs<UnaryExpression>(it)
            val atom = it.expression()
            assertIs<AtomicExpression>(atom)
            assertEquals(atomText, atom.atom().value)
        }
    }

    fun testParse(opText: String, atomText: String) {
        val text = "$opText$atomText"
        validate(text, atomText)
    }

    @Test
    fun testParse() {
        testParse("+", "a")
        testParse("*", "1")
        testParse("|", "1.1")
        testParse(">", "'h'")
        testParse("^", "\"hello world\"")
    }

    fun testParseWhitespace(opText: String, atomText: String, whitespace: String) {
        val text = "$opText$whitespace$atomText$whitespace"
        validate(text, atomText)
    }

    @Test
    fun testParseWhitespace() {
        for(whitespace in whitespaceCombos) {
            testParseWhitespace("+", "a", whitespace)
            testParseWhitespace("*", "1", whitespace)
            testParseWhitespace("|", "1.1", whitespace)
            testParseWhitespace(">", "'h'", whitespace)
            testParseWhitespace("^", "\"hello world\"", whitespace)
        }
    }

    fun testParseMissingExpression(opText: String) {
        testParse(opText, ::parseExpression) {
            assertIs<UnaryExpression>(it)
            assertFails {it.expression()}
        }
    }

    @Test
    fun testParseMissingExpression() {
        testParseMissingExpression("+")
        testParseMissingExpression("*")
        testParseMissingExpression("==")
    }

    @Test
    fun testReplaceExpression() {
        testReplaceMiddle("+", "a", "", "b") {parseExpression(it)!!}
        testReplaceMiddle("*", "a", "", "1.1") {parseExpression(it)!!}
        testReplaceMiddle("==", "1.1", "", "a") {parseExpression(it)!!}
        testReplaceMiddle("^", "\"hello world\"", "", "a") {parseExpression(it)!!}
        testReplaceMiddle("+a", "", "", "b") {parseExpression(it)!!}
    }

    @Test
    fun testAppendToBeginning() {
        testAppendToBeginning("a", "+")
        testAppendToBeginning("a", "*")
        testAppendToBeginning("a", "==")
        testAppendToBeginning("a", "^")
    }
}