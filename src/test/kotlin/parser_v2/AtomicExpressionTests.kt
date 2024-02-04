package parser_v2

import kotlinx.serialization.encodeToString
import kotlin.test.*

class AtomicExpressionTests {
    private inline fun <reified T: AtomicExpressionToken>tryParsing(text: String, whitespaceCombos: List<String>) {
        for (i in whitespaceCombos) {
            val s = "$text$i"
            val iterator = ParserIterator()
            iterator.add(s)
            val expression = parseExpression(iterator)
            assertIs<AtomicExpression>(expression)
            assertIs<T>(expression.atom())
            assertFails { iterator.nextToken() }

            val builder = StringBuilder()
            expression.atom().stringify(builder)
            assertEquals(text, builder.toString())
            builder.clear()

            expression.stringify(builder)
            assertEquals(s, builder.toString())
        }
    }

    @Test
    fun testParsing() {
        tryParsing<NameToken>("a", whitespaceCombos)
        tryParsing<StringToken>("\"hello world\"", whitespaceCombos)
        tryParsing<BadStringToken>("\"hello world", whitespaceCombosStartingWithNewLine)
        tryParsing<CharToken>("'a'", whitespaceCombos)
        //todo maybe change the tokenizer to stop tokenizing chars on a space after the first char
        tryParsing<BadCharToken>("'a", whitespaceCombosStartingWithNewLine)
        tryParsing<IntToken>("1", whitespaceCombos)
        tryParsing<FloatToken>("1.1", whitespaceCombos)
    }

    private fun trySerializing(text: String, whitespaceCombos: List<String>) {
        for (i in whitespaceCombos) {
            val s = "$text$i"
            val iterator = ParserIterator()
            iterator.add(s)
            val expression = parseExpression(iterator)
            val serialized = astJsonFormat.encodeToString(expression)
            val deserialized = astJsonFormat.decodeFromString<Expression>(serialized)
            assertEquals(expression, deserialized)
        }
    }

    @Test
    fun testSerializing() {
        trySerializing("a", whitespaceCombos)
        trySerializing("\"hello world\"", whitespaceCombos)
        trySerializing("\"hello world", whitespaceCombosStartingWithNewLine)
        trySerializing("'a'", whitespaceCombos)
        //todo maybe change the tokenizer to stop tokenizing chars on a space after the first char
        trySerializing("'a", whitespaceCombosStartingWithNewLine)
        trySerializing("1", whitespaceCombos)
        trySerializing("1.1", whitespaceCombos)
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