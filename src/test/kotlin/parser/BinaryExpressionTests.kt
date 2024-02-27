package parser

import kotlin.test.*

class BinaryExpressionTests {
    fun testParse(lhsText: String, opText: String, rhsText: String) {
        val text = "$lhsText $opText $rhsText"
        testParse(text, ::parseExpression) {
            assertIs<BinaryExpression>(it)
            val lhs = it.lhs()
            assertIs<AtomicExpression>(lhs)
            assertEquals(lhsText, lhs.atom().value)

            val op = it.op()
            assertEquals(opText, op.value)

            val rhs = it.rhs()
            assertIs<AtomicExpression>(rhs)
            assertEquals(rhsText, rhs.atom().value)
        }
    }

    @Test
    fun testParse() {
        testParse("a", "*", "b")
        testParse("1", "+", "b")
        testParse("a", "/", "1")
        testParse("'a'", "%", "b")
        testParse("a", "==", "\"b\"")
        testParse("'a'", "^%=", "\"b\"")
    }

    fun testParseNoRhs(lhsText: String, opText: String) {
        val text = "$lhsText $opText"
        testParse(text, ::parseExpression) {
            assertIs<BinaryExpression>(it)
            val lhs = it.lhs()
            assertIs<AtomicExpression>(lhs)
            assertEquals(lhsText, lhs.atom().value)

            val op = it.op()
            assertEquals(opText, op.value)

            assertFails { it.rhs() }
        }
    }

    @Test
    fun testParseNoRhs() {
        testParseNoRhs("a", "*")
        testParseNoRhs("1", "+")
        testParseNoRhs("a", "/")
        testParseNoRhs("'a'", "%")
        testParseNoRhs("\"a\"", "==")
        testParseNoRhs("'a'", "^%=")
    }

    fun testParseLeftLowerPrecedence(lhsText: String, lopText: String, mhsText: String, ropText: String, rhsText: String) {
        val text = "$lhsText $lopText $mhsText $ropText $rhsText"
        testParse(text, ::parseExpression) {
            assertIs<BinaryExpression>(it)
            val lhs = it.lhs()
            assertIs<AtomicExpression>(lhs)
            assertEquals(lhsText, lhs.atom().value)

            val lop = it.op()
            assertEquals(lopText, lop.value)

            val higherPrecedence = it.rhs()
            assertIs<BinaryExpression>(higherPrecedence)

            val rop = higherPrecedence.op()
            assertEquals(ropText, rop.value)

            val mhs = higherPrecedence.lhs()
            assertIs<AtomicExpression>(mhs)
            assertEquals(mhsText, mhs.atom().value)

            val rhs = higherPrecedence.rhs()
            assertIs<AtomicExpression>(rhs)
            assertEquals(rhsText, rhs.atom().value)
        }
    }

