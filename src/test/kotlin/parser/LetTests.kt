package parser

import kotlin.test.*

class LetTests {
    fun testParse(name: String, annotation: String?, expression: String, whitespace: String = "") {
        val text = if(annotation == null) "let $whitespace$name$whitespace=$whitespace $expression$whitespace"
        else "let $whitespace$name$whitespace:$whitespace$annotation$whitespace=$whitespace $expression$whitespace"

        testParse(text, ::parseLet) {
            assertIs<LetAst>(it)
            assertEquals("x", it.name()?.value)

            if(annotation == null) assertNull(it.typeAnnotation())
            else assertEquals(annotation, it.typeAnnotation()?.value)

            val expressionIterator = ParserIterator()
            expressionIterator.add("$expression$whitespace")
            val expressionAst = parseExpression(expressionIterator)!!
            assertEquals(expressionAst, it.expression())
        }
    }

    @Test
    fun testParse() {
        testParse("x", null, "y")
        testParse("x", null, "y + z")
        testParse("x", null, "+y")
        testParse("x", null, "(y)")
        testParse("x", null, "f(y)")
        testParse("x", null, "f(y, z)")

        testParse("x", "t", "y")
        testParse("x", "t", "y + z")
        testParse("x", "t", "+y")
        testParse("x", "t", "(y)")
        testParse("x", "t", "f(y)")
        testParse("x", "t", "f(y, z)")

        testParse("x", "U32", "y")
        testParse("x", "U32", "y + z")
        testParse("x", "U32", "+y")
        testParse("x", "U32", "(y)")
        testParse("x", "U32", "f(y)")
        testParse("x", "U32", "f(y, z)")

        for(whitespace in whitespaceCombos) {
            testParse("x", null, "y", whitespace)
            testParse("x", null, "y + z", whitespace)
            testParse("x", "t", "+y", whitespace)
            testParse("x", "t", "(y)", whitespace)
            testParse("x", "U32", "f(y)", whitespace)
            testParse("x", "U32", "f(y, z)", whitespace)
        }
    }

    @Test
    fun testParseMissingEverything() {
        testParse("let", ::parseLet) {
            assertIs<LetAst>(it)
            assertNull(it.name())
            assertNull(it.typeAnnotation())
            assertNull(it.expression())
        }
    }

    @Test
    fun testParseOnlyName() {
        testParse("let x", ::parseLet) {
            assertIs<LetAst>(it)
            assertEquals("x", it.name()?.value)
            assertNull(it.typeAnnotation())
            assertNull(it.expression())
        }
    }

    @Test
    fun testParseColonMissingAnnotation() {
        testParse("let x:", ::parseLet) {
            assertIs<LetAst>(it)
            assertEquals("x", it.name()?.value)
            assertNull(it.typeAnnotation())
            assertNull(it.expression())
        }
    }

    @Test
    fun testParseMissingExpression() {
        testParse("let x =", ::parseLet) {
            assertIs<LetAst>(it)
            assertEquals("x", it.name()?.value)
            assertNull(it.typeAnnotation())
            assertNull(it.expression())
        }
    }

    @Test
    fun testParseAnnotationMissingExpression() {
        testParse("let x:t =", ::parseLet) {
            assertIs<LetAst>(it)
            assertEquals("x", it.name()?.value)
            assertEquals("t", it.typeAnnotation()?.value)
            assertNull(it.expression())
        }
    }

    @Test
    fun testAppendToEnd() {
        testAppendToEnd("let", " x", ::parseLet)
        testAppendToEnd("let x", ":", ::parseLet)
        testAppendToEnd("let x", ":t", ::parseLet)
        testAppendToEnd("let x", "=", ::parseLet)
        testAppendToEnd("let x:t", "=", ::parseLet)
        testAppendToEnd("let x=", "x", ::parseLet)
        testAppendToEnd("let x:t=", "x", ::parseLet)
        testAppendToEnd("let x=x", "+y", ::parseLet)
        testAppendToEnd("let x:t=x", "+y", ::parseLet)
    }

    @Test
    fun testReplace() {
        testReplaceMiddle("let x", "", "=y", ":t", ::parseLet)
        testReplaceMiddle("let ", "x", "=z", "y:t", ::parseLet)
        testReplaceMiddle("let x", ":t", "=z", "", ::parseLet)
        testReplaceMiddle("let x", "=y", "", ":t=z", ::parseLet)
    }
}