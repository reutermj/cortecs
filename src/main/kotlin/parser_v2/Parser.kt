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

inline fun <T: StarAst>bulkLoad(nodes: List<Ast>, ctor: (List<Ast>, Int) -> T): T {
    var front = nodes.toMutableList()
    var back = mutableListOf<Ast>()
    var height = 0
    while(front.size > STAR_MAX_NODES) {
        for(i in front.indices step STAR_MAX_NODES) back.add(ctor(front.drop(i).take(STAR_MAX_NODES), height))

        front.clear()
        val temp = back
        back = front
        front = temp
        height++
    }

    return ctor(front, height)
}

inline fun <reified T: StarAst, S: Ast>parseStar(iterator: ParserIterator, empty: T, ctor: (List<Ast>, Int) -> T, parse: (ParserIterator) -> S?): T {
    var star = empty
    val nodes = mutableListOf<Ast>()
    while(true) {
        val node = reuse<T>(iterator)
        if(node != null) {
            if(nodes.any()) {
                star += bulkLoad(nodes, ctor)
                nodes.clear()
            }
            star += node
            continue
        }

        nodes.add(parse(iterator) ?: break)
    }

    if(nodes.any()) star += bulkLoad(nodes, ctor)
    return star
}

fun parseBlock(iterator: ParserIterator): BlockAst = parseStar(iterator, BlockAst.empty, ::BlockAst, ::parseBody)

fun parseBody(iterator: ParserIterator): BodyAst? {
    val body = reuse<BodyAst>(iterator)
    if(body != null) return body

    return when(iterator.peekToken()) {
        is LetToken -> parseLet(iterator)
        is ReturnToken -> parseReturn(iterator)
        else -> null
    }
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
    var lhs: Expression? = reuse<T>(iterator)?: nextPrecedenceLevel(iterator) ?: return null
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
        is OperatorToken -> parseUnaryExpression(iterator)
        else -> null
    }
}

fun parseUnaryExpression(iterator: ParserIterator): Expression {
    val builder = AstBuilder(iterator)
    val opIndex = builder.consume<OperatorToken>()
    consumeWhitespace(builder)
    val expressionIndex = builder.addSubnode(parseBaseExpression(iterator))
    if(expressionIndex == -1) {
        builder.emitError("Expected expression", Span.zero)
        return UnaryExpression(builder.nodes(), builder.errors(), opIndex, expressionIndex)
    }
    consumeWhitespace(builder)
    return UnaryExpression(builder.nodes(), builder.errors(), opIndex, expressionIndex)
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
    if(expressionIndex == -1) {
        builder.emitError("Expected expression", Span.zero)
        return GroupingExpression(builder.nodes(), builder.errors(), expressionIndex)
    }
    if(builder.consume<CloseParenToken>() == -1) builder.emitError("Expected )", Span.zero)
    consumeRemainingWhitespace(builder)

    return GroupingExpression(builder.nodes(), builder.errors(), expressionIndex)
}