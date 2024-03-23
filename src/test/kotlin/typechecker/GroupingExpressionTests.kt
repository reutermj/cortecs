package typechecker

import parser.*
import kotlin.test.*

class GroupingExpressionTests {
    fun validateGroupingExpression(whitespace: String, expressionText: String) {
        val prefix = "($whitespace"
        val text = "$prefix$expressionText)"
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)
        assertIs<GroupingExpression>(expression)

        // Requirement: grouping expression nodes produce grouping expression environments
        val environment = expression.environment
        assertIs<GroupingExpressionEnvironment>(environment)

        val subordinate = environment.subordinate

        // Requirement: grouping expressions produce as its type the same type as the subordinate expression
        assertEquals(subordinate.environment.expressionType, environment.expressionType)

        // Requirement: grouping expressions produce the same requirements as the subordinate expression
        assertEquals(subordinate.environment.requirements, environment.requirements)

        // Requirement: the relative offset to the subordinate expression is the span containing the
        // open paren and whitespace prior to the start of the subordinate expression
        val span = getSpan(prefix)
        assertEquals(span, subordinate.offset)
    }

    @Test
    fun testGroupingExpressionProductionRule() {
        // Tests the grouping expression production rule
        //  e: T | R
        // ----------
        // (e): T | R
        for(whitespace in whitespaceCombos) {
            validateGroupingExpression(whitespace, "x")
            validateGroupingExpression(whitespace, "1")
            validateGroupingExpression(whitespace, "1.1")
        }
    }

    fun validateErrorRelativeOffset(whitespace: String, expressionText: String) {
        val text = "($whitespace$expressionText)"
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)
        assertIs<GroupingExpression>(expression)
        val environment = expression.environment
        assertIs<GroupingExpressionEnvironment>(environment)
        val subordinate = environment.subordinate.environment

        // Requirement: grouping expressions produce the same number of errors as the subordinate
        // TODO: Requirement: The error message/kind should be checked in this test
        assertEquals(subordinate.errors.errors.size, environment.errors.errors.size)

        // Requirement: errors produced by grouping expressions have a relative offset
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
    fun testErrorRelativeOffset() {
        for(whitespace in whitespaceCombos) {
            validateErrorRelativeOffset(whitespace, "1()")
            validateErrorRelativeOffset(whitespace, "(1())")
            validateErrorRelativeOffset(whitespace, "((1()))")
        }
    }

    @Test
    fun testGroupingExpressionWithNoSubordinate() {
        val iterator = ParserIterator()
        iterator.add("(")
        val expression = parseExpression(iterator)
        assertIs<GroupingExpression>(expression)

        // Requirement: grouping expressions with no subordinate produce an empty expression environment
        val environment = expression.environment
        assertIs<EmptyExpressionEnvironment>(environment)
    }

    fun validateGroupingExpressionWithNoClosingParen(whitespace: String, expressionText: String) {
        val prefix = "($whitespace$expressionText"

        val iterator = ParserIterator()
        iterator.add(prefix)
        val expression = parseExpression(iterator)
        assertIs<GroupingExpression>(expression)

        val goldIterator = ParserIterator()
        goldIterator.add("$prefix)")
        val goldExpression = parseExpression(goldIterator)
        assertIs<GroupingExpression>(goldExpression)

        // Requirement: grouping expressions missing the close paren produce the same environment
        // as if it had the closing paren up to id substitution.
        assertTrue { expression.environment.equalsUpToId(goldExpression.environment) }
    }

    @Test
    fun testGroupingExpressionWithNoClosingParen() {
        for(whitespace in whitespaceCombos) {
            validateGroupingExpressionWithNoClosingParen(whitespace, "x")
            validateGroupingExpressionWithNoClosingParen(whitespace, "(x)")
        }
    }
}