package parser

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
    abstract val syntaxErrors: List<SyntaxError>

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
    val name: NameToken?
    val arguments: List<NameToken>
    override val unmatchedBraces: BraceMatcher
    override val syntaxErrors: List<SyntaxError>
    init {
        val errors = mutableListOf<SyntaxError>()
        var column = 0
        var i = 0

        val leftBraces = mutableListOf<Brace>()
        val rightBraces = mutableListOf<Brace>()

        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is FnToken -> {
                    i++
                    column += token.value.length
                    break
                }
                is CloseCurlyToken ->
                    if(rightBraces.any()) rightBraces.removeLast()
                    else leftBraces.add(Brace(0, column, BraceType.curly))
                is OpenCurlyToken -> rightBraces.add(Brace(0, column, BraceType.curly))
                is WhitespaceToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }

        for(brace in rightBraces) errors.add(UnmatchedBrace(brace))
        rightBraces.clear()
        val outLeftBraces = leftBraces.toList()

        var nameIndex = -1
        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is NameToken -> {
                    nameIndex = i
                    i++
                    column += token.value.length
                    break
                }
                is WhitespaceToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }
        name =
            if(nameIndex == -1) null
            else tokens[nameIndex] as NameToken

        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is OpenParenToken -> {
                    i++
                    column += token.value.length
                    break
                }
                is WhitespaceToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }

        val args = mutableListOf<NameToken>()
        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is CloseParenToken -> {
                    i++
                    column += token.value.length
                    break
                }
                is NameToken -> args.add(token)
                is WhitespaceToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }
        arguments = args

        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is CloseCurlyToken ->
                    if(rightBraces.any()) rightBraces.removeLast()
                    else leftBraces.add(Brace(0, column, BraceType.curly))
                is OpenCurlyToken -> rightBraces.add(Brace(0, column, BraceType.curly))
                is WhitespaceToken -> {}
                is NewLineToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }

        for(brace in leftBraces) errors.add(UnmatchedBrace(brace))
        unmatchedBraces = HardBreak(outLeftBraces, rightBraces)

        syntaxErrors = errors
    }
}
data class LetNode(override val tokens: List<Token>): ParseNode() {
    val name: NameToken?
    val value: NameToken?
    override val unmatchedBraces: BraceMatcher
    override val syntaxErrors: List<SyntaxError>

    init {
        val errors = mutableListOf<SyntaxError>()
        var column = 0
        var i = 0

        val leftBraces = mutableListOf<Brace>()
        val rightBraces = mutableListOf<Brace>()

        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is LetToken -> {
                    i++
                    column += token.value.length
                    break
                }
                is CloseCurlyToken ->
                    if(rightBraces.any()) rightBraces.removeLast()
                    else leftBraces.add(Brace(0, column, BraceType.curly))
                is OpenCurlyToken -> rightBraces.add(Brace(0, column, BraceType.curly))
                is WhitespaceToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }

        var nameIndex = -1
        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is NameToken -> {
                    nameIndex = i
                    i++
                    column += token.value.length
                    break
                }
                is WhitespaceToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }
        name =
            if(nameIndex == -1) null
            else tokens[nameIndex] as NameToken

        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is EqualSignToken -> {
                    i++
                    column += token.value.length
                    break
                }
                is WhitespaceToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }

        var valueIndex = -1
        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is NameToken -> {
                    valueIndex = i
                    i++
                    column += token.value.length
                    break
                }
                is WhitespaceToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }
        value =
            if(valueIndex == -1) null
            else tokens[valueIndex] as NameToken

        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is CloseCurlyToken ->
                    if(rightBraces.any()) rightBraces.removeLast()
                    else leftBraces.add(Brace(0, column, BraceType.curly))
                is OpenCurlyToken -> rightBraces.add(Brace(0, column, BraceType.curly))
                is WhitespaceToken -> {}
                is NewLineToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }

        unmatchedBraces = OpenToExtension(leftBraces, rightBraces)

        syntaxErrors = errors
    }
}

data class CatchAllNode(override val tokens: List<Token>): ParseNode() {
    override val unmatchedBraces: BraceMatcher
    override val syntaxErrors: List<SyntaxError>

    init {
        val errors = mutableListOf<SyntaxError>()
        var column = 0
        var i = 0

        val leftBraces = mutableListOf<Brace>()
        val rightBraces = mutableListOf<Brace>()

        while(i < tokens.size) {
            val token = tokens[i]
            when(token) {
                is CloseCurlyToken ->
                    if(rightBraces.any()) rightBraces.removeLast()
                    else leftBraces.add(Brace(0, column, BraceType.curly))
                is OpenCurlyToken -> rightBraces.add(Brace(0, column, BraceType.curly))
                is WhitespaceToken -> {}
                is NewLineToken -> {}
                else -> errors.add(UnexpectedToken(token, column))
            }
            i++
            column += token.value.length
        }

        unmatchedBraces = OpenToExtension(leftBraces, rightBraces)
        syntaxErrors = errors
    }
}

fun parseLine(tokens: List<Token>): ParseNode {
    for(token in tokens) {
        when(token) {
            is LetToken -> return LetNode(tokens)
            is FnToken -> return FnNode(tokens)
            else -> {}
        }
    }
    return CatchAllNode(tokens)
}