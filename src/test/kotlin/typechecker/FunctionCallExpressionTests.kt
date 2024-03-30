package typechecker

import parser.*
import kotlin.test.*

class FunctionCallExpressionTests {
    fun validate(function: String, args: List<String>, whitespace: String) {
        val iterator = ParserIterator()
        val prefix = "$function$whitespace($whitespace"
        iterator.add("$prefix${args.joinToString(separator = ",$whitespace")})")
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
        var accString = prefix
        for(i in args.indices) {
            assertEquals(getSpan(accString), argumentSubordinates[i].offset)
            accString += "${args[i]},$whitespace"
        }

        // Requirement: function call expressions produce as their type a fresh unification type variable
        assertIs<UnificationTypeVariable>(environment.expressionType)

        // Requirement: function call expressions unify the type of the function subordinate with an arrow type
        val functionType = environment.applySubstitution(functionSubordinate.environment.expressionType)
        assertIs<ArrowType>(functionType)

        // Requirement: Function call expressions produce no additional requirements
        val numSubordinateRequirements = argumentSubordinates.fold(numRequirements(functionSubordinate.environment.requirements)) { acc, sub -> acc + numRequirements(sub.environment.requirements)}
        assertEquals(numSubordinateRequirements, numRequirements(environment.requirements))
        
        // Requirement: Function call expressions contain all function subordinate requirements after applying
        // the substitution to them
        for((k, v) in functionSubordinate.environment.requirements.requirements) {
            val requirements = environment.requirements[k]!!
            for(requirement in v) {
                val applied = environment.applySubstitution(requirement)
                assertContains(requirements, applied)
            }
        }

        // Requirement: function call expressions produce all argument subordinate requirements as is
        for(subordinate in argumentSubordinates) {
            assertContainsAllRequirements(environment.requirements, subordinate.environment.requirements)
        }

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

        for(whitespace in whitespaceCombos) {
            validate("x", listOf(), whitespace)
            validate("x", listOf("y"), whitespace)
            validate("(x)", listOf("y"), whitespace)
            validate("(+x)", listOf("y"), whitespace)
            validate("(x + y)", listOf("y"), whitespace)
            validate("(x + y * z)", listOf("y"), whitespace)
            validate("(x + y * z)", listOf("y", "z"), whitespace)
            validate("(x + y * z)", listOf("y", "z", "1"), whitespace)
            validate("x", listOf("y", "z"), whitespace)
            validate("x", listOf("y", "z", "w"), whitespace)

            validate("x", listOf(), whitespace)
            validate("x", listOf("1"), whitespace)
            validate("x", listOf("1", "1.1"), whitespace)
            validate("x", listOf("1", "1.1", "\"hello world\""), whitespace)
        }
    }

    private fun validateErrorPassthrough(function: String, args: List<String>, whitespace: String) {
        val iterator = ParserIterator()
        val prefix = "$function$whitespace($whitespace"
        iterator.add("$prefix${args.joinToString(separator = ",$whitespace")})")
        val expression = parseExpression(iterator)
        assertIs<FunctionCallExpression>(expression)
        val environment = expression.environment
        assertIs<FunctionCallExpressionEnvironment>(environment)

        // Requirement: function call expressions produce an invalid type if any subordinate is invalid
        // todo this requirement may need to be reworked. Kotlin can figure out the type of baz in
        // fun foo(i: Int): Float = ...
        // fun bar() { val baz = foo(1()) }
        // but it cant figure out the type in cases like this
        // val baz = (1())(1, 2, 3)
        assertIs<Invalid>(environment.expressionType)

        // Requirement: function call expressions produce the same number of errors as the subordinates combined
        val numSubordinateErrors = environment.argumentSubordinates.fold(environment.functionSubordinate.environment.errors.errors.size) { acc, sub ->
            acc + sub.environment.errors.errors.size
        }
        assertEquals(numSubordinateErrors, environment.errors.errors.size)

        // Requirement: function call expressions produce the same errors as the function subordinate
        assertContainsAllErrors(environment.errors, environment.functionSubordinate.environment.errors)

        // Requirement: function call expressions produce the same errors as the argument subordinates
        // with the span containing the function expression, the open parenthesis, all whitespace, all prior arguments, and all prior commas
        // added to the relative offset
        var accString = prefix
        val argumentSubordinates = environment.argumentSubordinates
        for(i in args.indices) {
            val argumentErrors = argumentSubordinates[i].environment.errors.addOffset(getSpan(accString))
            assertContainsAllErrors(environment.errors, argumentErrors)
            accString += "${args[i]},$whitespace"
        }
    }

