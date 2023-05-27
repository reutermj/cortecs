package lsp

import org.eclipse.lsp4j.*
import kotlin.math.*

class CortecsFile private constructor(val file: CortecsFileTree) {
    constructor(text: String): this(CortecsFileTree.bulkLoad(text.lines()))

    fun getLine(line: Int): String = file.getLine(line)!!

    fun contentChange(change: TextDocumentContentChangeEvent): CortecsFile {
        val start = change.range.start
        val end = change.range.end
        val text =
            getLine(start.line).substring(0, start.character) + change.text + getLine(end.line).substring(end.character)
        val lines = text.lines()

        val n = lines.size
        val m = end.line - start.line + 1
        //if n > m: replace m lines and insert n-m lines
        //if n < m: replace n lines and delete m-n lines
        //if n = m: replace n lines
        val numLinesReplaced = min(n, m)
        val replaceList = lines.subList(0, numLinesReplaced)
        var updatedFile = file.replace(start.line, replaceList)
        updatedFile =
            if(n > m) updatedFile.insert(start.line + m, lines.subList(m, n))
            else updatedFile.delete(start.line + n, m - n)

        return CortecsFile(updatedFile)
    }
}

sealed interface CortecsFileTree {
    companion object {
        val order = 4
        val minChildren = (order / 2) + (order % 2)

        fun bulkLoad(lines: List<String>) =
            when(lines.size) {
                0 -> CortecsFileNone
                1 -> CortecsFileLeaf(lines.first())
                else -> {
                    var src = List<CortecsFileTree>(lines.size) { CortecsFileLeaf(lines[it]) }
                    while(src.size >= minChildren) src = oneLayer(src)
                    if(src.size == 1) src.first()
                    else CortecsFileNode(src)
                }
            }

        fun oneLayer(src: List<CortecsFileTree>): List<CortecsFileTree> {
            if(src.size < minChildren) throw Exception("programmer error")

            val dst = mutableListOf<CortecsFileTree>()
            var i = 0
            while(i < src.size) {
                val remaining = src.size - i
                val length =
                    if(remaining <= order) remaining
                    else if(remaining <= order + minChildren) remaining - minChildren
                    else order
                dst.add(CortecsFileNode(src.subList(i, i + length)))
                i += length
            }

            return dst
        }
    }

    val size: Int
    val height: Int
    fun getLine(line: Int, leftMostLineNumber: Int = 0): String?

    fun insert(line: Int, lines: List<String>): CortecsFileTree {
        if(lines.isEmpty()) return this

        var trees =
            if(line >= size) insertToTheRight(lines.map { CortecsFileLeaf(it) })
            else insert(line, lines, 0)

        while(trees.size >= minChildren) trees = oneLayer(trees)
        return trees.first()
    }

    fun insert(line: Int, lines: List<String>, leftMostLineNumber: Int): List<CortecsFileTree>
    fun insertToTheLeft(trees: List<CortecsFileTree>): List<CortecsFileTree>
    fun insertToTheRight(trees: List<CortecsFileTree>): List<CortecsFileTree>
    fun delete(line: Int, numLines: Int): CortecsFileTree {
        val took = take(line, 0)
        val dropped = drop(line + numLines, 0)

        var src =
            if(took.isEmpty()) dropped
            else if(dropped.isEmpty()) took
            else if(took.first().height < dropped.first().height) dropped.first().insertToTheLeft(took)
            else if(took.first().height >  dropped.first().height) took.last().insertToTheRight(dropped)
            else took + dropped
        while(src.size >= minChildren) src = oneLayer(src)

        return when(src.size) {
            0 -> CortecsFileNone
            1 -> src.first()
            else -> CortecsFileNode(src)
        }
    }
    fun drop(line: Int, leftMostLineNumber: Int): List<CortecsFileTree>
    fun take(line: Int, leftMostLineNumber: Int): List<CortecsFileTree>
    fun replace(line: Int, lines: List<String>) = replace(line, lines, 0)
    fun replace(line: Int, lines: List<String>, leftMostLineNumber: Int): CortecsFileTree
    fun inOrder(f: (String) -> Unit)
    fun isValid(isRoot: Boolean = true): Boolean
}

object CortecsFileNone: CortecsFileTree {
    override val size = 0
    override val height = 0
    override fun getLine(line: Int, leftMostLineNumber: Int) = null
    override fun insert(line: Int, lines: List<String>, leftMostLineNumber: Int) = lines.map { CortecsFileLeaf(it) }
    override fun insertToTheLeft(trees: List<CortecsFileTree>) = trees
    override fun insertToTheRight(trees: List<CortecsFileTree>) = trees
    override fun drop(line: Int, leftMostLineNumber: Int) = emptyList<CortecsFileTree>()
    override fun take(line: Int, leftMostLineNumber: Int) = emptyList<CortecsFileTree>()
    override fun replace(line: Int, lines: List<String>, leftMostLineNumber: Int) = throw Exception("Programmer error")
    override fun inOrder(f: (String) -> Unit) {}
    override fun isValid(isRoot: Boolean) = true
}

data class CortecsFileLeaf(val text: String): CortecsFileTree {
    override val size = 1
    override val height = 1
    override fun isValid(isRoot: Boolean) = true
    override fun getLine(line: Int, leftMostLineNumber: Int) =
        if(line == leftMostLineNumber) text
        else null

    override fun insert(line: Int, lines: List<String>, leftMostLineNumber: Int) =
        if(line == leftMostLineNumber) List(lines.size + 1) { if(it == lines.size) this else CortecsFileLeaf(lines[it]) }
        else List(lines.size + 1) { if(it == 0) this else CortecsFileLeaf(lines[it - 1]) }

