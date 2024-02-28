package parser

import kotlin.test.*

class UnaryExpressionTests {
    fun testParse(opText: String, atomText: String) {
        val text = "$opText$atomText"
        testParse(text, ::parseExpression) {
            assertIs<UnaryExpression>(it)
            val atom = it.expression()
            assertIs<AtomicExpression>(atom)
            assertEquals(atomText, atom.atom().value)
        }
    }
    @Test
    fun testParse() {
        testParse("+", "a")
        testParse("*", "1")
        testParse("|", "1.1")
        testParse(">", "'h'")
        testParse("^", "\"hello world\"")
    }

    fun testParseMissingExpression(opText: String) {
        testParse(opText, ::parseExpression) {
            assertIs<UnaryExpression>(it)
            assertFails { it.expression() }
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
        testReplaceMiddle("+", "a", "", "b")
        testReplaceMiddle("*", "a", "", "1.1")
        testReplaceMiddle("==", "1.1", "", "a")
        testReplaceMiddle("^", "\"hello world\"", "", "a")
        testReplaceMiddle("+a", "", "", "b")
    }

    @Test
    fun testAppendToBeginning() {
        testAppendToBeginning("a", "+")
        testAppendToBeginning("a", "*")
        testAppendToBeginning("a", "==")
        testAppendToBeginning("a", "^")
    }
}