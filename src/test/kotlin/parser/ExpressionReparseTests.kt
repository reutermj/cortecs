package parser
import kotlin.test.*

class ExpressionReparseTests {
    @Test
    fun testReparse001() {
        val inString = """let y = a +
                        |let w = w""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inBlock = parseBlock(SequenceBuilder(inIterator))

        val start = Span(0, 11)
        val end = Span(1, 7)
        val change = ""
        val outIterator = ParserIterator()
        inBlock.addToIterator(change, start, end, outIterator, null)
        val outBlock = parseBlock(SequenceBuilder(inIterator))

        val goldString = generateGoldText(inString, change, start, end)
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        val goldBlock = parseBlock(SequenceBuilder(inIterator))

        assertEquals(goldBlock, outBlock)
    }

    @Test
    fun testReparse002() {
        val inString = """let x = a *
                        |let y = b + c
                        |let z = d * e""".trimMargin()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inBlock = parseBlock(SequenceBuilder(inIterator))

        val start = Span(0, 11)
        val end = Span(1, 7)
        val change = ""
        val outIterator = ParserIterator()
        inBlock.addToIterator(change, start, end, outIterator, null)
        val outBlock = parseBlock(SequenceBuilder(inIterator))

        val goldString = generateGoldText(inString, change, start, end)
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        val goldBlock = parseBlock(SequenceBuilder(inIterator))

        assertEquals(goldBlock, outBlock)
    }
}
