package parser_v2

import errors.CortecsErrors

inline fun <reified T: Ast>reuse(iterator: ParserIterator): T? {
    val node = iterator.peekNode()
    if(node is T) {
        iterator.nextNode()
        return node
    }
    return null
}

fun consumeRemainingWhitespace(builder: AstBuilder) {
    while(true) {
        val newLine = builder.consume<NewLineToken>()
        val whitespace = builder.consume<WhitespaceToken>()
        if(newLine == -1 && whitespace == -1) break
    }
}

fun consumeWhitespace(builder: AstBuilder) {
    builder.consume<WhitespaceToken>()
    builder.markErrorLocation()
    consumeRemainingWhitespace(builder)
}

fun parseLet(iterator: ParserIterator): LetAst {
    val letNode = reuse<LetAst>(iterator)
    if(letNode != null) return letNode

    val builder = AstBuilder(iterator)
    builder.consume<LetToken>()
    consumeWhitespace(builder)

    val nameIndex = builder.consume<NameToken>()
    if(nameIndex == -1) {
        builder.emitError("Expected name", Span.zero)
        return LetAst(builder.nodes(), builder.errors(), -1, -1)
    }
    consumeWhitespace(builder)

    if(builder.consume<EqualSignToken>() == -1) {
        builder.emitError("Expected equal sign", Span.zero)
        return LetAst(builder.nodes(), builder.errors(), -1, -1)
    }
    consumeWhitespace(builder)

    val expressionIndex = builder.addSubnode(parseExpression(iterator))
    if(expressionIndex == -1) {
        builder.emitError("Expected expression", Span.zero)
        consumeRemainingWhitespace(builder)
    }

    return LetAst(builder.nodes(), builder.errors(), nameIndex, expressionIndex)
}

fun parseReturn(iterator: ParserIterator): ReturnAst {
    val returnNode = reuse<ReturnAst>(iterator)
    if(returnNode != null) return returnNode

    val builder = AstBuilder(iterator)
    builder.consume<ReturnToken>()
    consumeWhitespace(builder)

    val expressionIndex = builder.addSubnode(parseExpression(iterator))
    if(expressionIndex == -1) {
        builder.emitError("Expected expression", Span.zero)
        consumeRemainingWhitespace(builder)
    }

    return ReturnAst(builder.nodes(), builder.errors(), expressionIndex)
}

inline fun <reified T: BinaryExpression>parseBinaryExpressionGen(iterator: ParserIterator, acceptedTokens: Set<Char>, nextPrecedenceLevel: (ParserIterator) -> Expression?, ctor: (List<Ast>, CortecsErrors, Int, Int, Int) -> T): Expression? {
    val expression = reuse<T>(iterator)
    if(expression != null) return expression

    var lhs: Expression? = nextPrecedenceLevel(iterator) ?: return null
    while(true) {
        val token = iterator.peekToken()
        if(token !is OperatorToken) return lhs
        if(token.value[0] in acceptedTokens) {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace(builder)
            val rhsIndex = builder.addSubnode(nextPrecedenceLevel(iterator))
            if(rhsIndex == -1) {
                builder.emitError("Expected expression", Span.zero)
                return ctor(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            }
            lhs = ctor(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
        } else return lhs
    }
}

fun parseExpression(iterator: ParserIterator) = parseBinaryExpressionGen(iterator, setOf('|'), ::parseP2Expression, ::BinaryExpressionP1)
fun parseP2Expression(iterator: ParserIterator) = parseBinaryExpressionGen(iterator, setOf('^'), ::parseP3Expression, ::BinaryExpressionP2)
fun parseP3Expression(iterator: ParserIterator) = parseBinaryExpressionGen(iterator, setOf('&'), ::parseP4Expression, ::BinaryExpressionP3)
fun parseP4Expression(iterator: ParserIterator) = parseBinaryExpressionGen(iterator, setOf('=', '!'), ::parseP5Expression, ::BinaryExpressionP4)
fun parseP5Expression(iterator: ParserIterator) = parseBinaryExpressionGen(iterator, setOf('>', '<'), ::parseP6Expression, ::BinaryExpressionP5)
fun parseP6Expression(iterator: ParserIterator) = parseBinaryExpressionGen(iterator, setOf('+', '-', '~'), ::parseP7Expression, ::BinaryExpressionP6)
fun parseP7Expression(iterator: ParserIterator) = parseBinaryExpressionGen(iterator, setOf('*', '/', '%'), ::parseBaseExpression, ::BinaryExpressionP7)

fun parseBaseExpression(iterator: ParserIterator): Expression? {
    val expression = reuse<BaseExpression>(iterator)
    if(expression != null) return expression

    return when(iterator.peekToken()) {
        is OpenParenToken -> parseGroupingExpression(iterator)
        is AtomicExpressionToken -> parseAtomicExpression(iterator)
        else -> null
    }
}

fun parseAtomicExpression(iterator: ParserIterator): Expression {
    val builder = AstBuilder(iterator)
    val atomIndex = builder.consume<AtomicExpressionToken>()
    consumeWhitespace(builder)
    return AtomicExpression(builder.nodes(), builder.errors(), atomIndex)
}

fun parseGroupingExpression(iterator: ParserIterator): Expression {
    val builder = AstBuilder(iterator)
    builder.consume<OpenParenToken>()
    consumeWhitespace(builder)

    val expressionIndex = builder.addSubnode(parseExpression(iterator))
    if(builder.consume<CloseParenToken>() == -1) builder.emitError("Expected )", Span.zero)
    consumeRemainingWhitespace(builder)

    return GroupingExpression(builder.nodes(), builder.errors(), expressionIndex)
}