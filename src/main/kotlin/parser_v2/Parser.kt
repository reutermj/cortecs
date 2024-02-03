package parser_v2

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

//TODO come up with better name
fun consumeWhitespace1(builder: AstBuilder) {
    builder.consume<WhitespaceToken>()
    builder.markErrorLocation()
    consumeRemainingWhitespace(builder)
}

fun parseLet(iterator: ParserIterator): LetAst {
    val letNode = reuse<LetAst>(iterator)
    if(letNode != null) return letNode

    val builder = AstBuilder(iterator)
    builder.consume<LetToken>()
    consumeWhitespace1(builder)

    val nameIndex = builder.consume<NameToken>()
    if(nameIndex == -1) {
        builder.emitError("Expected name", Span(0, 0))
        return LetAst(builder.nodes(), builder.errors(), -1, -1)
    }
    consumeWhitespace1(builder)

    if(builder.consume<EqualSignToken>() == -1) {
        builder.emitError("Expected equal sign", Span(0, 0))
        return LetAst(builder.nodes(), builder.errors(), -1, -1)
    }
    consumeWhitespace1(builder)

    val expressionIndex = builder.addSubnode(parseExpression(iterator))

    return LetAst(builder.nodes(), builder.errors(), nameIndex, expressionIndex)
}

fun parseReturn(iterator: ParserIterator): ReturnAst {
    val returnNode = reuse<ReturnAst>(iterator)
    if(returnNode != null) return returnNode

    val builder = AstBuilder(iterator)
    builder.consume<ReturnToken>()
    consumeWhitespace1(builder)

    val expressionIndex = builder.addSubnode(parseExpression(iterator))

    return ReturnAst(builder.nodes(), builder.errors(), expressionIndex)
}

fun parseExpression(iterator: ParserIterator): Expression {
    val expression = reuse<BinaryExpressionP1>(iterator)
    if(expression != null) return expression
    return parseP1PExpression(iterator, parseP2Expression(iterator))
}

