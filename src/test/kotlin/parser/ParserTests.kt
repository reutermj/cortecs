package parser

import kotlin.test.*

class ParserTests {
    @Test
    fun testReparse001() {
        val inString = """if (someCondition) {
                         |let x = ( anotherCondition )
                         |}""".trimMargin()

        val inIterator  = ParserIterator()
        inIterator.add(inString)
        val inBlock = parseBlock(SequenceBuilder(inIterator))

        val text = ""
        val start = Span(0, 2)
        val end = Span(1, 7)
        val change = Change(text,  start, end)

        val outIterator = ParserIterator()
        inBlock.addToIterator(change, outIterator, null)
        val outBlock = parseBlock(SequenceBuilder(outIterator))

        val goldString = generateGoldText(inString, text, start, end)
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        val goldBlock = parseBlock(SequenceBuilder(goldIterator))

        assertEquals(goldBlock, outBlock)
    }

    @Test
    fun testDeleteLine001() {
        val n = 512

        val builder = StringBuilder()
        for(i in 0 until n) {
            if(setOf(true, false).random()) builder.append("let x$i = y\n")
            else builder.append("return x$i\n")
        }
        val inString = builder.toString()

        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inBlock = parseBlock(SequenceBuilder(inIterator))

        for(i in 0 until n) {
            val text = ""
            val start = Span(i, 0)
            val end = Span(i + 1, 0)
            val change = Change(text,  start, end)
            val outIterator = constructChangeIterator(inBlock, change)
            val outBlock = parseBlock(SequenceBuilder(outIterator))

            val goldString = generateGoldText(inString, text, start, end)
            val goldIterator = ParserIterator()
            goldIterator.add(goldString)
            val goldBlock = parseBlock(SequenceBuilder(goldIterator))

            assertEquals(goldBlock, outBlock)
        }
    }

    @Test
    fun testDeleteKeyword001() {
        val n = 512

        val builder = StringBuilder()
        for(i in 0 until n) {
            if(i % 2 == 0) builder.append("let x$i = y\n")
            else builder.append("return x$i\n")
        }
        val inString = builder.toString()

        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inBlock = parseBlock(SequenceBuilder(inIterator))

        //Currently not deleting the first keyword because parseBlock should never be responsible for
        //parsing garbage tokens as the first thing it does
        for(i in 1 until n) {
            val outIterator = ParserIterator()
            val text = ""
            val column = if(i % 2 == 0) 3 else 6
            val start = Span(i, 0)
            val end = Span(i, column)
            val change = Change(text,  start, end)
            inBlock.addToIterator(change, outIterator, null)
            val outBlock = parseBlock(SequenceBuilder(outIterator))

            val goldString = generateGoldText(inString, text, start, end)
            val goldIterator = ParserIterator()
            goldIterator.add(goldString)
            val goldBlock = parseBlock(SequenceBuilder(goldIterator))

            assertEquals(goldBlock, outBlock)
        }
    }

    @Test
    fun testDeleteKeyword002() {
        val n = 512

        val keywordSizes = intArrayOf(8, 3, 2, 3, 3, 6, 1, 6, 1)

        val builder = StringBuilder()
        for(i in 0 until n) {
            when(i % 9) {
                0 -> builder.append("fn foo$i() {\n")
                1 -> builder.append("let x$i = y\n")
                2 -> builder.append("if(x$i) {\n")
                3 -> builder.append("let x$i = y\n")
                4 -> builder.append("let x$i = y\n")
                5 -> builder.append("return x$i\n")
                6 -> builder.append("}\n")
                7 -> builder.append("return x$i\n")
                8 -> builder.append("}\n")
            }
        }
        val inString = builder.toString()

        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inProgram = parseProgram(inIterator)

        for(index in 0 until n) {
            val outIterator = ParserIterator()
            val text = ""
            val column = keywordSizes[index % 9]
            val start = Span(index, 0)
            val end = Span(index, column)
            val change = Change(text,  start, end)
            inProgram.addToIterator(change, outIterator, null)
            val outProgram = parseProgram(outIterator)

            val goldString = generateGoldText(inString, text, start, end)
            val goldIterator = ParserIterator()
            goldIterator.add(goldString)
            val goldProgram = parseProgram(goldIterator)

            assertEquals(goldProgram, outProgram)
        }
    }

    @Test
    fun testInsertIf001() {
        //tests insert of nested if statements in a fn equal number of close braces after the fn
        for(n in 1..128) {
            val inString = "fn foo() {\n" + "}\n".repeat(n) + "}"

            val inIterator = ParserIterator()
            inIterator.add(inString)
            val inProgram = parseProgram(inIterator)

            val outIterator = ParserIterator()
            val text = "if(x) {\n".repeat(n)
            val start = Span(1, 0)
            val end = Span(1, 0)
            val change = Change(text,  start, end)
            inProgram.addToIterator(change, outIterator, null)
            val outProgram = parseProgram(outIterator)

            val goldString = generateGoldText(inString, text, start, end)
            val goldIterator = ParserIterator()
            goldIterator.add(goldString)
            val goldProgram = parseProgram(goldIterator)

            assertEquals(goldProgram, outProgram)
        }
    }

