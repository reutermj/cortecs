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

        // Requirement: Let statements produce a single binding on the name
        val binding = environment.bindings[NameToken(name)]

        // Requirement: the produced binding is equal to the type produced by the subordinate
        assertEquals(subordinate.environment.expressionType, binding)

        // Requirement: Let statements produce all requirements produced by the subordinate
        assertEquals(subordinate.environment.requirements, environment.requirements)
    }

    @Test
    fun test() {
        validateLet("x", "y")
        validateLet("x", "+y")
        validateLet("x", "y + z")
        validateLet("x", "f(y)")
    }

    fun validateInvalidSubordinate(name: String, whitespace: String, expression: String) {
        val iterator = ParserIterator()
        val prefix = "let $whitespace$name$whitespace= $whitespace"
        iterator.add("$prefix$expression")
        val letAst = parseLet(iterator)
        val environment = letAst.environment
        val subordinate = environment.subordinate.environment

        // Requirement: Let statements produce a single binding on the name
        val binding = environment.bindings[NameToken(name)]

        // Requirement: the produced binding is equal to the type produced by the subordinate
        assertEquals(subordinate.expressionType, binding)

        // Requirement: Let statements produce all requirements produced by the subordinate
        assertEquals(subordinate.requirements, environment.requirements)

        // Requirement: Let statements produce all errors produced by the subordinate with the
        // span containing the let keyword, name, equal sign, and all whitespace added
        // to the relative offset
        val subordinateErrors = subordinate.errors.addOffset(getSpan(prefix))
        assertContainsAllErrors(environment.errors, subordinateErrors)
    }

    @Test
    fun testInvalidSubordinate() {
        for(whitespace in whitespaceCombos) {
            validateInvalidSubordinate("x", whitespace, "1()")
            validateInvalidSubordinate("x", whitespace, "+1()")
            validateInvalidSubordinate("x", whitespace, "(1())")
            validateInvalidSubordinate("x", whitespace, "1()+y")
            validateInvalidSubordinate("x", whitespace, "y+1()")
            validateInvalidSubordinate("x", whitespace, "1()+1()")
        }
    }

    fun testMissingSubordinate(name: String) {
        val iterator = ParserIterator()
        iterator.add("let $name =")
        val letAst = parseLet(iterator)
        val environment = letAst.environment
        val subordinate = environment.subordinate.environment

        // Requirement: Let statements without a subordinate produce a single binding on the name
        val binding = environment.bindings[NameToken(name)]

        // Requirement: the produced binding is an invalid type
        assertIs<Invalid>(binding)

        // Requirement: Let statements without a subordinate produce no requirements
        assertEquals(Requirements.empty, environment.requirements)

        // Requirement: Let statements without a subordinate produce an empty environment subordinate
        assertEquals(EmptyExpressionEnvironment, subordinate)
    }

    @Test
    fun testMissingSubordinate() {
        testMissingSubordinate("x")
        testMissingSubordinate("y")
    }

    @Test
    fun testMissingName() {
        val iterator = ParserIterator()
        iterator.add("let")
        val letAst = parseLet(iterator)
        val environment = letAst.environment
        val subordinate = environment.subordinate.environment

        // Requirement: Let statements without a name produce no bindings
        assertEquals(Bindings.empty, environment.bindings)

        // Requirement: Let statements without a name produce no requirements
        assertEquals(Requirements.empty, environment.requirements)

        // Requirement: Let statements without a name produce an empty environment subordinate
        assertEquals(EmptyExpressionEnvironment, subordinate)
    }

    fun validateAnnotation(name: String, annotation: String, expression: String, whitespace: String) {
        val prefix = "let $whitespace$name:$whitespace$annotation$whitespace= $whitespace"
        val text = "$prefix$expression"
        val iterator = ParserIterator()
        iterator.add(text)
        val letAst = parseLet(iterator)
        val environment = letAst.environment
        val subordinate = environment.subordinate.environment

        // Requirement: Applying the substitution to the type produced by the subordinate should
        // equal the type bound to name
        val binding = environment.bindings[NameToken(name)]!!
        val appliedType = environment.applySubstitution(subordinate.expressionType)
        assertEquals(binding, appliedType)

        // Requirement: the spans produced by the type bound to name are the same spans for the type
        // produced by the subordinate with the relative offset to the subordinate added to them
        val prefixSpan = getSpan(prefix)
        val subordinateSpans = subordinate.getSpansForType(subordinate.expressionType).map {prefixSpan + it}
        val requirementSpans = environment.getSpansForType(binding)
        assertContainsSameSpans(subordinateSpans, requirementSpans)
    }

    @Test
    fun testAnnotation() {
        for(whitespace in whitespaceCombos) {
            validateAnnotation("x", "U32", "y", whitespace)
            validateAnnotation("x", "U32", "(y)", whitespace)
            validateAnnotation("x", "U32", "+y", whitespace)
            validateAnnotation("x", "U32", "y + z", whitespace)
            validateAnnotation("x", "U32", "f(y)", whitespace)
        }
    }
}