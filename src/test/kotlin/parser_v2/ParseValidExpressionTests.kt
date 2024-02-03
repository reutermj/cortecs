package parser_v2

import kotlin.test.*

class ParseValidExpressionTests {
    @Test
    fun test001() {
        val s = "x + y"
        val iterator = ParserIterator()
        iterator.add(s)
        val expression = parseExpression(iterator)

        val reiterator = expression.createChangeIterator(Change(" * z", Span(0, 5), Span(0, 5)))
        val reexpression = parseExpression(reiterator)
        assertIs<BinaryExpression>(reexpression)
        assertEquals(OperatorToken("+"), reexpression.op())
        val l = reexpression.lhs()
        assertIs<AtomicExpression>(l)
        assertEquals(NameToken("x"), l.atom())
        val r = reexpression.rhs()
        assertIs<BinaryExpression>(r)
        assertEquals(OperatorToken("*"), r.op())
        val rl = r.lhs()
        assertIs<AtomicExpression>(rl)
        assertEquals(NameToken("y"), rl.atom())
        val rr = r.rhs()
        assertIs<AtomicExpression>(rr)
        assertEquals(NameToken("z"), rr.atom())
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