    @Test
    fun testInsertIf002() {
        //tests insert of nested if statements in a fn fewer close braces after the fn
        for(n in 1..128) {
            for(m in 1..5) {
                val inString = "fn foo() {\n" + "}\n".repeat(n) + "}"

                val inIterator = ParserIterator()
                inIterator.add(inString)
                val inProgram = parseProgram(inIterator)

                val outIterator = ParserIterator()
                val text = "if(x) {\n".repeat(n + m)
                val start = Span(1, 0)
                val end = Span(1, 0)
                val change = Change(text,  start, end)
                inProgram.addToIterator(change, outIterator, null)
                val outProgram = parseProgram(outIterator)

                val goldString = generateGoldText(inString, text, start, end)
                val goldIterator = ParserIterator()
                goldIterator.add(goldString)
                val goldProgram = parseProgram(goldIterator)

                assertEquals(goldProgram, outProgram)
            }
        }
    }

    @Test
    fun testInsertIf003() {
        //tests insert of nested if statements in a fn more close braces after the fn
        for(m in 1..5) {
            for(n in m..128) {
                val inString = "fn foo() {\n" + "}\n".repeat(n) + "}"

                val inIterator = ParserIterator()
                inIterator.add(inString)
                val inProgram = parseProgram(inIterator)

                val outIterator = ParserIterator()
                val text = "if(x) {\n".repeat(n - m)
                val start = Span(1, 0)
                val end = Span(1, 0)
                val change = Change(text,  start, end)
                inProgram.addToIterator(change, outIterator, null)
                val outProgram = parseProgram(outIterator)

                val goldString = generateGoldText(inString, text, start, end)
                val goldIterator = ParserIterator()
                goldIterator.add(goldString)
                val goldProgram = parseProgram(goldIterator)

                assertEquals(goldProgram, outProgram)
            }
        }
    }

    @Test
    fun testInsertRemovalCloseCurly001() {
        val n = 10000

        val builder = StringBuilder()
        builder.append("fn foo() {\n")
        for(i in 0..n) {
            builder.append("let x$i = y\n")
        }
        builder.append("}")
        val inString = builder.toString()

        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inProgram = parseProgram(inIterator)
        var currProgram = inProgram

        repeat(500) {
            val line = (0..n).random() + 1

            val insertIterator = ParserIterator()
            val insertText = "}\n"
            val insertStart = Span(line, 0)
            val insertEnd = Span(line, 0)
            val insertChange = Change(insertText,  insertStart, insertEnd)
            currProgram.addToIterator(insertChange, insertIterator, null)
            val insertProgram = parseProgram(insertIterator)

            val goldInsertString = generateGoldText(inString, insertText, insertStart, insertEnd)
            val goldInsertIterator = ParserIterator()
            goldInsertIterator.add(goldInsertString)
            val goldInsertProgram = parseProgram(goldInsertIterator)

            assertEquals(goldInsertProgram, insertProgram)

            val removeIterator = ParserIterator()
            val removeText = ""
            val removeStart = Span(line, 0)
            val removeEnd = Span(line + 1, 0)
            val removeChange = Change(removeText, removeStart, removeEnd)
            insertProgram.addToIterator(removeChange, removeIterator, null)
            val removeProgram = parseProgram(removeIterator)

            assertEquals(inProgram, removeProgram)

            currProgram = removeProgram
        }
    }

    @Test
    fun testInsertKeyword() {
        val inString = """fn foo() {
                         |x = y
                         |x
                         |}""".trimMargin()

        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inProgram = parseProgram(inIterator)

        val text1 = "let "
        val start1 = Span(1, 0)
        val end1 = Span(1, 0)
        val outIterator1 = ParserIterator()
        val change1 = Change(text1, start1, end1)
        inProgram.addToIterator(change1, outIterator1, null)
        val outProgram1 = parseProgram(outIterator1)

        val goldString1 = generateGoldText(inString, text1, start1, end1)
        val goldIterator1 = ParserIterator()
        goldIterator1.add(goldString1)
        val goldProgram1 = parseProgram(goldIterator1)

        assertEquals(goldProgram1, outProgram1)

        val text2 = "return "
        val start2 = Span(2, 0)
        val end2 = Span(2, 0)
        val outIterator2 = ParserIterator()
        val change2 = Change(text2, start2, end2)
        inProgram.addToIterator(change2, outIterator2, null)
        val outProgram2 = parseProgram(outIterator2)

        val goldString2 = generateGoldText(inString, text2, start2, end2)
        val goldIterator2 = ParserIterator()
        goldIterator2.add(goldString2)
        val goldProgram2 = parseProgram(goldIterator2)

        assertEquals(goldProgram2, outProgram2)

        val text3 = "let "
        val start3 = Span(1, 0)
        val end3 = Span(1, 0)
        val outIterator3 = ParserIterator()
        val change3 = Change(text3, start3, end3)
        outProgram2.addToIterator(change3, outIterator3, null)
        val outProgram3 = parseProgram(outIterator3)

        val goldString3 = generateGoldText(goldString2, text3, start3, end3)
        val goldIterator3 = ParserIterator()
        goldIterator3.add(goldString3)
        val goldProgram3 = parseProgram(goldIterator3)

        assertEquals(goldProgram3, outProgram3)
    }

