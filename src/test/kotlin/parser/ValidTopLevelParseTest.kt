package parser

import kotlin.test.*

class ValidTopLevelParseTest {
    @Test
    fun testTopLevelParse001() {
        val inString = """fn f() {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4 = starOf<ParameterAst>(listOf())
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(emptyList())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse002() {
        val inString = """fn f(x) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(emptyList())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse003() {
        val inString = """fn f(x: T) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("T")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(emptyList())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse004() {
        val inString = """fn f(x, y, z) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = CommaToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1Pa1 = CommaToken
        val goldExpressionF4Ps1Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa1, goldExpressionF4Ps1Pa2)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, null)
        val goldExpressionF4Ps2Pa0 = NameToken("z")
        val goldExpressionF4Ps2PaList = listOf(goldExpressionF4Ps2Pa0)
        val goldExpressionF4Ps2 = ParameterAst(goldExpressionF4Ps2PaList, goldExpressionF4Ps2Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1, goldExpressionF4Ps2))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(emptyList())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse005() {
        val inString = """fn f(x: T, y, z) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("T")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1Pa1 = CommaToken
        val goldExpressionF4Ps1Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa1, goldExpressionF4Ps1Pa2)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, null)
        val goldExpressionF4Ps2Pa0 = NameToken("z")
        val goldExpressionF4Ps2PaList = listOf(goldExpressionF4Ps2Pa0)
        val goldExpressionF4Ps2 = ParameterAst(goldExpressionF4Ps2PaList, goldExpressionF4Ps2Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1, goldExpressionF4Ps2))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(emptyList())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse006() {
        val inString = """fn f(x, y: T, z) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = CommaToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1Pa1 = ColonToken
        val goldExpressionF4Ps1Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps1Pa3 = TypeToken("T")
        val goldExpressionF4Ps1Pa4 = CommaToken
        val goldExpressionF4Ps1Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa1, goldExpressionF4Ps1Pa2, goldExpressionF4Ps1Pa3, goldExpressionF4Ps1Pa4, goldExpressionF4Ps1Pa5)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa3)
        val goldExpressionF4Ps2Pa0 = NameToken("z")
        val goldExpressionF4Ps2PaList = listOf(goldExpressionF4Ps2Pa0)
        val goldExpressionF4Ps2 = ParameterAst(goldExpressionF4Ps2PaList, goldExpressionF4Ps2Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1, goldExpressionF4Ps2))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(emptyList())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse007() {
        val inString = """fn f(x: S, y: T, z) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("S")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1Pa1 = ColonToken
        val goldExpressionF4Ps1Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps1Pa3 = TypeToken("T")
        val goldExpressionF4Ps1Pa4 = CommaToken
        val goldExpressionF4Ps1Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa1, goldExpressionF4Ps1Pa2, goldExpressionF4Ps1Pa3, goldExpressionF4Ps1Pa4, goldExpressionF4Ps1Pa5)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa3)
        val goldExpressionF4Ps2Pa0 = NameToken("z")
        val goldExpressionF4Ps2PaList = listOf(goldExpressionF4Ps2Pa0)
        val goldExpressionF4Ps2 = ParameterAst(goldExpressionF4Ps2PaList, goldExpressionF4Ps2Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1, goldExpressionF4Ps2))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(emptyList())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse008() {
        val inString = """fn f(x) {
                          |let x = y
                          |}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = NewLineToken
        val goldExpressionF9B0L0 = LetToken
        val goldExpressionF9B0L1 = WhitespaceToken(" ")
        val goldExpressionF9B0L2 = NameToken("x")
        val goldExpressionF9B0L3 = WhitespaceToken(" ")
        val goldExpressionF9B0L4 = EqualSignToken
        val goldExpressionF9B0L5 = WhitespaceToken(" ")
        val goldExpressionF9B0L6E0 = NameToken("y")
        val goldExpressionF9B0L6E1 = NewLineToken
        val goldExpressionF9B0L6EList = listOf(goldExpressionF9B0L6E0, goldExpressionF9B0L6E1)
        val goldExpressionF9B0L6 = AtomicExpression(goldExpressionF9B0L6EList, goldExpressionF9B0L6E0)
        val goldExpressionF9B0LList = listOf(goldExpressionF9B0L0, goldExpressionF9B0L1, goldExpressionF9B0L2, goldExpressionF9B0L3, goldExpressionF9B0L4, goldExpressionF9B0L5, goldExpressionF9B0L6)
        val goldExpressionF9B0 = LetAst(goldExpressionF9B0LList, goldExpressionF9B0L2, null, goldExpressionF9B0L6)
        val goldExpressionF9 = starOf<BodyAst>(listOf(goldExpressionF9B0))
        val goldExpressionF10 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9, goldExpressionF10)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF9)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse009() {
        val inString = """fn f(x) {
                         |return x
                         |}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = NewLineToken
        val goldExpressionF9B0R0 = ReturnToken
        val goldExpressionF9B0R1 = WhitespaceToken(" ")
        val goldExpressionF9B0R2E0 = NameToken("x")
        val goldExpressionF9B0R2E1 = NewLineToken
        val goldExpressionF9B0R2EList = listOf(goldExpressionF9B0R2E0, goldExpressionF9B0R2E1)
        val goldExpressionF9B0R2 = AtomicExpression(goldExpressionF9B0R2EList, goldExpressionF9B0R2E0)
        val goldExpressionF9B0RList = listOf(goldExpressionF9B0R0, goldExpressionF9B0R1, goldExpressionF9B0R2)
        val goldExpressionF9B0 = ReturnAst(goldExpressionF9B0RList, goldExpressionF9B0R2)
        val goldExpressionF9 = starOf<BodyAst>(listOf(goldExpressionF9B0))
        val goldExpressionF10 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9, goldExpressionF10)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF9)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse010() {
        val inString = """fn f(x) {
                         |if(x) {}
                         |}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = NewLineToken
        val goldExpressionF9B0I0 = IfToken
        val goldExpressionF9B0I1 = OpenParenToken
        val goldExpressionF9B0I2E0 = NameToken("x")
        val goldExpressionF9B0I2EList = listOf(goldExpressionF9B0I2E0)
        val goldExpressionF9B0I2 = AtomicExpression(goldExpressionF9B0I2EList, goldExpressionF9B0I2E0)
        val goldExpressionF9B0I3 = CloseParenToken
        val goldExpressionF9B0I4 = WhitespaceToken(" ")
        val goldExpressionF9B0I5 = OpenCurlyToken
        val goldExpressionF9B0I6 = starOf<BodyAst>(emptyList())
        val goldExpressionF9B0I7 = CloseCurlyToken
        val goldExpressionF9B0I8 = NewLineToken
        val goldExpressionF9B0IList = listOf(goldExpressionF9B0I0, goldExpressionF9B0I1, goldExpressionF9B0I2, goldExpressionF9B0I3, goldExpressionF9B0I4, goldExpressionF9B0I5, goldExpressionF9B0I6, goldExpressionF9B0I7, goldExpressionF9B0I8)
        val goldExpressionF9B0 = IfAst(goldExpressionF9B0IList, goldExpressionF9B0I2, goldExpressionF9B0I6)
        val goldExpressionF9 = starOf<BodyAst>(listOf(goldExpressionF9B0))
        val goldExpressionF10 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9, goldExpressionF10)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF9)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse011() {
        val inString = """fn""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionFList = listOf(goldExpressionF0)
        val goldExpression = FnAst(goldExpressionFList, null, null, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse012() {
        val inString = """fn f""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, null, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse013() {
        val inString = """fn f(""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4 = starOf<ParameterAst>(listOf())
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse014() {
        val inString = """fn f(x""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse015() {
        val inString = """fn f(x,""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = CommaToken
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse016() {
        val inString = """fn f(x:""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse017() {
        val inString = """fn f(x: T""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("T")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse018() {
        val inString = """fn f(x: T,""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("T")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0))
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse019() {
        val inString = """fn f(x: T, y""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("T")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, null)

        assertEquals(goldExpression, outExpression)

    }

    @Test
    fun testTopLevelParse020() {
        val inString = """fn f(x: T, y)""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("T")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, null)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse021() {
        val inString = """fn f(x: T, y) {""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("T")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(emptyList())
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse022() {
        val inString = """fn f(x: T, y) {
                 |let x = y""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("T")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, null)
        val goldExpressionF4 = starOf(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = NewLineToken
        val goldExpressionF9B0L0 = LetToken
        val goldExpressionF9B0L1 = WhitespaceToken(" ")
        val goldExpressionF9B0L2 = NameToken("x")
        val goldExpressionF9B0L3 = WhitespaceToken(" ")
        val goldExpressionF9B0L4 = EqualSignToken
        val goldExpressionF9B0L5 = WhitespaceToken(" ")
        val goldExpressionF9B0L6E0 = NameToken("y")
        val goldExpressionF9B0L6EList = listOf(goldExpressionF9B0L6E0)
        val goldExpressionF9B0L6 = AtomicExpression(goldExpressionF9B0L6EList, goldExpressionF9B0L6E0)
        val goldExpressionF9B0LList = listOf(goldExpressionF9B0L0, goldExpressionF9B0L1, goldExpressionF9B0L2, goldExpressionF9B0L3, goldExpressionF9B0L4, goldExpressionF9B0L5, goldExpressionF9B0L6)
        val goldExpressionF9B0 = LetAst(goldExpressionF9B0LList, goldExpressionF9B0L2, null, goldExpressionF9B0L6)
        val goldExpressionF9 = starOf<BodyAst>(listOf(goldExpressionF9B0))
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF9)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse023() {
        val inString = """fn f(x: t, y) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = NameToken("t")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, null)
        val goldExpressionF4 = starOf<ParameterAst>(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(listOf())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse024() {
        val inString = """fn f(x, y: t) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = CommaToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, null)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1Pa1 = ColonToken
        val goldExpressionF4Ps1Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps1Pa3 = NameToken("t")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa1, goldExpressionF4Ps1Pa2, goldExpressionF4Ps1Pa3)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa3)
        val goldExpressionF4 = starOf<ParameterAst>(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(listOf())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse025() {
        val inString = """fn f(x: s, y: t) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = NameToken("s")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1Pa1 = ColonToken
        val goldExpressionF4Ps1Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps1Pa3 = NameToken("t")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa1, goldExpressionF4Ps1Pa2, goldExpressionF4Ps1Pa3)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa3)
        val goldExpressionF4 = starOf<ParameterAst>(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(listOf())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse026() {
        val inString = """fn f(x: S, y: t) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = TypeToken("S")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1Pa1 = ColonToken
        val goldExpressionF4Ps1Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps1Pa3 = NameToken("t")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa1, goldExpressionF4Ps1Pa2, goldExpressionF4Ps1Pa3)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa3)
        val goldExpressionF4 = starOf<ParameterAst>(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(listOf())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse027() {
        val inString = """fn f(x: s, y: T) {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outExpression = parseFn(SequenceBuilder(inIterator))

        val goldExpressionF0 = FnToken
        val goldExpressionF1 = WhitespaceToken(" ")
        val goldExpressionF2 = NameToken("f")
        val goldExpressionF3 = OpenParenToken
        val goldExpressionF4Ps0Pa0 = NameToken("x")
        val goldExpressionF4Ps0Pa1 = ColonToken
        val goldExpressionF4Ps0Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps0Pa3 = NameToken("s")
        val goldExpressionF4Ps0Pa4 = CommaToken
        val goldExpressionF4Ps0Pa5 = WhitespaceToken(" ")
        val goldExpressionF4Ps0PaList = listOf(goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa1, goldExpressionF4Ps0Pa2, goldExpressionF4Ps0Pa3, goldExpressionF4Ps0Pa4, goldExpressionF4Ps0Pa5)
        val goldExpressionF4Ps0 = ParameterAst(goldExpressionF4Ps0PaList, goldExpressionF4Ps0Pa0, goldExpressionF4Ps0Pa3)
        val goldExpressionF4Ps1Pa0 = NameToken("y")
        val goldExpressionF4Ps1Pa1 = ColonToken
        val goldExpressionF4Ps1Pa2 = WhitespaceToken(" ")
        val goldExpressionF4Ps1Pa3 = TypeToken("T")
        val goldExpressionF4Ps1PaList = listOf(goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa1, goldExpressionF4Ps1Pa2, goldExpressionF4Ps1Pa3)
        val goldExpressionF4Ps1 = ParameterAst(goldExpressionF4Ps1PaList, goldExpressionF4Ps1Pa0, goldExpressionF4Ps1Pa3)
        val goldExpressionF4 = starOf<ParameterAst>(listOf(goldExpressionF4Ps0, goldExpressionF4Ps1))
        val goldExpressionF5 = CloseParenToken
        val goldExpressionF6 = WhitespaceToken(" ")
        val goldExpressionF7 = OpenCurlyToken
        val goldExpressionF8 = starOf<BodyAst>(listOf())
        val goldExpressionF9 = CloseCurlyToken
        val goldExpressionFList = listOf(goldExpressionF0, goldExpressionF1, goldExpressionF2, goldExpressionF3, goldExpressionF4, goldExpressionF5, goldExpressionF6, goldExpressionF7, goldExpressionF8, goldExpressionF9)
        val goldExpression = FnAst(goldExpressionFList, goldExpressionF2, goldExpressionF4, null, goldExpressionF8)

        assertEquals(goldExpression, outExpression)
    }

    @Test
    fun testTopLevelParse028() {
        val inString = """fn f(x): U32""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outFn = parseFn(SequenceBuilder(inIterator))

        val goldFnF0 = FnToken
        val goldFnF1 = WhitespaceToken(" ")
        val goldFnF2 = NameToken("f")
        val goldFnF3 = OpenParenToken
        val goldFnF4Ps0Pa0 = NameToken("x")
        val goldFnF4Ps0PaList = listOf(goldFnF4Ps0Pa0)
        val goldFnF4Ps0 = ParameterAst(goldFnF4Ps0PaList, goldFnF4Ps0Pa0, null)
        val goldFnF4 = starOf<ParameterAst>(listOf(goldFnF4Ps0))
        val goldFnF5 = CloseParenToken
        val goldFnF6 = ColonToken
        val goldFnF7 = WhitespaceToken(" ")
        val goldFnF8 = TypeToken("U32")
        val goldFnFList = listOf(goldFnF0, goldFnF1, goldFnF2, goldFnF3, goldFnF4, goldFnF5, goldFnF6, goldFnF7, goldFnF8)
        val goldFn = FnAst(goldFnFList, goldFnF2, goldFnF4, goldFnF8, null)

        assertEquals(goldFn, outFn)
    }

    @Test
    fun testTopLevelParse029() {
        val inString = """fn f(x): U32 {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outFn = parseFn(SequenceBuilder(inIterator))

        val goldFnF0 = FnToken
        val goldFnF1 = WhitespaceToken(" ")
        val goldFnF2 = NameToken("f")
        val goldFnF3 = OpenParenToken
        val goldFnF4Ps0Pa0 = NameToken("x")
        val goldFnF4Ps0PaList = listOf(goldFnF4Ps0Pa0)
        val goldFnF4Ps0 = ParameterAst(goldFnF4Ps0PaList, goldFnF4Ps0Pa0, null)
        val goldFnF4 = starOf<ParameterAst>(listOf(goldFnF4Ps0))
        val goldFnF5 = CloseParenToken
        val goldFnF6 = ColonToken
        val goldFnF7 = WhitespaceToken(" ")
        val goldFnF8 = TypeToken("U32")
        val goldFnF9 = WhitespaceToken(" ")
        val goldFnF10 = OpenCurlyToken
        val goldFnF11 = starOf<BodyAst>(listOf())
        val goldFnF12 = CloseCurlyToken
        val goldFnFList = listOf(goldFnF0, goldFnF1, goldFnF2, goldFnF3, goldFnF4, goldFnF5, goldFnF6, goldFnF7, goldFnF8, goldFnF9, goldFnF10, goldFnF11, goldFnF12)
        val goldFn = FnAst(goldFnFList, goldFnF2, goldFnF4, goldFnF8, goldFnF11)

        assertEquals(goldFn, outFn)
    }

    @Test
    fun testTopLevelParse030() {
        val inString = """fn f(x: U32): U32 {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outFn = parseFn(SequenceBuilder(inIterator))

        val goldFnF0 = FnToken
        val goldFnF1 = WhitespaceToken(" ")
        val goldFnF2 = NameToken("f")
        val goldFnF3 = OpenParenToken
        val goldFnF4Ps0Pa0 = NameToken("x")
        val goldFnF4Ps0Pa1 = ColonToken
        val goldFnF4Ps0Pa2 = WhitespaceToken(" ")
        val goldFnF4Ps0Pa3 = TypeToken("U32")
        val goldFnF4Ps0PaList = listOf(goldFnF4Ps0Pa0, goldFnF4Ps0Pa1, goldFnF4Ps0Pa2, goldFnF4Ps0Pa3)
        val goldFnF4Ps0 = ParameterAst(goldFnF4Ps0PaList, goldFnF4Ps0Pa0, goldFnF4Ps0Pa3)
        val goldFnF4 = starOf<ParameterAst>(listOf(goldFnF4Ps0))
        val goldFnF5 = CloseParenToken
        val goldFnF6 = ColonToken
        val goldFnF7 = WhitespaceToken(" ")
        val goldFnF8 = TypeToken("U32")
        val goldFnF9 = WhitespaceToken(" ")
        val goldFnF10 = OpenCurlyToken
        val goldFnF11 = starOf<BodyAst>(listOf())
        val goldFnF12 = CloseCurlyToken
        val goldFnFList = listOf(goldFnF0, goldFnF1, goldFnF2, goldFnF3, goldFnF4, goldFnF5, goldFnF6, goldFnF7, goldFnF8, goldFnF9, goldFnF10, goldFnF11, goldFnF12)
        val goldFn = FnAst(goldFnFList, goldFnF2, goldFnF4, goldFnF8, goldFnF11)

        assertEquals(goldFn, outFn)
    }

    @Test
    fun testTopLevelParse031() {
        val inString = """fn f(x: U32): t {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outFn = parseFn(SequenceBuilder(inIterator))

        val goldFnF0 = FnToken
        val goldFnF1 = WhitespaceToken(" ")
        val goldFnF2 = NameToken("f")
        val goldFnF3 = OpenParenToken
        val goldFnF4Ps0Pa0 = NameToken("x")
        val goldFnF4Ps0Pa1 = ColonToken
        val goldFnF4Ps0Pa2 = WhitespaceToken(" ")
        val goldFnF4Ps0Pa3 = TypeToken("U32")
        val goldFnF4Ps0PaList = listOf(goldFnF4Ps0Pa0, goldFnF4Ps0Pa1, goldFnF4Ps0Pa2, goldFnF4Ps0Pa3)
        val goldFnF4Ps0 = ParameterAst(goldFnF4Ps0PaList, goldFnF4Ps0Pa0, goldFnF4Ps0Pa3)
        val goldFnF4 = starOf<ParameterAst>(listOf(goldFnF4Ps0))
        val goldFnF5 = CloseParenToken
        val goldFnF6 = ColonToken
        val goldFnF7 = WhitespaceToken(" ")
        val goldFnF8 = NameToken("t")
        val goldFnF9 = WhitespaceToken(" ")
        val goldFnF10 = OpenCurlyToken
        val goldFnF11 = starOf<BodyAst>(listOf())
        val goldFnF12 = CloseCurlyToken
        val goldFnFList = listOf(goldFnF0, goldFnF1, goldFnF2, goldFnF3, goldFnF4, goldFnF5, goldFnF6, goldFnF7, goldFnF8, goldFnF9, goldFnF10, goldFnF11, goldFnF12)
        val goldFn = FnAst(goldFnFList, goldFnF2, goldFnF4, goldFnF8, goldFnF11)

        assertEquals(goldFn, outFn)
    }

    @Test
    fun testTopLevelParse032() {
        val inString = """fn f(x: s): t {}""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val outFn = parseFn(SequenceBuilder(inIterator))

        val goldFnF0 = FnToken
        val goldFnF1 = WhitespaceToken(" ")
        val goldFnF2 = NameToken("f")
        val goldFnF3 = OpenParenToken
        val goldFnF4Ps0Pa0 = NameToken("x")
        val goldFnF4Ps0Pa1 = ColonToken
        val goldFnF4Ps0Pa2 = WhitespaceToken(" ")
        val goldFnF4Ps0Pa3 = NameToken("s")
        val goldFnF4Ps0PaList = listOf(goldFnF4Ps0Pa0, goldFnF4Ps0Pa1, goldFnF4Ps0Pa2, goldFnF4Ps0Pa3)
        val goldFnF4Ps0 = ParameterAst(goldFnF4Ps0PaList, goldFnF4Ps0Pa0, goldFnF4Ps0Pa3)
        val goldFnF4 = starOf<ParameterAst>(listOf(goldFnF4Ps0))
        val goldFnF5 = CloseParenToken
        val goldFnF6 = ColonToken
        val goldFnF7 = WhitespaceToken(" ")
        val goldFnF8 = NameToken("t")
        val goldFnF9 = WhitespaceToken(" ")
        val goldFnF10 = OpenCurlyToken
        val goldFnF11 = starOf<BodyAst>(listOf())
        val goldFnF12 = CloseCurlyToken
        val goldFnFList = listOf(goldFnF0, goldFnF1, goldFnF2, goldFnF3, goldFnF4, goldFnF5, goldFnF6, goldFnF7, goldFnF8, goldFnF9, goldFnF10, goldFnF11, goldFnF12)
        val goldFn = FnAst(goldFnFList, goldFnF2, goldFnF4, goldFnF8, goldFnF11)

        assertEquals(goldFn, outFn)
    }

    @Test
    fun testTopLevelParse033() {

    }

    @Test
    fun testTopLevelParse034() {

    }

    @Test
    fun testTopLevelParse035() {

    }

    @Test
    fun testTopLevelParse036() {

    }

    @Test
    fun testTopLevelParse037() {

    }

    @Test
    fun testTopLevelParse038() {

    }

    @Test
    fun testTopLevelParse039() {

    }

    @Test
    fun testTopLevelParse040() {

    }
}