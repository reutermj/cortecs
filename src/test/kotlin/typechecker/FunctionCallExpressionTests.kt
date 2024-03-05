package typechecker

import parser.*
import kotlin.test.*

class FunctionCallExpressionTests {
    fun validate(text: String) {
        val iterator = ParserIterator()
        iterator.add(text)
        val expression = parseExpression(iterator)!!
        assertIs<FunctionCallExpression>(expression)

        val environment = expression.environment
    }

    @Test
    fun testAtom() {
        validate("1(x)")
    }
}