    @Test
    fun testInsertKeyword001() {
        val n = 2
        val builder = StringBuilder()
        builder.append("fn foo() {\n")
        for(i in 0..n) {
            when(i % 6) {
                0 -> builder.append("let x = y\n")
                1 -> builder.append("x = y\n")
                2 -> builder.append("x\n")
                3 -> builder.append("return x\n")
                4 -> builder.append("x = y\n")
                5 -> builder.append("x\n")
            }
        }
        builder.append("}")

        val inString = builder.toString()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inProgram = parseProgram(inIterator)

        for(i in 0.. n) {
            if(i % 3 == 0) continue

            val text1 = if(i % 3 == 1) "let " else "return "
            val start1 = Span(i + 1, 0)
            val end1 = Span(i + 1, 0)
            val outIterator1 = ParserIterator()
            val change1 = Change(text1, start1, end1)
            inProgram.addToIterator(change1, outIterator1, null)
            val outProgram1 = parseProgram(outIterator1)

            val goldString1 = generateGoldText(inString, text1, start1, end1)
            val goldIterator1 = ParserIterator()
            goldIterator1.add(goldString1)
            val goldProgram1 = parseProgram(goldIterator1)

            assertEquals(goldProgram1, outProgram1)
        }
    }

    @Test
    fun testDeleteToCreateKeyword001() {
        val builder = StringBuilder()
        builder.append("fn foo() {\n")
        for(i in 0..512) {
            if(i % 2 == 0) builder.append("let x$i = y\n")
            else builder.append("return x$i\n")
        }
        builder.append("}")
        val inString = builder.toString()
        val inIterator = ParserIterator()
        inIterator.add(inString)
        val inProgram = parseProgram(inIterator)

        for(i in 0 until 512) {
            for(j in 0 until 100 step 4) {
                if(i + j + 1 >= 512) break

                val text = ""
                val start = Span(i + 1, 2)
                val end = Span(j + i + 3, 2)
                val outIterator = ParserIterator()
                val change = Change(text, start, end)
                inProgram.addToIterator(change, outIterator, null)
                val outProgram = parseProgram(outIterator)

                val goldString = generateGoldText(inString, text, start, end)
                val goldIterator = ParserIterator()
                goldIterator.add(goldString)
                val goldProgram = parseProgram(goldIterator)

                assertEquals(goldProgram, outProgram)
            }
        }
    }

    @Test
    fun testVectorEquals() {
        repeat(100) {
            val chunks = mutableListOf<StarAst<BodyAst>>()
            var x = 0
            repeat(500) {
                val builder = StringBuilder()
                for(i in 0..((8..1024).random())) {
                    builder.append("let x$x = y\n")
                    x++
                }
                val iter = ParserIterator()
                iter.add(builder.toString())
                chunks.add(parseBlock(SequenceBuilder(iter)))
            }

            while(chunks.size > 1) {
                val i = (0 until (chunks.size - 1)).random()
                val left = chunks[i]
                val right = chunks[i + 1]
                val added = left + right
                chunks[i] = added
                chunks.removeAt(i + 1)
            }

            val goldBuilder = StringBuilder()
            for(i in 0 until x) {
                goldBuilder.append("let x$i = y\n")
            }
            val goldIterator = ParserIterator()
            goldIterator.add(goldBuilder.toString())
            val goldBlock = parseBlock(SequenceBuilder(goldIterator))

            assertEquals(goldBlock, chunks[0])
        }
    }

    @Test
    fun testVectorAssociativity() {
        repeat(100) {
            val chunks = mutableListOf<StarAst<BodyAst>>()
            var x = 0
            repeat(500) {
                val builder = StringBuilder()
                for(i in 0..((8..1024).random())) {
                    builder.append("let x$x = y\n")
                    x++
                }
                val iter = ParserIterator()
                iter.add(builder.toString())
                chunks.add(parseBlock(SequenceBuilder(iter)))
            }

            while(chunks.size > 1) {
                val i = (0 until (chunks.size - 1)).random()
                val left = chunks[i]
                val right = chunks[i + 1]
                val added = left + right
                chunks[i] = added
                chunks.removeAt(i + 1)
            }

            var y = 0
            chunks[0].inOrder {
                val name = (it as LetAst).name!!
                val number = name.value.substring(1).toInt()
                assertEquals(y, number)
                y++
            }

            assertEquals(x, y)
        }
    }
}