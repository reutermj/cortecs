package parser

import kotlinx.serialization.Serializable

@Serializable
data class Change(val text: String, val start: Span, val end: Span)

class ParserIterator {
    private val iterators = mutableListOf<IParserIterator>()
    private var i = 0

    fun add(s: String) {
        if(s.any()) {
            val stringIter = iterators.lastOrNull()
            if(stringIter is ParserStringIterator) {
                iterators.removeLast()
                iterators.add(ParserStringIterator(stringIter.text + s))
            } else iterators.add(ParserStringIterator(s))
        }
    }

    fun add(node: Ast) {
        iterators.add(ParserNodeIterator(node))
    }

    fun inject(nodes: List<Ast>) {
        iterators.addAll(i, nodes.map { ParserNodeIterator(it) })
    }

    fun hasNext() = i < iterators.size

    fun isNextToken() = iterators[i].isToken()
    fun peekToken() = iterators[i].peekToken()
    fun peekNode() = iterators[i].peekNode()
    fun next() {
        iterators[i].next()
        while(i < iterators.size && !iterators[i].hasNext()) i++
    }

    override fun equals(other: Any?): Boolean {
        if(other !is ParserIterator) return false
        return iterators == other.iterators
    }

    override fun hashCode() = iterators.hashCode() xor i.hashCode()
}

internal sealed interface IParserIterator {
    fun hasNext(): Boolean
    fun isToken(): Boolean
    fun peekToken(): TokenImpl
    fun peekNode(): Ast
    fun next()
}

internal class ParserStringIterator(val text: String): IParserIterator {
    private var i = 0
    private var cache: TokenImpl? = null

    override fun hasNext() = i < text.length
    override fun isToken() = true
    override fun peekToken(): TokenImpl {
        if(cache == null) cache = nextToken(text, i)
        return cache!!
    }
    override fun peekNode() = throw Exception("Programmer Error")
    override fun next() {
        i += (cache ?: nextToken(text, i)).value.length
        cache = null
    }

    override fun equals(other: Any?): Boolean {
        if(other !is ParserStringIterator) return false
        return text == other.text
    }

    override fun hashCode() = text.hashCode() xor i.hashCode()
}

internal class ParserNodeIterator(val node: Ast): IParserIterator {
    private var i = true
    override fun hasNext() = i
    override fun isToken() = node is Token
    override fun peekToken() =
        if(i) node.firstToken()
        else throw Exception("Programmer Error")
    override fun peekNode() = if(i) node else throw Exception("Programmer Error")
    override fun next() { i = false }

    override fun equals(other: Any?): Boolean {
        if(other !is ParserNodeIterator) return false
        return node == other.node
    }

    override fun hashCode() = node.hashCode() xor i.hashCode()
}

fun constructChangeIterator(node: Ast, change: Change): ParserIterator {
    val iterator = ParserIterator()
    val updatedChange =
        if(change.start == Span.zero) {
            iterator.add(change.text)
            change.copy(text = "")
        } else change

    node.addToIterator(updatedChange, iterator, null)
    return iterator
}
