package tokenizer

sealed class Token() {
    abstract val value: String
    abstract val line: Int
    abstract val column: Int
}

data class EofToken(override val line: Int, override val column: Int): Token() {
    constructor(unused: String, line: Int, column: Int): this(line, column)
    override val value = "<EOF>"
}

data class WhitespaceToken(override val value: String, override val line: Int, override val column: Int): Token()
data class StringToken(override val value: String, override val line: Int, override val column: Int): Token()
data class BadStringToken(override val value: String, override val line: Int, override val column: Int): Token()
data class CharToken(override val value: String, override val line: Int, override val column: Int): Token()
data class BadCharToken(override val value: String, override val line: Int, override val column: Int): Token()
data class IntToken(override val value: String, override val line: Int, override val column: Int): Token()
data class FloatToken(override val value: String, override val line: Int, override val column: Int): Token()
data class BadNumberToken(override val value: String, override val line: Int, override val column: Int): Token()

fun writeBracket(value: String, line: Int, column: Int) =
    when(value) {
        "(" -> OpenParenToken(line, column)
        ")" -> CloseParenToken(line, column)
        "{" -> OpenCurlyToken(line, column)
        "}" -> CloseCurlyToken(line, column)
        else -> throw Exception()
    }
data class OpenParenToken(override val line: Int, override val column: Int): Token() {
    override val value = "("
}
data class CloseParenToken(override val line: Int, override val column: Int): Token() {
    override val value = ")"
}
data class OpenCurlyToken(override val line: Int, override val column: Int): Token() {
    override val value = "{"
}
data class CloseCurlyToken(override val line: Int, override val column: Int): Token() {
    override val value = "}"
}

fun writeName(value: String, line: Int, column: Int) =
    when(value) {
        "let" -> LetToken(line, column)
        "fn" -> FnToken(line, column)
        "return" -> ReturnToken(line, column)
        "component" -> ComponentToken(line, column)
        else -> NameToken(value, line, column)
    }
data class NameToken(override val value: String, override val line: Int, override val column: Int): Token()
data class LetToken(override val line: Int, override val column: Int): Token() {
    override val value = "let"
}
data class FnToken(override val line: Int, override val column: Int): Token() {
    override val value = "fn"
}
data class ReturnToken(override val line: Int, override val column: Int): Token() {
    override val value = "return"
}
data class ComponentToken(override val line: Int, override val column: Int): Token() {
    override val value = "component"
}

data class CommaToken(override val line: Int, override val column: Int): Token() {
    constructor(unused: String, line: Int, column: Int): this(line, column)
    override val value = ","
}

data class DotToken(override val line: Int, override val column: Int): Token() {
    constructor(unused: String, line: Int, column: Int): this(line, column)
    override val value = "."
}

data class ColonToken(override val line: Int, override val column: Int): Token() {
    constructor(unused: String, line: Int, column: Int): this(line, column)
    override val value = ":"
}

data class BackSlashToken(override val line: Int, override val column: Int): Token() {
    override val value = "\\"
}
data class EqualSignToken(override val line: Int, override val column: Int): Token() {
    override val value = "="
}
data class PlusToken(override val line: Int, override val column: Int): Token() {
    override val value = "+"
}
data class MinusToken(override val line: Int, override val column: Int): Token() {
    override val value = "-"
}
data class MulToken(override val line: Int, override val column: Int): Token() {
    override val value = "*"
}
data class DivToken(override val line: Int, override val column: Int): Token() {
    override val value = "/"
}
data class ModToken(override val line: Int, override val column: Int): Token() {
    override val value = "%"
}
data class EqToken(override val line: Int, override val column: Int): Token() {
    override val value = "=="
}
data class NeqToken(override val line: Int, override val column: Int): Token() {
    override val value = "!="
}
data class LtToken(override val line: Int, override val column: Int): Token() {
    override val value = "<"
}
data class LteToken(override val line: Int, override val column: Int): Token() {
    override val value = "<="
}
data class GtToken(override val line: Int, override val column: Int): Token() {
    override val value = ">"
}
data class GteToken(override val line: Int, override val column: Int): Token() {
    override val value = ">="
}
data class AndToken(override val line: Int, override val column: Int): Token() {
    override val value = "&&"
}
data class OrToken(override val line: Int, override val column: Int): Token() {
    override val value = "||"
}
data class BitAndToken(override val line: Int, override val column: Int): Token() {
    override val value = "&"
}
data class BitOrToken(override val line: Int, override val column: Int): Token() {
    override val value = "|"
}
data class BitNorToken(override val line: Int, override val column: Int): Token() {
    override val value = "^"
}
data class BitNotToken(override val line: Int, override val column: Int): Token() {
    override val value = "~"
}
data class BadOperatorToken(override val value: String, override val line: Int, override val column: Int): Token()

fun writeOperator(value: String, line: Int, column: Int) =
    when(value) {
        "\\" ->  BackSlashToken(line, column)
        "=" ->  EqualSignToken(line, column)
        "+" ->  PlusToken(line, column)
        "-" ->  MinusToken(line, column)
        "*" ->  MulToken(line, column)
        "/" ->  DivToken(line, column)
        "%" ->  ModToken(line, column)
        "==" ->  EqToken(line, column)
        "!=" ->  NeqToken(line, column)
        "<" ->  LtToken(line, column)
        "<=" ->  LteToken(line, column)
        ">" ->  GtToken(line, column)
        ">=" ->  GteToken(line, column)
        "&&" ->  AndToken(line, column)
        "||" ->  OrToken(line, column)
        "&" ->  BitAndToken(line, column)
        "|" ->  BitOrToken(line, column)
        "^" ->  BitNorToken(line, column)
        "~" ->  BitNotToken(line, column)
        else -> BadOperatorToken(value, line, column)
    }