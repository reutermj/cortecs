package typechecker

import parser.*
import kotlin.test.*

class BodyTests {
    fun getBodyEnvironment(s: String): BlockEnvironment {
        val iterator = ParserIterator()
        iterator.add(s)
        val body = parseBody(iterator)
        assertIs<StarKeepBuilding<BodyAst>>(body)
        return body.node.environment
    }

    @Test
    fun testLet001() {
        val environment = getBodyEnvironment("let x = y")
        assertEquals(1, environment.subordinates.size)
        val subordinate = environment.subordinates.first()
        assertIs<ExpressionEnvironment>(subordinate)
        val expressionType = subordinate.type
        assertIs<UnificationTypeVariable>(expressionType)
        assertEquals(TypeScheme(listOf(expressionType), expressionType), environment.bindings[NameToken("x")])
        val yRequirements = environment.requirements[NameToken("y")]!!
        assertEquals(1, yRequirements.size)
        assertEquals(expressionType, yRequirements.first())
    }

    @Test
    fun testLet002() {
        val environment = getBodyEnvironment("let x = 1")
        assertEquals(1, environment.subordinates.size)
        val subordinate = environment.subordinates.first()
        assertIs<ExpressionEnvironment>(subordinate)
        val expressionType = subordinate.type
        assertIs<I32Type>(expressionType)
        assertEquals(TypeScheme(emptyList(), expressionType), environment.bindings[NameToken("x")])
        assertEquals(Requirements.empty, environment.requirements)
    }
}