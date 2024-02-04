package parser_v2

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.test.*
import kotlinx.serialization.modules.*

class AstSerializationTests {
    @Test
    fun test001() {
        val s = "let x = y + z"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        println(astJsonFormat.encodeToString(let))
    }
}