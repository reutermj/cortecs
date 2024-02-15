package parser

import kotlin.test.*

class ReturnTests {
    @Test
    fun testParse() {
        repeat(500) {
            for(w in whitespaceCombos) {
                val expressionString = randomExpression()
                val iterator = ParserIterator()
                iterator.add(expressionString)
                val goldExpression = parseExpression(iterator)!!
                val s = "return ${w}$expressionString"
                testParse(s, ::parseReturn) {
                    assertEquals(goldExpression, it.expression())
                }
            }
        }
    }
}