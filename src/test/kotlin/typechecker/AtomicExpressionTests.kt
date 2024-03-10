package typechecker

import parser.*
import kotlin.test.*

class AtomicExpressionTests {
    inline fun <reified T : Type> validateAtom(text: String, shouldHaveRequirements: Boolean) {
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)!!
        assertIs<AtomicExpression>(expression)

        val environment = expression.environment
        assertIs<T>(environment.expressionType)
        if (shouldHaveRequirements) {
            val requirements = environment.requirements[NameToken(text)]!!
            assertEquals(1, requirements.size)
            val requirement = requirements.first()
            assertEquals(environment.expressionType, requirement)
        } else {
            assertEquals(Requirements.empty, environment.requirements)
        }

        val spans = environment.getSpansForId(environment.expressionType.id)
        assertEquals(1, spans.size)
        assertEquals(Span.zero, spans.first())
    }

    @Test
    fun testAtom() {
        validateAtom<UnificationTypeVariable>("x", true)

        validateAtom<I8Type>("0b", false)
        validateAtom<I16Type>("0s", false)
        validateAtom<I32Type>("0", false)
        validateAtom<I64Type>("0l", false)

        validateAtom<U8Type>("0ub", false)
        validateAtom<U16Type>("0us", false)
        validateAtom<U32Type>("0u", false)
        validateAtom<U64Type>("0ul", false)

        validateAtom<F32Type>("0.0", false)
        validateAtom<F64Type>("0.0d", false)

        validateAtom<CharacterType>("'a'", false)
        validateAtom<StringType>("\"hello world\"", false)
    }
}