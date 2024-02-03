package parser_v2

import errors.*
import kotlinx.serialization.*

sealed interface Token

@Serializable
sealed class TokenImpl: Ast(), Token {
    override val errors: CortecsErrors
        get() = CortecsErrors(null, emptyList())
    abstract val value: String
    override val span get() = Span(0, value.length)
    override fun firstTokenOrNull(): TokenImpl? = this
    override fun forceReparse(iter: ParserIterator) {
        iter.add(value)
    }
    override fun addToIterator(change: Change, iter: ParserIterator, wasNextTokenModified: Boolean): Boolean {
        if(shouldDelete(change.start, change.end)) return true
        if(shouldKeep(change.start, change.end)) {
            if(wasNextTokenModified) forceReparse(iter) // the next element has changed so need to reparse the last element of this node
            else iter.add(this) //the token following this node is kept; don't force reparse
            return false
        }
        if(change.end.line == 0 && change.end.column < value.length) iter.add(value.substring(change.end.column))
        if(change.start > Span.zero) iter.add(change.text)
        if(change.start.line == 0 && change.start.column >= 0) iter.add(value.substring(0, change.start.column))
        return true
    }
    override fun addAllButFirstToIterator(iter: ParserIterator) {}
}

@Serializable
data object NewLineToken: TokenImpl() {
    override val value = "\n"
    override val span get() = Span(1, 0)
    override fun addToIterator(change: Change, iter: ParserIterator, wasNextTokenModified: Boolean): Boolean {
        if(shouldDelete(change.start, change.end)) return true
        if(shouldKeep(change.start, change.end)) {
            if(wasNextTokenModified) forceReparse(iter) // the next element has changed so need to reparse the last element of this node
            else iter.add(this) //the token following this node is kept; don't force reparse
            return false
        }

        if(change.start > Span.zero) iter.add(change.text)
        iter.add(value)
        return true
    }
}

sealed interface BindableToken: Token
//only used internally in the typechecker
@Serializable
data object ReturnTypeToken: TokenImpl(), BindableToken {
    override val value = "<return type>"
}

sealed interface Keyword: Token

sealed interface TopLevelKeyword: Keyword

sealed interface BodyKeyword: Keyword
@Serializable
data object LetToken: TokenImpl(), BodyKeyword {
    override val value = "let"
}
@Serializable
data object IfToken: TokenImpl(), BodyKeyword {
    override val value = "if"
}
@Serializable
data object FunctionToken: TokenImpl(), TopLevelKeyword {
    override val value = "function"
}
@Serializable
data object ReturnToken: TokenImpl(), BodyKeyword {
    override val value = "return"
}

@Serializable
data class WhitespaceToken(override val value: String): TokenImpl()
@Serializable
data class OperatorToken(override val value: String): TokenImpl(), BindableToken

sealed interface TypeAnnotationToken: Token
@Serializable
data class TypeToken(override val value: String): TokenImpl(), TypeAnnotationToken
@Serializable
data class BadToken(override val value: String): TokenImpl()

sealed interface AtomicExpressionToken: Token
@Serializable
data class NameToken(override val value: String): TokenImpl(), AtomicExpressionToken, BindableToken, TypeAnnotationToken
@Serializable
data class StringToken(override val value: String): TokenImpl(), AtomicExpressionToken
@Serializable
data class BadStringToken(override val value: String): TokenImpl(), AtomicExpressionToken
@Serializable
data class CharToken(override val value: String): TokenImpl(), AtomicExpressionToken
@Serializable
data class BadCharToken(override val value: String): TokenImpl(), AtomicExpressionToken
@Serializable
data class IntToken(override val value: String): TokenImpl(), AtomicExpressionToken
@Serializable
data class FloatToken(override val value: String): TokenImpl(), AtomicExpressionToken
@Serializable
data class BadNumberToken(override val value: String): TokenImpl(), AtomicExpressionToken
@Serializable
data object OpenParenToken: TokenImpl() {
    override val value = "("
}
@Serializable
data object CloseParenToken: TokenImpl() {
    override val value = ")"
}
@Serializable
data object OpenCurlyToken: TokenImpl() {
    override val value = "{"
}
@Serializable
data object CloseCurlyToken: TokenImpl() {
    override val value = "}"
}
@Serializable
data object CommaToken: TokenImpl() {
    override val value = ","
}
@Serializable
data object DotToken: TokenImpl() {
    override val value = "."
}
@Serializable
data object ColonToken: TokenImpl() {
    override val value = ":"
}
@Serializable
data object BackSlashToken: TokenImpl() {
    override val value = "/"
}
@Serializable
data object EqualSignToken: TokenImpl() {
    override val value = "="
}