    @Test
    fun testParseLeftLowerPrecedence() {
        testParseLeftLowerPrecedence("a", "|", "b", "^", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "&", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "==", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "!", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "<", "c")
        testParseLeftLowerPrecedence("a", "|", "b", ">", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "+", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "-", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "~", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "|", "b", "%", "c")

        testParseLeftLowerPrecedence("a", "^", "b", "&", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "==", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "!", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "<", "c")
        testParseLeftLowerPrecedence("a", "^", "b", ">", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "+", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "-", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "~", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "^", "b", "%", "c")

        testParseLeftLowerPrecedence("a", "&", "b", "==", "c")
        testParseLeftLowerPrecedence("a", "&", "b", "!", "c")
        testParseLeftLowerPrecedence("a", "&", "b", "<", "c")
        testParseLeftLowerPrecedence("a", "&", "b", ">", "c")
        testParseLeftLowerPrecedence("a", "&", "b", "+", "c")
        testParseLeftLowerPrecedence("a", "&", "b", "-", "c")
        testParseLeftLowerPrecedence("a", "&", "b", "~", "c")
        testParseLeftLowerPrecedence("a", "&", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "&", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "&", "b", "%", "c")

        testParseLeftLowerPrecedence("a", "==", "b", "<", "c")
        testParseLeftLowerPrecedence("a", "==", "b", ">", "c")
        testParseLeftLowerPrecedence("a", "==", "b", "+", "c")
        testParseLeftLowerPrecedence("a", "==", "b", "-", "c")
        testParseLeftLowerPrecedence("a", "==", "b", "~", "c")
        testParseLeftLowerPrecedence("a", "==", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "==", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "==", "b", "%", "c")

        testParseLeftLowerPrecedence("a", "!", "b", "<", "c")
        testParseLeftLowerPrecedence("a", "!", "b", ">", "c")
        testParseLeftLowerPrecedence("a", "!", "b", "+", "c")
        testParseLeftLowerPrecedence("a", "!", "b", "-", "c")
        testParseLeftLowerPrecedence("a", "!", "b", "~", "c")
        testParseLeftLowerPrecedence("a", "!", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "!", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "!", "b", "%", "c")

        testParseLeftLowerPrecedence("a", "<", "b", "+", "c")
        testParseLeftLowerPrecedence("a", "<", "b", "-", "c")
        testParseLeftLowerPrecedence("a", "<", "b", "~", "c")
        testParseLeftLowerPrecedence("a", "<", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "<", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "<", "b", "%", "c")

        testParseLeftLowerPrecedence("a", ">", "b", "+", "c")
        testParseLeftLowerPrecedence("a", ">", "b", "-", "c")
        testParseLeftLowerPrecedence("a", ">", "b", "~", "c")
        testParseLeftLowerPrecedence("a", ">", "b", "*", "c")
        testParseLeftLowerPrecedence("a", ">", "b", "/", "c")
        testParseLeftLowerPrecedence("a", ">", "b", "%", "c")

        testParseLeftLowerPrecedence("a", "+", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "+", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "+", "b", "%", "c")

        testParseLeftLowerPrecedence("a", "-", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "-", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "-", "b", "%", "c")

        testParseLeftLowerPrecedence("a", "~", "b", "*", "c")
        testParseLeftLowerPrecedence("a", "~", "b", "/", "c")
        testParseLeftLowerPrecedence("a", "~", "b", "%", "c")
    }

    fun testParseRightLowerPrecedence(lhsText: String, lopText: String, mhsText: String, ropText: String, rhsText: String) {
        val text = "$lhsText $lopText $mhsText $ropText $rhsText"
        testParse(text, ::parseExpression) {
            assertIs<BinaryExpression>(it)
            val higherPrecedence = it.lhs()
            assertIs<BinaryExpression>(higherPrecedence)

            val lop = higherPrecedence.op()
            assertEquals(lopText, lop.value)

            val lhs = higherPrecedence.lhs()
            assertIs<AtomicExpression>(lhs)
            assertEquals(lhsText, lhs.atom().value)

            val mhs = higherPrecedence.rhs()
            assertIs<AtomicExpression>(mhs)
            assertEquals(mhsText, mhs.atom().value)

            val rop = it.op()
            assertEquals(ropText, rop.value)

            val rhs = it.rhs()
            assertIs<AtomicExpression>(rhs)
            assertEquals(rhsText, rhs.atom().value)
        }
    }

