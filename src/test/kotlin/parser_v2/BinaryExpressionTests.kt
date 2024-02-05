package parser_v2

import kotlin.test.*

class BinaryExpressionTests {
    val opTokensByPrecedence = listOf(
        listOf("|"),
        listOf("^"),
        listOf("&"),
        listOf("==", "!"),
        listOf(">", "<"),
        listOf("+", "-", "~"),
        listOf("*", "/", "%"),
    )

    @Test
    fun testParsingSameOperator() {
        for(ops in opTokensByPrecedence)
            for(op in ops)
                for(w in whitespaceCombos) {
                    val s1 = "a$w$op${w}b$w"
                    tryParse(s1, ::parseExpression) {
                        assertIs<BinaryExpression>(it)
                        assertEquals(op, it.op().value)
                        val lhs = it.lhs()
                        assertIs<AtomicExpression>(lhs)
                        assertEquals(NameToken("a"), lhs.atom())
                        val rhs = it.rhs()
                        assertIs<AtomicExpression>(rhs)
                        assertEquals(NameToken("b"), rhs.atom())
                    }

                    val s2 = "a$w$op${w}b$w$op${w}c$w"
                    tryParse(s2, ::parseExpression) {
                        assertIs<BinaryExpression>(it)
                        assertEquals(op, it.op().value)
                        val l = it.lhs()
                        assertIs<BinaryExpression>(l)
                        val ll = l.lhs()
                        assertIs<AtomicExpression>(ll)
                        assertEquals(NameToken("a"), ll.atom())
                        val lr = l.rhs()
                        assertIs<AtomicExpression>(lr)
                        assertEquals(NameToken("b"), lr.atom())
                        val r = it.rhs()
                        assertIs<AtomicExpression>(r)
                        assertEquals(NameToken("c"), r.atom())
                    }

                    val s3 = "a$w$op${w}b$w$op${w}c$w$op${w}d$w"
                    tryParse(s3, ::parseExpression) {
                        assertIs<BinaryExpression>(it)
                        assertEquals(op, it.op().value)
                        val l = it.lhs()
                        assertIs<BinaryExpression>(l)
                        val ll = l.lhs()
                        assertIs<BinaryExpression>(ll)
                        val lll = ll.lhs()
                        assertIs<AtomicExpression>(lll)
                        assertEquals(NameToken("a"), lll.atom())
                        val llr = ll.rhs()
                        assertIs<AtomicExpression>(llr)
                        assertEquals(NameToken("b"), llr.atom())
                        val lr = l.rhs()
                        assertIs<AtomicExpression>(lr)
                        assertEquals(NameToken("c"), lr.atom())
                        val r = it.rhs()
                        assertIs<AtomicExpression>(r)
                        assertEquals(NameToken("d"), r.atom())
                    }
                }

    }
}