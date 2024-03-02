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
        assertEquals(1, environment.subordinates.size)
        val subordinate = environment.subordinates.first()
        assertEquals(span, subordinate.offset)
        assertEquals(subordinate.environment.type, environment.type)
        assertEquals(subordinate.environment.requirements, environment.requirements)

        val spans = environment.getSpansForId(environment.type.id)
        assertEquals(1, spans.size)
        assertEquals(span, spans.first())
    }

    @Test
    fun test() {
        validateGroupingExpression("(", "x", ")")
        validateGroupingExpression("(", "1", ")")
        validateGroupingExpression("(", "1.1", ")")
        for(whitespace in whitespaceCombos) {
            validateGroupingExpression("($whitespace", "x", ")")
            validateGroupingExpression("($whitespace", "1", ")")
            validateGroupingExpression("($whitespace", "1.1", ")")
        }
    }
}