package parser

sealed class SequenceAst(final override val nodes: List<Ast>): Ast {
    final override val offset = nodes.fold(Offset.zero) { acc, e -> acc + e.offset }
    final override val firstTokenOrNull: Token? = nodes.firstOrNull()?.firstTokenOrNull

    override fun addToIterator(change: String, start: Offset, end: Offset, iter: ParserIterator, next: Token?) {
        if(keepOrDelete(start, end, iter, next)) return
        var s = start
        var e = end
        for (i in nodes.indices) {
            val eNext =
                if(i + 1 in nodes.indices) nodes[i + 1]
                else next
            nodes[i].addToIterator(change, s, e, iter, eNext?.firstTokenOrNull)

            s -= nodes[i].offset
            e -= nodes[i].offset
        }
    }

    final override fun forceReparse(iter: ParserIterator) {
        for(node in nodes.dropLast(1)) iter.add(node)
        nodes.last().forceReparse(iter)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SequenceAst) return false
        if(this::class != other::class) return false
        return nodes == other.nodes
    }

    override fun hashCode() = nodes.hashCode()
}
