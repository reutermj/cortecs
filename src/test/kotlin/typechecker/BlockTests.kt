package typechecker

import parser.*
import kotlin.test.*

class BlockTests {
    @Test
    fun test001() {
        val s = """let x = id
                         |let y = x(1)""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val xScheme = environment.bindings[NameToken("x")]!!
        assertEquals(1, xScheme.boundVariables.size)
        val xVar = xScheme.boundVariables.first()
        assertIs<UnificationTypeVariable>(xVar)
        assertEquals(xVar, xScheme.type)

        val yScheme = environment.bindings[NameToken("y")]!!
        assertEquals(1, yScheme.boundVariables.size)
        val yVar = yScheme.boundVariables.first()
        assertIs<UnificationTypeVariable>(yVar)
        assertEquals(yScheme.boundVariables.first(), yScheme.type)

        val idReq = environment.requirements[NameToken("id")]!!
        assertEquals(1, idReq.size)
        assertEquals(xVar, idReq.first())

        val xCompat = environment.compatibilities[xVar]!!
        assertEquals(1, xCompat.size)
        assertEquals(ArrowType(I32Type, yVar), xCompat.first())

        val yCompat = environment.compatibilities[yVar]!!
        assertEquals(0, yCompat.size)
    }
}