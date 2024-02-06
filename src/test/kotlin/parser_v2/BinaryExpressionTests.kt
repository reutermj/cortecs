package parser_v2

import kotlin.test.*

class BinaryExpressionTests {
    val operators = listOf("|", "^", "&", "==", "!", ">", "<", "+", "-", "~", "*", "/", "%")

    fun opToPrecedence(op: OperatorToken): Int =
        when(op.value.first()) {
            '|' -> 1
            '^' -> 2
            '&' -> 3
            '=', '!' -> 4
            '>', '<' -> 5
            '+', '-', '~' -> 6
            '*', '/', '%' -> 7
            else -> throw Exception()
        }

    fun assertCorrectPrecedence(expression: Expression, lastPrecedence: Int, nextName: String): String =
        when(expression) {
            is AtomicExpression -> {
                assertEquals(NameToken(nextName), expression.atom())
                val c = nextName[0] + 1
                "$c"
            }
            is BinaryExpression -> {
                val precedence = opToPrecedence(expression.op())
                assertTrue { precedence >= lastPrecedence}
                val nn = assertCorrectPrecedence(expression.lhs(), precedence, nextName)
                assertCorrectPrecedence(expression.rhs(), precedence, nn)
            }
            else -> throw Exception()
        }

    @Test
    fun testParsing() {
        repeat(100) {
            for (w in whitespaceCombos) {
                val op1 = operators.random()
                val op2 = operators.random()
                val op3 = operators.random()
                val op4 = operators.random()
                val op5 = operators.random()

                val s1 = "a$w$op1${w}b$w"
                testParse(s1, ::parseExpression) {
                    assertNotNull(it)
                    assertCorrectPrecedence(it, 0, "a")
                }

                val s2 = "a$w$op1${w}b$w$op2${w}c$w"
                testParse(s2, ::parseExpression) {
                    assertNotNull(it)
                    assertCorrectPrecedence(it, 0, "a")
                }

                val s3 = "a$w$op1${w}b$w$op2${w}c$w$op3${w}d$w"
                testParse(s3, ::parseExpression) {
                    assertNotNull(it)
                    assertCorrectPrecedence(it, 0, "a")
                }

                val s4 = "a$w$op1${w}b$w$op2${w}c$w$op3${w}d$w$op4${w}e$w"
                testParse(s4, ::parseExpression) {
                    assertNotNull(it)
                    assertCorrectPrecedence(it, 0, "a")
                }

                val s5 = "a$w$op1${w}b$w$op2${w}c$w$op3${w}d$w$op4${w}e$w$op5${w}f$w"
                testParse(s5, ::parseExpression) {
                    assertNotNull(it)
                    assertCorrectPrecedence(it, 0, "a")
                }
            }
        }
    }

    @Test
    fun testInsertAfter() {
        repeat(100) {
            for (w in whitespaceCombos) {
                val op1 = operators.random()
                val op1Span = getSpan(op1)
                val op2 = operators.random()
                val op2Span = getSpan(op2)
                val op3 = operators.random()
                val op3Span = getSpan(op3)
                val op4 = operators.random()
                val op4Span = getSpan(op4)
                val op5 = operators.random()

                val whitespaceSpan = getSpan(w)

                val s1 = "a$w"
                val text1 = "$op1${w}b$w"
                val span1 = Span(0, 1) + whitespaceSpan
                val change1 = Change(text1, span1, span1)
                testReparse(s1, change1) { parseExpression(it)!! }

                val s2 = "a$w$op1${w}b$w"
                val text2 = "$op2${w}c$w"
                val span2 = span1 + op1Span + whitespaceSpan + Span(0, 1) + whitespaceSpan
                val change2 = Change(text2, span2, span2)
                testReparse(s2, change2) { parseExpression(it)!! }

                val s3 = "a$w$op1${w}b$w$op2${w}c$w"
                val text3 = "$op3${w}d$w"
                val span3 = span2 + op2Span + whitespaceSpan + Span(0, 1) + whitespaceSpan
                val change3 = Change(text3, span3, span3)
                testReparse(s3, change3) { parseExpression(it)!! }

                val s4 = "a$w$op1${w}b$w$op2${w}c$w$op3${w}d$w"
                val text4 = "$op4${w}e$w"
                val span4 = span3 + op3Span + whitespaceSpan + Span(0, 1) + whitespaceSpan
                val change4 = Change(text4, span4, span4)
                testReparse(s4, change4) { parseExpression(it)!! }

                val s5 = "a$w$op1${w}b$w$op2${w}c$w$op3${w}d$w$op4${w}e$w"
                val text5 = "$op5${w}f$w"
                val span5 = span4 + op4Span + whitespaceSpan + Span(0, 1) + whitespaceSpan
                val change5 = Change(text5, span5, span5)
                testReparse(s5, change5) { parseExpression(it)!! }
            }
        }
    }

