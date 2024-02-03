package parser

import kotlin.test.*

class FunctionCallExpressionTests {
    private fun tryParsing(function: String, arguments: List<String>, tailingComma: List<String> = listOf("", ",")) {
        for (w in whitespaceCombos) {
            for(comma in tailingComma) {
                val builder = StringBuilder()

                if(arguments.any()) {
                    builder.append(arguments.first())
                    builder.append(w)
                    for(argument in arguments.drop(1)) {
                        builder.append(",")
                        builder.append(w)
                        builder.append(argument)
                        builder.append(w)
                    }
                    builder.append(comma)
                }

                val s = "$function$w($w$builder)$w"

                val functionExpression =
                    run {
                        val iterator = ParserIterator()
                        iterator.add("$function$w")
                        parseExpression(iterator)
                    }

                val argumentExpressions =
                    List(arguments.size) {
                        val iterator = ParserIterator()
                        iterator.add("${arguments[it]}$w")
                        parseExpression(iterator)
                    }

                testParse(s, ::parseExpression) {
                    assertIs<FunctionCallExpression>(it)
                    assertEquals(functionExpression, it.function())
                    val argumentsAst = it.arguments()
                    var i = 0
                    argumentsAst.inOrder { argument ->
                        assertEquals(argumentExpressions[i], argument.expression())
                        i++
                    }

                    assertEquals(arguments.size, i)
                }
            }
        }
    }

    @Test
    fun testParse() {
        tryParsing("a", listOf(), listOf(""))
        tryParsing("a", listOf("b"))
        tryParsing("a", listOf("b", "c"))
        tryParsing("a", listOf("b", "c", "d"))
        tryParsing("a", listOf("b+c", "+d", "(e)", "f()"))
        tryParsing("(a + b)", listOf("c", "d", "e"))
        tryParsing("f(x)", listOf("c", "d", "e"))
    }

    @Test
    fun testReparse() {
        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "f"
            val text = "$w(a)"
            val span = Span(0, 1)
            val change = Change(text, span, span)
            testReparse(inString, change) { parseExpression(it)!! }
        }

        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "f(a)(b)"
            val text = "$w(c)"
            val span = Span(0, inString.length)
            val change = Change(text, span, span)
            testReparse(inString, change) { parseExpression(it)!! }
        }

        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "f(a$w)"
            val text = ", bc"
            val span = Span(0, 3) + whitespaceSpan
            val change = Change(text, span, span)
            testReparse(inString, change) { parseExpression(it)!! }
        }

        for (w in whitespaceCombos) {
            val whitespaceSpan = getSpan(w)
            val inString = "(a$w)"
            val text = "f"
            val span = Span.zero
            val change = Change(text, span, span)
            testReparse(inString, change) { parseExpression(it)!! }
        }
    }
}