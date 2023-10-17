package parser

import typechecker.*

sealed interface Ast {
    val environment: Environment
    val span: Span
    val firstTokenOrNull: Token?
    val nodes: List<Ast>

    fun shouldKeep(start: Span, end: Span) = end < Span.zero || span < start
    fun shouldDelete(start: Span, end: Span) = span == Span.zero || start <= Span.zero && span <= end
    fun addToIterator(change: String, start: Span, end: Span, iter: ParserIterator, next: Token?)
    fun forceReparse(iter: ParserIterator)
    fun keepOrDelete(start: Span, end: Span, iter: ParserIterator, next: Token?) =
        if(shouldDelete(start, end)) true
        else if(shouldKeep(start, end)) {
            if(next?.shouldKeep(start - span, end - span) != false) iter.add(this) //the token following this node is kept; don't force reparse
            else forceReparse(iter) // the next element has changed so need to reparse the last element of this vector
            true
        } else false
}

sealed interface TopLevelAst: Ast
class FnAst(sequence: List<Ast>, val name: NameToken?, val parameters: StarAst<ParameterAst>?, val returnType: TypeAnnotationToken?, val block: StarAst<BodyAst>?): SequenceAst(sequence), TopLevelAst {
    override val environment: Environment by lazy { generateFnEnvironment(this) }
}
class ParameterAst(sequence: List<Ast>, val name: NameToken, val typeAnnotation: TypeAnnotationToken?): SequenceAst(sequence) {
    override val environment = EmptyEnvironment
}

sealed interface BodyAst: Ast
class LetAst(sequence: List<Ast>, val name: NameToken?, val typeAnnotation: TypeAnnotationToken?, val expression: Expression?): SequenceAst(sequence), BodyAst {
    override val environment: Environment by lazy { generateLetEnvironment(this) }
}

class ReturnAst(sequence: List<Ast>, val expression: Expression?): SequenceAst(sequence), BodyAst {
    override val environment: Environment by lazy { generateReturnEnvironment(this) }
}
class IfAst(sequence: List<Ast>, val condition: Expression?, val block: StarAst<BodyAst>?): SequenceAst(sequence), BodyAst {
    override val environment: Environment by lazy { generateIfEnvironment(this) }
}

class GarbageAst(sequence: List<Ast>): SequenceAst(sequence), TopLevelAst, BodyAst {
    override val environment: Environment
        get() = EmptyEnvironment

    //todo improve
    override fun addToIterator(change: String, start: Span, end: Span, iter: ParserIterator, next: Token?) {
        var s = start
        var e = end
        for (i in nodes.indices) {
            val eNext =
                if(i + 1 in nodes.indices) nodes[i + 1]
                else next
            nodes[i].addToIterator(change, s, e, iter, eNext?.firstTokenOrNull)

            s -= nodes[i].span
            e -= nodes[i].span
        }
    }
}

sealed interface Expression: Ast {
    val type: Type
}

sealed interface SingleExpression: Expression
class UnaryExpression(sequence: List<Ast>, val op: OperatorToken, val expression: Expression?): SequenceAst(sequence), SingleExpression {
    override lateinit var type: Type
    override val environment: Environment by lazy {
        val (t, c) = generateUnaryExpressionEnvironment(this)
        type = t
        c
    }
}
class GroupingExpression(sequence: List<Ast>, val expression: Expression?): SequenceAst(sequence), SingleExpression {
    override lateinit var type: Type
    override val environment: Environment by lazy {
        val (t, c) = generateGroupingExpressionEnvironment(this)
        type = t
        c
    }
}
class AtomicExpression(sequence: List<Ast>, val atom: AtomicExpressionToken): SequenceAst(sequence), SingleExpression {
    override lateinit var type: Type
    override val environment: Environment by lazy {
        val (t, c) = generateAtomicExpressionEnvironment(this)
        type = t
        c
    }
}

sealed interface CompoundExpression: Expression
class FnCallExpression(sequence: List<Ast>, val function: Expression, val arguments: StarAst<Argument>?): SequenceAst(sequence), CompoundExpression {
    override lateinit var type: Type
    override val environment: Environment by lazy {
        val (t, c) = generateFnCallExpressionEnvironment(this)
        type = t
        c
    }
}
class BinaryExpression(sequence: List<Ast>, val lhs: Expression, val op: OperatorToken, val rhs: Expression?): SequenceAst(sequence), CompoundExpression {
    override lateinit var type: Type
    override val environment: Environment by lazy {
        val (t, c) = generateBinaryExpressionEnvironment(this)
        type = t
        c
    }
}

class Argument(sequence: List<Ast>, val argument: Expression): SequenceAst(sequence) {
    override val environment = EmptyEnvironment
}
