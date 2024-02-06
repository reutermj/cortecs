package parser_v2

import errors.*
import kotlinx.serialization.*

sealed interface Ast {
    val span: Span
    val errors: CortecsErrors
    fun firstTokenOrNull(): Token?
    fun shouldKeep(start: Span, end: Span) = end < Span.zero || span < start
    fun shouldDelete(start: Span, end: Span) = span == Span.zero || start <= Span.zero && span <= end
    fun createChangeIterator(change: Change): ParserIterator {
        val iterator = ParserIterator()
        addToIterator(change, iterator, false)
        if(change.start == Span.zero) iterator.add(change.text)
        return iterator
    }
    fun addToIterator(change: Change, iter: ParserIterator, wasNextTokenModified: Boolean): Boolean
    fun addAllButFirstToIterator(iter: ParserIterator)
    fun forceReparse(iter: ParserIterator)
    fun stringify(builder: StringBuilder)
}

@Serializable
sealed class AstImpl: Ast {
    abstract val nodes: List<Ast>

    private var _firstToken: Token? = null
    override fun firstTokenOrNull(): Token? {
        if(_firstToken != null) return _firstToken

        for(node in nodes) {
            val first = node.firstTokenOrNull()
            if(first != null) {
                _firstToken = first
                return first
            }
        }
        return null
    }

    override fun addAllButFirstToIterator(iter: ParserIterator) {
        for(node in nodes.drop(1).reversed()) iter.add(node)
        nodes.firstOrNull()?.addAllButFirstToIterator(iter)
    }

    private var _span: Span? = null
    override val span: Span
        get() {
            if(_span == null) _span = nodes.fold(Span.zero) { acc, e -> acc + e.span }
            return _span!!
        }

    override fun forceReparse(iter: ParserIterator) {
        val nonEmptyNodes = nodes.filter { it.span != Span.zero } //TODO have a better way of dealing with empty nodes
        nonEmptyNodes.last().forceReparse(iter)
        for(node in nonEmptyNodes.dropLast(1).reversed()) iter.add(node)
    }

    override fun addToIterator(change: Change, iter: ParserIterator, wasNextTokenModified: Boolean): Boolean {
        if(nodes.isEmpty()) return wasNextTokenModified
        if(shouldDelete(change.start, change.end)) return true
        if(shouldKeep(change.start, change.end)) {
            if(wasNextTokenModified) forceReparse(iter) // the next element has changed so need to reparse the last element of this node
            else iter.add(this) //the token following this node is kept; don't force reparse
            return false
        }

        var s = Span.zero
        val spans = Array(nodes.size) {
            val span = s
            s += nodes[it].span
            span
        }

        var modified = wasNextTokenModified
        for (i in nodes.indices.reversed()) {
            val start = change.start - spans[i]
            val end = change.end - spans[i]
            modified = nodes[i].addToIterator(change.copy(start = start, end = end), iter, modified)
        }

        return modified
    }

    override fun stringify(builder: StringBuilder) {
        for(node in nodes) node.stringify(builder)
    }
}

@Serializable
data class LetAst(override val nodes: List<Ast>, override val errors: CortecsErrors, val nameIndex: Int, val expressionIndex: Int): AstImpl() {
    fun name(): NameToken =
        if(nameIndex == -1) throw Exception("Name not available")
        else nodes[nameIndex] as NameToken

    fun expression(): Expression =
        if(expressionIndex == -1) throw Exception("Expression not available")
        else nodes[expressionIndex] as Expression
}

@Serializable
data class ReturnAst(override val nodes: List<Ast>, override val errors: CortecsErrors, val expressionIndex: Int): AstImpl() {
    fun expression(): Expression =
        if(expressionIndex == -1) throw Exception("Expression not available")
        else nodes[expressionIndex] as Expression
}

