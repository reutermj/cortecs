package typechecker

import parser.*
import kotlin.test.*

class LetTests {
    fun validateLet(name: String, expressionText: String) {
        val prefix = "let $name = "
        val prefixSpan = getSpan(prefix)
        val text = "$prefix$expressionText"
        val iterator = ParserIterator()
        iterator.add(text)

        val letAst = parseLet(iterator)
        val environment = letAst.environment
        val subordinate = environment.subordinate
        assertEquals(prefixSpan, subordinate.offset)

        val binding = environment.bindings[NameToken(name)]
        assertEquals(subordinate.environment.expressionType, binding)

        assertEquals(subordinate.environment.requirements, environment.requirements)
    }

    @Test
    fun test() {
        validateLet("x", "y")
        validateLet("x", "+y")
        validateLet("x", "y + z")
        validateLet("x", "f(y)")
    }

    @Test
    fun testInvalidSubordinate() {
        val iterator = ParserIterator()
        iterator.add("let x = 1()")
        val letAst = parseLet(iterator)

        val environment = letAst.environment

        assertIs<Invalid>(environment.bindings[NameToken("x")])

        assertEquals(1, environment.errors.errors.size)
        assertEquals(Span(0, 8), environment.errors.errors.first().offset)

    }

    fun testMissingSubordinate(text: String) {
        val iterator = ParserIterator()
        iterator.add(text)
        val letAst = parseLet(iterator)
        assertEquals(EmptyExpressionEnvironment, letAst.environment.subordinate.environment)
    }

    @Test
    fun testMissingSubordinate() {
        testMissingSubordinate("let")
        testMissingSubordinate("let x")
        testMissingSubordinate("let x =")
    }
}