package parser_v2

import kotlin.test.*

class AtomicExpressionTests {
    val atomicExpressionTokens = listOf("a", "\"hello world\"", "'a'", "1", "1.1")
    val opsByPrecedence = listOf(
        listOf("|"),
        listOf("^"),
        listOf("&"),
        listOf("=", "!"),
        listOf(">", "<"),
        listOf("+", "-"),
        listOf("*", "/", "%")
    )
    @Test
    fun testParsing() {
        for(s in atomicExpressionTokens) {
            val iterator = ParserIterator()
            iterator.add(s)
            val expression = parseExpression(iterator)
            assertIs<AtomicExpression>(expression)

            val builder = StringBuilder()
            expression.atom().stringify(builder)
            assertEquals(s, builder.toString())
        }
    }

    @Test
    fun testWhitespacePreservation() {
        for (i in whitespaceCombos) {
            val s = "a$i"
            val iterator = ParserIterator()
            iterator.add(s)
            val expression = parseExpression(iterator)
            assertIs<AtomicExpression>(expression)
            assertEquals(NameToken("a"), expression.atom())
            assertFails { iterator.nextToken() }

            val builder = StringBuilder()
            expression.stringify(builder)
            assertEquals(s, builder.toString())
        }
    }

    @Test
    fun test003() {

    }

    @Test
    fun test004() {

    }

    @Test
    fun test005() {

    }

    @Test
    fun test006() {

    }

    @Test
    fun test007() {

    }

    @Test
    fun test008() {

    }

    @Test
    fun test009() {

    }

    @Test
    fun test010() {

    }
}