fun parseP1PExpression(iterator: ParserIterator, lhs: Expression): Expression {
    val token = iterator.peekToken()
    if(token !is OperatorToken) return lhs
    return when(token.value[0]) {
        '|' -> {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace1(builder)
            val rhsIndex = builder.addSubnode(parseP2Expression(iterator))
            consumeRemainingWhitespace(builder)
            val expression = BinaryExpressionP1(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            parseP1PExpression(iterator, expression)
        }
        else -> lhs
    }
}

fun parseP2Expression(iterator: ParserIterator): Expression {
    val expression = reuse<BinaryExpressionP2>(iterator)
    if(expression != null) return expression
    return parseP2PExpression(iterator, parseP3Expression(iterator))
}

fun parseP2PExpression(iterator: ParserIterator, lhs: Expression): Expression {
    val token = iterator.peekToken()
    if(token !is OperatorToken) return lhs
    return when(token.value[0]) {
        '^' -> {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace1(builder)
            val rhsIndex = builder.addSubnode(parseP3Expression(iterator))
            consumeRemainingWhitespace(builder)
            val expression = BinaryExpressionP2(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            parseP2PExpression(iterator, expression)
        }
        else -> lhs
    }
}

fun parseP3Expression(iterator: ParserIterator): Expression {
    val expression = reuse<BinaryExpressionP3>(iterator)
    if(expression != null) return expression
    return parseP3PExpression(iterator, parseP4Expression(iterator))
}

fun parseP3PExpression(iterator: ParserIterator, lhs: Expression): Expression {
    val token = iterator.peekToken()
    if(token !is OperatorToken) return lhs
    return when(token.value[0]) {
        '&' -> {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace1(builder)
            val rhsIndex = builder.addSubnode(parseP4Expression(iterator))
            consumeRemainingWhitespace(builder)
            val expression = BinaryExpressionP3(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            parseP3PExpression(iterator, expression)
        }
        else -> lhs
    }
}

fun parseP4Expression(iterator: ParserIterator): Expression {
    val expression = reuse<BinaryExpressionP4>(iterator)
    if(expression != null) return expression
    return parseP4PExpression(iterator, parseP5Expression(iterator))
}

fun parseP4PExpression(iterator: ParserIterator, lhs: Expression): Expression {
    val token = iterator.peekToken()
    if(token !is OperatorToken) return lhs
    return when(token.value[0]) {
        '=', '!' -> {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace1(builder)
            val rhsIndex = builder.addSubnode(parseP5Expression(iterator))
            consumeRemainingWhitespace(builder)
            val expression = BinaryExpressionP4(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            parseP4PExpression(iterator, expression)
        }
        else -> lhs
    }
}

fun parseP5Expression(iterator: ParserIterator): Expression {
    val expression = reuse<BinaryExpressionP5>(iterator)
    if(expression != null) return expression
    return parseP5PExpression(iterator, parseP6Expression(iterator))
}

fun parseP5PExpression(iterator: ParserIterator, lhs: Expression): Expression {
    val token = iterator.peekToken()
    if(token !is OperatorToken) return lhs
    return when(token.value[0]) {
        '>', '<' -> {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace1(builder)
            val rhsIndex = builder.addSubnode(parseP6Expression(iterator))
            consumeRemainingWhitespace(builder)
            val expression = BinaryExpressionP5(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            parseP5PExpression(iterator, expression)
        }
        else -> lhs
    }
}

fun parseP6Expression(iterator: ParserIterator): Expression {
    val expression = reuse<BinaryExpressionP6>(iterator)
    if(expression != null) return expression
    return parseP6PExpression(iterator, parseP7Expression(iterator))
}

fun parseP6PExpression(iterator: ParserIterator, lhs: Expression): Expression {
    val token = iterator.peekToken()
    if(token !is OperatorToken) return lhs
    return when(token.value[0]) {
        '+', '-' -> {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace1(builder)
            val rhsIndex = builder.addSubnode(parseP7Expression(iterator))
            consumeRemainingWhitespace(builder)
            val expression = BinaryExpressionP6(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            parseP6PExpression(iterator, expression)
        }
        else -> lhs
    }
}

fun parseP7Expression(iterator: ParserIterator): Expression {
    val expression = reuse<BinaryExpressionP7>(iterator)
    if(expression != null) return expression
    return parseP7PExpression(iterator, parseBaseExpression(iterator))
}

fun parseP7PExpression(iterator: ParserIterator, lhs: Expression): Expression {
    val token = iterator.peekToken()
    if(token !is OperatorToken) return lhs
    return when(token.value[0]) {
        '*', '/', '%' -> {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace1(builder)
            val rhsIndex = builder.addSubnode(parseBaseExpression(iterator))
            consumeRemainingWhitespace(builder)
            val expression = BinaryExpressionP7(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            parseP7PExpression(iterator, expression)
        }
        else -> lhs
    }
}

fun parseBaseExpression(iterator: ParserIterator): Expression {
    val expression = reuse<BaseExpression>(iterator)
    if(expression != null) return expression

    return when(iterator.peekToken()) {
        is OpenParenToken -> parseGroupingExpression(iterator)
        is AtomicExpressionToken -> parseAtomicExpression(iterator)
        else -> TODO()
    }
}

fun parseAtomicExpression(iterator: ParserIterator): Expression {
    val builder = AstBuilder(iterator)
    val atomIndex = builder.consume<AtomicExpressionToken>()
    consumeRemainingWhitespace(builder)
    return AtomicExpression(builder.nodes(), builder.errors(), atomIndex)
}

fun parseGroupingExpression(iterator: ParserIterator): Expression {
    val builder = AstBuilder(iterator)
    builder.consume<OpenParenToken>()
    consumeWhitespace1(builder)

    val expressionIndex = builder.addSubnode(parseExpression(iterator))
    consumeWhitespace1(builder)

    if(builder.consume<CloseParenToken>() == -1) builder.emitError("Expected )", Span.zero)

    return GroupingExpression(builder.nodes(), builder.errors(), expressionIndex)
}