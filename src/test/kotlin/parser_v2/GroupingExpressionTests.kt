package parser_v2

import kotlin.test.*

class GroupingExpressionTests {
    private fun tryParsing(text: String) {
        for (w in whitespaceCombos) {
            val s = "($w$text)$w"
            val iterator = ParserIterator()
            iterator.add(text)
            val expression = parseExpression(iterator)
            testParse(s, ::parseExpression) {
                assertIs<GroupingExpression>(it)
                assertEquals(expression, it.expression())
            }
        }
    }

    @Test
    fun testParse() {
        tryParsing("a")
        tryParsing("+a")
        tryParsing("a + b")
        tryParsing("a + b * c")
        tryParsing("f(x)")
    }

    @Test
    fun testReparse() {
        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "(${w}a$w)"
            val text = "bc"
            val start = Span(0, 1) + whitespaceSpan
            val end = start + Span(0, 1)
            val change = Change(text, start, end)
            testReparse(inString, change) { parseExpression(it)!! }
        }

        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "(${w}a$w)"
            val text = "${w}+bc"
            val span = Span(0, 1) + whitespaceSpan + Span(0, 1)
            val change = Change(text, span, span)
            testReparse(inString, change) { parseExpression(it)!! }
        }

        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "(${w}a$w)"
            val text = "bc+$w"
            val span = Span(0, 1) + whitespaceSpan
            val change = Change(text, span, span)
            testReparse(inString, change) { parseExpression(it)!! }
        }
    }
}