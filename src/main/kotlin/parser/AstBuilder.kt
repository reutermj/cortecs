package parser

import errors.*

class AstBuilder(val iterator: ParserIterator) {
    private var _errorLocation: Span? = null
    private var _currentLocation = Span.zero

    private var _nodes = mutableListOf<Ast>()
    private var _errors = mutableListOf<CortecsError>()

    inline fun <reified T: Token> consume(): Int {
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
            _errorLocation = _currentLocation + it
        }
        _currentLocation += node.span
        return _nodes.size - 1
    }

    fun emitError(text: String, span: Span) {
        _errors.add(CortecsError(text, _errorLocation ?: Span.zero, span))
    }

    fun markErrorLocation() {
        _errorLocation = _currentLocation
    }

    fun iterator() = iterator
    fun nodes(): List<Ast> = _nodes
    fun errors(): CortecsErrors = CortecsErrors(_errorLocation, _errors)
    fun getCurrentLocation() = _currentLocation
}