    @Test
    fun testParseRightLowerPrecedence() {
        testParseRightLowerPrecedence("a", "^", "b", "|", "c")
        testParseRightLowerPrecedence("a", "&", "b", "|", "c")
        testParseRightLowerPrecedence("a", "==", "b", "|", "c")
        testParseRightLowerPrecedence("a", "!", "b", "|", "c")
        testParseRightLowerPrecedence("a", "<", "b", "|", "c")
        testParseRightLowerPrecedence("a", ">", "b", "|", "c")
        testParseRightLowerPrecedence("a", "+", "b", "|", "c")
        testParseRightLowerPrecedence("a", "-", "b", "|", "c")
        testParseRightLowerPrecedence("a", "~", "b", "|", "c")
        testParseRightLowerPrecedence("a", "*", "b", "|", "c")
        testParseRightLowerPrecedence("a", "/", "b", "|", "c")
        testParseRightLowerPrecedence("a", "%", "b", "|", "c")

        testParseRightLowerPrecedence("a", "&", "b", "^", "c")
        testParseRightLowerPrecedence("a", "==", "b", "^", "c")
        testParseRightLowerPrecedence("a", "!", "b", "^", "c")
        testParseRightLowerPrecedence("a", "<", "b", "^", "c")
        testParseRightLowerPrecedence("a", ">", "b", "^", "c")
        testParseRightLowerPrecedence("a", "+", "b", "^", "c")
        testParseRightLowerPrecedence("a", "-", "b", "^", "c")
        testParseRightLowerPrecedence("a", "~", "b", "^", "c")
        testParseRightLowerPrecedence("a", "*", "b", "^", "c")
        testParseRightLowerPrecedence("a", "/", "b", "^", "c")
        testParseRightLowerPrecedence("a", "%", "b", "^", "c")

        testParseRightLowerPrecedence("a", "==", "b", "&", "c")
        testParseRightLowerPrecedence("a", "!", "b", "&", "c")
        testParseRightLowerPrecedence("a", "<", "b", "&", "c")
        testParseRightLowerPrecedence("a", ">", "b", "&", "c")
        testParseRightLowerPrecedence("a", "+", "b", "&", "c")
        testParseRightLowerPrecedence("a", "-", "b", "&", "c")
        testParseRightLowerPrecedence("a", "~", "b", "&", "c")
        testParseRightLowerPrecedence("a", "*", "b", "&", "c")
        testParseRightLowerPrecedence("a", "/", "b", "&", "c")
        testParseRightLowerPrecedence("a", "%", "b", "&", "c")

        testParseRightLowerPrecedence("a", "<", "b", "==", "c")
        testParseRightLowerPrecedence("a", ">", "b", "==", "c")
        testParseRightLowerPrecedence("a", "+", "b", "==", "c")
        testParseRightLowerPrecedence("a", "-", "b", "==", "c")
        testParseRightLowerPrecedence("a", "~", "b", "==", "c")
        testParseRightLowerPrecedence("a", "*", "b", "==", "c")
        testParseRightLowerPrecedence("a", "/", "b", "==", "c")
        testParseRightLowerPrecedence("a", "%", "b", "==", "c")

        testParseRightLowerPrecedence("a", "<", "b", "!", "c")
        testParseRightLowerPrecedence("a", ">", "b", "!", "c")
        testParseRightLowerPrecedence("a", "+", "b", "!", "c")
        testParseRightLowerPrecedence("a", "-", "b", "!", "c")
        testParseRightLowerPrecedence("a", "~", "b", "!", "c")
        testParseRightLowerPrecedence("a", "*", "b", "!", "c")
        testParseRightLowerPrecedence("a", "/", "b", "!", "c")
        testParseRightLowerPrecedence("a", "%", "b", "!", "c")

        testParseRightLowerPrecedence("a", "+", "b", "<", "c")
        testParseRightLowerPrecedence("a", "-", "b", "<", "c")
        testParseRightLowerPrecedence("a", "~", "b", "<", "c")
        testParseRightLowerPrecedence("a", "*", "b", "<", "c")
        testParseRightLowerPrecedence("a", "/", "b", "<", "c")
        testParseRightLowerPrecedence("a", "%", "b", "<", "c")

        testParseRightLowerPrecedence("a", "+", "b", ">", "c")
        testParseRightLowerPrecedence("a", "-", "b", ">", "c")
        testParseRightLowerPrecedence("a", "~", "b", ">", "c")
        testParseRightLowerPrecedence("a", "*", "b", ">", "c")
        testParseRightLowerPrecedence("a", "/", "b", ">", "c")
        testParseRightLowerPrecedence("a", "%", "b", ">", "c")

        testParseRightLowerPrecedence("a", "*", "b", "+", "c")
        testParseRightLowerPrecedence("a", "/", "b", "+", "c")
        testParseRightLowerPrecedence("a", "%", "b", "+", "c")

        testParseRightLowerPrecedence("a", "*", "b", "-", "c")
        testParseRightLowerPrecedence("a", "/", "b", "-", "c")
        testParseRightLowerPrecedence("a", "%", "b", "-", "c")

        testParseRightLowerPrecedence("a", "*", "b", "~", "c")
        testParseRightLowerPrecedence("a", "/", "b", "~", "c")
        testParseRightLowerPrecedence("a", "%", "b", "~", "c")
    }

