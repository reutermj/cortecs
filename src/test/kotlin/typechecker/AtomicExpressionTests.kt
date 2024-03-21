package typechecker

import parser.*
import kotlin.test.*

class AtomicExpressionTests {
    private fun validateAtomicExpressionEnvironment(text: String): AtomicExpressionEnvironment {
        //Requirement: atomic expression nodes produce atomic expression environments
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)
        assertIs<AtomicExpression>(expression)
        return expression.environment
    }

    private fun validateZeroRelativeOffset(environment: AtomicExpressionEnvironment) {
        //Requirement: the relative offset of the produced type is always (0,0)
        val spans = environment.getSpansForType(environment.expressionType)
        assertEquals(1, spans.size)
        assertEquals(Span.zero, spans.first())
    }

    @Test
    fun testVariable() {
        // Tests the variable production rule
        // x is a Variable, U is fresh
        // ---------------------------
        // x: U | x: [U]
        val text = "x"
        val environment = validateAtomicExpressionEnvironment(text)

        //Requirement: variables produce a fresh unification type variable
        assertIs<UnificationTypeVariable>(environment.expressionType)

        //Requirement: variables produce a single requirement on the variable
        val requirements = environment.requirements[NameToken(text)]!!
        assertEquals(1, requirements.size)

        //Requirement: the produced unification type variable and the produced requirement are equal
        val requirement = requirements.first()
        assertEquals(environment.expressionType, requirement)

        validateZeroRelativeOffset(environment)
    }

    private inline fun <reified T: Type> validateConstant(text: String) {
        val environment = validateAtomicExpressionEnvironment(text)

        //Requirement: constants of type T produce type T
        assertIs<T>(environment.expressionType)

        //Requirement: constants produce no requirements
        assertEquals(Requirements.empty, environment.requirements)

        validateZeroRelativeOffset(environment)
    }

    @Test
    fun testConstant() {
        // Tests the constant production rule
        // c is a Constant of type T
        // ---------------------------------------
        // c: T | (/)

        validateConstant<I8Type>("0b")
        validateConstant<I16Type>("0s")
        validateConstant<I32Type>("0")
        validateConstant<I64Type>("0l")

        validateConstant<U8Type>("0ub")
        validateConstant<U16Type>("0us")
        validateConstant<U32Type>("0u")
        validateConstant<U64Type>("0ul")

        validateConstant<F32Type>("0.0")
        validateConstant<F64Type>("0.0d")

        validateConstant<CharacterType>("'a'")
        validateConstant<StringType>("\"hello world\"")
    }
}