    @Test
    fun testInsertBefore() {
        repeat(100) {
            for (w in whitespaceCombos) {
                val op1 = operators.random()
                val op2 = operators.random()
                val op3 = operators.random()
                val op4 = operators.random()
                val op5 = operators.random()

                val s1 = "b$w"
                val text = "a$w$op1${w}"
                val change = Change(text, Span.zero, Span.zero)
                testReparse(s1, change) { parseExpression(it)!! }

                val s2 = "b$w$op2${w}c$w"
                testReparse(s2, change) { parseExpression(it)!! }

                val s3 = "b$w$op2${w}c$w$op3${w}d$w"
                testReparse(s3, change) { parseExpression(it)!! }

                val s4 = "b$w$op2${w}c$w$op3${w}d$w$op4${w}e$w"
                testReparse(s4, change) { parseExpression(it)!! }

                val s5 = "b$w$op2${w}c$w$op3${w}d$w$op4${w}e$w$op5${w}f$w"
                testReparse(s5, change) { parseExpression(it)!! }
            }
        }
    }

    @Test
    fun testReplace() {
        repeat(100) {
            for (w in whitespaceCombos) {
                val op = operators.random()
                val op1 = operators.random()
                val op1Span = getSpan(op1)
                val op2 = operators.random()
                val op2Span = getSpan(op2)
                val op3 = operators.random()
                val op3Span = getSpan(op3)
                val op4 = operators.random()
                val op4Span = getSpan(op4)
                val op5 = operators.random()
                val op5Span = getSpan(op5)

                val whitespaceSpan = getSpan(w)

                val s1 = "a$w$op1${w}b$w"
                val start1 = Span(0, 1) + whitespaceSpan
                val end1 = start1 + op1Span
                val change1 = Change(op, start1, end1)
                testReparse(s1, change1) { parseExpression(it)!! }

                val s2 = "a$w$op1${w}b$w$op2${w}c$w"
                val start2 = start1 + op1Span + whitespaceSpan + Span(0, 1) + whitespaceSpan
                val end2 = start2 + op2Span
                val change2 = Change(op, start2, end2)
                testReparse(s2, change1) { parseExpression(it)!! }
                testReparse(s2, change2) { parseExpression(it)!! }

                val s3 = "a$w$op1${w}b$w$op2${w}c$w$op3${w}d$w"
                val start3 = start2 + op2Span + whitespaceSpan + Span(0, 1) + whitespaceSpan
                val end3 = start3 + op3Span
                val change3 = Change(op, start3, end3)
                testReparse(s3, change1) { parseExpression(it)!! }
                testReparse(s3, change2) { parseExpression(it)!! }
                testReparse(s3, change3) { parseExpression(it)!! }

                val s4 = "a$w$op1${w}b$w$op2${w}c$w$op3${w}d$w$op4${w}e$w"
                val start4 = start3 + op3Span + whitespaceSpan + Span(0, 1) + whitespaceSpan
                val end4 = start4 + op4Span
                val change4 = Change(op, start4, end4)
                testReparse(s4, change1) { parseExpression(it)!! }
                testReparse(s4, change2) { parseExpression(it)!! }
                testReparse(s4, change3) { parseExpression(it)!! }
                testReparse(s4, change4) { parseExpression(it)!! }

                val s5 = "a$w$op1${w}b$w$op2${w}c$w$op3${w}d$w$op4${w}e$w$op5${w}e$w"
                val start5 = start4 + op4Span + whitespaceSpan + Span(0, 1) + whitespaceSpan
                val end5 = start5 + op5Span
                val change5 = Change(op, start5, end5)
                testReparse(s5, change1) { parseExpression(it)!! }
                testReparse(s5, change2) { parseExpression(it)!! }
                testReparse(s5, change3) { parseExpression(it)!! }
                testReparse(s5, change4) { parseExpression(it)!! }
                testReparse(s5, change5) { parseExpression(it)!! }
            }
        }
    }
}