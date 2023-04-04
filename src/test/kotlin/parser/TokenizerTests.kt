package parser

import kotlin.test.*

class TokenizerTests {
    fun getTokens(text: String): List<Token> {
        val iter = ParserIterator()
        iter.add(text)
        val tokens = mutableListOf<Token>()
        while(iter.hasNext()) {
            tokens.add(iter.peekToken())
            iter.next()
        }
        return tokens
    }

    @Test
    fun testValidName() {
        repeat(100000) {
            val (text, goldTokens) = generateName()
            val tokens = getTokens(text)
            assertEquals(goldTokens, tokens)
        }
    }

    @Test
    fun testValidInt() {
        repeat(100000) {
            val (text, goldTokens) = generateInt()
            val tokens = getTokens(text)
            assertEquals(goldTokens, tokens)
        }
    }

    @Test
    fun testValidFloat() {
        repeat(100000) {
            val (text, goldTokens) = generateFloat()
            val tokens = getTokens(text)
            assertEquals(goldTokens, tokens)
        }
    }

    @Test
    fun testUnaryAndInt() {
        repeat(100000) {
            val (a, at) = generateUnaryOperator()
            val (b, bt) = generateInt()
            val text = "${a}${b}"
            val goldTokens = at + bt
            val tokens = getTokens(text)
            assertEquals(goldTokens, tokens)
        }
    }

    @Test
    fun testUnaryAndFloat() {
        repeat(100000) {
            val (a, at) = generateUnaryOperator()
            val (b, bt) = generateFloat()
            val text = "${a}${b}"
            val goldTokens = at + bt
            val tokens = getTokens(text)
            assertEquals(goldTokens, tokens)
        }
    }

    @Test
    fun testIntName() {
        repeat(100000) {
            val (a, at) = generateInt()
            val (b, bt) = generateName(removeSetInitial = setOf('b', 'B', 's', 'S', 'l', 'L', 'f', 'F', 'd', 'D'))
            val text = "${a}${b}"
            val goldTokens = at + bt
            val tokens = getTokens(text)
            assertEquals(goldTokens, tokens)
        }
    }

    @Test
    fun testFloatName() {
        repeat(100000) {
            val (a, at) = generateFloat()
            val (b, bt) = generateName(removeSetInitial = setOf('b', 'B', 's', 'S', 'l', 'L', 'f', 'F', 'd', 'D'))
            val text = "${a}${b}"
            val goldTokens = at + bt
            val tokens = getTokens(text)
            assertEquals(goldTokens, tokens)
        }
    }

    fun generateInt(): Pair<String, List<Token>> {
        val a = (0..100000).random().toString()
        val b = listOf("", "b", "B", "s", "S", "l", "L").random()
        val text = "${a}${b}"

        return Pair(text, listOf(IntToken(text)))
    }

    fun generateFloat(): Pair<String, List<Token>> {
        val text =
            when((0 until 4).random()) {
                0 -> {
                    val a = (0..100000).random().toString()
                    val b = listOf("f", "F", "d", "D").random()
                    "${a}${b}"
                }
                1 -> {
                    val a = (0..100000).random().toString()
                    val b = listOf("", "f", "F", "d", "D").random()
                    "${a}.${b}"
                }
                2 -> {
                    val a = (0..100000).random().toString()
                    val b = listOf("", "f", "F", "d", "D").random()
                    ".${a}${b}"
                }
                3 -> {
                    val a = (0..100000).random().toString()
                    val b = (0..100000).random().toString()
                    val c = listOf("", "f", "F", "d", "D").random()
                    "${a}.${b}${c}"
                }
                else -> throw Exception("programmer error")
            }
        return Pair(text, listOf(FloatToken(text)))
    }

    fun generateUnaryOperator(): Pair<String, List<Token>> {
        val (text, token) = listOf(Pair("+", OperatorToken("+")), Pair("-", OperatorToken("-")), Pair("!", OperatorToken("!")), Pair("~", OperatorToken("~"))).random()
        return Pair(text, listOf(token))
    }

    fun isKeyword(text: String) =
        when(text) {
            "if", "function", "let", "return" -> true
            else -> false
        }

    fun generateName(removeSetInitial: Set<Char> = emptySet(), removeSetAll: Set<Char> = emptySet()) : Pair<String, List<Token>> {
        var text = ""
        while(true) {
            val initial = (initialName - removeSetInitial - removeSetAll).random()
            val restChars = (allName - removeSetAll)
            val rest = String(CharArray((0 until 100).random()) { restChars.random() })
            text = initial + rest
            if(!isKeyword(text)) break
        }
        return Pair(text, listOf(NameToken(text)))
    }
}