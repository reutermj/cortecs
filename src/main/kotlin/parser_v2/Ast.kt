package parser_v2

import errors.*
import kotlinx.serialization.*
import java.lang.reflect.Type

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
data class ProgramAst(override val nodes: List<Ast>, override val height: Int): StarAst<TopLevelAst>() {
    companion object {
        val empty = ProgramAst(emptyList(), 0)
    }
    override fun ctor(nodes: List<Ast>, height: Int) = ProgramAst(nodes, height)
}

sealed class TopLevelAst: AstImpl()

@Serializable
data class FunctionAst(override val nodes: List<Ast>, override val errors: CortecsErrors, val nameIndex: Int, val parametersIndex: Int, val blockIndex: Int): TopLevelAst() {
    fun name(): NameToken =
        if(nameIndex == -1) throw Exception("Name not available")
        else nodes[nameIndex] as NameToken

    fun parameters(): ParametersAst =
        if(parametersIndex == -1) throw Exception("Parameters not available")
        else nodes[parametersIndex] as ParametersAst

    fun block(): BlockAst =
        if(blockIndex == -1) throw Exception("Block not available")
        else nodes[blockIndex] as BlockAst
}

@Serializable
data class ParametersAst(override val nodes: List<Ast>, override val height: Int): StarAst<ParameterAst>() {
    companion object {
        val empty = ParametersAst(emptyList(), 0)
    }
    override fun ctor(nodes: List<Ast>, height: Int) = ParametersAst(nodes, height)
}
@Serializable
data class ParameterAst(override val nodes: List<Ast>, override val errors: CortecsErrors, val nameIndex: Int, val typeIndex: Int): AstImpl() {
    fun name(): NameToken =
        if(nameIndex == -1) throw Exception("Name not available")
        else nodes[nameIndex] as NameToken

    fun type(): TypeAnnotationToken? =
        if(typeIndex == -1) null
        else nodes[typeIndex] as TypeAnnotationToken
}

@Serializable
data class BlockAst(override val nodes: List<Ast>, override val height: Int): StarAst<BodyAst>() {
    companion object {
        val empty = BlockAst(emptyList(), 0)
    }
    override fun ctor(nodes: List<Ast>, height: Int) = BlockAst(nodes, height)
}

sealed class BodyAst: AstImpl()

@Serializable
data class LetAst(override val nodes: List<Ast>, override val errors: CortecsErrors, val nameIndex: Int, val expressionIndex: Int): BodyAst() {
    fun name(): NameToken =
        if(nameIndex == -1) throw Exception("Name not available")
        else nodes[nameIndex] as NameToken

    fun expression(): Expression =
        if(expressionIndex == -1) throw Exception("Expression not available")
        else nodes[expressionIndex] as Expression
}

@Serializable
data class ReturnAst(override val nodes: List<Ast>, override val errors: CortecsErrors, val expressionIndex: Int): BodyAst() {
    fun expression(): Expression =
        if(expressionIndex == -1) throw Exception("Expression not available")
        else nodes[expressionIndex] as Expression
}

@Serializable
data class IfAst(override val nodes: List<Ast>, override val errors: CortecsErrors, val conditionIndex: Int, val blockIndex: Int): BodyAst() {
    fun condition(): Expression =
        if(conditionIndex == -1) throw Exception("Expression not available")
        else nodes[conditionIndex] as Expression

    fun block(): BlockAst =
        if(blockIndex == -1) throw Exception("Expression not available")
        else nodes[blockIndex] as BlockAst
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
data class ArgumentsAst(override val nodes: List<Ast>, override val height: Int): StarAst<ArgumentAst>() {
    companion object {
        val empty = ArgumentsAst(emptyList(), 0)
    }
    override fun ctor(nodes: List<Ast>, height: Int) = ArgumentsAst(nodes, height)
}
@Serializable
data class ArgumentAst(override val nodes: List<Ast>, override val errors: CortecsErrors, val expressionIndex: Int): AstImpl() {
    fun expression(): Expression =
        if(expressionIndex == -1) throw Exception("Name not available")
        else nodes[expressionIndex] as Expression
}
@Serializable
data class FunctionCallExpression(override val nodes: List<Ast>, override val errors: CortecsErrors, val functionIndex: Int, val argumentsIndex: Int): BaseExpression() {
    fun function(): Expression =
        if(functionIndex == -1) throw Exception("Name not available")
        else nodes[functionIndex] as Expression

    fun arguments(): ArgumentsAst =
        if(argumentsIndex == -1) throw Exception("Name not available")
        else nodes[argumentsIndex] as ArgumentsAst
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