    fun testParseEqualPrecedence(lhsText: String, lopText: String, mhsText: String, ropText: String, rhsText: String) {
        val text = "$lhsText $lopText $mhsText $ropText $rhsText"
        testParse(text, ::parseExpression) {
            assertIs<BinaryExpression>(it)
            val firstExpression = it.lhs()
            assertIs<BinaryExpression>(firstExpression)

            val lop = firstExpression.op()
            assertEquals(lopText, lop.value)

            val lhs = firstExpression.lhs()
            assertIs<AtomicExpression>(lhs)
            assertEquals(lhsText, lhs.atom().value)

            val mhs = firstExpression.rhs()
            assertIs<AtomicExpression>(mhs)
            assertEquals(mhsText, mhs.atom().value)

            val rop = it.op()
            assertEquals(ropText, rop.value)

            val rhs = it.rhs()
            assertIs<AtomicExpression>(rhs)
            assertEquals(rhsText, rhs.atom().value)
        }
    }

    @Test
    fun testParseEqualPrecedence() {
        testParseEqualPrecedence("a", "|", "b", "|", "c")

        testParseEqualPrecedence("a", "^", "b", "^", "c")

        testParseEqualPrecedence("a", "&", "b", "&", "c")

        testParseEqualPrecedence("a", "==", "b", "==", "c")
        testParseEqualPrecedence("a", "!", "b", "==", "c")
        testParseEqualPrecedence("a", "==", "b", "!", "c")
        testParseEqualPrecedence("a", "!", "b", "!", "c")

        testParseEqualPrecedence("a", ">", "b", ">", "c")
        testParseEqualPrecedence("a", "<", "b", ">", "c")
        testParseEqualPrecedence("a", ">", "b", "<", "c")
        testParseEqualPrecedence("a", "<", "b", "<", "c")

        testParseEqualPrecedence("a", "+", "b", "+", "c")
        testParseEqualPrecedence("a", "-", "b", "+", "c")
        testParseEqualPrecedence("a", "~", "b", "+", "c")
        testParseEqualPrecedence("a", "+", "b", "-", "c")
        testParseEqualPrecedence("a", "-", "b", "-", "c")
        testParseEqualPrecedence("a", "~", "b", "-", "c")
        testParseEqualPrecedence("a", "+", "b", "~", "c")
        testParseEqualPrecedence("a", "-", "b", "~", "c")
        testParseEqualPrecedence("a", "~", "b", "~", "c")

        testParseEqualPrecedence("a", "*", "b", "*", "c")
        testParseEqualPrecedence("a", "/", "b", "*", "c")
        testParseEqualPrecedence("a", "%", "b", "*", "c")
        testParseEqualPrecedence("a", "*", "b", "/", "c")
        testParseEqualPrecedence("a", "/", "b", "/", "c")
        testParseEqualPrecedence("a", "%", "b", "/", "c")
        testParseEqualPrecedence("a", "*", "b", "%", "c")
        testParseEqualPrecedence("a", "/", "b", "%", "c")
        testParseEqualPrecedence("a", "%", "b", "%", "c")
    }