//P1  -> P2 P1'
//P1' -> `|` P2 P1' | epsilon
//P2  -> P3 P2'
//P2' -> ^ P3 P2' | epsilon
//P3  -> P4 P3'
//P3' -> & P4 P3' | epsilon
//P4  -> P5 P4'
//P4' -> = P5 P4' | ! P5 P4' | epsilon
//P5  -> P6 P5'
//P5' -> > P6 P5' | < P6 P5' | epsilon
//P6  -> P7 P6'
//P6' -> + P7 P6' | - P7 P6' | epsilon
//P7  -> E P7'
//P7' -> * E P7' | / E P7' | % E P7' | epsilon
//E  -> (P1) | atom
@Serializable
sealed class Expression: AstImpl()
@Serializable
sealed class BaseExpression: Expression()
@Serializable
data class AtomicExpression(override val nodes: List<Ast>, override val errors: CortecsErrors, val atomIndex: Int): BaseExpression() {
    fun atom(): AtomicExpressionToken =
        if(atomIndex == -1) throw Exception("Name not available")
        else nodes[atomIndex] as AtomicExpressionToken
}
@Serializable
data class GroupingExpression(override val nodes: List<Ast>, override val errors: CortecsErrors, val expressionIndex: Int): BaseExpression() {
    fun expression(): Expression =
        if(expressionIndex == -1) throw Exception("Name not available")
        else nodes[expressionIndex] as Expression
}
@Serializable
data class UnaryExpression(override val nodes: List<Ast>, override val errors: CortecsErrors, val opIndex: Int, val expressionIndex: Int): BaseExpression() {
    fun op(): OperatorToken =
        if(opIndex == -1) throw Exception("op not available")
        else nodes[opIndex] as OperatorToken
    fun expression(): Expression =
        if(expressionIndex == -1) throw Exception("Name not available")
        else nodes[expressionIndex] as Expression
}
@Serializable
sealed class BinaryExpression: Expression() {
    abstract val lhsIndex: Int
    abstract val rhsIndex: Int
    abstract val opIndex: Int

    fun lhs(): Expression =
        if(lhsIndex == -1) throw Exception("lhs not available")
        else nodes[lhsIndex] as Expression

    fun rhs(): Expression =
        if(rhsIndex == -1) throw Exception("rhs not available")
        else nodes[rhsIndex] as Expression

    fun op(): OperatorToken =
        if(opIndex == -1) throw Exception("op not available")
        else nodes[opIndex] as OperatorToken
}
@Serializable
data class BinaryExpressionP1(override val nodes: List<Ast>, override val errors: CortecsErrors, override val lhsIndex: Int, override val opIndex: Int, override val rhsIndex: Int): BinaryExpression()
@Serializable
data class BinaryExpressionP2(override val nodes: List<Ast>, override val errors: CortecsErrors, override val lhsIndex: Int, override val opIndex: Int, override val rhsIndex: Int): BinaryExpression()
@Serializable
data class BinaryExpressionP3(override val nodes: List<Ast>, override val errors: CortecsErrors, override val lhsIndex: Int, override val opIndex: Int, override val rhsIndex: Int): BinaryExpression()
@Serializable
data class BinaryExpressionP4(override val nodes: List<Ast>, override val errors: CortecsErrors, override val lhsIndex: Int, override val opIndex: Int, override val rhsIndex: Int): BinaryExpression()
@Serializable
data class BinaryExpressionP5(override val nodes: List<Ast>, override val errors: CortecsErrors, override val lhsIndex: Int, override val opIndex: Int, override val rhsIndex: Int): BinaryExpression()
@Serializable
data class BinaryExpressionP6(override val nodes: List<Ast>, override val errors: CortecsErrors, override val lhsIndex: Int, override val opIndex: Int, override val rhsIndex: Int): BinaryExpression()
@Serializable
data class BinaryExpressionP7(override val nodes: List<Ast>, override val errors: CortecsErrors, override val lhsIndex: Int, override val opIndex: Int, override val rhsIndex: Int): BinaryExpression()
