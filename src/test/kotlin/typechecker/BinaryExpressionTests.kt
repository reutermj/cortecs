package typechecker

import parser.*
import kotlin.test.*

class BinaryExpressionTests {
    fun validateBinaryExpression(lhs: String, preOpText: String, op: String, postOp: String, rhs: String) {
        val preOp = "$lhs$preOpText"
        val opSpan = getSpan(preOp)
        val preRhs = "$preOp$op$postOp"
        val rhsSpan = getSpan(preRhs)
        val text = "$preRhs$rhs"
        val iterator = ParserIterator()
        iterator.add(text)

        val expression = parseExpression(iterator)
        assertIs<BinaryExpression>(expression)
        val environment = expression.environment
        assertIs<BinaryExpressionEnvironment>(environment)
        val lhsSubordinate = environment.lhsSubordinate
        assertEquals(Span.zero, lhsSubordinate.offset)
        val rhsSubordinate = environment.rhsSubordinate
        assertEquals(rhsSpan, rhsSubordinate.offset)

        val retType = environment.expressionType
        assertIs<UnificationTypeVariable>(retType)

        val opRequirements = environment.requirements[OperatorToken(op)]!!
        assertEquals(1, opRequirements.size)
        val opReq = opRequirements.first()
        assertIs<ArrowType>(opReq)
        val lhsType = opReq.lhs
        assertIs<ProductType>(lhsType)
        assertEquals(lhsType.types, listOf(lhsSubordinate.environment.expressionType, rhsSubordinate.environment.expressionType))
        assertEquals(opReq.rhs, environment.expressionType)

        assertEquals(listOf(Span.zero), environment.getSpansForId(lhsSubordinate.environment.expressionType.id))
        assertEquals(listOf(rhsSpan), environment.getSpansForId(rhsSubordinate.environment.expressionType.id))
        assertEquals(listOf(opSpan), environment.getSpansForId(opReq.id))
        assertEquals(listOf(opSpan), environment.getSpansForId(lhsType.id))
        assertEquals(listOf(opSpan), environment.getSpansForId(environment.expressionType.id))
    }

    @Test
    fun test() {
        validateBinaryExpression("x", "", "+", "", "y")
        validateBinaryExpression("1", "", "-", "", "2")
        validateBinaryExpression("1.1", "", "==", "", "2.2")
        for(whitespace in whitespaceCombos) {
            validateBinaryExpression("x", whitespace, "+", whitespace, "y")
            validateBinaryExpression("1", whitespace, "-", whitespace, "2")
            validateBinaryExpression("1.1", whitespace, "==", whitespace, "2.2")
        }
    }

    fun testInvalidSubordinate(text: String, offset: Span) {
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)!!
        assertIs<BinaryExpression>(expression)

        val environment = expression.environment
        assertIs<BinaryExpressionEnvironment>(environment)
        assertIs<Invalid>(environment.expressionType)
        assertIs<Invalid>(environment.opType)
        assertNull(environment.requirements[OperatorToken("+")])

        assertEquals(1, environment.errors.errors.size)
        assertEquals(offset, environment.errors.errors.first().offset)
    }

    @Test
    fun testInvalidSubordinate() {
        testInvalidSubordinate("1() + x", Span.zero)
        testInvalidSubordinate("x + 1()", Span(0, 4))
    }

    @Test
    fun testMissingRhs() {
        val iterator = ParserIterator()
        iterator.add("x +")
        val expression = parseExpression(iterator)!!
        assertIs<BinaryExpression>(expression)

        val environment = expression.environment
        assertIs<BinaryExpressionEnvironment>(environment)
        assertIs<Invalid>(environment.expressionType)
        assertIs<Invalid>(environment.opType)
        assertNull(environment.requirements[OperatorToken("+")])

        assertEquals(0, environment.errors.errors.size)
    }
}