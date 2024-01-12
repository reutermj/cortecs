package parser

import errors.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import typechecker.*

@Serializable
sealed class Ast {
    abstract val errors: List<CortecsError>
    var _environment: Environment? = null
    val environment: Environment
        get() {
            if(_environment == null) _environment = generateEnvironment()
            return _environment!!
        }
    abstract fun generateEnvironment(): Environment
    abstract val span: Span
    fun shouldKeep(start: Span, end: Span) = end < Span.zero || span < start
    fun shouldDelete(start: Span, end: Span) = span == Span.zero || start <= Span.zero && span <= end
    abstract fun firstTokenOrNull(): TokenImpl?

    abstract fun addToIterator(change: Change, iter: ParserIterator, next: TokenImpl?)
    abstract fun forceReparse(iter: ParserIterator)
    fun keepOrDelete(start: Span, end: Span, iter: ParserIterator, next: TokenImpl?) =
        if(shouldDelete(start, end)) true
        else if(shouldKeep(start, end)) {
            if(next?.shouldKeep(start - span, end - span) != false) iter.add(this) //the token following this node is kept; don't force reparse
            else forceReparse(iter) // the next element has changed so need to reparse the last element of this vector
            true
        } else false
}

@Serializable
sealed class AstImpl: Ast() {
    abstract val nodes: List<Ast>
    private var _span: Span? = null
    override val span: Span
        get() {
            if(_span == null) _span = nodes.fold(Span.zero) { acc, e -> acc + e.span }
            return _span!!
        }
    private var _firstToken: TokenImpl? = null
    override fun firstTokenOrNull(): TokenImpl? {
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
    override fun forceReparse(iter: ParserIterator) {
        val nonEmptyNodes = nodes.filter { it.span != Span.zero } //TODO have a better way of dealing with empty nodes
        for(node in nonEmptyNodes.dropLast(1)) iter.add(node)
        nonEmptyNodes.last().forceReparse(iter)
    }

    override fun addToIterator(change: Change, iter: ParserIterator, next: TokenImpl?) {
        if(keepOrDelete(change.start, change.end, iter, next)) return
        var s = change.start
        var e = change.end
        for (i in nodes.indices) {
            val eNext = nodes.drop(i + 1).firstNotNullOfOrNull { it.firstTokenOrNull() } ?: next
            nodes[i].addToIterator(change.copy(start = s, end = e), iter, eNext)

            s -= nodes[i].span
            e -= nodes[i].span
        }
    }
}

@Serializable
sealed class TopLevelAst: AstImpl()
@Serializable
data class FnAst(override val nodes: List<Ast>, override val errors: List<CortecsError>, val name: NameToken?, val parameters: StarAst<ParameterAst>?, val returnType: TypeAnnotationToken?, val block: StarAst<BodyAst>?): TopLevelAst() {
    override fun generateEnvironment() = generateFnEnvironment(this)
}
@Serializable
data class ParameterAst(override val nodes: List<Ast>, override val errors: List<CortecsError>, val name: NameToken, val typeAnnotation: TypeAnnotationToken?): AstImpl() {
    override fun generateEnvironment() = EmptyEnvironment
}
@Serializable
sealed class BodyAst: AstImpl()
@Serializable
data class LetAst(override val nodes: List<Ast>, override val errors: List<CortecsError>, val name: NameToken?, val typeAnnotation: TypeAnnotationToken?, val expression: Expression?): BodyAst() {
    override fun generateEnvironment() = generateLetEnvironment(this)
}
@Serializable
data class ReturnAst(override val nodes: List<Ast>, override val errors: List<CortecsError>, val expression: Expression?): BodyAst() {
    override fun generateEnvironment() = generateReturnEnvironment(this)
}
@Serializable
data class IfAst(override val nodes: List<Ast>, override val errors: List<CortecsError>, val condition: Expression?, val block: StarAst<BodyAst>?): BodyAst() {
    override fun generateEnvironment() = generateIfEnvironment(this)
}
@Serializable
data class TopGarbageAst(override val nodes: List<Ast>, override val errors: List<CortecsError>): TopLevelAst() {
    override fun generateEnvironment() = EmptyEnvironment
}
@Serializable
data class BodyGarbageAst(override val nodes: List<Ast>, override val errors: List<CortecsError>): BodyAst() {
    override fun generateEnvironment() =  EmptyEnvironment
}
@Serializable
sealed class Expression: AstImpl() {
    var _expressionType: Type? = null
    val expressionType get() = _expressionType!!

    override fun generateEnvironment(): Environment {
        val (t, e) = generateEnvironmentAndType()
        _expressionType = t
        return e
    }

    abstract fun generateEnvironmentAndType(): Pair<Type, Environment>
}
@Serializable
data class EmptyExpression(override val errors: List<CortecsError>): Expression() {
    override val nodes = emptyList<Ast>()
    override fun generateEnvironmentAndType() = Pair(Invalid, EmptyEnvironment)
}

@Serializable
sealed class SingleExpression: Expression()
@Serializable
data class UnaryExpression(override val nodes: List<Ast>, override val errors: List<CortecsError>, val op: OperatorToken, val expression: Expression?): SingleExpression() {
    override fun generateEnvironmentAndType() = generateUnaryExpressionEnvironment(this)
}
@Serializable
data class GroupingExpression(override val nodes: List<Ast>, override val errors: List<CortecsError>, val expression: Expression?): SingleExpression() {
    override fun generateEnvironmentAndType() = generateGroupingExpressionEnvironment(this)
}
@Serializable
data class AtomicExpression(override val nodes: List<Ast>, override val errors: List<CortecsError>, val atom: AtomicExpressionToken): SingleExpression() {
    override fun generateEnvironmentAndType() = generateAtomicExpressionEnvironment(this)
}
@Serializable
sealed class CompoundExpression: Expression()
@Serializable
data class FnCallExpression(override val nodes: List<Ast>, override val errors: List<CortecsError>, val function: Expression, val arguments: StarAst<Argument>?): CompoundExpression() {
    override fun generateEnvironmentAndType() = generateFnCallExpressionEnvironment(this)
}
@Serializable
data class BinaryExpression(override val nodes: List<Ast>, override val errors: List<CortecsError>, val lhs: Expression, val op: OperatorToken, val rhs: Expression?): CompoundExpression() {
    override fun generateEnvironmentAndType() = generateBinaryExpressionEnvironment(this)
}

@Serializable
data class Argument(override val nodes: List<Ast>, override val errors: List<CortecsError>, val argument: Expression): AstImpl() {
    override fun generateEnvironment() = argument.environment
}
