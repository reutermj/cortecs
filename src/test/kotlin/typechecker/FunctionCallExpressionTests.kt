package typechecker

import parser.*
import kotlin.test.*

class FunctionCallExpressionTests {
    fun validate(function: String, args: List<String>) {
        val iterator = ParserIterator()
        iterator.add("$function(${args.joinToString(separator = ",")})")
        val expression = parseExpression(iterator)
        assertIs<FunctionCallExpression>(expression)
        val environment = expression.environment
        assertIs<FunctionCallExpressionEnvironment>(environment)

        // Requirement: the relative offset to the function subordinate is (0,0)
        val functionSubordinate = environment.functionSubordinate
        assertEquals(Span.zero, functionSubordinate.offset)

        // Requirement: there are as many argument subordinates as there are arguments
        val argumentSubordinates = environment.argumentSubordinates
        assertEquals(args.size, argumentSubordinates.size)

        // Requirement: the relative offset of each argument subordinate is equal to the span containing
        // the function expression, the open parenthesis, all whitespace, all prior arguments, and all prior commas
        var accString = "$function("
        for(i in args.indices) {
            assertEquals(getSpan(accString), argumentSubordinates[i].offset)
            accString += args[i] + (if(i == args.size - 1) "" else ",")
        }

        // Requirement: function call expressions produce as their type a fresh unification type variable
        assertIs<UnificationTypeVariable>(environment.expressionType)

        // Requirement: function call expressions unify the type of the function subordinate with an arrow type
        val functionType = environment.applySubstitution(functionSubordinate.environment.expressionType)
        assertIs<ArrowType>(functionType)

        val lhs = functionType.lhs
        when(args.size) {
            // Requirement: when 0 arguments are passed, lhs is unit type
            0 -> assertIs<UnitType>(lhs)

            // Requirement: when 1 argument is passed, lhs is the type produced by the only argument subordinate
            1 -> {
                val subordinate = environment.argumentSubordinates.first()
                assertEquals(subordinate.environment.expressionType, lhs)
            }

            // Requirement: when more than one arguments are passed, lhs is the product type where each place
            // is the type produced by the respective argument subordinate.
            else -> {
                assertIs<ProductType>(lhs)
                for(i in args.indices) {
                    val expressionType = lhs.types[i]
                    val subordinate = environment.argumentSubordinates[i]
                    assertEquals(subordinate.environment.expressionType, expressionType)
                }
            }
        }

        // Requirement: the rhs is the fresh type variable produced
        assertEquals(environment.expressionType, functionType.rhs)
    }

    @Test
    fun testValid() {
        // Tests the function call expression production rule
        // f: T | R, e0: T0 | R0, ..., en: Tn | Rn, U is fresh
        // ---------------------------------------------------
        //         f(e0, ..., en): U | R, R0, ..., Rn

        validate("x", listOf())
        validate("x", listOf("y"))
        validate("x", listOf("y", "z"))
        validate("x", listOf("y", "z", "w"))

        validate("x", listOf())
        validate("x", listOf("1"))
        validate("x", listOf("1", "1.1"))
        validate("x", listOf("1", "1.1", "\"hello world\""))
    }

    fun testInvalid(text: String, span: Span) {
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)!!
        assertIs<FunctionCallExpression>(expression)

        val environment = expression.environment
        assertIs<FunctionCallExpressionEnvironment>(environment)
        assertIs<Invalid>(environment.expressionType)
        assertIs<Invalid>(environment.functionType)

        assertEquals(1, environment.errors.errors.size)
        assertEquals(span, environment.errors.errors.first().offset)
    }

    @Test
    fun testInvalid() {
        testInvalid("1(x, y, z)", Span.zero)
        testInvalid("f(1(), y, z)", Span(0, 2))
        testInvalid("f(x, 1(), z)", Span(0, 5))
        testInvalid("f(x, y, 1())", Span(0, 8))
    }
}