package parser

import kotlinx.serialization.*
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

class SerializationTests {
    fun parseExpressionForTest(s: String): Ast {
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        return parseExpression(builder)!!
    }

    fun parseLetForTest(s: String): Ast {
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        return parseLet(builder)
    }

    fun parseReturnForTest(s: String): Ast {
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        return parseReturn(builder)
    }

    fun parseIfForTest(s: String): Ast {
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        return parseReturn(builder)
    }

    fun parseBlockForTest(s: String): Ast {
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        return parseBlock(builder)
    }

    fun parseFnForTest(s: String): Ast {
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        return parseFn(builder)
    }

    fun runAstEncodeDecodeTest(ast: Ast) {
        val encode = astJsonFormat.encodeToString(ast)
        val decode = astJsonFormat.decodeFromString<Ast>(encode)
        assertEquals(ast, decode)
    }

    @Test
    fun test001() {
        val s = "x"
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test002() {
        val s = "+x"
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test003() {
        val s = "x + y"
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test004() {
        val s = "f(x, y, z)"
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test005() {
        val s = "(f(x + y, -z, w))"
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test006() {
        val s = "1"
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test007() {
        val s = "1.1"
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test008() {
        val s = "\"hello world\""
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test009() {
        val s = "'c'"
        runAstEncodeDecodeTest(parseExpressionForTest(s))
    }

    @Test
    fun test010() {
        val s = "let x = y"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test011() {
        val s = "let x = +y"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test012() {
        val s = "let x = y + z"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test013() {
        val s = "let x = f(x, y, z)"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test014() {
        val s = "let x = (y)"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test015() {
        val s = "let x: t = y"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test016() {
        val s = "let x: U32 = y"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test017() {
        val s = "let x: t = f(y)"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test018() {
        val s = "let x: U32 = f(y)"
        runAstEncodeDecodeTest(parseLetForTest(s))
    }

    @Test
    fun test019() {
        val s = "return x"
        runAstEncodeDecodeTest(parseReturnForTest(s))
    }

    @Test
    fun test020() {
        val s = "return +x"
        runAstEncodeDecodeTest(parseReturnForTest(s))
    }

    @Test
    fun test021() {
        val s = "return x + y"
        runAstEncodeDecodeTest(parseReturnForTest(s))
    }

    @Test
    fun test022() {
        val s = "return f(x, y, z)"
        runAstEncodeDecodeTest(parseReturnForTest(s))
    }

    @Test
    fun test023() {
        val s = "return (x)"
        runAstEncodeDecodeTest(parseReturnForTest(s))
    }

    @Test
    fun test024() {
        val s = "if(x) {}"
        runAstEncodeDecodeTest(parseIfForTest(s))
    }

    @Test
    fun test025() {
        val s = "if(!x) {}"
        runAstEncodeDecodeTest(parseIfForTest(s))
    }

    @Test
    fun test026() {
        val s = "if(x | y) {}"
        runAstEncodeDecodeTest(parseIfForTest(s))
    }

    @Test
    fun test027() {
        val s = "if(f(x, y, z)) {}"
        runAstEncodeDecodeTest(parseIfForTest(s))
    }

    @Test
    fun test028() {
        val s = "if((x)) {}"
        runAstEncodeDecodeTest(parseIfForTest(s))
    }

    @Test
    fun test029() {
        val s = """if(x) {
                         |let y = x
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseIfForTest(s))
    }

    @Test
    fun test030() {
        val s = """if(x) {
                         |let y = x
                         |return y
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseIfForTest(s))
    }

    @Test
    fun test031() {
        val s = """let x = y
                         |return x""".trimMargin()
        runAstEncodeDecodeTest(parseBlockForTest(s))
    }

    @Test
    fun test032() {
        val s = """fn id(x) {
                         |return x
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseFnForTest(s))
    }

    @Test
    fun test033() {
        val s = """fn id(x) {
                         |let y = x
                         |return y
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseFnForTest(s))
    }

    @Test
    fun test034() {
        val s = """fn id(x: t) {
                         |let y = x
                         |return y
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseFnForTest(s))
    }

    @Test
    fun test035() {
        val s = """fn id(x): t {
                         |let y = x
                         |return y
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseFnForTest(s))
    }

    @Test
    fun test036() {
        val s = """fn id(x: t): t {
                         |let y = x
                         |return y
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseFnForTest(s))
    }

    @Test
    fun test037() {
        val s = """fn id(x: I32) {
                         |let y = x
                         |return y
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseFnForTest(s))
    }

    @Test
    fun test038() {
        val s = """fn id(x): I32 {
                         |let y = x
                         |return y
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseFnForTest(s))
    }

    @Test
    fun test039() {
        val s = """fn id(x: I32): I32 {
                         |let y = x
                         |return y
                         |}""".trimMargin()
        runAstEncodeDecodeTest(parseFnForTest(s))
    }

    @Test
    fun test040() {

    }
}