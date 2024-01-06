package parser

import kotlin.test.*

class TokenReparseTests {
    @Test
    fun tokenReparse001() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 5)
        val end = Span(0, 9)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a12349"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse002() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 5)
        val end = Span(1, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse003() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 5)
        val end = Span(0, 10)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse004() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 5)
        val end = Span(0, 15)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse005() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 5)
        val end = Span(1, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse006() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 0)
        val end = Span(0, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "56789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse007() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 0)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse008() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, -5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse009() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(-1, 0)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse010() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(-1, 5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse011() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(-1, -5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse012() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 10)
        val end = Span(0, 10)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse013() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 10)
        val end = Span(0, 15)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse014() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 10)
        val end = Span(1, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse015() {
        val inToken = NameToken("a123456789")
        val change = ""
        val start = Span(0, 10)
        val end = Span(1, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse016() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 5)
        val end = Span(0, 9)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234b9"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse017() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 5)
        val end = Span(1, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234b"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse018() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 5)
        val end = Span(0, 10)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234b"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse019() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 5)
        val end = Span(0, 15)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234b"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse020() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 5)
        val end = Span(1, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a1234b"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse021() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 0)
        val end = Span(0, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "56789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse022() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 0)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse023() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, -5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse024() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(-1, 0)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse025() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(-1, 5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse026() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(-1, -5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse027() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 10)
        val end = Span(0, 10)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789b"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse028() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 10)
        val end = Span(0, 15)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789b"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse029() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 10)
        val end = Span(1, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789b"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse030() {
        val inToken = NameToken("a123456789")
        val change = "b"
        val start = Span(0, 10)
        val end = Span(1, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "a123456789b"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse031() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(0, 0)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse032() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(1, 0)
        val end = Span(1, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse033() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(1, 0)
        val end = Span(1, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse034() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(1, 0)
        val end = Span(2, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse035() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(1, 0)
        val end = Span(2, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse036() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(0, -5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse037() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(-1, 0)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse038() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(-1, 5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse039() {
        val inToken = NewLineToken
        val change = ""
        val start = Span(-1, -5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse040() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(0, 0)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse041() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(1, 0)
        val end = Span(1, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\nb"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse042() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(1, 0)
        val end = Span(1, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\nb"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse043() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(1, 0)
        val end = Span(2, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\nb"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse044() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(1, 0)
        val end = Span(2, 5)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\nb"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse045() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(0, -5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse046() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(-1, 0)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse047() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(-1, 5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse048() {
        val inToken = NewLineToken
        val change = "b"
        val start = Span(-1, -5)
        val end = Span(0, 0)
        val outIterator = ParserIterator()
        inToken.addToIterator(Change(change, start, end), outIterator, null)
        val goldString = "\n"
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        assertEquals(goldIterator, outIterator)
    }

    @Test
    fun tokenReparse049() {
        val inToken1 = NewLineToken
        val inToken2 = NameToken("a123456789")
        val change = ""
        val start1 = Span(1, 0)
        val end1 = Span(1, 3)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString += outIterator.peekToken().value
            outIterator.next()
        }
        val goldString = "\n3456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse050() {
        val inToken1 = NewLineToken
        val inToken2 = NameToken("a123456789")
        val change = "b"
        val start1 = Span(1, 0)
        val end1 = Span(1, 3)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString += outIterator.peekToken().value
            outIterator.next()
        }
        val goldString = "\nb3456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse051() {
        val inToken1 = NewLineToken
        val inToken2 = NameToken("a123456789")
        val change = "b"
        val start1 = Span(1, 0)
        val end1 = Span(1, 10)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString += outIterator.peekToken().value
            outIterator.next()
        }
        val goldString = "\nb"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse052() {
        val inToken1 = NewLineToken
        val inToken2 = NameToken("a123456789")
        val change = "b"
        val start1 = Span(0, 0)
        val end1 = Span(1, 0)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString += outIterator.peekToken().value
            outIterator.next()
        }
        val goldString = "a123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse053() {
        val inToken1 = NewLineToken
        val inToken2 = NameToken("a123456789")
        val change = "b"
        val start1 = Span(1, 0)
        val end1 = Span(2, 0)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString += outIterator.peekToken().value
            outIterator.next()
        }
        val goldString = "\nb"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse054() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 1)
        val end1 = Span(0, 10)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a b123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse055() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 1)
        val end1 = Span(0, 10)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "ac b123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse056() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 5)
        val end1 = Span(0, 16)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse057() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 5)
        val end1 = Span(0, 16)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a1234c56789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse058() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 10)
        val end1 = Span(0, 11)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789b123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse059() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 10)
        val end1 = Span(0, 11)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789cb123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse060() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 10)
        val end1 = Span(0, 21)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse061() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 10)
        val end1 = Span(0, 21)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789c"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse062() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 11)
        val end1 = Span(0, 21)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789 "
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse063() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 11)
        val end1 = Span(0, 21)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789 c"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse064() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = NewLineToken
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 10)
        val end1 = Span(1, 0)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789b123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse065() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = NewLineToken
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 10)
        val end1 = Span(1, 0)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789cb123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse066() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = NewLineToken
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 5)
        val end1 = Span(1, 5)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a123456789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse067() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = NewLineToken
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 5)
        val end1 = Span(1, 5)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        var outString = ""
        while(outIterator.hasNext()) {
            outString +=
                if(outIterator.isNextToken()) outIterator.peekToken().value
                else (outIterator.peekNode() as TokenImpl).value
            outIterator.next()
        }
        val goldString = "a1234c56789"
        assertEquals(goldString, outString)
    }

    @Test
    fun tokenReparse068() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = NewLineToken
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 5)
        val end1 = Span(1, 5)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        val outToken = outIterator.peekToken()
        val goldToken = NameToken("a123456789")
        assertEquals(goldToken, outToken)
    }

    @Test
    fun tokenReparse069() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = NewLineToken
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 5)
        val end1 = Span(1, 5)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        val outToken = outIterator.peekToken()
        val goldToken = NameToken("a1234c56789")
        assertEquals(goldToken, outToken)
    }

    @Test
    fun tokenReparse070() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = ""
        val start1 = Span(0, 5)
        val end1 = Span(0, 16)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        val outToken = outIterator.peekToken()
        val goldToken = NameToken("a123456789")
        assertEquals(goldToken, outToken)
    }

    @Test
    fun tokenReparse071() {
        val inToken1 = NameToken("a123456789")
        val inToken2 = WhitespaceToken(" ")
        val inToken3 = NameToken("b123456789")
        val change = "c"
        val start1 = Span(0, 5)
        val end1 = Span(0, 16)
        val outIterator = ParserIterator()
        inToken1.addToIterator(Change(change, start1, end1), outIterator, inToken2)
        val start2 = start1 - inToken1.span
        val end2 = end1 - inToken1.span
        inToken2.addToIterator(Change(change, start2, end2), outIterator, inToken3)
        val start3 = start2 - inToken2.span
        val end3 = end2 - inToken2.span
        inToken3.addToIterator(Change(change, start3, end3), outIterator, null)
        val outToken = outIterator.peekToken()
        val goldToken = NameToken("a1234c56789")
        assertEquals(goldToken, outToken)
    }
}