package parser_v2

import kotlinx.serialization.*
import kotlin.test.*

class AtomicExpressionTests {
    private inline fun <reified T: AtomicExpressionToken>tryParsing(text: String, whitespaceCombos: List<String>) {
        for (i in whitespaceCombos) {
            val s = "$text$i"
            tryParse(s, ::parseExpression) {
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
    fun testParsing() {
        tryParsing<NameToken>("a", whitespaceCombos)
        tryParsing<StringToken>("\"hello world\"", whitespaceCombos)
        tryParsing<BadStringToken>("\"hello world", whitespaceCombosStartingWithNewLine)
        tryParsing<CharToken>("'a'", whitespaceCombos)
        //todo maybe change the tokenizer to stop tokenizing chars on a space after the first char
        tryParsing<BadCharToken>("'a", whitespaceCombosStartingWithNewLine)
        tryParsing<IntToken>("1", whitespaceCombos)
        tryParsing<FloatToken>("1.1", whitespaceCombos)
    }

    @Test
    fun testReparsing() {
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
}