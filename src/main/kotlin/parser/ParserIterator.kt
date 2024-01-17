package parser

import kotlinx.serialization.*

@Serializable
data class Change(val text: String, val start: Span, val end: Span)

class ParserIterator {
    val elements = mutableListOf<ParserIteratorNodeType>()
    var tokenCache: TokenImpl? = null
    var stringIndex = 0
    fun add(node: Ast) {
        elements.add(ParserIteratorAst(node))
    }

    fun add(text: String) {
        if(stringIndex != 0) throw Exception("Programmer Error")
        if(text.isEmpty()) return
        val last = elements.lastOrNull()
        if(last is ParserIteratorString) {
            elements.removeLast()
            elements.add(ParserIteratorString(text + last.text))
        } else elements.add(ParserIteratorString(text))
    }

    fun peekNode(): Ast? = when(val last = elements.lastOrNull()) {
        null -> null
        is ParserIteratorAst -> last.node
        is ParserIteratorString -> null
    }

    fun nextNode() {
        when(val last = elements.lastOrNull()) {
            null -> throw Exception("Iterator is empty")
            is ParserIteratorAst -> elements.removeLast()
            is ParserIteratorString -> throw Exception("Current iterator element is not a node")
        }
    }

    fun peekToken(): Token? = when(val last = elements.lastOrNull()) {
        null -> null
        is ParserIteratorString -> {
            if(tokenCache == null) {
                val token = nextToken(last.text, stringIndex)
                stringIndex += token.value.length
                tokenCache = token
            }
            tokenCache
        }

        is ParserIteratorAst -> last.node.firstTokenOrNull() ?: throw Exception("Programmer error")
    }

    fun nextToken() {
        when(val last = elements.lastOrNull()) {
            null -> throw Exception("Iterator is empty")
            is ParserIteratorString -> {
                if(stringIndex == last.text.length) {
                    stringIndex = 0
                    elements.removeLast()
                }
                tokenCache = null
            }

            is ParserIteratorAst -> {
                elements.removeLast()
                last.node.addAllButFirstToIterator(this)
            }
        }
    }
}

sealed interface ParserIteratorNodeType
data class ParserIteratorString(val text: String): ParserIteratorNodeType
data class ParserIteratorAst(val node: Ast): ParserIteratorNodeType