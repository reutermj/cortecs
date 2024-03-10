package typechecker

import parser.*
import kotlin.test.*

class FunctionCallExpressionTests {
    fun validate(function: String, args: List<String>) {
        val iterator = ParserIterator()
        iterator.add("$function(${args.joinToString(separator = ",")})")
        val expression = parseExpression(iterator)!!
        assertIs<FunctionCallExpression>(expression)

        val environment = expression.environment
        assertIs<FunctionCallExpressionEnvironment>(environment)
        assertIs<UnificationTypeVariable>(environment.expressionType)

        assertEquals(args.size, environment.argumentSubordinates.size)
        assertEquals(0, environment.errors.errors.size)

        val functionType = environment.functionType
        assertIs<ArrowType>(functionType)
        val lhs = functionType.lhs
        assertEquals(environment.expressionType, functionType.rhs)
        val functionRequirements = environment.requirements[NameToken(function)]!!
        assertEquals(1, functionRequirements.size)
        val functionRequirement = functionRequirements.first()
        assertEquals(functionType, functionRequirement)

        when (args.size) {
            0 -> assertIs<UnitType>(lhs)
            1 -> {
                val subordinate = environment.argumentSubordinates.first()
                assertEquals(subordinate.environment.expressionType, lhs)
            }

            else -> {
                assertIs<ProductType>(lhs)
                for (i in args.indices) {
                    val expressionType = lhs.types[i]
                    val subordinate = environment.argumentSubordinates[i]
                    assertEquals(subordinate.environment.expressionType, expressionType)
                }
            }
        }
    }

    @Test
    fun testValid() {
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