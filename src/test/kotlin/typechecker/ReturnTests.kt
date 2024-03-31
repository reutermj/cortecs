package typechecker

import parser.*
import kotlin.test.*

class ReturnTests {
    fun validateReturn(expressionText: String, whitespace: String) {
        val prefix = "return$whitespace"
        val prefixSpan = getSpan(prefix)
        val text = "$prefix$expressionText"
        val iterator = ParserIterator()
        iterator.add(text)
        val returnAst = parseReturn(iterator)
        val environment = returnAst.environment
        val subordinate = environment.subordinate
        assertEquals(prefixSpan, subordinate.offset)

        // Requirement: return statements produce a single requirement on ReturnTypeToken
        val retTypes = environment.requirements[ReturnTypeToken]
        assertNotNull(retTypes)
        assertEquals(1, retTypes.size)

        // Requirement: the requirement on ReturnTypeToken is the type produced by the subordinate
        assertEquals(subordinate.environment.expressionType, retTypes.first())

        // Requirement: return statements produce all requirements produced by the subordinate
        assertContainsAllRequirements(environment.requirements, subordinate.environment.requirements)

        // Requirement: return statements produce no bindings
        assertEquals(Bindings.empty, environment.bindings)
    }

    @Test
    fun test() {
        for(whitespace in nonEmptyWhitespaceCombos) {
            validateReturn("x", whitespace)
            validateReturn("+x", whitespace)
            validateReturn("x + y", whitespace)
            validateReturn("f(x)", whitespace)
        }

    }

    fun validateInvalidSubordinate(expressionText: String, whitespace: String) {
        val prefix = "return$whitespace"

        val text = "$prefix$expressionText"
        val iterator = ParserIterator()
        iterator.add(text)
        val returnAst = parseReturn(iterator)
        val environment = returnAst.environment

        // Requirement: when the subordinate is invalid, return statements produce no requirements on ReturnTypeToken
        assertNull(environment.requirements[ReturnTypeToken])

        // Requirement: Return statements produce all errors of the subordinate offset by the
        // span containing the return keyword and all whitespace prior to the subordinate
        val subordinate = environment.subordinate
        val subordinateErrors = subordinate.environment.errors.addOffset(getSpan(prefix))
        assertContainsAllErrors(environment.errors, subordinateErrors)

        // Requirement: Return statements produce exactly as many errors as the subordinate
        assertEquals(subordinateErrors.errors.size, environment.errors.errors.size)
    }

    @Test
    fun testInvalidSubordinate() {
        for(whitespace in nonEmptyWhitespaceCombos) {
            validateInvalidSubordinate("1()", whitespace)
            validateInvalidSubordinate("(1())", whitespace)
            validateInvalidSubordinate("+1()", whitespace)
            validateInvalidSubordinate("1() + x", whitespace)
            validateInvalidSubordinate("x + 1()", whitespace)
            validateInvalidSubordinate("1() + 1()", whitespace)
        }
    }

    @Test
    fun testMissingSubordinate() {
        val iterator = ParserIterator()
        iterator.add("return")
        val returnAst = parseReturn(iterator)

        val environment = returnAst.environment

        // Requirement: return statements without a subordinate produce no bindings
        assertEquals(Bindings.empty, environment.bindings)

        // Requirement: return statements without a subordinate produce no requirements
        assertEquals(Requirements.empty, environment.requirements)
    }
}