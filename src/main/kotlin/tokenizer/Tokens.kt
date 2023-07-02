package tokenizer

sealed class Token() {
    abstract val value: String
    override fun toString() = value
}

object EofToken: Token() {
    override val value = "<EOF>"
}

object NewLineToken: Token() {
    override val value = "\n"
    override fun toString() = "<newline>"
}
class WhitespaceToken(override val value: String): Token()
class StringToken(override val value: String): Token()
class BadStringToken(override val value: String): Token()
class CharToken(override val value: String): Token()
class BadCharToken(override val value: String): Token()
class IntToken(override val value: String): Token()
class FloatToken(override val value: String): Token()
class BadNumberToken(override val value: String): Token()

fun writeBracket(value: String) =
    when(value) {
        "(" -> OpenParenToken
        ")" -> CloseParenToken
        "{" -> OpenCurlyToken
        "}" -> CloseCurlyToken
        else -> throw Exception()
    }
object OpenParenToken: Token() {
    override val value = "("
}
object CloseParenToken: Token() {
    override val value = ")"
}
object OpenCurlyToken: Token() {
    override val value = "{"
}
object CloseCurlyToken: Token() {
    override val value = "}"
}

fun writeName(value: String) =
    when(value) {
        "let" -> LetToken
        "fn" -> FnToken
        "return" -> ReturnToken
        "component" -> ComponentToken
        else -> NameToken(value)
    }
class NameToken(override val value: String): Token()
object LetToken: Token() {
    override val value = "let"
}
object FnToken: Token() {
    override val value = "fn"
}
object ReturnToken: Token() {
    override val value = "return"
}
object ComponentToken: Token() {
    override val value = "component"
}

object CommaToken: Token() {
    override val value = ","
}

object DotToken: Token() {
    override val value = "."
}

object ColonToken: Token() {
    override val value = ":"
}

object BackSlashToken: Token() {
    override val value = "\\"
}
object EqualSignToken: Token() {
    override val value = "="
}
object PlusToken: Token() {
    override val value = "+"
}
object MinusToken: Token() {
    override val value = "-"
}
object MulToken: Token() {
    override val value = "*"
}
object DivToken: Token() {
    override val value = "/"
}
object ModToken: Token() {
    override val value = "%"
}
object EqToken: Token() {
    override val value = "=="
}
object NeqToken: Token() {
    override val value = "!="
}
object LtToken: Token() {
    override val value = "<"
}
object LteToken: Token() {
    override val value = "<="
}
object GtToken: Token() {
    override val value = ">"
}
object GteToken: Token() {
    override val value = ">="
}
object AndToken: Token() {
    override val value = "&&"
}
object OrToken: Token() {
    override val value = "||"
}
object BitAndToken: Token() {
    override val value = "&"
}
object BitOrToken: Token() {
    override val value = "|"
}
object BitNorToken: Token() {
    override val value = "^"
}
object BitNotToken: Token() {
    override val value = "~"
}
class BadOperatorToken(override val value: String): Token()

fun writeOperator(value: String) =
    when(value) {
        "\\" ->  BackSlashToken
        "=" ->  EqualSignToken
        "+" ->  PlusToken
        "-" ->  MinusToken
        "*" ->  MulToken
        "/" ->  DivToken
        "%" ->  ModToken
        "==" ->  EqToken
        "!=" ->  NeqToken
        "<" ->  LtToken
        "<=" ->  LteToken
        ">" ->  GtToken
        ">=" ->  GteToken
        "&&" ->  AndToken
        "||" ->  OrToken
        "&" ->  BitAndToken
        "|" ->  BitOrToken
        "^" ->  BitNorToken
        "~" ->  BitNotToken
        else -> BadOperatorToken(value)
    }