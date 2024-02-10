package parser_v2

import kotlin.test.*

class StarAstTest {
    fun getBlock(offset: Int, num: Int): BlockAst {
        val builder = StringBuilder()
        for(i in offset until (offset + num))
            builder.append("let x = $i\n")
        val iterator = ParserIterator()
        iterator.add(builder.toString())
        return parseBlock(iterator)
    }

    fun validateStarRec(star: StarAst, height: Int, nextNumber: Int): Int {
        assertEquals(height, star.height)
        var nextNum = nextNumber
        if(height == 0) {
            for(node in star.nodes) {
                assertIs<LetAst>(node)
                val expression = node.expression()
                assertIs<AtomicExpression>(expression)
                assertEquals(IntToken("$nextNum"), expression.atom())
                nextNum++
            }
        } else {
            for(node in star.nodes) {
                assertIs<StarAst>(node)
                nextNum = validateStarRec(node, height - 1, nextNum)
            }
        }
        return nextNum
    }

    fun validateStar(star: StarAst, offset: Int, num: Int) {
        assertEquals(offset + num, validateStarRec(star, star.height, offset))
    }

    @Test
    fun testConcatenation() {
        for(i in 0..250) {
            val left = getBlock(0, i)
            for(j in 0..250) {
                val right = getBlock(i, j)
                val combined = left + right
                validateStar(combined, 0, i + j)
            }
        }
    }

    @Test
    fun testAssociativity() {
        repeat(250) {
            val blocks = mutableListOf<BlockAst>()
            var offset = 0
            repeat(250) {
                val num = (0..250).random()
                blocks.add(getBlock(offset, num))
                offset += num
            }
            while(blocks.size > 1) {
                val index = (0 until blocks.size - 1).random()
                val left = blocks[index]
                val right = blocks.removeAt(index + 1)
                blocks[index] = left + right
            }

            validateStar(blocks.first(), 0, offset)
        }
    }
}