package typechecker

import parser.*
import kotlin.test.*

class GroupingExpressionTests {
    fun validateGroupingExpression(prefix: String, expressionText: String, postfix: String) {
        val span = getSpan(prefix)
        val text = "$prefix$expressionText$postfix"
        val iterator = ParserIterator()
        iterator.add(text)

        val expression = parseExpression(iterator)
        assertIs<GroupingExpression>(expression)
        val environment = expression.environment
        assertIs<GroupingExpressionEnvironment>(environment)
        val subordinate = environment.subordinate
        assertEquals(span, subordinate.offset)
        assertEquals(subordinate.environment.expressionType, environment.expressionType)
        assertEquals(subordinate.environment.requirements, environment.requirements)

        val spans = environment.getSpansForId(environment.expressionType.id)
        assertEquals(1, spans.size)
        assertEquals(span, spans.first())
    }

    @Test
    fun test() {
        validateGroupingExpression("(", "x", ")")
        validateGroupingExpression("(", "1", ")")
        validateGroupingExpression("(", "1.1", ")")
        for (whitespace in whitespaceCombos) {
            validateGroupingExpression("($whitespace", "x", ")")
            validateGroupingExpression("($whitespace", "1", ")")
            validateGroupingExpression("($whitespace", "1.1", ")")
        }
    }

    @Test
    fun testError() {
        val iterator = ParserIterator()
        iterator.add("(1())")
        val expression = parseExpression(iterator)!!
        assertIs<GroupingExpression>(expression)

        val environment = expression.environment
        assertIs<GroupingExpressionEnvironment>(environment)
        assertIs<Invalid>(environment.expressionType)

        assertEquals(1, environment.errors.errors.size)
        assertEquals(Span(0, 1), environment.errors.errors.first().offset)
    }
}