    @Test
    fun testAppendToEnd() {
        testAppendToEnd("a", " + b")
        testAppendToEnd("a +", " b")
        testAppendToEnd("a + b +", " c")
        testAppendToEnd("a + b + c +", " d")

        testAppendToEnd("a + b", " + c")
        testAppendToEnd("a + b", " * c")
        testAppendToEnd("a + b", " | c")
        testAppendToEnd("a * b", " + c")
        testAppendToEnd("a * b", " * c")
        testAppendToEnd("a * b", " | c")
        testAppendToEnd("a | b", " + c")
        testAppendToEnd("a | b", " * c")
        testAppendToEnd("a | b", " | c")

        testAppendToEnd("a + b + c", " + d")
        testAppendToEnd("a + b + c", " * d")
        testAppendToEnd("a + b + c", " | d")
        testAppendToEnd("a * b + c", " + d")
        testAppendToEnd("a * b + c", " * d")
        testAppendToEnd("a * b + c", " | d")
        testAppendToEnd("a | b + c", " + d")
        testAppendToEnd("a | b + c", " * d")
        testAppendToEnd("a | b + c", " | d")
        testAppendToEnd("a + b * c", " + d")
        testAppendToEnd("a + b * c", " * d")
        testAppendToEnd("a + b * c", " | d")
        testAppendToEnd("a * b * c", " + d")
        testAppendToEnd("a * b * c", " * d")
        testAppendToEnd("a * b * c", " | d")
        testAppendToEnd("a | b * c", " + d")
        testAppendToEnd("a | b * c", " * d")
        testAppendToEnd("a | b * c", " | d")
        testAppendToEnd("a + b | c", " + d")
        testAppendToEnd("a + b | c", " * d")
        testAppendToEnd("a + b | c", " | d")
        testAppendToEnd("a * b | c", " + d")
        testAppendToEnd("a * b | c", " * d")
        testAppendToEnd("a * b | c", " | d")
        testAppendToEnd("a | b | c", " + d")
        testAppendToEnd("a | b | c", " * d")
        testAppendToEnd("a | b | c", " | d")
    }

    @Test
    fun testAppendToBeginning() {
        testAppendToBeginning("b", "a + ")

        testAppendToBeginning("b + c", "a + ")
        testAppendToBeginning("b * c", "a + ")
        testAppendToBeginning("b | c", "a + ")
        testAppendToBeginning("b + c", "a * ")
        testAppendToBeginning("b * c", "a * ")
        testAppendToBeginning("b | c", "a * ")
        testAppendToBeginning("b + c", "a | ")
        testAppendToBeginning("b * c", "a | ")
        testAppendToBeginning("b | c", "a | ")

        testAppendToBeginning("b + c + d", "a + ")
        testAppendToBeginning("b + c + d", "a * ")
        testAppendToBeginning("b + c + d", "a | ")
        testAppendToBeginning("b * c + d", "a + ")
        testAppendToBeginning("b * c + d", "a * ")
        testAppendToBeginning("b * c + d", "a | ")
        testAppendToBeginning("b | c + d", "a + ")
        testAppendToBeginning("b | c + d", "a * ")
        testAppendToBeginning("b | c + d", "a | ")
        testAppendToBeginning("b + c * d", "a + ")
        testAppendToBeginning("b + c * d", "a * ")
        testAppendToBeginning("b + c * d", "a | ")
        testAppendToBeginning("b * c * d", "a + ")
        testAppendToBeginning("b * c * d", "a * ")
        testAppendToBeginning("b * c * d", "a | ")
        testAppendToBeginning("b | c * d", "a + ")
        testAppendToBeginning("b | c * d", "a * ")
        testAppendToBeginning("b | c * d", "a | ")
        testAppendToBeginning("b + c | d", "a + ")
        testAppendToBeginning("b + c | d", "a * ")
        testAppendToBeginning("b + c | d", "a | ")
        testAppendToBeginning("b * c | d", "a + ")
        testAppendToBeginning("b * c | d", "a * ")
        testAppendToBeginning("b * c | d", "a | ")
        testAppendToBeginning("b | c | d", "a + ")
        testAppendToBeginning("b | c | d", "a * ")
        testAppendToBeginning("b | c | d", "a | ")
    }