    @Test
    fun testInvalid() {
        for(whitespace in whitespaceCombos) {
            validateErrorPassthrough("x", listOf("1()", "y", "z"), whitespace)
            validateErrorPassthrough("(1())", listOf("1()", "y", "z"), whitespace)
            validateErrorPassthrough("x", listOf("1()", "y", "1()"), whitespace)
            validateErrorPassthrough("x", listOf("1()", "1()", "z"), whitespace)
            validateErrorPassthrough("x", listOf("(1() + 1())", "1()", "z"), whitespace)
            validateErrorPassthrough("x", listOf("1()", "1()", "1()"), whitespace)
            validateErrorPassthrough("x", listOf("1()", "(1() + 1() * 1())", "1()"), whitespace)
            validateErrorPassthrough("x", listOf("x", "1()", "z"), whitespace)
            validateErrorPassthrough("x", listOf("x", "1()", "1()"), whitespace)
            validateErrorPassthrough("x", listOf("x", "y", "1()"), whitespace)
        }
    }

    private fun validateUnificationError(function: String, args: List<String>, whitespace: String) {
        val iterator = ParserIterator()
        val prefix = "$function$whitespace($whitespace"
        iterator.add("$prefix${args.joinToString(separator = ",$whitespace")})")
        val expression = parseExpression(iterator)
        assertIs<FunctionCallExpression>(expression)
        val environment = expression.environment
        assertIs<FunctionCallExpressionEnvironment>(environment)

        // Requirement: function call expressions produce an invalid type on unification errors
        assertIs<Invalid>(environment.expressionType)

        // Requirement: on unification error, function call expressions produce the one more error than the subordinates combined
        val functionSubordinate = environment.functionSubordinate.environment
        val numSubordinateErrors = environment.argumentSubordinates.fold(functionSubordinate.errors.errors.size) { acc, sub ->
            acc + sub.environment.errors.errors.size
        }
        assertEquals(numSubordinateErrors + 1, environment.errors.errors.size)

        val subordinateErrors = functionSubordinate.errors.errors.toMutableList()
        var accString = prefix
        val argumentSubordinates = environment.argumentSubordinates
        for(i in args.indices) {
            val argumentErrors = argumentSubordinates[i].environment.errors.addOffset(getSpan(accString))
            subordinateErrors.addAll(argumentErrors.errors)
            accString += "${args[i]},$whitespace"
        }

        val additionalError = environment.errors.errors.first { !subordinateErrors.contains(it) }
        val spans = functionSubordinate.getSpansForType(functionSubordinate.expressionType)
        assertEquals(1, spans.size)
        val span = spans.first()
        assertEquals(span, additionalError.offset)
    }

    @Test
    fun testUnificationErrors() {
        // currently, (1())(x, y) will skip the unification step in the outer function call expression because
        // it sees the 1() as producing an invalid type and short circuits the generation of the environment.
        // TODO figure out if this is the desired behavior
        for(whitespace in whitespaceCombos) {
            validateUnificationError("1", listOf("x"), whitespace)
            validateUnificationError("(1)", listOf("x"), whitespace)
            validateUnificationError("((1))", listOf("x"), whitespace)
            validateUnificationError("(((1)))", listOf("x"), whitespace)
        }
    }

