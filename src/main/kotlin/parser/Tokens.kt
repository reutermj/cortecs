package parser

sealed class Token(val value: String): Ast {
    override val offset: Offset
        get() = Offset(0, value.length)
    override val firstTokenOrNull: Token?
        get() = this
    override val nodes: List<Ast>
        get() = listOf(this)

    override fun addToIterator(change: String, start: Offset, end: Offset, iter: ParserIterator, next: Token?) {
        if(keepOrDelete(start, end, iter, next)) return

        if(start.line == 0 && start.column >= 0) iter.add(value.substring(0, start.column))
        if(start > Offset.zero) iter.add(change)
        if(end.line == 0 && end.column < value.length) iter.add(value.substring(end.column))
    }

    override fun forceReparse(iter: ParserIterator) {
        iter.add(value)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Token) return false
        if(this::class != other::class) return false
        return value == other.value
    }

    override fun hashCode() = 0x13cd430a xor value.hashCode()
}

object NewLineToken: Token("\n") {
    override val offset: Offset
        get() = Offset(1, 0)

    override fun addToIterator(change: String, start: Offset, end: Offset, iter: ParserIterator, next: Token?) {
        if(keepOrDelete(start, end, iter, next)) return

        iter.add(value)
        if(start > Offset.zero) iter.add(change)
    }
}

interface Keyword
interface TopLevelKeyword
interface BodyKeyword
object LetToken: Token("let"), Keyword, BodyKeyword
object IfToken: Token("if"), Keyword, BodyKeyword
object FunctionToken: Token("function"), Keyword, TopLevelKeyword
object ReturnToken: Token("return"), Keyword, BodyKeyword

class WhitespaceToken(value: String): Token(value)
class OperatorToken(value: String): Token(value)
class TypeToken(value: String): Token(value)
class BadToken(value: String): Token(value)

sealed class AtomicExpressionToken(value: String): Token(value)
class NameToken(value: String): AtomicExpressionToken(value)
class StringToken(value: String): AtomicExpressionToken(value)
class BadStringToken(value: String): AtomicExpressionToken(value)
class CharToken(value: String): AtomicExpressionToken(value)
class BadCharToken(value: String): AtomicExpressionToken(value)
class IntToken(value: String): AtomicExpressionToken(value)
class FloatToken(value: String): AtomicExpressionToken(value)
class BadNumberToken(value: String): AtomicExpressionToken(value)

object OpenParenToken: Token("(")
object CloseParenToken: Token(")")
object OpenCurlyToken: Token("{")
object CloseCurlyToken: Token("}")

object CommaToken: Token(",")
object DotToken: Token(".")
object ColonToken: Token(":")
object BackSlashToken: Token("\\")
object EqualSignToken: Token("=")