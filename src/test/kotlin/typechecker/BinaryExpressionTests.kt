package typechecker

import parser.*
import kotlin.test.*

class BinaryExpressionTests {
    fun validateBinaryExpression(lhs: String, op: String, rhs: String, whitespace: String) {
        val preOp = "$lhs$whitespace"
        val preRhs = "$preOp$op$whitespace"
        val text = "$preRhs$rhs"
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)
        assertIs<BinaryExpression>(expression)
        val environment = expression.environment
        assertIs<BinaryExpressionEnvironment>(environment)

        // Requirement: the relative offset to the left subordinate is (0,0)
        val lhsSubordinate = environment.lhsSubordinate
        assertEquals(Span.zero, lhsSubordinate.offset)

        // Requirement: the relative offset to the right subordinate expression is the span containing the
        // left subordinate, the operator, and all whitespace prior to the start of the right subordinate expression
        val rhsSubordinate = environment.rhsSubordinate
        assertNotNull(rhsSubordinate)
        val rhsSpan = getSpan(preRhs)
        assertEquals(rhsSpan, rhsSubordinate.offset)

        // Requirement: binary expressions produce as their type a fresh unification type variable
        val retType = environment.expressionType
        assertIs<UnificationTypeVariable>(retType)

        // Requirement: Unary expressions produce all requirements of the left subordinate
        assertContainsAllRequirements(environment.requirements, lhsSubordinate.environment.requirements)

        // Requirement: Unary expressions produce all requirements of the right subordinate
        assertContainsAllRequirements(environment.requirements, rhsSubordinate.environment.requirements)

        // Requirement: Binary expressions produce a single additional requirement on the operator
        val opRequirements = environment.requirements[OperatorToken(op)]!!
        val lhsSubordinateOpRequirements = lhsSubordinate.environment.requirements[OperatorToken(op)] ?: emptyList()
        val rhsSubordinateOpRequirements = rhsSubordinate.environment.requirements[OperatorToken(op)] ?: emptyList()
        assertEquals(lhsSubordinateOpRequirements.size + rhsSubordinateOpRequirements.size + 1, opRequirements.size)
        val opReq = opRequirements.first { !lhsSubordinateOpRequirements.contains(it) && !rhsSubordinateOpRequirements.contains(it) }

        // Requirement: The produced additional requirement is an arrow type where:
        //   * the lhs is a two place product type where:
        //     * the first place is the type produced by the left subordinate
        //     * the second place is the type produced by the right subordinate
        //   * the rhs is the fresh type variable produced by the binary expression
        assertIs<ArrowType>(opReq)
        val lhsType = opReq.lhs
        assertIs<ProductType>(lhsType)
        assertEquals(listOf(lhsSubordinate.environment.expressionType, rhsSubordinate.environment.expressionType), lhsType.types)
        assertEquals(environment.expressionType, opReq.rhs)

        // Requirement: the relative offset of the additional requirement is the span containing the
        // left subordinate and the whitespace prior to the start of the operator
        val opSpan = getSpan(preOp)
        assertEquals(listOf(opSpan), environment.getSpansForType(opReq))

        // Requirement: the relative offset of the produced type is the span containing the
        // left subordinate and the whitespace prior to the start of the operator
        assertEquals(listOf(opSpan), environment.getSpansForType(environment.expressionType))
    }

    @Test
    fun test() {
        // Tests the unary expression production rule
        //    e0: T0 | R0, e1: T1 | R1, U is fresh
        // -------------------------------------------
        // e0 <op> e1: U | R0, R1, <op>: (T0, T1) -> U
        for(whitespace in whitespaceCombos) {
            validateBinaryExpression("x", "+", "y", whitespace)
            validateBinaryExpression("(x + y)", "+", "z", whitespace)
            validateBinaryExpression("x", "+", "(y + z)", whitespace)
            validateBinaryExpression("(x + y)", "+", "(z + w)", whitespace)
            validateBinaryExpression("1", "-", "2", whitespace)
            validateBinaryExpression("1.1", "==", "2.2", whitespace)
        }
    }

    fun testInvalidSubordinate(lhs: String, op: String, rhs: String, whitespace: String) {
        val preOp = "$lhs$whitespace"
        val preRhs = "$preOp$op$whitespace"
        val text = "$preRhs$rhs"
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)!!
        assertIs<BinaryExpression>(expression)
        val environment = expression.environment
        assertIs<BinaryExpressionEnvironment>(environment)

        val lhsErrors = environment.lhsSubordinate.environment.errors
        val rhsErrors = environment.rhsSubordinate!!.environment.errors

        // Requirement: binary expressions produce the same number of errors as the
        // left and right subordinates combined
        // TODO: Requirement: The error message/kind should be checked in this test
        assertEquals(lhsErrors.errors.size + rhsErrors.errors.size, environment.errors.errors.size)

        // Requirement: binary expressions produce all errors of the left subordinate with the same relative offset
        assertContainsAllErrors(environment.errors, lhsErrors)

        // Requirement: binary expressions produce all errors of the right subordinate with
        // the relative equal to the span containing the left subordinate, the operator, and
        // all whitespace prior to the start of the right subordinate expression added to the
        // relative offset of the error in the right subordinate
        // TODO probably try wording this one more time
        assertContainsAllErrors(environment.errors, rhsErrors.addOffset(getSpan(preRhs)))
    }

    @Test
    fun testInvalidSubordinate() {
        for(whitespace in whitespaceCombos) {
            testInvalidSubordinate("1()", "+", "x", whitespace)
            testInvalidSubordinate("x", "+", "1()", whitespace)
            testInvalidSubordinate("1()", "+", "1()", whitespace)
        }
    }

    fun validateMissingRightSubordinate(lhs: String, op: String, whitespace: String) {
        val iterator = ParserIterator()
        iterator.add("$lhs$whitespace$op")
        val expression = parseExpression(iterator)!!
        assertIs<BinaryExpression>(expression)

        val environment = expression.environment
        assertIs<BinaryExpressionEnvironment>(environment)

        val lhsSubordinate = environment.lhsSubordinate

        // Requirement: binary expressions with no right subordinate produce an invalid type
        assertIs<Invalid>(environment.expressionType)

        // Requirement: binary expressions with no right subordinate produce the same requirements as the left subordinate
        assertEquals(lhsSubordinate.environment.requirements, environment.requirements)

        // Requirement: binary expressions with no right subordinate produce the same errors as the left subordinate
        assertEquals(lhsSubordinate.environment.errors, environment.errors)
    }

    @Test
    fun testMissingRhs() {
        for(whitespace in whitespaceCombos) {
            validateMissingRightSubordinate("x", "+", whitespace)
            validateMissingRightSubordinate("1()", "+", whitespace)
            validateMissingRightSubordinate("(+x)", "+", whitespace)
            validateMissingRightSubordinate("(x + y)", "+", whitespace)
        }
    }
}