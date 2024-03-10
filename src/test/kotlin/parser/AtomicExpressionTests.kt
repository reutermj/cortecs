package parser

import kotlin.test.*

class AtomicExpressionTests {
    private inline fun <reified T: AtomicExpressionToken> testParseAtomType(text: String) {
        testParse(text, ::parseExpression) {
            assertIs<AtomicExpression>(it)
            assertIs<T>(it.atom())
        }
    }

    @Test
    fun testParseAtomType() {
        testParseAtomType<NameToken>("a")
        testParseAtomType<StringToken>("\"hello world\"")
        testParseAtomType<BadStringToken>("\"hello world")
        testParseAtomType<CharToken>("'a'")
        testParseAtomType<BadCharToken>("'a")
        testParseAtomType<IntToken>("1")
        testParseAtomType<FloatToken>("1.1")
        testParseAtomType<FloatToken>(".1")
        testParseAtomType<FloatToken>("1.")
    }

    @Test
    fun testParseWhitespaceAfterAtom() {
        for(whitespace in whitespaceCombos) {
            testParse("a$whitespace", ::parseExpression) {}
            testParse("'a'$whitespace", ::parseExpression) {}
            testParse("1$whitespace", ::parseExpression) {}
        }
    }

    fun testReplaceFullAtom(inText: String, outText: String) {
        val start = Span.zero
        val end = Span(0, inText.length)
        val change = Change(outText, start, end)
        testReparse(inText, change) {parseExpression(it)!!}
    }

    @Test
    fun testReplaceFullAtom() {
        testReplaceFullAtom("a", "b")
        testReplaceFullAtom("a", "1.1")
        testReplaceFullAtom("'a'", "b")
        testReplaceFullAtom("\"abc\"", "b")
    }

    @Test
    fun testAppendToBeginning() {
        testAppendToBeginning("b", "a")
        testAppendToBeginning("a", "'")
        testAppendToBeginning("a", "\"")
        testAppendToBeginning(".1", "1")
    }

    @Test
    fun testAppendToEnd() {
        testAppendToEnd("a", "b") {parseExpression(it)!!}
        testAppendToEnd("'a", "'") {parseExpression(it)!!}
        testAppendToEnd("\"a", "\"") {parseExpression(it)!!}
        testAppendToEnd("1", ".1") {parseExpression(it)!!}
        testAppendToEnd("1", ".") {parseExpression(it)!!}
    }

    fun testNullParse(inString: String) {
        val iterator = ParserIterator()
        iterator.add(inString)
        val expression = parseExpression(iterator)
        assertNull(expression)
    }

    @Test
    fun testNullParse() {
        testNullParse("")
        testNullParse("Abc")
        testNullParse("let")
        testNullParse("if")
        testNullParse("function")
        testNullParse("return")
        testNullParse("   ")
        testNullParse("\n")
        testNullParse(")")
        testNullParse("{")
        testNullParse("}")
        testNullParse(",")
        testNullParse(".")
        testNullParse(":")
        testNullParse("=")
    }
}