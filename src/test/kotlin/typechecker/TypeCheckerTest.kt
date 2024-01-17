package typechecker

import errors.CortecsError
import parser.*
import kotlin.test.*

class TypeCheckerTest {
    @Test
    fun test001() {
        val text = """function f() {
            |let x = w
            |let y: I32 = x
            |let z: F32 = w
            |}""".trimMargin()

        val iterator = ParserIterator()
        iterator.add(text)
        val function = parseFunction(iterator)
        val environment = typeCheckFunction(function)

        val substitution = Substitution()
        val errors = mutableListOf<CortecsError>()
        for(placeholder in environment.requirements.placeholderLookup[NameToken("w")]!!) {
            substitution.fillPlaceholder(placeholder, I32Type(Span.zero))
            for(requirement in environment.requirements.requirementsLookup[placeholder.id]!!) {
                val lhs = substitution.apply(placeholder)
                val rhs = substitution.apply(requirement)
                Constraints().unify(lhs, rhs, substitution, environment.requirements, errors)
            }
        }

        assertEquals(1, errors.size)
        val error = errors.first()
        assertEquals(Span(3, 13), error.offset)
    }

    @Test
    fun test002() {
        val text = """function f() {
            |let x = w
            |let y: I32 = x
            |let z: F32 = w
            |}""".trimMargin()

        val iterator = ParserIterator()
        iterator.add(text)
        val function = parseFunction(iterator)
        val environment = typeCheckFunction(function)

        val substitution = Substitution()
        val errors = mutableListOf<CortecsError>()
        for(placeholder in environment.requirements.placeholderLookup[NameToken("w")]!!) {
            substitution.fillPlaceholder(placeholder, F32Type(Span.zero))
            for(requirement in environment.requirements.requirementsLookup[placeholder.id]!!) {
                val lhs = substitution.apply(placeholder)
                val rhs = substitution.apply(requirement)
                Constraints().unify(lhs, rhs, substitution, environment.requirements, errors)
            }
        }

        assertEquals(1, errors.size)
        val error = errors.first()
        assertEquals(Span(2, 13), error.offset)
    }

    @Test
    fun test003() {
        val text = """function f(a: I32) {
            |let x: F32 = a
            |}""".trimMargin()

        val iterator = ParserIterator()
        iterator.add(text)
        val function = parseFunction(iterator)
        val environment = typeCheckFunction(function)
        val errors = environment.errors.errors

        assertEquals(1, errors.size)
        val error = errors.first()
        assertEquals(Span(1, 13), error.offset)
    }

    @Test
    fun test004() {
        val text = """function f() {
            |let a = 1
            |let x: F32 = a
            |}""".trimMargin()

        val iterator = ParserIterator()
        iterator.add(text)
        val function = parseFunction(iterator)
        val environment = typeCheckFunction(function)
        val errors = environment.errors.errors

        assertEquals(1, errors.size)
        val error = errors.first()
        assertEquals(Span(2, 13), error.offset)
    }

    @Test
    fun test005() {
        val text = """function f() {
            |let y: I32 = w(1)
            |}""".trimMargin()

        val iterator = ParserIterator()
        iterator.add(text)
        val function = parseFunction(iterator)
        val environment = typeCheckFunction(function)

        val substitution = Substitution()
        val errors = mutableListOf<CortecsError>()
        for(placeholder in environment.requirements.placeholderLookup[NameToken("w")]!!) {
            substitution.fillPlaceholder(placeholder, ArrowType(Span.zero, I32Type(Span.zero), F32Type(Span.zero)))
            for(requirement in environment.requirements.requirementsLookup[placeholder.id]!!) {
                val lhs = substitution.apply(placeholder)
                val rhs = substitution.apply(requirement)
                Constraints().unify(lhs, rhs, substitution, environment.requirements, errors)
            }
        }

        assertEquals(1, errors.size)
        val error = errors.first()
        assertEquals(Span(1, 13), error.offset)
    }
}