    fun validateRequirements(function: String, functionName: String, args: List<String>, whitespace: String) {
        val iterator = ParserIterator()
        val prefix = "$function$whitespace($whitespace"
        iterator.add("$prefix${args.joinToString(separator = ",$whitespace")})")
        val expression = parseExpression(iterator)
        assertIs<FunctionCallExpression>(expression)
        val environment = expression.environment
        assertIs<FunctionCallExpressionEnvironment>(environment)

        val functionEnvironment = environment.functionSubordinate.environment
        val functionTypeSpans = functionEnvironment.getSpansForType(functionEnvironment.expressionType)

        // Requirement: applying the substitution to the type produced by the function subordinate
        // produces an arrow type
        val functionTypeApplied = environment.applySubstitution(functionEnvironment.expressionType)
        assertIs<ArrowType>(functionTypeApplied)

        // Requirement: the produced arrow type produce the same spans as the type produced by the
        // function call expression
        val functionTypeAppliedSpans = environment.getSpansForType(functionTypeApplied)
        assertContainsSameSpans(functionTypeSpans, functionTypeAppliedSpans)

        if(functionName != "") {
            // Requirement: function call expressions update the requirement of function names to be
            // the arrow type
            val functionRequirements = environment.requirements[NameToken(functionName)]!!
            assertEquals(1, functionRequirements.size)
            val functionRequirement = functionRequirements.first()
            assertIs<ArrowType>(functionRequirement)
            assertEquals(functionTypeApplied, functionRequirement)

            // Requirement: the spans produced by the requirement are the same spans as
            // the type produced by the function call expression
            val functionRequirementSpans = environment.getSpansForType(functionRequirement)
            assertContainsSameSpans(functionTypeSpans, functionRequirementSpans)
        }

        when(args.size) {
            0 -> {
                // Requirement: the spans produced by the unit type are the same spans as
                // the type produced by the function call expression
                val unit = functionTypeApplied.lhs
                assertIs<UnitType>(unit)
                val unitSpans = environment.getSpansForType(unit)
                assertContainsSameSpans(functionTypeSpans, unitSpans)
            }
            1 -> {
                // Requirement: the spans produced by a single type is the same as the spans
                // produced by the single argument subordinate offset by the span containing the function expression,
                // the open parenthesis, all whitespace, all prior arguments, and all prior commas
                // added to the relative offset
                val argumentSubordinate = environment.argumentSubordinates.first().environment
                val offset = getSpan(prefix)
                val argumentSpans = argumentSubordinate.getSpansForType(argumentSubordinate.expressionType).map { offset + it }
                val productSpans = environment.getSpansForType(functionTypeApplied.lhs)
                assertContainsSameSpans(argumentSpans, productSpans)
            }
            2 -> {
                val product = functionTypeApplied.lhs
                assertIs<ProductType>(product)
                val productTypeSpans = environment.getSpansForType(product)

                // Requirement: the spans produced by the product type are the same spans as
                // the type produced by the function call expression
                assertContainsSameSpans(functionTypeSpans, productTypeSpans)

                val argumentSubordinates = environment.argumentSubordinates
                var accString = prefix
                for(i in args.indices) {
                    // Requirement: the spans produced by each argument type is the same as the spans
                    // produced by the argument subordinate offset by the span containing the function expression,
                    // the open parenthesis, all whitespace, all prior arguments, and all prior commas
                    // added to the relative offset
                    val argumentSubordinate = argumentSubordinates[i].environment
                    val offset = getSpan(accString)
                    val argumentSpans = argumentSubordinate.getSpansForType(argumentSubordinate.expressionType).map { offset + it }
                    val productSpans = environment.getSpansForType(product.types[i])
                    assertContainsSameSpans(argumentSpans, productSpans)
                    accString += "${args[i]},$whitespace"
                }
            }
        }
    }

    @Test
    fun requirement() {
        for(whitespace in whitespaceCombos) {
            validateRequirements("f", "f", listOf(), whitespace)
            validateRequirements("f", "f", listOf("1"), whitespace)
            validateRequirements("f", "f", listOf("1", "x"), whitespace)

            validateRequirements("(${whitespace}f)", "f", listOf(), whitespace)
            validateRequirements("(${whitespace}f)", "f", listOf("1"), whitespace)
            validateRequirements("(${whitespace}f)", "f", listOf("1", "x"), whitespace)

            validateRequirements("((${whitespace}f))", "f", listOf(), whitespace)
            validateRequirements("((${whitespace}f))", "f", listOf("1"), whitespace)
            validateRequirements("((${whitespace}f))", "f", listOf("1", "x"), whitespace)

            validateRequirements("(+x)", "", listOf(), whitespace)
            validateRequirements("(+x)", "", listOf("1"), whitespace)
            validateRequirements("(+x)", "", listOf("1", "x"), whitespace)

            validateRequirements("(x+y)", "", listOf(), whitespace)
            validateRequirements("(x+y)", "", listOf("1"), whitespace)
            validateRequirements("(x+y)", "", listOf("1", "x"), whitespace)
        }
    }
}