    override fun insertToTheLeft(trees: List<CortecsFileTree>) =
        List(trees.size + 1) { if(it == trees.size) this else trees[it] }

    override fun insertToTheRight(trees: List<CortecsFileTree>) =
        List(trees.size + 1) { if(it == 0) this else trees[it - 1] }

    override fun drop(line: Int, leftMostLineNumber: Int) =
        if(leftMostLineNumber < line) emptyList()
        else listOf(this)

    override fun take(line: Int, leftMostLineNumber: Int) =
        if(line <= leftMostLineNumber) emptyList()
        else listOf(this)

    override fun replace(line: Int, lines: List<String>, leftMostLineNumber: Int) =
        if(line <= leftMostLineNumber && leftMostLineNumber < line + lines.size) CortecsFileLeaf(lines[leftMostLineNumber - line])
        else this

    override fun inOrder(f: (String) -> Unit) {
        f(text)
    }
}

data class CortecsFileNode(val children: List<CortecsFileTree>): CortecsFileTree {
    val offsets = run {
        var i = 0
        List(children.size) {
            val offset = i
            i += children[it].size
            offset
        }
    }
    override val size = children.fold(0) { acc, child -> acc + child.size}
    override val height = children.first().height + 1

    override fun isValid(isRoot: Boolean): Boolean {
        if(!isRoot && children.size < CortecsFileTree.minChildren) return false
        if(children.size > CortecsFileTree.order) return false

        for(child in children)
            if(!child.isValid(false) || child.height + 1 != height || child == CortecsFileNone)
                return false
        return true
    }

    private fun pickChild(line: Int, leftMostLineNumber: Int): Int {
        var l = 0
        var r = children.size - 1

        while(true) {
            val m = (l + r) / 2
            val child = children[m]
            val offset = leftMostLineNumber + offsets[m]
            val size = child.size
            if(line < offset) r = m - 1
            else if(line >= offset + size) l = m + 1
            else return m // offset <= line < offset + size
        }
    }

    override fun getLine(line: Int, leftMostLineNumber: Int): String? {
        val index = pickChild(line, leftMostLineNumber)
        return children[index].getLine(line, leftMostLineNumber + offsets[index])
    }

    override fun insert(line: Int, lines: List<String>, leftMostLineNumber: Int): List<CortecsFileTree> {
        val index = pickChild(line, leftMostLineNumber)
        val src = mutableListOf<CortecsFileTree>()
        for(i in 0 until index) src.add(children[i])
        src.addAll(children[index].insert(line, lines, leftMostLineNumber + offsets[index]))
        for(i in index + 1 until children.size) src.add(children[i])
        return if(src.size < CortecsFileTree.minChildren) listOf(CortecsFileNode(src))
        else CortecsFileTree.oneLayer(src)
    }

    override fun drop(line: Int, leftMostLineNumber: Int): List<CortecsFileTree> {
        val index = pickChild(line, leftMostLineNumber)
        val src = mutableListOf<CortecsFileTree>()

        val remaining = children[index].drop(line, leftMostLineNumber + offsets[index])
        if(remaining.any()) {
            if(remaining.first().height == height - 1) {
                src.addAll(remaining)
                for(i in index + 1 until children.size) src.add(children[i])
            } else if(index < children.size - 1) {
                src.addAll(children[index + 1].insertToTheLeft(remaining))
                for(i in index + 2 until children.size) src.add(children[i])
            } else return remaining
        } else for(i in index + 1 until children.size) src.add(children[i])

        return if(src.size < CortecsFileTree.minChildren) src
        else CortecsFileTree.oneLayer(src)
    }

    override fun take(line: Int, leftMostLineNumber: Int): List<CortecsFileTree> {
        val index = pickChild(line, leftMostLineNumber)
        val src = mutableListOf<CortecsFileTree>()

        val remaining = children[index].take(line, leftMostLineNumber + offsets[index])
        if(remaining.any()) {
            if(remaining.first().height == height - 1) {
                for(i in 0 until index) src.add(children[i])
                src.addAll(remaining)
            } else if(index > 0) {
                for(i in 0 until index - 1) src.add(children[i])
                src.addAll(children[index - 1].insertToTheRight(remaining))
            } else return remaining
        } else for(i in 0 until index) src.add(children[i])

        return if(src.size < CortecsFileTree.minChildren) src
        else CortecsFileTree.oneLayer(src)
    }

    override fun insertToTheLeft(trees: List<CortecsFileTree>): List<CortecsFileTree> {
        val src = mutableListOf<CortecsFileTree>()
        if(trees.first().height == height - 1) {
            src.addAll(trees)
            src.addAll(children)
        } else {
            src.addAll(children.first().insertToTheLeft(trees))
            for(i in 1 until children.size) src.add(children[i])
        }
        return CortecsFileTree.oneLayer(src)
    }


    override fun insertToTheRight(trees: List<CortecsFileTree>): List<CortecsFileTree> {
        val src = mutableListOf<CortecsFileTree>()
        if(trees.first().height == height - 1) {
            src.addAll(children)
            src.addAll(trees)
        } else {
            for(i in 0 until children.size - 1) src.add(children[i])
            src.addAll(children.last().insertToTheRight(trees))
        }
        return CortecsFileTree.oneLayer(src)
    }

    override fun replace(line: Int, lines: List<String>, leftMostLineNumber: Int): CortecsFileTree {
        val lIndex = pickChild(line, leftMostLineNumber)
        val rIndex = pickChild(line + lines.size - 1, leftMostLineNumber)
        val c = List(children.size) {
            if(it in lIndex..rIndex) children[it].replace(line, lines, leftMostLineNumber + offsets[it])
            else children[it]
        }
        return CortecsFileNode(c)
    }

    override fun inOrder(f: (String) -> Unit) {
        for(child in children) child.inOrder(f)
    }
}