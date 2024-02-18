package typechecker

import parser.*
import kotlin.test.*

class BlockTests {
    @Test
    fun test001() {
        val s = """let x = 1
                         |let y: U32 = x""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment
        println()
    }
}