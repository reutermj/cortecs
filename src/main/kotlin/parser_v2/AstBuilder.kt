package parser_v2

import errors.*

class AstBuilder(val iterator: ParserIterator) {
    private var errorLocation: Span? = null
    private var currentLocation = Span.zero

    private var _nodes = mutableListOf<Ast>()
    private var errors = mutableListOf<CortecsErrorV2>()

    inline fun <reified T: Token>consume(): Int {
        val token = iterator.peekToken()
        if(token is T) {
            iterator.nextToken()
            return addSubnode(token)
        }
        return -1
    }

    fun addSubnode(node: Ast?): Int {
        if(node == null) return -1

        _nodes.add(node)
        node.errors.errorSpan?.let {
            errorLocation = currentLocation + it
        }
        currentLocation += node.span
        return _nodes.size - 1
    }

    fun emitError(text: String, span: Span) {
        errors.add(CortecsErrorV2(text, errorLocation ?: Span.zero, span))
    }

    fun markErrorLocation() {
        errorLocation = currentLocation
    }

    fun iterator() = iterator
    fun nodes(): List<Ast> = _nodes
    fun errors(): CortecsErrors = CortecsErrors(errorLocation, errors)
}

