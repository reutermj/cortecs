package typechecker

import parser.*
import kotlin.test.*

class ReturnTests {
    fun validateReturn(expressionText: String) {
        val prefix = "return "
        val prefixSpan = getSpan(prefix)
        val text = "$prefix$expressionText"
        val iterator = ParserIterator()
        iterator.add(text)

        val returnAst = parseReturn(iterator)
        val environment = returnAst.environment
        val subordinate = environment.subordinate
        assertEquals(prefixSpan, subordinate.offset)

        val retTypes = environment.requirements[ReturnTypeToken]
        assertNotNull(retTypes)
        assertEquals(1, retTypes.size)
        assertEquals(subordinate.environment.expressionType, retTypes.first())
    }

    @Test
    fun test() {
        validateReturn("x")
        validateReturn("+x")
        validateReturn("x + y")
        validateReturn("f(x)")
    }

    @Test
    fun testInvalidSubordinate() {
        val iterator = ParserIterator()
        iterator.add("return 1()")
        val returnAst = parseReturn(iterator)

        val environment = returnAst.environment
        assertIs<ReturnEnvironment>(environment)

        assertNull(environment.requirements[ReturnTypeToken])

        assertEquals(1, environment.errors.errors.size)
        assertEquals(Span(0, 7), environment.errors.errors.first().offset)
    }

    @Test
    fun testMissingSubordinate() {
        val iterator = ParserIterator()
        iterator.add("return")
        val returnAst = parseReturn(iterator)

        val environment = returnAst.environment
        assertEquals(EmptyExpressionEnvironment, environment.subordinate.environment)
    }
}