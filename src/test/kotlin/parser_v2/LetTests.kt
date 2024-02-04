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
                        val iterator = ParserIterator()
                        iterator.add(s)
                        val let = parseLet(iterator)

                        assertEquals(NameToken("x"), let.name())
                        val expression = let.expression()
                        assertIs<AtomicExpression>(expression)
                        assertEquals(NameToken("y"), expression.atom())
                        assertFails { iterator.nextToken() }

                        val builder = StringBuilder()
                        let.stringify(builder)
                        assertEquals(s, builder.toString())
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