package parser_v2

import kotlinx.serialization.json.Json
import kotlin.test.*

class AstSerializationTests {
    @Test
    fun test001() {
        val s = "let x = y"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
    }
}