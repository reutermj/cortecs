package parser_v2

import kotlin.test.*

class AtomicExpressionTests {
    private inline fun <reified T: AtomicExpressionToken>tryParsing(text: String, whitespaceCombos: List<String>) {
        for (i in whitespaceCombos) {
            val s = "$text$i"
            testParse(s, ::parseExpression) {
                assertIs<AtomicExpression>(it)
                assertIs<T>(it.atom())

                val builder = StringBuilder()
                it.atom().stringify(builder)
                assertEquals(text, builder.toString())
                builder.clear()
            }
        }
    }

    @Test
    fun testParse() {
        tryParsing<NameToken>("a", whitespaceCombos)
        tryParsing<StringToken>("\"hello world\"", whitespaceCombos)
        tryParsing<BadStringToken>("\"hello world", whitespaceCombosStartingWithNewLine)
        tryParsing<CharToken>("'a'", whitespaceCombos)
        tryParsing<BadCharToken>("'a", whitespaceCombosStartingWithNewLine)
        tryParsing<IntToken>("1", whitespaceCombos)
        tryParsing<FloatToken>("1.1", whitespaceCombos)
    }

    @Test
    fun testReparse() {
        for (i in whitespaceCombos) {
            val inString = "a$i"
            val text = "bc"
            val start = Span(0, 1)
            val end = Span(0, 1)
            val change = Change(text, start, end)
            testReparse(inString, change) { parseExpression(it)!! }
        }

        for (i in whitespaceCombos) {
            val inString = "c$i"
            val text = "ab"
            val start = Span(0, 0)
            val end = Span(0, 0)
            val change = Change(text, start, end)
            testReparse(inString, change) { parseExpression(it)!! }
        }
    }

    fun testNullParse(inString: String) {
        val iterator = ParserIterator()
        iterator.add(inString)
        val expression = parseExpression(iterator)
        assertNull(expression)
    }

    @Test
    fun testBadToken() {
        testNullParse("")
        testNullParse("Abc")
        testNullParse("let")
        testNullParse("if")
        testNullParse("function")
        testNullParse("return")
        testNullParse("   ")
        testNullParse("\n")
        testNullParse(")")
        testNullParse("{")
        testNullParse("}")
        testNullParse(",")
        testNullParse(".")
        testNullParse(":")
        testNullParse("=")
    }
}