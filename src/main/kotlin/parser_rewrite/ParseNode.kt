package parser_rewrite

import lsp.*
import tokenizer.*

interface SyntaxError {
    val offset: Int
    val column: Int
    val length: Int
}
data class MismatchedBrace(val brace: Brace): SyntaxError {
    override val offset = brace.offset
    override val column = brace.column
    override val length = 1
    override fun toString() = "Mismatched"
}
data class UnmatchedBrace(val brace: Brace): SyntaxError {
    override val offset = brace.offset
    override val column = brace.column
    override val length = 1
    override fun toString() = "Unmatched"
}
data class UnexpectedToken(val token: Token, override val column: Int): SyntaxError {
    override val offset = 0
    override val length = token.value.length
    override fun toString() = "Unexpected"
}
data class TokenColumn<T: Token>(val token: T, val column: Int)
abstract class ParseNode {
    abstract val tokens: List<Token>
    abstract val unmatchedBraces: BraceMatcher

    protected val _syntaxErrors = mutableListOf<SyntaxError>()
    val syntaxErrors: List<SyntaxError>
        get() = _syntaxErrors

    protected var index = 0
    protected var column = 0
    var killed = false
    protected inline fun <reified T: Token>expect(): TokenColumn<T>? {
        if(killed) return null
        while(index < tokens.size) {
            when(val token = tokens[index]) {
                is WhitespaceToken -> {
                    index++
                    column += token.value.length
                }
                is T -> {
                    val c = column
                    index++
                    column += token.value.length
                    return TokenColumn(token, c)
                }
                else -> {
                    _syntaxErrors.add(UnexpectedToken(token, column))
                    killed = true
                    return null
                }
            }
        }
        killed = true
        return null
    }

    protected fun <T>optional(f: () -> T?): T? {
        if(killed) return null
        val i = index
        val ret = f()
        if(ret == null) {
            killed = false
            index = i
            _syntaxErrors.removeAt(_syntaxErrors.size - 1)
        }
        return ret
    }

    fun update(startPosition: Int, endPosition: Int, text: String): List<ParseNode> {
        val builder = StringBuilder()
        var i = 0
        var j = 0
        while(j < tokens.size && i < startPosition) {
            val token = tokens[j++]
            if(i + token.value.length < startPosition) builder.append(token.value)
            else builder.append(token.value.substring(0, startPosition - i))
            i += token.value.length
        }

        builder.append(text)

        while(j < tokens.size) {
            val token = tokens[j++]
            if(i >= endPosition) builder.append(token.value)
            else if(i + token.value.length > endPosition) builder.append(token.value.substring(endPosition - i))
            i += token.value.length
        }

        return tokenize(builder.toString()).map { parseLine(it) }
    }

    fun update(startPosition: Int, endPosition: Int, text: String, endLine: ParseNode): List<ParseNode> {
        val builder = StringBuilder()
        var i = 0
        for(token in tokens) {
            if(i + token.value.length < startPosition) builder.append(token.value)
            else if(i < startPosition) builder.append(token.value.substring(0, startPosition - i))
            i += token.value.length
        }

        builder.append(text)

        i = 0
        for(token in endLine.tokens) {
            if(i >= endPosition) builder.append(token.value)
            else if(i + token.value.length >= endPosition) builder.append(token.value.substring(endPosition - i))
            i += token.value.length
        }

        return tokenize(builder.toString()).map { parseLine(it) }
    }
}

data class FnNode(override val tokens: List<Token>): ParseNode() {
    val fnKeyword = expect<FnToken>()
    val name = expect<NameToken>()
    val openParen = expect<OpenParenToken>()
    val params = optional { expect<NameToken>() }
    val closeParen = expect<CloseParenToken>()
    val openCurly = expect<OpenCurlyToken>()

    override val unmatchedBraces =
        if(openCurly != null) HardBreak(emptyList(), listOf(Brace(0, openCurly.column, BraceType.curly)))
        else HardBreak(emptyList(), emptyList())
}
data class LetNode(override val tokens: List<Token>): ParseNode() {
    override val unmatchedBraces = OpenToExtension(emptyList(), emptyList())

    val letKeyword = expect<LetToken>()
    val name = expect<NameToken>()
    val equalSign = expect<EqualSignToken>()
    val value = expect<NameToken>()
}

data class CloseCurlyNode(override val tokens: List<Token>): ParseNode() {
    val closeCurly = expect<CloseCurlyToken>()
    override val unmatchedBraces = OpenToExtension(listOf(Brace(0, closeCurly!!.column, BraceType.curly)), emptyList())
}

data class UnexpectedNode(override val tokens: List<Token>): ParseNode() {
    override val unmatchedBraces = OpenToExtension(emptyList(), emptyList())
}
data class WhitespaceNode(override val tokens: List<Token>): ParseNode() {
    override val unmatchedBraces = OpenToExtension(emptyList(), emptyList())
}

fun parseLine(tokens: List<Token>): ParseNode {
    for(token in tokens) {
        when(token) {
            is WhitespaceToken -> {}
            is NewLineToken -> return WhitespaceNode(tokens)
            is LetToken -> return LetNode(tokens)
            is FnToken -> return FnNode(tokens)
            is CloseCurlyToken -> return CloseCurlyNode(tokens)
            else ->  return UnexpectedNode(tokens)
        }
    }
    throw Exception("Programmer error")
}