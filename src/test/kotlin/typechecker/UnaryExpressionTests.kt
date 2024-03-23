package typechecker

import parser.*
import kotlin.test.*

class UnaryExpressionTests {
    fun validateUnaryExpression(op: String, whitespace: String, expressionText: String) {
        val prefix = "$op$whitespace"
        val prefixSpan = getSpan(prefix)
        val text = "$prefix$expressionText"
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)
        assertIs<UnaryExpression>(expression)

        // Requirement: unary expression nodes produce unary expression environments
        val environment = expression.environment
        assertIs<UnaryExpressionEnvironment>(environment)
        val subordinate = environment.subordinate

        // Requirement: the relative offset to the subordinate expression is the span containing the
        // operator and whitespace prior to the start of the subordinate expression
        assertEquals(prefixSpan, subordinate.offset)

        // Requirement: Unary expressions produce as its type a fresh unification type variable
        val retType = environment.expressionType
        assertIs<UnificationTypeVariable>(retType)

        // Requirement: Unary expressions produce all requirements of the subordinate
        for((name, types) in subordinate.environment.requirements.requirements) {
            val requirements = environment.requirements[name]!!
            for(type in types) {
                assertContains(requirements, type)
            }
        }

        // Requirement: Unary expressions produce a single additional requirement on the operator
        val opRequirements = environment.requirements[OperatorToken(op)]!!
        val subordinateRequirements = environment.subordinate.environment.requirements[OperatorToken(op)] ?: emptyList()
        assertEquals(subordinateRequirements.size + 1, opRequirements.size)
        val opReq = opRequirements.first { !subordinateRequirements.contains(it) }

        // Requirement: The produced additional requirement is an arrow type where:
        //   * the lhs is the type produced by the subordinate
        //   * the rhs is the fresh type variable produced by the unary expression
        assertIs<ArrowType>(opReq)
        assertEquals(opReq.lhs, subordinate.environment.expressionType)
        assertEquals(opReq.rhs, environment.expressionType)

        // Requirement: The relative offset of the additional requirement is (0,0)
        assertEquals(listOf(Span.zero), environment.getSpansForType(opReq))

        // Requirement: The relative offset of the produced type is (0,0)
        assertEquals(listOf(Span.zero), environment.getSpansForType(environment.expressionType))
    }

    @Test
    fun test() {
        // Tests the unary expression production rule
        //     e: T | R, U is fresh
        // -----------------------------
        // <op> e: U | R, <op>: (T) -> U
        for(whitespace in whitespaceCombos) {
            validateUnaryExpression("+", whitespace, "x")
            validateUnaryExpression("+", whitespace, "(+x)")
            validateUnaryExpression("+", whitespace, "(+ +x)")
            validateUnaryExpression("-", whitespace, "1")
            validateUnaryExpression("==", whitespace, "1.1")
        }
    }

    fun validateErrorRelativeOffset(operator: String, whitespace: String, expressionText: String) {
        val text = "$operator$whitespace$expressionText"
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)
        assertIs<UnaryExpression>(expression)
        val environment = expression.environment
        assertIs<UnaryExpressionEnvironment>(environment)
        val subordinate = environment.subordinate.environment

        // Requirement: unary expressions produce the same number of errors as the subordinate
        // TODO: Requirement: The error message/kind should be checked in this test
        assertEquals(subordinate.errors.errors.size, environment.errors.errors.size)

        // Requirement: errors produced by unary expressions have a relative offset
        // equal to the relative offset of the error produced by the subordinate expression
        // plus the relative offset to the subordinate expression
        val offset = environment.subordinate.offset
        for(i in 0 until environment.errors.errors.size) {
            val subordinateError = subordinate.errors.errors[i]
            val expressionError = environment.errors.errors[i]

            assertEquals(offset + subordinateError.offset, expressionError.offset)
        }
    }

    @Test
    fun testInvalidSubordinate() {
        for(whitespace in whitespaceCombos) {
            validateErrorRelativeOffset("+", whitespace, "1()")
            validateErrorRelativeOffset("+", whitespace, "(1())")
            validateErrorRelativeOffset("+", whitespace, "(+1())")
        }
    }

    @Test
    fun testMissingSubordinate() {
        val iterator = ParserIterator()
        iterator.add("+")
        val expression = parseExpression(iterator)
        assertIs<UnaryExpression>(expression)

        // Requirement: unary expressions with no subordinate produce an empty expression environment
        val environment = expression.environment
        assertEquals(EmptyExpressionEnvironment, environment)
    }
}