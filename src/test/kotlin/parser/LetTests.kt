package parser

import kotlin.test.*

class LetTests {
    @Test
    fun testParse() {
        repeat(500) {
            for(w in whitespaceCombos) {
                val expressionString = randomExpression()
                val iterator = ParserIterator()
                iterator.add(expressionString)
                val goldExpression = parseExpression(iterator)!!
                val s = "let ${w}x${w}= ${w}$expressionString"
                testParse(s, ::parseLet) {
                    assertEquals(NameToken("x"), it.name())
                    assertEquals(goldExpression, it.expression())
                }
            }
        }
    }
}