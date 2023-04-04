package parser

import kotlin.test.*

class ValidExpressionParseTests {
    @Test
    fun testExpressionParse001() {
        val inString = "x"
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = NameToken("x")
        val goldExpressionEList = listOf(goldExpressionE0)
        val goldExpression = AtomicExpression(goldExpressionEList, goldExpressionE0)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse002() {
        val inString = "1"
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = IntToken("1")
        val goldExpressionEList = listOf(goldExpressionE0)
        val goldExpression = AtomicExpression(goldExpressionEList, goldExpressionE0)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse003() {
        val inString = "1.0"
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = FloatToken("1.0")
        val goldExpressionEList = listOf(goldExpressionE0)
        val goldExpression = AtomicExpression(goldExpressionEList, goldExpressionE0)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse004() {
        val inString = """"asdf""""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = StringToken(""""asdf"""")
        val goldExpressionEList = listOf(goldExpressionE0)
        val goldExpression = AtomicExpression(goldExpressionEList, goldExpressionE0)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse005() {
        val inString = """'a'"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = CharToken("'a'")
        val goldExpressionEList = listOf(goldExpressionE0)
        val goldExpression = AtomicExpression(goldExpressionEList, goldExpressionE0)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse006() {
        val inString = """+x"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = OperatorToken("+")
        val goldExpressionE1E0 = NameToken("x")
        val goldExpressionE1EList = listOf(goldExpressionE1E0)
        val goldExpressionE1 = AtomicExpression(goldExpressionE1EList, goldExpressionE1E0)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1)
        val goldExpression = UnaryExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse007() {
        val inString = """* +x"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = OperatorToken("*")
        val goldExpressionE1 = WhitespaceToken(" ")
        val goldExpressionE2E0 = OperatorToken("+")
        val goldExpressionE2E1E0 = NameToken("x")
        val goldExpressionE2E1EList = listOf(goldExpressionE2E1E0)
        val goldExpressionE2E1 = AtomicExpression(goldExpressionE2E1EList, goldExpressionE2E1E0)
        val goldExpressionE2EList = listOf(goldExpressionE2E0, goldExpressionE2E1)
        val goldExpressionE2 = UnaryExpression(goldExpressionE2EList, goldExpressionE2E0, goldExpressionE2E1)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2)
        val goldExpression = UnaryExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse008() {
        val inString = """x + y"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("x")
        val goldExpressionE0E1 = WhitespaceToken(" ")
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OperatorToken("+")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = NameToken("y")
        val goldExpressionE3EList = listOf(goldExpressionE3E0)
        val goldExpressionE3 = AtomicExpression(goldExpressionE3EList, goldExpressionE3E0)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse009() {
        val inString = """x + y * z"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("x")
        val goldExpressionE0E1 = WhitespaceToken(" ")
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OperatorToken("+")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0E0 = NameToken("y")
        val goldExpressionE3E0E1 = WhitespaceToken(" ")
        val goldExpressionE3E0EList = listOf(goldExpressionE3E0E0, goldExpressionE3E0E1)
        val goldExpressionE3E0 = AtomicExpression(goldExpressionE3E0EList, goldExpressionE3E0E0)
        val goldExpressionE3E1 = OperatorToken("*")
        val goldExpressionE3E2 = WhitespaceToken(" ")
        val goldExpressionE3E3E0 = NameToken("z")
        val goldExpressionE3E3EList = listOf(goldExpressionE3E3E0)
        val goldExpressionE3E3 = AtomicExpression(goldExpressionE3E3EList, goldExpressionE3E3E0)
        val goldExpressionE3EList = listOf(goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E2, goldExpressionE3E3)
        val goldExpressionE3 = BinaryOpExpression(goldExpressionE3EList, goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E3)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse010() {
        val inString = """x * y + z"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0E0 = NameToken("x")
        val goldExpressionE0E0E1 = WhitespaceToken(" ")
        val goldExpressionE0E0EList = listOf(goldExpressionE0E0E0, goldExpressionE0E0E1)
        val goldExpressionE0E0 = AtomicExpression(goldExpressionE0E0EList, goldExpressionE0E0E0)
        val goldExpressionE0E1 = OperatorToken("*")
        val goldExpressionE0E2 = WhitespaceToken(" ")
        val goldExpressionE0E3E0 = NameToken("y")
        val goldExpressionE0E3E1 = WhitespaceToken(" ")
        val goldExpressionE0E3EList = listOf(goldExpressionE0E3E0, goldExpressionE0E3E1)
        val goldExpressionE0E3 = AtomicExpression(goldExpressionE0E3EList, goldExpressionE0E3E0)
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E2, goldExpressionE0E3)
        val goldExpressionE0 = BinaryOpExpression(goldExpressionE0EList, goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E3)
        val goldExpressionE1 = OperatorToken("+")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = NameToken("z")
        val goldExpressionE3EList = listOf(goldExpressionE3E0)
        val goldExpressionE3 = AtomicExpression(goldExpressionE3EList, goldExpressionE3E0)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse011() {
        val inString = """w * x < y + z"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0E0 = NameToken("w")
        val goldExpressionE0E0E1 = WhitespaceToken(" ")
        val goldExpressionE0E0EList = listOf(goldExpressionE0E0E0, goldExpressionE0E0E1)
        val goldExpressionE0E0 = AtomicExpression(goldExpressionE0E0EList, goldExpressionE0E0E0)
        val goldExpressionE0E1 = OperatorToken("*")
        val goldExpressionE0E2 = WhitespaceToken(" ")
        val goldExpressionE0E3E0 = NameToken("x")
        val goldExpressionE0E3E1 = WhitespaceToken(" ")
        val goldExpressionE0E3EList = listOf(goldExpressionE0E3E0, goldExpressionE0E3E1)
        val goldExpressionE0E3 = AtomicExpression(goldExpressionE0E3EList, goldExpressionE0E3E0)
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E2, goldExpressionE0E3)
        val goldExpressionE0 = BinaryOpExpression(goldExpressionE0EList, goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E3)
        val goldExpressionE1 = OperatorToken("<")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0E0 = NameToken("y")
        val goldExpressionE3E0E1 = WhitespaceToken(" ")
        val goldExpressionE3E0EList = listOf(goldExpressionE3E0E0, goldExpressionE3E0E1)
        val goldExpressionE3E0 = AtomicExpression(goldExpressionE3E0EList, goldExpressionE3E0E0)
        val goldExpressionE3E1 = OperatorToken("+")
        val goldExpressionE3E2 = WhitespaceToken(" ")
        val goldExpressionE3E3E0 = NameToken("z")
        val goldExpressionE3E3EList = listOf(goldExpressionE3E3E0)
        val goldExpressionE3E3 = AtomicExpression(goldExpressionE3E3EList, goldExpressionE3E3E0)
        val goldExpressionE3EList = listOf(goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E2, goldExpressionE3E3)
        val goldExpressionE3 = BinaryOpExpression(goldExpressionE3EList, goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E3)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse012() {
        val inString = """w * x + y < z"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0E0E0 = NameToken("w")
        val goldExpressionE0E0E0E1 = WhitespaceToken(" ")
        val goldExpressionE0E0E0EList = listOf(goldExpressionE0E0E0E0, goldExpressionE0E0E0E1)
        val goldExpressionE0E0E0 = AtomicExpression(goldExpressionE0E0E0EList, goldExpressionE0E0E0E0)
        val goldExpressionE0E0E1 = OperatorToken("*")
        val goldExpressionE0E0E2 = WhitespaceToken(" ")
        val goldExpressionE0E0E3E0 = NameToken("x")
        val goldExpressionE0E0E3E1 = WhitespaceToken(" ")
        val goldExpressionE0E0E3EList = listOf(goldExpressionE0E0E3E0, goldExpressionE0E0E3E1)
        val goldExpressionE0E0E3 = AtomicExpression(goldExpressionE0E0E3EList, goldExpressionE0E0E3E0)
        val goldExpressionE0E0EList = listOf(goldExpressionE0E0E0, goldExpressionE0E0E1, goldExpressionE0E0E2, goldExpressionE0E0E3)
        val goldExpressionE0E0 = BinaryOpExpression(goldExpressionE0E0EList, goldExpressionE0E0E0, goldExpressionE0E0E1, goldExpressionE0E0E3)
        val goldExpressionE0E1 = OperatorToken("+")
        val goldExpressionE0E2 = WhitespaceToken(" ")
        val goldExpressionE0E3E0 = NameToken("y")
        val goldExpressionE0E3E1 = WhitespaceToken(" ")
        val goldExpressionE0E3EList = listOf(goldExpressionE0E3E0, goldExpressionE0E3E1)
        val goldExpressionE0E3 = AtomicExpression(goldExpressionE0E3EList, goldExpressionE0E3E0)
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E2, goldExpressionE0E3)
        val goldExpressionE0 = BinaryOpExpression(goldExpressionE0EList, goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E3)
        val goldExpressionE1 = OperatorToken("<")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = NameToken("z")
        val goldExpressionE3EList = listOf(goldExpressionE3E0)
        val goldExpressionE3 = AtomicExpression(goldExpressionE3EList, goldExpressionE3E0)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse013() {
        val inString = """w + x * y < z"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0E0 = NameToken("w")
        val goldExpressionE0E0E1 = WhitespaceToken(" ")
        val goldExpressionE0E0EList = listOf(goldExpressionE0E0E0, goldExpressionE0E0E1)
        val goldExpressionE0E0 = AtomicExpression(goldExpressionE0E0EList, goldExpressionE0E0E0)
        val goldExpressionE0E1 = OperatorToken("+")
        val goldExpressionE0E2 = WhitespaceToken(" ")
        val goldExpressionE0E3E0E0 = NameToken("x")
        val goldExpressionE0E3E0E1 = WhitespaceToken(" ")
        val goldExpressionE0E3E0EList = listOf(goldExpressionE0E3E0E0, goldExpressionE0E3E0E1)
        val goldExpressionE0E3E0 = AtomicExpression(goldExpressionE0E3E0EList, goldExpressionE0E3E0E0)
        val goldExpressionE0E3E1 = OperatorToken("*")
        val goldExpressionE0E3E2 = WhitespaceToken(" ")
        val goldExpressionE0E3E3E0 = NameToken("y")
        val goldExpressionE0E3E3E1 = WhitespaceToken(" ")
        val goldExpressionE0E3E3EList = listOf(goldExpressionE0E3E3E0, goldExpressionE0E3E3E1)
        val goldExpressionE0E3E3 = AtomicExpression(goldExpressionE0E3E3EList, goldExpressionE0E3E3E0)
        val goldExpressionE0E3EList = listOf(goldExpressionE0E3E0, goldExpressionE0E3E1, goldExpressionE0E3E2, goldExpressionE0E3E3)
        val goldExpressionE0E3 = BinaryOpExpression(goldExpressionE0E3EList, goldExpressionE0E3E0, goldExpressionE0E3E1, goldExpressionE0E3E3)
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E2, goldExpressionE0E3)
        val goldExpressionE0 = BinaryOpExpression(goldExpressionE0EList, goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E3)
        val goldExpressionE1 = OperatorToken("<")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = NameToken("z")
        val goldExpressionE3EList = listOf(goldExpressionE3E0)
        val goldExpressionE3 = AtomicExpression(goldExpressionE3EList, goldExpressionE3E0)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse014() {
        val inString = """w < x * y + z"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("w")
        val goldExpressionE0E1 = WhitespaceToken(" ")
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OperatorToken("<")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0E0E0 = NameToken("x")
        val goldExpressionE3E0E0E1 = WhitespaceToken(" ")
        val goldExpressionE3E0E0EList = listOf(goldExpressionE3E0E0E0, goldExpressionE3E0E0E1)
        val goldExpressionE3E0E0 = AtomicExpression(goldExpressionE3E0E0EList, goldExpressionE3E0E0E0)
        val goldExpressionE3E0E1 = OperatorToken("*")
        val goldExpressionE3E0E2 = WhitespaceToken(" ")
        val goldExpressionE3E0E3E0 = NameToken("y")
        val goldExpressionE3E0E3E1 = WhitespaceToken(" ")
        val goldExpressionE3E0E3EList = listOf(goldExpressionE3E0E3E0, goldExpressionE3E0E3E1)
        val goldExpressionE3E0E3 = AtomicExpression(goldExpressionE3E0E3EList, goldExpressionE3E0E3E0)
        val goldExpressionE3E0EList = listOf(goldExpressionE3E0E0, goldExpressionE3E0E1, goldExpressionE3E0E2, goldExpressionE3E0E3)
        val goldExpressionE3E0 = BinaryOpExpression(goldExpressionE3E0EList, goldExpressionE3E0E0, goldExpressionE3E0E1, goldExpressionE3E0E3)
        val goldExpressionE3E1 = OperatorToken("+")
        val goldExpressionE3E2 = WhitespaceToken(" ")
        val goldExpressionE3E3E0 = NameToken("z")
        val goldExpressionE3E3EList = listOf(goldExpressionE3E3E0)
        val goldExpressionE3E3 = AtomicExpression(goldExpressionE3E3EList, goldExpressionE3E3E0)
        val goldExpressionE3EList = listOf(goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E2, goldExpressionE3E3)
        val goldExpressionE3 = BinaryOpExpression(goldExpressionE3EList, goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E3)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse015() {
        val inString = """w < x + y * z"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("w")
        val goldExpressionE0E1 = WhitespaceToken(" ")
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OperatorToken("<")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0E0 = NameToken("x")
        val goldExpressionE3E0E1 = WhitespaceToken(" ")
        val goldExpressionE3E0EList = listOf(goldExpressionE3E0E0, goldExpressionE3E0E1)
        val goldExpressionE3E0 = AtomicExpression(goldExpressionE3E0EList, goldExpressionE3E0E0)
        val goldExpressionE3E1 = OperatorToken("+")
        val goldExpressionE3E2 = WhitespaceToken(" ")
        val goldExpressionE3E3E0E0 = NameToken("y")
        val goldExpressionE3E3E0E1 = WhitespaceToken(" ")
        val goldExpressionE3E3E0EList = listOf(goldExpressionE3E3E0E0, goldExpressionE3E3E0E1)
        val goldExpressionE3E3E0 = AtomicExpression(goldExpressionE3E3E0EList, goldExpressionE3E3E0E0)
        val goldExpressionE3E3E1 = OperatorToken("*")
        val goldExpressionE3E3E2 = WhitespaceToken(" ")
        val goldExpressionE3E3E3E0 = NameToken("z")
        val goldExpressionE3E3E3EList = listOf(goldExpressionE3E3E3E0)
        val goldExpressionE3E3E3 = AtomicExpression(goldExpressionE3E3E3EList, goldExpressionE3E3E3E0)
        val goldExpressionE3E3EList = listOf(goldExpressionE3E3E0, goldExpressionE3E3E1, goldExpressionE3E3E2, goldExpressionE3E3E3)
        val goldExpressionE3E3 = BinaryOpExpression(goldExpressionE3E3EList, goldExpressionE3E3E0, goldExpressionE3E3E1, goldExpressionE3E3E3)
        val goldExpressionE3EList = listOf(goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E2, goldExpressionE3E3)
        val goldExpressionE3 = BinaryOpExpression(goldExpressionE3EList, goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E3)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse016() {
        val inString = "(x)"
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = OpenParenToken
        val goldExpressionE1E0 = NameToken("x")
        val goldExpressionE1EList = listOf(goldExpressionE1E0)
        val goldExpressionE1 = AtomicExpression(goldExpressionE1EList, goldExpressionE1E0)
        val goldExpressionE2 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2)
        val goldExpression = GroupingExpression(goldExpressionEList, goldExpressionE1)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse017() {
        val inString = """(x + y)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = OpenParenToken
        val goldExpressionE1E0E0 = NameToken("x")
        val goldExpressionE1E0E1 = WhitespaceToken(" ")
        val goldExpressionE1E0EList = listOf(goldExpressionE1E0E0, goldExpressionE1E0E1)
        val goldExpressionE1E0 = AtomicExpression(goldExpressionE1E0EList, goldExpressionE1E0E0)
        val goldExpressionE1E1 = OperatorToken("+")
        val goldExpressionE1E2 = WhitespaceToken(" ")
        val goldExpressionE1E3E0 = NameToken("y")
        val goldExpressionE1E3EList = listOf(goldExpressionE1E3E0)
        val goldExpressionE1E3 = AtomicExpression(goldExpressionE1E3EList, goldExpressionE1E3E0)
        val goldExpressionE1EList = listOf(goldExpressionE1E0, goldExpressionE1E1, goldExpressionE1E2, goldExpressionE1E3)
        val goldExpressionE1 = BinaryOpExpression(goldExpressionE1EList, goldExpressionE1E0, goldExpressionE1E1, goldExpressionE1E3)
        val goldExpressionE2 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2)
        val goldExpression = GroupingExpression(goldExpressionEList, goldExpressionE1)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse018() {
        val inString = """(x + y) * z"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = OpenParenToken
        val goldExpressionE0E1E0E0 = NameToken("x")
        val goldExpressionE0E1E0E1 = WhitespaceToken(" ")
        val goldExpressionE0E1E0EList = listOf(goldExpressionE0E1E0E0, goldExpressionE0E1E0E1)
        val goldExpressionE0E1E0 = AtomicExpression(goldExpressionE0E1E0EList, goldExpressionE0E1E0E0)
        val goldExpressionE0E1E1 = OperatorToken("+")
        val goldExpressionE0E1E2 = WhitespaceToken(" ")
        val goldExpressionE0E1E3E0 = NameToken("y")
        val goldExpressionE0E1E3EList = listOf(goldExpressionE0E1E3E0)
        val goldExpressionE0E1E3 = AtomicExpression(goldExpressionE0E1E3EList, goldExpressionE0E1E3E0)
        val goldExpressionE0E1EList = listOf(goldExpressionE0E1E0, goldExpressionE0E1E1, goldExpressionE0E1E2, goldExpressionE0E1E3)
        val goldExpressionE0E1 = BinaryOpExpression(goldExpressionE0E1EList, goldExpressionE0E1E0, goldExpressionE0E1E1, goldExpressionE0E1E3)
        val goldExpressionE0E2 = CloseParenToken
        val goldExpressionE0E3 = WhitespaceToken(" ")
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E2, goldExpressionE0E3)
        val goldExpressionE0 = GroupingExpression(goldExpressionE0EList, goldExpressionE0E1)
        val goldExpressionE1 = OperatorToken("*")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = NameToken("z")
        val goldExpressionE3EList = listOf(goldExpressionE3E0)
        val goldExpressionE3 = AtomicExpression(goldExpressionE3EList, goldExpressionE3E0)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse019() {
        val inString = """x + (y * z)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("x")
        val goldExpressionE0E1 = WhitespaceToken(" ")
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OperatorToken("+")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = OpenParenToken
        val goldExpressionE3E1E0E0 = NameToken("y")
        val goldExpressionE3E1E0E1 = WhitespaceToken(" ")
        val goldExpressionE3E1E0EList = listOf(goldExpressionE3E1E0E0, goldExpressionE3E1E0E1)
        val goldExpressionE3E1E0 = AtomicExpression(goldExpressionE3E1E0EList, goldExpressionE3E1E0E0)
        val goldExpressionE3E1E1 = OperatorToken("*")
        val goldExpressionE3E1E2 = WhitespaceToken(" ")
        val goldExpressionE3E1E3E0 = NameToken("z")
        val goldExpressionE3E1E3EList = listOf(goldExpressionE3E1E3E0)
        val goldExpressionE3E1E3 = AtomicExpression(goldExpressionE3E1E3EList, goldExpressionE3E1E3E0)
        val goldExpressionE3E1EList = listOf(goldExpressionE3E1E0, goldExpressionE3E1E1, goldExpressionE3E1E2, goldExpressionE3E1E3)
        val goldExpressionE3E1 = BinaryOpExpression(goldExpressionE3E1EList, goldExpressionE3E1E0, goldExpressionE3E1E1, goldExpressionE3E1E3)
        val goldExpressionE3E2 = CloseParenToken
        val goldExpressionE3EList = listOf(goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E2)
        val goldExpressionE3 = GroupingExpression(goldExpressionE3EList, goldExpressionE3E1)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse020() {
        val inString = """x * (y + z)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("x")
        val goldExpressionE0E1 = WhitespaceToken(" ")
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OperatorToken("*")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = OpenParenToken
        val goldExpressionE3E1E0E0 = NameToken("y")
        val goldExpressionE3E1E0E1 = WhitespaceToken(" ")
        val goldExpressionE3E1E0EList = listOf(goldExpressionE3E1E0E0, goldExpressionE3E1E0E1)
        val goldExpressionE3E1E0 = AtomicExpression(goldExpressionE3E1E0EList, goldExpressionE3E1E0E0)
        val goldExpressionE3E1E1 = OperatorToken("+")
        val goldExpressionE3E1E2 = WhitespaceToken(" ")
        val goldExpressionE3E1E3E0 = NameToken("z")
        val goldExpressionE3E1E3EList = listOf(goldExpressionE3E1E3E0)
        val goldExpressionE3E1E3 = AtomicExpression(goldExpressionE3E1E3EList, goldExpressionE3E1E3E0)
        val goldExpressionE3E1EList = listOf(goldExpressionE3E1E0, goldExpressionE3E1E1, goldExpressionE3E1E2, goldExpressionE3E1E3)
        val goldExpressionE3E1 = BinaryOpExpression(goldExpressionE3E1EList, goldExpressionE3E1E0, goldExpressionE3E1E1, goldExpressionE3E1E3)
        val goldExpressionE3E2 = CloseParenToken
        val goldExpressionE3EList = listOf(goldExpressionE3E0, goldExpressionE3E1, goldExpressionE3E2)
        val goldExpressionE3 = GroupingExpression(goldExpressionE3EList, goldExpressionE3E1)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse021() {
        val inString = """f(x)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("f")
        val goldExpressionE0EList = listOf(goldExpressionE0E0)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OpenParenToken
        val goldExpressionE2As0A0E0 = NameToken("x")
        val goldExpressionE2As0A0EList = listOf(goldExpressionE2As0A0E0)
        val goldExpressionE2As0A0 = AtomicExpression(goldExpressionE2As0A0EList, goldExpressionE2As0A0E0)
        val goldExpressionE2As0AList = listOf(goldExpressionE2As0A0)
        val goldExpressionE2As0 = Argument(goldExpressionE2As0AList, goldExpressionE2As0A0)
        val goldExpressionE2 = starOf(listOf(goldExpressionE2As0))
        val goldExpressionE3 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = FunctionCallExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse022() {
        val inString = """f(x,)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("f")
        val goldExpressionE0EList = listOf(goldExpressionE0E0)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OpenParenToken
        val goldExpressionE2As0A0E0 = NameToken("x")
        val goldExpressionE2As0A0EList = listOf(goldExpressionE2As0A0E0)
        val goldExpressionE2As0A0 = AtomicExpression(goldExpressionE2As0A0EList, goldExpressionE2As0A0E0)
        val goldExpressionE2As0A1 = CommaToken
        val goldExpressionE2As0AList = listOf(goldExpressionE2As0A0, goldExpressionE2As0A1)
        val goldExpressionE2As0 = Argument(goldExpressionE2As0AList, goldExpressionE2As0A0)
        val goldExpressionE2 = starOf(listOf(goldExpressionE2As0))
        val goldExpressionE3 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = FunctionCallExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse023() {
        val inString = """f(x, y)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("f")
        val goldExpressionE0EList = listOf(goldExpressionE0E0)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OpenParenToken
        val goldExpressionE2As0A0E0 = NameToken("x")
        val goldExpressionE2As0A0EList = listOf(goldExpressionE2As0A0E0)
        val goldExpressionE2As0A0 = AtomicExpression(goldExpressionE2As0A0EList, goldExpressionE2As0A0E0)
        val goldExpressionE2As0A1 = CommaToken
        val goldExpressionE2As0A2 = WhitespaceToken(" ")
        val goldExpressionE2As0AList = listOf(goldExpressionE2As0A0, goldExpressionE2As0A1, goldExpressionE2As0A2)
        val goldExpressionE2As0 = Argument(goldExpressionE2As0AList, goldExpressionE2As0A0)
        val goldExpressionE2As1A0E0 = NameToken("y")
        val goldExpressionE2As1A0EList = listOf(goldExpressionE2As1A0E0)
        val goldExpressionE2As1A0 = AtomicExpression(goldExpressionE2As1A0EList, goldExpressionE2As1A0E0)
        val goldExpressionE2As1AList = listOf(goldExpressionE2As1A0)
        val goldExpressionE2As1 = Argument(goldExpressionE2As1AList, goldExpressionE2As1A0)
        val goldExpressionE2 = starOf(listOf(goldExpressionE2As0, goldExpressionE2As1))
        val goldExpressionE3 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = FunctionCallExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse024() {
        val inString = """f(x, y,)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("f")
        val goldExpressionE0EList = listOf(goldExpressionE0E0)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OpenParenToken
        val goldExpressionE2As0A0E0 = NameToken("x")
        val goldExpressionE2As0A0EList = listOf(goldExpressionE2As0A0E0)
        val goldExpressionE2As0A0 = AtomicExpression(goldExpressionE2As0A0EList, goldExpressionE2As0A0E0)
        val goldExpressionE2As0A1 = CommaToken
        val goldExpressionE2As0A2 = WhitespaceToken(" ")
        val goldExpressionE2As0AList = listOf(goldExpressionE2As0A0, goldExpressionE2As0A1, goldExpressionE2As0A2)
        val goldExpressionE2As0 = Argument(goldExpressionE2As0AList, goldExpressionE2As0A0)
        val goldExpressionE2As1A0E0 = NameToken("y")
        val goldExpressionE2As1A0EList = listOf(goldExpressionE2As1A0E0)
        val goldExpressionE2As1A0 = AtomicExpression(goldExpressionE2As1A0EList, goldExpressionE2As1A0E0)
        val goldExpressionE2As1A1 = CommaToken
        val goldExpressionE2As1AList = listOf(goldExpressionE2As1A0, goldExpressionE2As1A1)
        val goldExpressionE2As1 = Argument(goldExpressionE2As1AList, goldExpressionE2As1A0)
        val goldExpressionE2 = starOf(listOf(goldExpressionE2As0, goldExpressionE2As1))
        val goldExpressionE3 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = FunctionCallExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse025() {
        val inString = """f(x + y, z)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("f")
        val goldExpressionE0EList = listOf(goldExpressionE0E0)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OpenParenToken
        val goldExpressionE2As0A0E0E0 = NameToken("x")
        val goldExpressionE2As0A0E0E1 = WhitespaceToken(" ")
        val goldExpressionE2As0A0E0EList = listOf(goldExpressionE2As0A0E0E0, goldExpressionE2As0A0E0E1)
        val goldExpressionE2As0A0E0 = AtomicExpression(goldExpressionE2As0A0E0EList, goldExpressionE2As0A0E0E0)
        val goldExpressionE2As0A0E1 = OperatorToken("+")
        val goldExpressionE2As0A0E2 = WhitespaceToken(" ")
        val goldExpressionE2As0A0E3E0 = NameToken("y")
        val goldExpressionE2As0A0E3EList = listOf(goldExpressionE2As0A0E3E0)
        val goldExpressionE2As0A0E3 = AtomicExpression(goldExpressionE2As0A0E3EList, goldExpressionE2As0A0E3E0)
        val goldExpressionE2As0A0EList = listOf(goldExpressionE2As0A0E0, goldExpressionE2As0A0E1, goldExpressionE2As0A0E2, goldExpressionE2As0A0E3)
        val goldExpressionE2As0A0 = BinaryOpExpression(goldExpressionE2As0A0EList, goldExpressionE2As0A0E0, goldExpressionE2As0A0E1, goldExpressionE2As0A0E3)
        val goldExpressionE2As0A1 = CommaToken
        val goldExpressionE2As0A2 = WhitespaceToken(" ")
        val goldExpressionE2As0AList = listOf(goldExpressionE2As0A0, goldExpressionE2As0A1, goldExpressionE2As0A2)
        val goldExpressionE2As0 = Argument(goldExpressionE2As0AList, goldExpressionE2As0A0)
        val goldExpressionE2As1A0E0 = NameToken("z")
        val goldExpressionE2As1A0EList = listOf(goldExpressionE2As1A0E0)
        val goldExpressionE2As1A0 = AtomicExpression(goldExpressionE2As1A0EList, goldExpressionE2As1A0E0)
        val goldExpressionE2As1AList = listOf(goldExpressionE2As1A0)
        val goldExpressionE2As1 = Argument(goldExpressionE2As1AList, goldExpressionE2As1A0)
        val goldExpressionE2 = starOf(listOf(goldExpressionE2As0, goldExpressionE2As1))
        val goldExpressionE3 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = FunctionCallExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse026() {
        val inString = """f(x + y, g(z))"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = NameToken("f")
        val goldExpressionE0EList = listOf(goldExpressionE0E0)
        val goldExpressionE0 = AtomicExpression(goldExpressionE0EList, goldExpressionE0E0)
        val goldExpressionE1 = OpenParenToken
        val goldExpressionE2As0A0E0E0 = NameToken("x")
        val goldExpressionE2As0A0E0E1 = WhitespaceToken(" ")
        val goldExpressionE2As0A0E0EList = listOf(goldExpressionE2As0A0E0E0, goldExpressionE2As0A0E0E1)
        val goldExpressionE2As0A0E0 = AtomicExpression(goldExpressionE2As0A0E0EList, goldExpressionE2As0A0E0E0)
        val goldExpressionE2As0A0E1 = OperatorToken("+")
        val goldExpressionE2As0A0E2 = WhitespaceToken(" ")
        val goldExpressionE2As0A0E3E0 = NameToken("y")
        val goldExpressionE2As0A0E3EList = listOf(goldExpressionE2As0A0E3E0)
        val goldExpressionE2As0A0E3 = AtomicExpression(goldExpressionE2As0A0E3EList, goldExpressionE2As0A0E3E0)
        val goldExpressionE2As0A0EList = listOf(goldExpressionE2As0A0E0, goldExpressionE2As0A0E1, goldExpressionE2As0A0E2, goldExpressionE2As0A0E3)
        val goldExpressionE2As0A0 = BinaryOpExpression(goldExpressionE2As0A0EList, goldExpressionE2As0A0E0, goldExpressionE2As0A0E1, goldExpressionE2As0A0E3)
        val goldExpressionE2As0A1 = CommaToken
        val goldExpressionE2As0A2 = WhitespaceToken(" ")
        val goldExpressionE2As0AList = listOf(goldExpressionE2As0A0, goldExpressionE2As0A1, goldExpressionE2As0A2)
        val goldExpressionE2As0 = Argument(goldExpressionE2As0AList, goldExpressionE2As0A0)
        val goldExpressionE2As1A0E0E0 = NameToken("g")
        val goldExpressionE2As1A0E0EList = listOf(goldExpressionE2As1A0E0E0)
        val goldExpressionE2As1A0E0 = AtomicExpression(goldExpressionE2As1A0E0EList, goldExpressionE2As1A0E0E0)
        val goldExpressionE2As1A0E1 = OpenParenToken
        val goldExpressionE2As1A0E2As0A0E0 = NameToken("z")
        val goldExpressionE2As1A0E2As0A0EList = listOf(goldExpressionE2As1A0E2As0A0E0)
        val goldExpressionE2As1A0E2As0A0 = AtomicExpression(goldExpressionE2As1A0E2As0A0EList, goldExpressionE2As1A0E2As0A0E0)
        val goldExpressionE2As1A0E2As0AList = listOf(goldExpressionE2As1A0E2As0A0)
        val goldExpressionE2As1A0E2As0 = Argument(goldExpressionE2As1A0E2As0AList, goldExpressionE2As1A0E2As0A0)
        val goldExpressionE2As1A0E2 = starOf(listOf(goldExpressionE2As1A0E2As0))
        val goldExpressionE2As1A0E3 = CloseParenToken
        val goldExpressionE2As1A0EList = listOf(goldExpressionE2As1A0E0, goldExpressionE2As1A0E1, goldExpressionE2As1A0E2, goldExpressionE2As1A0E3)
        val goldExpressionE2As1A0 = FunctionCallExpression(goldExpressionE2As1A0EList, goldExpressionE2As1A0E0, goldExpressionE2As1A0E2)
        val goldExpressionE2As1AList = listOf(goldExpressionE2As1A0)
        val goldExpressionE2As1 = Argument(goldExpressionE2As1AList, goldExpressionE2As1A0)
        val goldExpressionE2 = starOf(listOf(goldExpressionE2As0, goldExpressionE2As1))
        val goldExpressionE3 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = FunctionCallExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse027() {
        val inString = """f(x)(y)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0E0 = NameToken("f")
        val goldExpressionE0E0EList = listOf(goldExpressionE0E0E0)
        val goldExpressionE0E0 = AtomicExpression(goldExpressionE0E0EList, goldExpressionE0E0E0)
        val goldExpressionE0E1 = OpenParenToken
        val goldExpressionE0E2As0A0E0 = NameToken("x")
        val goldExpressionE0E2As0A0EList = listOf(goldExpressionE0E2As0A0E0)
        val goldExpressionE0E2As0A0 = AtomicExpression(goldExpressionE0E2As0A0EList, goldExpressionE0E2As0A0E0)
        val goldExpressionE0E2As0AList = listOf(goldExpressionE0E2As0A0)
        val goldExpressionE0E2As0 = Argument(goldExpressionE0E2As0AList, goldExpressionE0E2As0A0)
        val goldExpressionE0E2 = starOf(listOf(goldExpressionE0E2As0))
        val goldExpressionE0E3 = CloseParenToken
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E2, goldExpressionE0E3)
        val goldExpressionE0 = FunctionCallExpression(goldExpressionE0EList, goldExpressionE0E0, goldExpressionE0E2)
        val goldExpressionE1 = OpenParenToken
        val goldExpressionE2As0A0E0 = NameToken("y")
        val goldExpressionE2As0A0EList = listOf(goldExpressionE2As0A0E0)
        val goldExpressionE2As0A0 = AtomicExpression(goldExpressionE2As0A0EList, goldExpressionE2As0A0E0)
        val goldExpressionE2As0AList = listOf(goldExpressionE2As0A0)
        val goldExpressionE2As0 = Argument(goldExpressionE2As0AList, goldExpressionE2As0A0)
        val goldExpressionE2 = starOf(listOf(goldExpressionE2As0))
        val goldExpressionE3 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = FunctionCallExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse028() {
        val inString = """(a + b)(x)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = OpenParenToken
        val goldExpressionE0E1E0E0 = NameToken("a")
        val goldExpressionE0E1E0E1 = WhitespaceToken(" ")
        val goldExpressionE0E1E0EList = listOf(goldExpressionE0E1E0E0, goldExpressionE0E1E0E1)
        val goldExpressionE0E1E0 = AtomicExpression(goldExpressionE0E1E0EList, goldExpressionE0E1E0E0)
        val goldExpressionE0E1E1 = OperatorToken("+")
        val goldExpressionE0E1E2 = WhitespaceToken(" ")
        val goldExpressionE0E1E3E0 = NameToken("b")
        val goldExpressionE0E1E3EList = listOf(goldExpressionE0E1E3E0)
        val goldExpressionE0E1E3 = AtomicExpression(goldExpressionE0E1E3EList, goldExpressionE0E1E3E0)
        val goldExpressionE0E1EList = listOf(goldExpressionE0E1E0, goldExpressionE0E1E1, goldExpressionE0E1E2, goldExpressionE0E1E3)
        val goldExpressionE0E1 = BinaryOpExpression(goldExpressionE0E1EList, goldExpressionE0E1E0, goldExpressionE0E1E1, goldExpressionE0E1E3)
        val goldExpressionE0E2 = CloseParenToken
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1, goldExpressionE0E2)
        val goldExpressionE0 = GroupingExpression(goldExpressionE0EList, goldExpressionE0E1)
        val goldExpressionE1 = OpenParenToken
        val goldExpressionE2As0A0E0 = NameToken("x")
        val goldExpressionE2As0A0EList = listOf(goldExpressionE2As0A0E0)
        val goldExpressionE2As0A0 = AtomicExpression(goldExpressionE2As0A0EList, goldExpressionE2As0A0E0)
        val goldExpressionE2As0AList = listOf(goldExpressionE2As0A0)
        val goldExpressionE2As0 = Argument(goldExpressionE2As0AList, goldExpressionE2As0A0)
        val goldExpressionE2 = starOf(listOf(goldExpressionE2As0))
        val goldExpressionE3 = CloseParenToken
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = FunctionCallExpression(goldExpressionEList, goldExpressionE0, goldExpressionE2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse029() {
        val inString = """!f(x)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0 = OperatorToken("!")
        val goldExpressionE1E0E0 = NameToken("f")
        val goldExpressionE1E0EList = listOf(goldExpressionE1E0E0)
        val goldExpressionE1E0 = AtomicExpression(goldExpressionE1E0EList, goldExpressionE1E0E0)
        val goldExpressionE1E1 = OpenParenToken
        val goldExpressionE1E2As0A0E0 = NameToken("x")
        val goldExpressionE1E2As0A0EList = listOf(goldExpressionE1E2As0A0E0)
        val goldExpressionE1E2As0A0 = AtomicExpression(goldExpressionE1E2As0A0EList, goldExpressionE1E2As0A0E0)
        val goldExpressionE1E2As0AList = listOf(goldExpressionE1E2As0A0)
        val goldExpressionE1E2As0 = Argument(goldExpressionE1E2As0AList, goldExpressionE1E2As0A0)
        val goldExpressionE1E2 = starOf(listOf(goldExpressionE1E2As0))
        val goldExpressionE1E3 = CloseParenToken
        val goldExpressionE1EList = listOf(goldExpressionE1E0, goldExpressionE1E1, goldExpressionE1E2, goldExpressionE1E3)
        val goldExpressionE1 = FunctionCallExpression(goldExpressionE1EList, goldExpressionE1E0, goldExpressionE1E2)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1)
        val goldExpression = UnaryExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse030() {
        val inString = """+x + y"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = OperatorToken("+")
        val goldExpressionE0E1E0 = NameToken("x")
        val goldExpressionE0E1E1 = WhitespaceToken(" ")
        val goldExpressionE0E1EList = listOf(goldExpressionE0E1E0, goldExpressionE0E1E1)
        val goldExpressionE0E1 = AtomicExpression(goldExpressionE0E1EList, goldExpressionE0E1E0)
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = UnaryExpression(goldExpressionE0EList, goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE1 = OperatorToken("+")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = NameToken("y")
        val goldExpressionE3EList = listOf(goldExpressionE3E0)
        val goldExpressionE3 = AtomicExpression(goldExpressionE3EList, goldExpressionE3E0)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse031() {
        val inString = """+x * +y"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = OperatorToken("+")
        val goldExpressionE0E1E0 = NameToken("x")
        val goldExpressionE0E1E1 = WhitespaceToken(" ")
        val goldExpressionE0E1EList = listOf(goldExpressionE0E1E0, goldExpressionE0E1E1)
        val goldExpressionE0E1 = AtomicExpression(goldExpressionE0E1EList, goldExpressionE0E1E0)
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = UnaryExpression(goldExpressionE0EList, goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE1 = OperatorToken("*")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = OperatorToken("+")
        val goldExpressionE3E1E0 = NameToken("y")
        val goldExpressionE3E1EList = listOf(goldExpressionE3E1E0)
        val goldExpressionE3E1 = AtomicExpression(goldExpressionE3E1EList, goldExpressionE3E1E0)
        val goldExpressionE3EList = listOf(goldExpressionE3E0, goldExpressionE3E1)
        val goldExpressionE3 = UnaryExpression(goldExpressionE3EList, goldExpressionE3E0, goldExpressionE3E1)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse032() {
        val inString = """+x * ~f(y)"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseExpression(SequenceBuilder(inIterator))!!

        val goldExpressionE0E0 = OperatorToken("+")
        val goldExpressionE0E1E0 = NameToken("x")
        val goldExpressionE0E1E1 = WhitespaceToken(" ")
        val goldExpressionE0E1EList = listOf(goldExpressionE0E1E0, goldExpressionE0E1E1)
        val goldExpressionE0E1 = AtomicExpression(goldExpressionE0E1EList, goldExpressionE0E1E0)
        val goldExpressionE0EList = listOf(goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE0 = UnaryExpression(goldExpressionE0EList, goldExpressionE0E0, goldExpressionE0E1)
        val goldExpressionE1 = OperatorToken("*")
        val goldExpressionE2 = WhitespaceToken(" ")
        val goldExpressionE3E0 = OperatorToken("~")
        val goldExpressionE3E1E0E0 = NameToken("f")
        val goldExpressionE3E1E0EList = listOf(goldExpressionE3E1E0E0)
        val goldExpressionE3E1E0 = AtomicExpression(goldExpressionE3E1E0EList, goldExpressionE3E1E0E0)
        val goldExpressionE3E1E1 = OpenParenToken
        val goldExpressionE3E1E2As0A0E0 = NameToken("y")
        val goldExpressionE3E1E2As0A0EList = listOf(goldExpressionE3E1E2As0A0E0)
        val goldExpressionE3E1E2As0A0 = AtomicExpression(goldExpressionE3E1E2As0A0EList, goldExpressionE3E1E2As0A0E0)
        val goldExpressionE3E1E2As0AList = listOf(goldExpressionE3E1E2As0A0)
        val goldExpressionE3E1E2As0 = Argument(goldExpressionE3E1E2As0AList, goldExpressionE3E1E2As0A0)
        val goldExpressionE3E1E2 = starOf(listOf(goldExpressionE3E1E2As0))
        val goldExpressionE3E1E3 = CloseParenToken
        val goldExpressionE3E1EList = listOf(goldExpressionE3E1E0, goldExpressionE3E1E1, goldExpressionE3E1E2, goldExpressionE3E1E3)
        val goldExpressionE3E1 = FunctionCallExpression(goldExpressionE3E1EList, goldExpressionE3E1E0, goldExpressionE3E1E2)
        val goldExpressionE3EList = listOf(goldExpressionE3E0, goldExpressionE3E1)
        val goldExpressionE3 = UnaryExpression(goldExpressionE3EList, goldExpressionE3E0, goldExpressionE3E1)
        val goldExpressionEList = listOf(goldExpressionE0, goldExpressionE1, goldExpressionE2, goldExpressionE3)
        val goldExpression = BinaryOpExpression(goldExpressionEList, goldExpressionE0, goldExpressionE1, goldExpressionE3)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testExpressionParse033() {

    }

    @Test
    fun testExpressionParse034() {

    }

    @Test
    fun testExpressionParse035() {

    }

    @Test
    fun testExpressionParse036() {

    }

    @Test
    fun testExpressionParse037() {

    }

    @Test
    fun testExpressionParse038() {

    }

    @Test
    fun testExpressionParse039() {

    }

    @Test
    fun testExpressionParse040() {

    }
}