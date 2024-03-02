package typechecker

import parser.*
import kotlin.test.*

class UnaryExpressionTests {
    fun validateUnaryExpression(op: String, pretext: String, expressionText: String) {
        val prefix = "$op$pretext"
        val prefixSpan = getSpan(prefix)
        val text = "$prefix$expressionText"
        val iterator = ParserIterator()
        iterator.add(text)

        val expression = parseExpression(iterator)
        assertIs<UnaryExpression>(expression)
        val environment = expression.environment
        assertIs<UnaryExpressionEnvironment>(environment)
        val subordinate = environment.subordinate
        assertEquals(prefixSpan, subordinate.offset)

        val retType = environment.type
        assertIs<UnificationTypeVariable>(retType)

        val opRequirements = environment.requirements[OperatorToken(op)]!!
        assertEquals(1, opRequirements.size)
        val opReq = opRequirements.first()
        assertIs<ArrowType>(opReq)
        assertEquals(opReq.lhs, subordinate.environment.type)
        assertEquals(opReq.rhs, environment.type)

        assertEquals(listOf(prefixSpan), environment.getSpansForId(subordinate.environment.type.id))
        assertEquals(listOf(Span.zero), environment.getSpansForId(opReq.id))
        assertEquals(listOf(Span.zero), environment.getSpansForId(environment.type.id))
    }

    @Test
    fun test() {
        validateUnaryExpression("+", "", "x")
        validateUnaryExpression("-", "", "1")
        validateUnaryExpression("==", "", "1.1")
        for(whitespace in whitespaceCombos) {
            validateUnaryExpression("+", whitespace, "x")
            validateUnaryExpression("-", whitespace, "1")
            validateUnaryExpression("==", whitespace, "1.1")
        }
    }
}