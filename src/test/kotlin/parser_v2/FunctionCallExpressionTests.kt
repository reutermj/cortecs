package parser_v2

import kotlin.test.*

class FunctionCallExpressionTests {
    private fun tryParsing(function: String, arguments: List<String>) {
        for (w in whitespaceCombos) {
            val builder = StringBuilder()
            for(argument in arguments) {
                builder.append(argument)
                builder.append(w)
                builder.append(",")
                builder.append(w)
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
                }
            }
        }
    }

    @Test
    fun testParse() {
        tryParsing("a", listOf("b"))
    }

//    @Test
//    fun testReparse() {
//        for (w in whitespaceCombos) {
//            val whitespaceSpan = getSpan(w)
//            val inString = "(${w}a$w)"
//            val text = "bc"
//            val start = Span(0, 1) + whitespaceSpan
//            val end = start + Span(0, 1)
//            val change = Change(text, start, end)
//            testReparse(inString, change) { parseExpression(it)!! }
//        }
//
//        for (w in whitespaceCombos) {
//            val whitespaceSpan = getSpan(w)
//            val inString = "(${w}a$w)"
//            val text = "${w}+bc"
//            val span = Span(0, 1) + whitespaceSpan + Span(0, 1)
//            val change = Change(text, span, span)
//            testReparse(inString, change) { parseExpression(it)!! }
//        }
//
//        for (w in whitespaceCombos) {
//            val whitespaceSpan = getSpan(w)
//            val inString = "(${w}a$w)"
//            val text = "bc+$w"
//            val span = Span(0, 1) + whitespaceSpan
//            val change = Change(text, span, span)
//            testReparse(inString, change) { parseExpression(it)!! }
//        }
//    }
}