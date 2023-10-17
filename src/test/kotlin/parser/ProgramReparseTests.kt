package parser

import kotlin.test.*

class ProgramReparseTests {
    @Test
    fun testReparse001() {
        val inIterator = ParserIterator()
        inIterator.add("")
        var program = parseProgram(inIterator)

        val s = "fn f(x) {}"
        var position = 1
        var line = 0
        var column = 0
        for(c in s) {
            val changeIterator = constructChangeIterator(program, "$c", Span(line, column), Span(line, column))
            program = parseProgram(changeIterator)

            if(c == '\n') {
                line++
                column = 0
            } else column++

            val goldString = s.substring(0, position)
            val goldIterator = ParserIterator()
            goldIterator.add(goldString)
            val goldProgram = parseProgram(goldIterator)

            assertEquals(goldProgram, program)

            position++
        }
    }
}