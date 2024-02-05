package parser_v2

import kotlin.test.*

class LetTests {
    @Test
    fun testWhitespaceHandling() {
        for(i in whitespaceCombos)
            for(j in whitespaceCombos)
                for(k in whitespaceCombos)
                    for(l in whitespaceCombos) {
                        val s = "let ${i}x${j}=${k}y${l}"
                        tryParse(s, ::parseLet) {
                            assertEquals(NameToken("x"), it.name())
                            val expression = it.expression()
                            assertIs<AtomicExpression>(expression)
                            assertEquals(NameToken("y"), expression.atom())
                        }
                    }
    }

    @Test
    fun test002() {

    }

    @Test
    fun test003() {

    }

    @Test
    fun test004() {

    }

    @Test
    fun test005() {

    }

    @Test
    fun test006() {

    }

    @Test
    fun test007() {

    }

    @Test
    fun test008() {

    }

    @Test
    fun test009() {

    }

    @Test
    fun test010() {

    }
}