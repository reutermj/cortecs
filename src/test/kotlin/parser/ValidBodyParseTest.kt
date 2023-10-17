package parser

import kotlin.test.*

class ValidBodyParseTest {
    @Test
    fun parseBody001() {
        val inString = """return x"""
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseReturn(SequenceBuilder(inIterator))

        val goldExpressionR0 = ReturnToken
        val goldExpressionR1 = WhitespaceToken(" ")
        val goldExpressionR2E0 = NameToken("x")
        val goldExpressionR2EList = listOf(goldExpressionR2E0)
        val goldExpressionR2 = AtomicExpression(goldExpressionR2EList, goldExpressionR2E0)
        val goldExpressionRList = listOf(goldExpressionR0, goldExpressionR1, goldExpressionR2)
        val goldExpression = ReturnAst(goldExpressionRList, goldExpressionR2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody002() {
        val inString = """return x + y""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseReturn(SequenceBuilder(inIterator))

        val goldExpressionR0 = ReturnToken
        val goldExpressionR1 = WhitespaceToken(" ")
        val goldExpressionR2E0E0 = NameToken("x")
        val goldExpressionR2E0E1 = WhitespaceToken(" ")
        val goldExpressionR2E0EList = listOf(goldExpressionR2E0E0, goldExpressionR2E0E1)
        val goldExpressionR2E0 = AtomicExpression(goldExpressionR2E0EList, goldExpressionR2E0E0)
        val goldExpressionR2E1 = OperatorToken("+")
        val goldExpressionR2E2 = WhitespaceToken(" ")
        val goldExpressionR2E3E0 = NameToken("y")
        val goldExpressionR2E3EList = listOf(goldExpressionR2E3E0)
        val goldExpressionR2E3 = AtomicExpression(goldExpressionR2E3EList, goldExpressionR2E3E0)
        val goldExpressionR2EList = listOf(goldExpressionR2E0, goldExpressionR2E1, goldExpressionR2E2, goldExpressionR2E3)
        val goldExpressionR2 = BinaryExpression(goldExpressionR2EList, goldExpressionR2E0, goldExpressionR2E1, goldExpressionR2E3)
        val goldExpressionRList = listOf(goldExpressionR0, goldExpressionR1, goldExpressionR2)
        val goldExpression = ReturnAst(goldExpressionRList, goldExpressionR2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody003() {
        val inString = """return +x""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseReturn(SequenceBuilder(inIterator))

        val goldExpressionR0 = ReturnToken
        val goldExpressionR1 = WhitespaceToken(" ")
        val goldExpressionR2E0 = OperatorToken("+")
        val goldExpressionR2E1E0 = NameToken("x")
        val goldExpressionR2E1EList = listOf(goldExpressionR2E1E0)
        val goldExpressionR2E1 = AtomicExpression(goldExpressionR2E1EList, goldExpressionR2E1E0)
        val goldExpressionR2EList = listOf(goldExpressionR2E0, goldExpressionR2E1)
        val goldExpressionR2 = UnaryExpression(goldExpressionR2EList, goldExpressionR2E0, goldExpressionR2E1)
        val goldExpressionRList = listOf(goldExpressionR0, goldExpressionR1, goldExpressionR2)
        val goldExpression = ReturnAst(goldExpressionRList, goldExpressionR2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody004() {
        val inString = """return (x)""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseReturn(SequenceBuilder(inIterator))

        val goldExpressionR0 = ReturnToken
        val goldExpressionR1 = WhitespaceToken(" ")
        val goldExpressionR2E0 = OpenParenToken
        val goldExpressionR2E1E0 = NameToken("x")
        val goldExpressionR2E1EList = listOf(goldExpressionR2E1E0)
        val goldExpressionR2E1 = AtomicExpression(goldExpressionR2E1EList, goldExpressionR2E1E0)
        val goldExpressionR2E2 = CloseParenToken
        val goldExpressionR2EList = listOf(goldExpressionR2E0, goldExpressionR2E1, goldExpressionR2E2)
        val goldExpressionR2 = GroupingExpression(goldExpressionR2EList, goldExpressionR2E1)
        val goldExpressionRList = listOf(goldExpressionR0, goldExpressionR1, goldExpressionR2)
        val goldExpression = ReturnAst(goldExpressionRList, goldExpressionR2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody005() {
        val inString = """return f(x)""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseReturn(SequenceBuilder(inIterator))

        val goldExpressionR0 = ReturnToken
        val goldExpressionR1 = WhitespaceToken(" ")
        val goldExpressionR2E0E0 = NameToken("f")
        val goldExpressionR2E0EList = listOf(goldExpressionR2E0E0)
        val goldExpressionR2E0 = AtomicExpression(goldExpressionR2E0EList, goldExpressionR2E0E0)
        val goldExpressionR2E1 = OpenParenToken
        val goldExpressionR2E2As0A0E0 = NameToken("x")
        val goldExpressionR2E2As0A0EList = listOf(goldExpressionR2E2As0A0E0)
        val goldExpressionR2E2As0A0 = AtomicExpression(goldExpressionR2E2As0A0EList, goldExpressionR2E2As0A0E0)
        val goldExpressionR2E2As0AList = listOf(goldExpressionR2E2As0A0)
        val goldExpressionR2E2As0 = Argument(goldExpressionR2E2As0AList, goldExpressionR2E2As0A0)
        val goldExpressionR2E2 = starOf(listOf(goldExpressionR2E2As0))
        val goldExpressionR2E3 = CloseParenToken
        val goldExpressionR2EList = listOf(goldExpressionR2E0, goldExpressionR2E1, goldExpressionR2E2, goldExpressionR2E3)
        val goldExpressionR2 = FnCallExpression(goldExpressionR2EList, goldExpressionR2E0, goldExpressionR2E2)
        val goldExpressionRList = listOf(goldExpressionR0, goldExpressionR1, goldExpressionR2)
        val goldExpression = ReturnAst(goldExpressionRList, goldExpressionR2)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody006() {
        val inString = """return""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseReturn(SequenceBuilder(inIterator))

        val goldExpressionR0 = ReturnToken
        val goldExpressionRList = listOf(goldExpressionR0)
        val goldExpression = ReturnAst(goldExpressionRList, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody007() {
        val inString = """let x = y""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseLet(SequenceBuilder(inIterator))

        val goldExpressionL0 = LetToken
        val goldExpressionL1 = WhitespaceToken(" ")
        val goldExpressionL2 = NameToken("x")
        val goldExpressionL3 = WhitespaceToken(" ")
        val goldExpressionL4 = EqualSignToken
        val goldExpressionL5 = WhitespaceToken(" ")
        val goldExpressionL6E0 = NameToken("y")
        val goldExpressionL6EList = listOf(goldExpressionL6E0)
        val goldExpressionL6 = AtomicExpression(goldExpressionL6EList, goldExpressionL6E0)
        val goldExpressionLList = listOf(goldExpressionL0, goldExpressionL1, goldExpressionL2, goldExpressionL3, goldExpressionL4, goldExpressionL5, goldExpressionL6)
        val goldExpression = LetAst(goldExpressionLList, goldExpressionL2, null, goldExpressionL6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody008() {
        val inString = """let x = y + z""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseLet(SequenceBuilder(inIterator))

        val goldExpressionL0 = LetToken
        val goldExpressionL1 = WhitespaceToken(" ")
        val goldExpressionL2 = NameToken("x")
        val goldExpressionL3 = WhitespaceToken(" ")
        val goldExpressionL4 = EqualSignToken
        val goldExpressionL5 = WhitespaceToken(" ")
        val goldExpressionL6E0E0 = NameToken("y")
        val goldExpressionL6E0E1 = WhitespaceToken(" ")
        val goldExpressionL6E0EList = listOf(goldExpressionL6E0E0, goldExpressionL6E0E1)
        val goldExpressionL6E0 = AtomicExpression(goldExpressionL6E0EList, goldExpressionL6E0E0)
        val goldExpressionL6E1 = OperatorToken("+")
        val goldExpressionL6E2 = WhitespaceToken(" ")
        val goldExpressionL6E3E0 = NameToken("z")
        val goldExpressionL6E3EList = listOf(goldExpressionL6E3E0)
        val goldExpressionL6E3 = AtomicExpression(goldExpressionL6E3EList, goldExpressionL6E3E0)
        val goldExpressionL6EList = listOf(goldExpressionL6E0, goldExpressionL6E1, goldExpressionL6E2, goldExpressionL6E3)
        val goldExpressionL6 = BinaryExpression(goldExpressionL6EList, goldExpressionL6E0, goldExpressionL6E1, goldExpressionL6E3)
        val goldExpressionLList = listOf(goldExpressionL0, goldExpressionL1, goldExpressionL2, goldExpressionL3, goldExpressionL4, goldExpressionL5, goldExpressionL6)
        val goldExpression = LetAst(goldExpressionLList, goldExpressionL2, null, goldExpressionL6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody009() {
        val inString = """let x = +y""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseLet(SequenceBuilder(inIterator))

        val goldExpressionL0 = LetToken
        val goldExpressionL1 = WhitespaceToken(" ")
        val goldExpressionL2 = NameToken("x")
        val goldExpressionL3 = WhitespaceToken(" ")
        val goldExpressionL4 = EqualSignToken
        val goldExpressionL5 = WhitespaceToken(" ")
        val goldExpressionL6E0 = OperatorToken("+")
        val goldExpressionL6E1E0 = NameToken("y")
        val goldExpressionL6E1EList = listOf(goldExpressionL6E1E0)
        val goldExpressionL6E1 = AtomicExpression(goldExpressionL6E1EList, goldExpressionL6E1E0)
        val goldExpressionL6EList = listOf(goldExpressionL6E0, goldExpressionL6E1)
        val goldExpressionL6 = UnaryExpression(goldExpressionL6EList, goldExpressionL6E0, goldExpressionL6E1)
        val goldExpressionLList = listOf(goldExpressionL0, goldExpressionL1, goldExpressionL2, goldExpressionL3, goldExpressionL4, goldExpressionL5, goldExpressionL6)
        val goldExpression = LetAst(goldExpressionLList, goldExpressionL2, null, goldExpressionL6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody010() {
        val inString = """let x = (y)""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseLet(SequenceBuilder(inIterator))

        val goldExpressionL0 = LetToken
        val goldExpressionL1 = WhitespaceToken(" ")
        val goldExpressionL2 = NameToken("x")
        val goldExpressionL3 = WhitespaceToken(" ")
        val goldExpressionL4 = EqualSignToken
        val goldExpressionL5 = WhitespaceToken(" ")
        val goldExpressionL6E0 = OpenParenToken
        val goldExpressionL6E1E0 = NameToken("y")
        val goldExpressionL6E1EList = listOf(goldExpressionL6E1E0)
        val goldExpressionL6E1 = AtomicExpression(goldExpressionL6E1EList, goldExpressionL6E1E0)
        val goldExpressionL6E2 = CloseParenToken
        val goldExpressionL6EList = listOf(goldExpressionL6E0, goldExpressionL6E1, goldExpressionL6E2)
        val goldExpressionL6 = GroupingExpression(goldExpressionL6EList, goldExpressionL6E1)
        val goldExpressionLList = listOf(goldExpressionL0, goldExpressionL1, goldExpressionL2, goldExpressionL3, goldExpressionL4, goldExpressionL5, goldExpressionL6)
        val goldExpression = LetAst(goldExpressionLList, goldExpressionL2, null, goldExpressionL6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody011() {
        val inString = """let x = f(y)""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseLet(SequenceBuilder(inIterator))

        val goldExpressionL0 = LetToken
        val goldExpressionL1 = WhitespaceToken(" ")
        val goldExpressionL2 = NameToken("x")
        val goldExpressionL3 = WhitespaceToken(" ")
        val goldExpressionL4 = EqualSignToken
        val goldExpressionL5 = WhitespaceToken(" ")
        val goldExpressionL6E0E0 = NameToken("f")
        val goldExpressionL6E0EList = listOf(goldExpressionL6E0E0)
        val goldExpressionL6E0 = AtomicExpression(goldExpressionL6E0EList, goldExpressionL6E0E0)
        val goldExpressionL6E1 = OpenParenToken
        val goldExpressionL6E2As0A0E0 = NameToken("y")
        val goldExpressionL6E2As0A0EList = listOf(goldExpressionL6E2As0A0E0)
        val goldExpressionL6E2As0A0 = AtomicExpression(goldExpressionL6E2As0A0EList, goldExpressionL6E2As0A0E0)
        val goldExpressionL6E2As0AList = listOf(goldExpressionL6E2As0A0)
        val goldExpressionL6E2As0 = Argument(goldExpressionL6E2As0AList, goldExpressionL6E2As0A0)
        val goldExpressionL6E2 = starOf(listOf(goldExpressionL6E2As0))
        val goldExpressionL6E3 = CloseParenToken
        val goldExpressionL6EList = listOf(goldExpressionL6E0, goldExpressionL6E1, goldExpressionL6E2, goldExpressionL6E3)
        val goldExpressionL6 = FnCallExpression(goldExpressionL6EList, goldExpressionL6E0, goldExpressionL6E2)
        val goldExpressionLList = listOf(goldExpressionL0, goldExpressionL1, goldExpressionL2, goldExpressionL3, goldExpressionL4, goldExpressionL5, goldExpressionL6)
        val goldExpression = LetAst(goldExpressionLList, goldExpressionL2, null, goldExpressionL6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody012() {
        val inString = """let""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseLet(SequenceBuilder(inIterator))

        val goldExpressionL0 = LetToken
        val goldExpressionLList = listOf(goldExpressionL0)
        val goldExpression = LetAst(goldExpressionLList, null, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody013() {
        val inString = """let x""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseLet(SequenceBuilder(inIterator))

        val goldExpressionL0 = LetToken
        val goldExpressionL1 = WhitespaceToken(" ")
        val goldExpressionL2 = NameToken("x")
        val goldExpressionLList = listOf(goldExpressionL0, goldExpressionL1, goldExpressionL2)
        val goldExpression = LetAst(goldExpressionLList, goldExpressionL2, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody014() {
        val inString = """let x =""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseLet(SequenceBuilder(inIterator))

        val goldExpressionL0 = LetToken
        val goldExpressionL1 = WhitespaceToken(" ")
        val goldExpressionL2 = NameToken("x")
        val goldExpressionL3 = WhitespaceToken(" ")
        val goldExpressionL4 = EqualSignToken
        val goldExpressionLList = listOf(goldExpressionL0, goldExpressionL1, goldExpressionL2, goldExpressionL3, goldExpressionL4)
        val goldExpression = LetAst(goldExpressionLList, goldExpressionL2, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody015() {
        val inString = """if(x) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = NameToken("x")
        val goldExpressionI2EList = listOf(goldExpressionI2E0)
        val goldExpressionI2 = AtomicExpression(goldExpressionI2EList, goldExpressionI2E0)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = starOf<BodyAst>(listOf())
        val goldExpressionI7 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody016() {
        val inString = """if(x + y) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0E0 = NameToken("x")
        val goldExpressionI2E0E1 = WhitespaceToken(" ")
        val goldExpressionI2E0EList = listOf(goldExpressionI2E0E0, goldExpressionI2E0E1)
        val goldExpressionI2E0 = AtomicExpression(goldExpressionI2E0EList, goldExpressionI2E0E0)
        val goldExpressionI2E1 = OperatorToken("+")
        val goldExpressionI2E2 = WhitespaceToken(" ")
        val goldExpressionI2E3E0 = NameToken("y")
        val goldExpressionI2E3EList = listOf(goldExpressionI2E3E0)
        val goldExpressionI2E3 = AtomicExpression(goldExpressionI2E3EList, goldExpressionI2E3E0)
        val goldExpressionI2EList = listOf(goldExpressionI2E0, goldExpressionI2E1, goldExpressionI2E2, goldExpressionI2E3)
        val goldExpressionI2 = BinaryExpression(goldExpressionI2EList, goldExpressionI2E0, goldExpressionI2E1, goldExpressionI2E3)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = starOf<BodyAst>(listOf())
        val goldExpressionI7 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody017() {
        val inString = """if(+x) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = OperatorToken("+")
        val goldExpressionI2E1E0 = NameToken("x")
        val goldExpressionI2E1EList = listOf(goldExpressionI2E1E0)
        val goldExpressionI2E1 = AtomicExpression(goldExpressionI2E1EList, goldExpressionI2E1E0)
        val goldExpressionI2EList = listOf(goldExpressionI2E0, goldExpressionI2E1)
        val goldExpressionI2 = UnaryExpression(goldExpressionI2EList, goldExpressionI2E0, goldExpressionI2E1)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = starOf<BodyAst>(listOf())
        val goldExpressionI7 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody018() {
        val inString = """if((x)) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = OpenParenToken
        val goldExpressionI2E1E0 = NameToken("x")
        val goldExpressionI2E1EList = listOf(goldExpressionI2E1E0)
        val goldExpressionI2E1 = AtomicExpression(goldExpressionI2E1EList, goldExpressionI2E1E0)
        val goldExpressionI2E2 = CloseParenToken
        val goldExpressionI2EList = listOf(goldExpressionI2E0, goldExpressionI2E1, goldExpressionI2E2)
        val goldExpressionI2 = GroupingExpression(goldExpressionI2EList, goldExpressionI2E1)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = starOf<BodyAst>(listOf())
        val goldExpressionI7 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody019() {
        val inString = """if(f(x)) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0E0 = NameToken("f")
        val goldExpressionI2E0EList = listOf(goldExpressionI2E0E0)
        val goldExpressionI2E0 = AtomicExpression(goldExpressionI2E0EList, goldExpressionI2E0E0)
        val goldExpressionI2E1 = OpenParenToken
        val goldExpressionI2E2As0A0E0 = NameToken("x")
        val goldExpressionI2E2As0A0EList = listOf(goldExpressionI2E2As0A0E0)
        val goldExpressionI2E2As0A0 = AtomicExpression(goldExpressionI2E2As0A0EList, goldExpressionI2E2As0A0E0)
        val goldExpressionI2E2As0AList = listOf(goldExpressionI2E2As0A0)
        val goldExpressionI2E2As0 = Argument(goldExpressionI2E2As0AList, goldExpressionI2E2As0A0)
        val goldExpressionI2E2 = starOf(listOf(goldExpressionI2E2As0))
        val goldExpressionI2E3 = CloseParenToken
        val goldExpressionI2EList = listOf(goldExpressionI2E0, goldExpressionI2E1, goldExpressionI2E2, goldExpressionI2E3)
        val goldExpressionI2 = FnCallExpression(goldExpressionI2EList, goldExpressionI2E0, goldExpressionI2E2)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = starOf<BodyAst>(listOf())
        val goldExpressionI7 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody020() {
        val inString = """if(x) {
                         |let x = y
                         |}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = NameToken("x")
        val goldExpressionI2EList = listOf(goldExpressionI2E0)
        val goldExpressionI2 = AtomicExpression(goldExpressionI2EList, goldExpressionI2E0)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = NewLineToken
        val goldExpressionI7B0L0 = LetToken
        val goldExpressionI7B0L1 = WhitespaceToken(" ")
        val goldExpressionI7B0L2 = NameToken("x")
        val goldExpressionI7B0L3 = WhitespaceToken(" ")
        val goldExpressionI7B0L4 = EqualSignToken
        val goldExpressionI7B0L5 = WhitespaceToken(" ")
        val goldExpressionI7B0L6E0 = NameToken("y")
        val goldExpressionI7B0L6E1 = NewLineToken
        val goldExpressionI7B0L6EList = listOf(goldExpressionI7B0L6E0, goldExpressionI7B0L6E1)
        val goldExpressionI7B0L6 = AtomicExpression(goldExpressionI7B0L6EList, goldExpressionI7B0L6E0)
        val goldExpressionI7B0LList = listOf(goldExpressionI7B0L0, goldExpressionI7B0L1, goldExpressionI7B0L2, goldExpressionI7B0L3, goldExpressionI7B0L4, goldExpressionI7B0L5, goldExpressionI7B0L6)
        val goldExpressionI7B0 = LetAst(goldExpressionI7B0LList, goldExpressionI7B0L2, null, goldExpressionI7B0L6)
        val goldExpressionI7 = starOf<BodyAst>(listOf(goldExpressionI7B0))
        val goldExpressionI8 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7, goldExpressionI8)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI7)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody021() {
        val inString = """if(x) {
                         |return x
                         |}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = NameToken("x")
        val goldExpressionI2EList = listOf(goldExpressionI2E0)
        val goldExpressionI2 = AtomicExpression(goldExpressionI2EList, goldExpressionI2E0)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = NewLineToken
        val goldExpressionI7B0R0 = ReturnToken
        val goldExpressionI7B0R1 = WhitespaceToken(" ")
        val goldExpressionI7B0R2E0 = NameToken("x")
        val goldExpressionI7B0R2E1 = NewLineToken
        val goldExpressionI7B0R2EList = listOf(goldExpressionI7B0R2E0, goldExpressionI7B0R2E1)
        val goldExpressionI7B0R2 = AtomicExpression(goldExpressionI7B0R2EList, goldExpressionI7B0R2E0)
        val goldExpressionI7B0RList = listOf(goldExpressionI7B0R0, goldExpressionI7B0R1, goldExpressionI7B0R2)
        val goldExpressionI7B0 = ReturnAst(goldExpressionI7B0RList, goldExpressionI7B0R2)
        val goldExpressionI7 = starOf<BodyAst>(listOf(goldExpressionI7B0))
        val goldExpressionI8 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7, goldExpressionI8)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI7)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody022() {
        val inString = """if(x) {
                         |if(y) {}
                         |}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = NameToken("x")
        val goldExpressionI2EList = listOf(goldExpressionI2E0)
        val goldExpressionI2 = AtomicExpression(goldExpressionI2EList, goldExpressionI2E0)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = NewLineToken
        val goldExpressionI7B0I0 = IfToken
        val goldExpressionI7B0I1 = OpenParenToken
        val goldExpressionI7B0I2E0 = NameToken("y")
        val goldExpressionI7B0I2EList = listOf(goldExpressionI7B0I2E0)
        val goldExpressionI7B0I2 = AtomicExpression(goldExpressionI7B0I2EList, goldExpressionI7B0I2E0)
        val goldExpressionI7B0I3 = CloseParenToken
        val goldExpressionI7B0I4 = WhitespaceToken(" ")
        val goldExpressionI7B0I5 = OpenCurlyToken
        val goldExpressionI7B0I6 = starOf<BodyAst>(listOf())
        val goldExpressionI7B0I7 = CloseCurlyToken
        val goldExpressionI7B0I8 = NewLineToken
        val goldExpressionI7B0IList = listOf(goldExpressionI7B0I0, goldExpressionI7B0I1, goldExpressionI7B0I2, goldExpressionI7B0I3, goldExpressionI7B0I4, goldExpressionI7B0I5, goldExpressionI7B0I6, goldExpressionI7B0I7, goldExpressionI7B0I8)
        val goldExpressionI7B0 = IfAst(goldExpressionI7B0IList, goldExpressionI7B0I2, goldExpressionI7B0I6)
        val goldExpressionI7 = starOf<BodyAst>(listOf(goldExpressionI7B0))
        val goldExpressionI8 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7, goldExpressionI8)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI7)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody023() {
        val inString = """if(x) {
                         |if(y) {
                         |let z = x
                         |}
                         |return y
                         |}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = NameToken("x")
        val goldExpressionI2EList = listOf(goldExpressionI2E0)
        val goldExpressionI2 = AtomicExpression(goldExpressionI2EList, goldExpressionI2E0)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = NewLineToken
        val goldExpressionI7B0I0 = IfToken
        val goldExpressionI7B0I1 = OpenParenToken
        val goldExpressionI7B0I2E0 = NameToken("y")
        val goldExpressionI7B0I2EList = listOf(goldExpressionI7B0I2E0)
        val goldExpressionI7B0I2 = AtomicExpression(goldExpressionI7B0I2EList, goldExpressionI7B0I2E0)
        val goldExpressionI7B0I3 = CloseParenToken
        val goldExpressionI7B0I4 = WhitespaceToken(" ")
        val goldExpressionI7B0I5 = OpenCurlyToken
        val goldExpressionI7B0I6 = NewLineToken
        val goldExpressionI7B0I7B0L0 = LetToken
        val goldExpressionI7B0I7B0L1 = WhitespaceToken(" ")
        val goldExpressionI7B0I7B0L2 = NameToken("z")
        val goldExpressionI7B0I7B0L3 = WhitespaceToken(" ")
        val goldExpressionI7B0I7B0L4 = EqualSignToken
        val goldExpressionI7B0I7B0L5 = WhitespaceToken(" ")
        val goldExpressionI7B0I7B0L6E0 = NameToken("x")
        val goldExpressionI7B0I7B0L6E1 = NewLineToken
        val goldExpressionI7B0I7B0L6EList = listOf(goldExpressionI7B0I7B0L6E0, goldExpressionI7B0I7B0L6E1)
        val goldExpressionI7B0I7B0L6 = AtomicExpression(goldExpressionI7B0I7B0L6EList, goldExpressionI7B0I7B0L6E0)
        val goldExpressionI7B0I7B0LList = listOf(goldExpressionI7B0I7B0L0, goldExpressionI7B0I7B0L1, goldExpressionI7B0I7B0L2, goldExpressionI7B0I7B0L3, goldExpressionI7B0I7B0L4, goldExpressionI7B0I7B0L5, goldExpressionI7B0I7B0L6)
        val goldExpressionI7B0I7B0 = LetAst(goldExpressionI7B0I7B0LList, goldExpressionI7B0I7B0L2, null, goldExpressionI7B0I7B0L6)
        val goldExpressionI7B0I7 = starOf<BodyAst>(listOf(goldExpressionI7B0I7B0))
        val goldExpressionI7B0I8 = CloseCurlyToken
        val goldExpressionI7B0I9 = NewLineToken
        val goldExpressionI7B0IList = listOf(goldExpressionI7B0I0, goldExpressionI7B0I1, goldExpressionI7B0I2, goldExpressionI7B0I3, goldExpressionI7B0I4, goldExpressionI7B0I5, goldExpressionI7B0I6, goldExpressionI7B0I7, goldExpressionI7B0I8, goldExpressionI7B0I9)
        val goldExpressionI7B0 = IfAst(goldExpressionI7B0IList, goldExpressionI7B0I2, goldExpressionI7B0I7)
        val goldExpressionI7B1R0 = ReturnToken
        val goldExpressionI7B1R1 = WhitespaceToken(" ")
        val goldExpressionI7B1R2E0 = NameToken("y")
        val goldExpressionI7B1R2E1 = NewLineToken
        val goldExpressionI7B1R2EList = listOf(goldExpressionI7B1R2E0, goldExpressionI7B1R2E1)
        val goldExpressionI7B1R2 = AtomicExpression(goldExpressionI7B1R2EList, goldExpressionI7B1R2E0)
        val goldExpressionI7B1RList = listOf(goldExpressionI7B1R0, goldExpressionI7B1R1, goldExpressionI7B1R2)
        val goldExpressionI7B1 = ReturnAst(goldExpressionI7B1RList, goldExpressionI7B1R2)
        val goldExpressionI7 = starOf<BodyAst>(listOf(goldExpressionI7B0, goldExpressionI7B1))
        val goldExpressionI8 = CloseCurlyToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6, goldExpressionI7, goldExpressionI8)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI7)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody024() {
        val inString = """if""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionIList = listOf(goldExpressionI0)
        val goldExpression = IfAst(goldExpressionIList, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody025() {
        val inString = """if(""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1)
        val goldExpression = IfAst(goldExpressionIList, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody026() {
        val inString = """if(x""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = NameToken("x")
        val goldExpressionI2EList = listOf(goldExpressionI2E0)
        val goldExpressionI2 = AtomicExpression(goldExpressionI2EList, goldExpressionI2E0)
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody027() {
        val inString = """if(x)""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = NameToken("x")
        val goldExpressionI2EList = listOf(goldExpressionI2E0)
        val goldExpressionI2 = AtomicExpression(goldExpressionI2EList, goldExpressionI2E0)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody028() {
        val inString = """if(x) {""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseIf(SequenceBuilder(inIterator))

        val goldExpressionI0 = IfToken
        val goldExpressionI1 = OpenParenToken
        val goldExpressionI2E0 = NameToken("x")
        val goldExpressionI2EList = listOf(goldExpressionI2E0)
        val goldExpressionI2 = AtomicExpression(goldExpressionI2EList, goldExpressionI2E0)
        val goldExpressionI3 = CloseParenToken
        val goldExpressionI4 = WhitespaceToken(" ")
        val goldExpressionI5 = OpenCurlyToken
        val goldExpressionI6 = starOf<BodyAst>(listOf())
        val goldExpressionIList = listOf(goldExpressionI0, goldExpressionI1, goldExpressionI2, goldExpressionI3, goldExpressionI4, goldExpressionI5, goldExpressionI6)
        val goldExpression = IfAst(goldExpressionIList, goldExpressionI2, goldExpressionI6)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun parseBody029() {
        val inString = """let x: T = y""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outLet = parseLet(SequenceBuilder(inIterator))

        val goldLetL0 = LetToken
        val goldLetL1 = WhitespaceToken(" ")
        val goldLetL2 = NameToken("x")
        val goldLetL3 = ColonToken
        val goldLetL4 = WhitespaceToken(" ")
        val goldLetL5 = TypeToken("T")
        val goldLetL6 = WhitespaceToken(" ")
        val goldLetL7 = EqualSignToken
        val goldLetL8 = WhitespaceToken(" ")
        val goldLetL9E0 = NameToken("y")
        val goldLetL9EList = listOf(goldLetL9E0)
        val goldLetL9 = AtomicExpression(goldLetL9EList, goldLetL9E0)
        val goldLetLList = listOf(goldLetL0, goldLetL1, goldLetL2, goldLetL3, goldLetL4, goldLetL5, goldLetL6, goldLetL7, goldLetL8, goldLetL9)
        val goldLet = LetAst(goldLetLList, goldLetL2, goldLetL5, goldLetL9)

        assertEquals(goldLet, outLet)
    }

    @Test
    fun parseBody030() {
        val inString = """let x: t = y""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outLet = parseLet(SequenceBuilder(inIterator))

        val goldLetL0 = LetToken
        val goldLetL1 = WhitespaceToken(" ")
        val goldLetL2 = NameToken("x")
        val goldLetL3 = ColonToken
        val goldLetL4 = WhitespaceToken(" ")
        val goldLetL5 = NameToken("t")
        val goldLetL6 = WhitespaceToken(" ")
        val goldLetL7 = EqualSignToken
        val goldLetL8 = WhitespaceToken(" ")
        val goldLetL9E0 = NameToken("y")
        val goldLetL9EList = listOf(goldLetL9E0)
        val goldLetL9 = AtomicExpression(goldLetL9EList, goldLetL9E0)
        val goldLetLList = listOf(goldLetL0, goldLetL1, goldLetL2, goldLetL3, goldLetL4, goldLetL5, goldLetL6, goldLetL7, goldLetL8, goldLetL9)
        val goldLet = LetAst(goldLetLList, goldLetL2, goldLetL5, goldLetL9)

        assertEquals(goldLet, outLet)
    }

    @Test
    fun parseBody031() {

    }

    @Test
    fun parseBody032() {

    }

    @Test
    fun parseBody033() {

    }

    @Test
    fun parseBody034() {

    }

    @Test
    fun parseBody035() {

    }

    @Test
    fun parseBody036() {

    }

    @Test
    fun parseBody037() {

    }

    @Test
    fun parseBody038() {

    }

    @Test
    fun parseBody039() {

    }

    @Test
    fun parseBody040() {

    }
}