package parser_v2

import kotlin.test.*

class UnaryExpressionTests {
    private fun tryParsing(op: String, text: String) {
        for (w in whitespaceCombos) {
            val s = "$op$w$text"
            val iterator = ParserIterator()
            iterator.add(text)
            val expression = parseExpression(iterator)
            testParse(s, ::parseExpression) {
                assertIs<UnaryExpression>(it)
                assertEquals(op, it.op().value)
                assertEquals(expression, it.expression())
            }
        }
    }

    @Test
    fun testParse() {
        tryParsing("+", "a")
        tryParsing("+", "(a)")
    }

    @Test
    fun testReparse() {
        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "+${w}a$w"
            val text = "bc"
            val start = Span(0, 1) + whitespaceSpan
            val end = start + Span(0, 1)
            val change = Change(text, start, end)
            testReparse(inString, change) { parseExpression(it)!! }
        }

        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "+${w}a$w"
            val text = "${w}+bc"
            val span = Span(0, 1) + whitespaceSpan + Span(0, 1)
            val change = Change(text, span, span)
            testReparse(inString, change) { parseExpression(it)!! }
        }

        for (w in whitespaceCombos) {
            val inString = "+${w}a$w"
            val text = "bc+$w"
            val span = Span.zero
            val change = Change(text, span, span)
            testReparse(inString, change) { parseExpression(it)!! }
        }
    }
}