    @Test
    fun testReplaceOperator() {
        testReplaceMiddle("a ", "+", " b", "-")
        testReplaceMiddle("a ", "+", " b", "*")
        testReplaceMiddle("a ", "+", " b", ">")
        testReplaceMiddle("a ", "/", " b", "-")
        testReplaceMiddle("a ", "/", " b", "*")
        testReplaceMiddle("a ", "/", " b", ">")
        testReplaceMiddle("a ", "<", " b", "-")
        testReplaceMiddle("a ", "<", " b", "*")
        testReplaceMiddle("a ", "<", " b", ">")

        testReplaceMiddle("a + b ", "+", " c", "-")
        testReplaceMiddle("a + b ", "+", " c", "*")
        testReplaceMiddle("a + b ", "+", " c", ">")
        testReplaceMiddle("a + b ", "/", " c", "-")
        testReplaceMiddle("a + b ", "/", " c", "*")
        testReplaceMiddle("a + b ", "/", " c", ">")
        testReplaceMiddle("a + b ", "<", " c", "-")
        testReplaceMiddle("a + b ", "<", " c", "*")
        testReplaceMiddle("a + b ", "<", " c", ">")
        testReplaceMiddle("a / b ", "+", " c", "-")
        testReplaceMiddle("a / b ", "+", " c", "*")
        testReplaceMiddle("a / b ", "+", " c", ">")
        testReplaceMiddle("a / b ", "/", " c", "-")
        testReplaceMiddle("a / b ", "/", " c", "*")
        testReplaceMiddle("a / b ", "/", " c", ">")
        testReplaceMiddle("a / b ", "<", " c", "-")
        testReplaceMiddle("a / b ", "<", " c", "*")
        testReplaceMiddle("a / b ", "<", " c", ">")
        testReplaceMiddle("a < b ", "+", " c", "-")
        testReplaceMiddle("a < b ", "+", " c", "*")
        testReplaceMiddle("a < b ", "+", " c", ">")
        testReplaceMiddle("a < b ", "/", " c", "-")
        testReplaceMiddle("a < b ", "/", " c", "*")
        testReplaceMiddle("a < b ", "/", " c", ">")
        testReplaceMiddle("a < b ", "<", " c", "-")
        testReplaceMiddle("a < b ", "<", " c", "*")
        testReplaceMiddle("a < b ", "<", " c", ">")

        testReplaceMiddle("a ", "+", " b + c", "-")
        testReplaceMiddle("a ", "+", " b + c", "*")
        testReplaceMiddle("a ", "+", " b + c", ">")
        testReplaceMiddle("a ", "/", " b + c", "-")
        testReplaceMiddle("a ", "/", " b + c", "*")
        testReplaceMiddle("a ", "/", " b + c", ">")
        testReplaceMiddle("a ", "<", " b + c", "-")
        testReplaceMiddle("a ", "<", " b + c", "*")
        testReplaceMiddle("a ", "<", " b + c", ">")
        testReplaceMiddle("a ", "+", " b / c", "-")
        testReplaceMiddle("a ", "+", " b / c", "*")
        testReplaceMiddle("a ", "+", " b / c", ">")
        testReplaceMiddle("a ", "/", " b / c", "-")
        testReplaceMiddle("a ", "/", " b / c", "*")
        testReplaceMiddle("a ", "/", " b / c", ">")
        testReplaceMiddle("a ", "<", " b / c", "-")
        testReplaceMiddle("a ", "<", " b / c", "*")
        testReplaceMiddle("a ", "<", " b / c", ">")
        testReplaceMiddle("a ", "+", " b < c", "-")
        testReplaceMiddle("a ", "+", " b < c", "*")
        testReplaceMiddle("a ", "+", " b < c", ">")
        testReplaceMiddle("a ", "/", " b < c", "-")
        testReplaceMiddle("a ", "/", " b < c", "*")
        testReplaceMiddle("a ", "/", " b < c", ">")
        testReplaceMiddle("a ", "<", " b < c", "-")
        testReplaceMiddle("a ", "<", " b < c", "*")
        testReplaceMiddle("a ", "<", " b < c", ">")
    }
}