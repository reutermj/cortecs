package parser

import typechecker.*

sealed interface Token: Ast {
    val value: String
}

sealed class TokenImpl(override val value: String): Token {
    override val environment = EmptyEnvironment
    override val span: Span
        get() = Span(0, value.length)
    override val firstTokenOrNull: Token?
        get() = this
    override val nodes: List<Ast>
        get() = listOf(this)

    override fun addToIterator(change: String, start: Span, end: Span, iter: ParserIterator, next: Token?) {
        if(keepOrDelete(start, end, iter, next)) return

        if(start.line == 0 && start.column >= 0) iter.add(value.substring(0, start.column))
        if(start > Span.zero) iter.add(change)
        if(end.line == 0 && end.column < value.length) iter.add(value.substring(end.column))
    }

    override fun forceReparse(iter: ParserIterator) {
        iter.add(value)
    }

    override fun toString() = value

    override fun equals(other: Any?): Boolean {
        if (other !is Token) return false
        if(this::class != other::class) return false
        return value == other.value
    }

    override fun hashCode() = 0x13cd430a xor value.hashCode()
}

object NewLineToken: TokenImpl("\n") {
    override val value = "\n"
    override val span: Span
        get() = Span(1, 0)

    override fun addToIterator(change: String, start: Span, end: Span, iter: ParserIterator, next: Token?) {
        if(keepOrDelete(start, end, iter, next)) return

        iter.add(value)
        if(start > Span.zero) iter.add(change)
    }
}

sealed interface BindableToken: Token
//only used internally in the typechecker
object ReturnTypeToken: TokenImpl("<return type>"), BindableToken

sealed interface TypeAnnotationToken: Token

sealed interface Keyword: Token
sealed interface TopLevelKeyword: Token
sealed interface BodyKeyword: Token
object LetToken: TokenImpl("let"), Keyword, BodyKeyword
object IfToken: TokenImpl("if"), Keyword, BodyKeyword
object FnToken: TokenImpl("fn"), Keyword, TopLevelKeyword
object ReturnToken: TokenImpl("return"), Keyword, BodyKeyword

class WhitespaceToken(value: String): TokenImpl(value)
class OperatorToken(value: String): TokenImpl(value), BindableToken
class TypeToken(value: String): TokenImpl(value), TypeAnnotationToken
class BadToken(value: String): TokenImpl(value)

sealed interface AtomicExpressionToken: Token
class NameToken(value: String): TokenImpl(value), AtomicExpressionToken, BindableToken, TypeAnnotationToken
class StringToken(value: String): TokenImpl(value), AtomicExpressionToken
class BadStringToken(value: String): TokenImpl(value), AtomicExpressionToken
class CharToken(value: String): TokenImpl(value), AtomicExpressionToken
class BadCharToken(value: String): TokenImpl(value), AtomicExpressionToken
class IntToken(value: String): TokenImpl(value), AtomicExpressionToken
class FloatToken(value: String): TokenImpl(value), AtomicExpressionToken
class BadNumberToken(value: String): TokenImpl(value), AtomicExpressionToken

object OpenParenToken: TokenImpl("(")
object CloseParenToken: TokenImpl(")")
object OpenCurlyToken: TokenImpl("{")
object CloseCurlyToken: TokenImpl("}")

object CommaToken: TokenImpl(",")
object DotToken: TokenImpl(".")
object ColonToken: TokenImpl(":")
object BackSlashToken: TokenImpl("\\")
object EqualSignToken: TokenImpl("=")