package typechecker

import parser.*
import kotlin.test.*

class ExpressionTests {
    fun getExpressionEnvironment(s: String): ExpressionEnvironment {
        val iterator = ParserIterator()
        iterator.add(s)
        val expression = parseExpression(iterator)!!
        return expression.environment
    }

    @Test
    fun testName() {
        val s = "a"
        val environment = getExpressionEnvironment(s)
        assertEquals(environment.type, environment.requirements[NameToken("a")]?.first())
        assertTrue { environment.subordinates.isEmpty() }
    }

    inline fun <reified T: Type>testConstant(s: String) {
        val environment = getExpressionEnvironment(s)
        assertIs<T>(environment.type)
        assertTrue { environment.subordinates.isEmpty() }
        assertEquals(Requirements.empty, environment.requirements)
    }

    @Test
    fun testConstant() {
        testConstant<I8Type>("1b")
        testConstant<I16Type>("1s")
        testConstant<I32Type>("1")
        testConstant<I64Type>("1l")
        testConstant<U8Type>("1ub")
        testConstant<U16Type>("1us")
        testConstant<U32Type>("1u")
        testConstant<U64Type>("1ul")
        testConstant<F32Type>("1.1")
        testConstant<F64Type>("1.1d")
        testConstant<StringType>("\"hello world\"")
        testConstant<CharacterType>("'a'")
    }

    val atomics = listOf("a", "1b", "1s", "1", "1l", "1ub", "1us", "1u", "1ul", "1.1", "1.1d", "\"hello world\"", "'a'")

    fun testGrouping(s: String) {
        val environment = getExpressionEnvironment("($s)")
        assertEquals(1, environment.subordinates.size)
        val subordinate = environment.subordinates.first()
        assertEquals(environment.type, subordinate.type)
        assertEquals(environment.requirements, subordinate.requirements)
    }

    @Test
    fun testGrouping() {
        for(atom in atomics) {
            testGrouping(atom)
        }
        testGrouping("+a")
        testGrouping("a + b")
        testGrouping("f(a, b, c)")
    }

    fun testUnary(op: String, s: String) {
        val environment = getExpressionEnvironment("$op $s")
        assertEquals(1, environment.subordinates.size)
        val subordinate = environment.subordinates.first()
        val operator = environment.requirements[OperatorToken(op)]!!
        assertEquals(1, operator.size)
        assertEquals(ArrowType(subordinate.type, environment.type), operator.first())
    }

    @Test
    fun testUnary() {
        for(atom in atomics) {
            testUnary("+", atom)
            testUnary("+", "($atom)")
            testUnary("+", "f($atom, $atom, $atom)")
        }
    }

    fun testBinary(lhs: String, op: String, rhs: String) {
        val environment = getExpressionEnvironment("$lhs $op $rhs")
        assertEquals(2, environment.subordinates.size)
        val lSub = environment.subordinates[0]
        val rSub = environment.subordinates[1]
        val operator = environment.requirements[OperatorToken(op)]!!
        assertEquals(1, operator.size)
        assertEquals(ArrowType(ProductType(listOf(lSub.type, rSub.type)), environment.type), operator.first())
    }

    @Test
    fun testBinary() {
        for(lAtom in atomics) {
            for(rAtom in atomics) {
                testBinary(lAtom, "+", rAtom)
                testBinary("f($lAtom, $rAtom, $lAtom)", "+", rAtom)
                testBinary(lAtom, "+", "f($rAtom, $lAtom, $rAtom)")
                testBinary("f($lAtom, $rAtom, $lAtom)", "+", "f($rAtom, $lAtom, $rAtom)")
                testBinary("-$lAtom", "+", rAtom)
                testBinary(lAtom, "+", "-$rAtom")
                testBinary("-$lAtom", "+", "-$rAtom")
                testBinary("($lAtom)", "+", rAtom)
                testBinary(lAtom, "+", "($rAtom)")
                testBinary("($lAtom)", "+", "($rAtom)")
            }
        }
    }

    @Test
    fun test() {
        getExpressionEnvironment("f(x)(y)")
    }
}