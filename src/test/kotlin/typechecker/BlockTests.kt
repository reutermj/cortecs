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
        val xType = xCompat.first()
        assertIs<ArrowType>(xType)
        assertIs<I32Type>(xType.lhs)
        assertEquals(yVar, xType.rhs)

        val yCompat = environment.compatibilities[yVar]!!
        assertEquals(0, yCompat.size)
    }

    @Test
    fun test002() {
        val s = """let x = id
                         |let y = x(1)
                         |let z = x(1.1)""".trimMargin()
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

        val zScheme = environment.bindings[NameToken("z")]!!
        assertEquals(1, zScheme.boundVariables.size)
        val zVar = zScheme.boundVariables.first()
        assertIs<UnificationTypeVariable>(zVar)
        assertEquals(zScheme.boundVariables.first(), zScheme.type)

        val idReq = environment.requirements[NameToken("id")]!!
        assertEquals(1, idReq.size)
        assertEquals(xVar, idReq.first())

        val xCompat = environment.compatibilities[xVar]!!
        assertEquals(2, xCompat.size)
        for (type in xCompat) {
            assertIs<ArrowType>(type)
            when (type.lhs) {
                is I32Type -> assertEquals(yVar, type.rhs)
                is F32Type -> assertEquals(zVar, type.rhs)
                else -> assert(false)
            }
        }

        val yCompat = environment.compatibilities[yVar]!!
        assertEquals(0, yCompat.size)

        val zCompat = environment.compatibilities[zVar]!!
        assertEquals(0, zCompat.size)
    }

    @Test
    fun test003() {
        val s = """let x = 1
                         |let y: F32 = x""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment
    }
}