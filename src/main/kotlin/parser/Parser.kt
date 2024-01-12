package parser

import errors.CortecsError

fun parseProgram(iterator: ParserIterator): StarAst<TopLevelAst> = buildStarAst(iterator) {
    when(iterator.peekToken()) {
        is FnToken -> parseFn(getBuilder())
        else -> parseGarbageTopLevel(getBuilder())
    }.let { KeepBuildingStar(it) }
}

fun parseFn(builder: AstBuilder): FnAst = buildAst(builder) {
    consume<FnToken>()
    val name = consume<NameToken> { t, s -> CortecsError("Expected name token", s, Span.zero) } ?: return@buildAst FnAst(getSequence(), errors, null, null, null, null)

    consume<OpenParenToken> { t, s -> CortecsError("Expected (", s, Span.zero) } ?: return@buildAst FnAst(getSequence(), errors, name, null, null, null)
    val parameters = parseParameters(getBuilder())
    consume<CloseParenToken> { t, s -> CortecsError("Expected )", s, Span.zero) } ?: return@buildAst FnAst(getSequence(), errors, name, parameters, null, null)

    val returnType = consume<ColonToken>()?.let { consume<TypeAnnotationToken> { t, s -> CortecsError("Expected return type", s, Span.zero) } ?: return@buildAst FnAst(getSequence(), errors, name, parameters, null, null) }

    consume<OpenCurlyToken> { t, s -> CortecsError("Expected {", s, Span.zero) } ?: return@buildAst FnAst(getSequence(), errors, name, parameters, returnType, null)
    val block = parseBlock(getBuilder())
    consume<CloseCurlyToken> { t, s -> CortecsError("Expected }", s, Span.zero) } ?: return@buildAst FnAst(getSequence(), errors, name, parameters, returnType, block)

    FnAst(getSequence(), errors, name, parameters, returnType, block)
}

fun parseGarbageTopLevel(builder: AstBuilder): TopGarbageAst = buildAst(builder) {
    while(iterator.hasNext()) {
        when(peekToken()) {
            is TopLevelKeyword -> break
            is BodyKeyword -> parseBlock(getBuilder())
            else -> consume<Token>()
        }
    }
    TopGarbageAst(getSequence(), emptyList())
}

fun parseParameters(builder: AstBuilder): StarAst<ParameterAst> = buildStarAst(builder) {
    buildAst(getBuilder()) {
        val name = consume<NameToken>() ?: return@buildStarAst StopBuildingStar
        val type = consume<ColonToken>()?.let { consume<TypeAnnotationToken> { t, s -> CortecsError("Expected type annotation", s, Span.zero) } ?: return@buildAst ParameterAst(getSequence(), errors, name, null) }
        consume<CommaToken>()
        ParameterAst(getSequence(), errors, name, type)
    }.let { KeepBuildingStar(it) }
}

fun parseBlock(builder: AstBuilder): StarAst<BodyAst> = buildStarAst(builder) {
    when(iterator.peekToken()) {
        is TopLevelKeyword -> StopBuildingStar
        is CloseCurlyToken -> StopBuildingStar
        is IfToken -> KeepBuildingStar(parseIf(getBuilder()))
        is LetToken -> KeepBuildingStar(parseLet(getBuilder()))
        is ReturnToken -> KeepBuildingStar(parseReturn(getBuilder()))
        else -> KeepBuildingStar(parseGarbageBody(getBuilder()))
    }
}

fun parseIf(builder: AstBuilder) = buildAst(builder) {
    consume<IfToken>()
    consume<OpenParenToken> { t, s -> CortecsError("Expected (", s, Span.zero) } ?: return@buildAst IfAst(getSequence(), errors, null, null)
    val condition = parseExpression(getBuilder()) ?: return@buildAst IfAst(getSequence(), errors, null, null)
    consume<CloseParenToken> { t, s -> CortecsError("Expected )", s, Span.zero) } ?: return@buildAst IfAst(getSequence(), errors, condition, null)

    consume<OpenCurlyToken> { t, s -> CortecsError("Expected {", s, Span.zero) } ?: return@buildAst IfAst(getSequence(), errors, condition, null)
    val block = parseBlock(getBuilder())
    consume<CloseCurlyToken> { t, s -> CortecsError("Expected }", s, Span.zero) } ?: return@buildAst IfAst(getSequence(), errors, condition, block)

    IfAst(getSequence(), errors, condition, block)
}

fun parseLet(builder: AstBuilder) = buildAst(builder) {
    consume<LetToken>()
    val name = consume<NameToken> { t, s -> CortecsError("Expected name", s, Span.zero) } ?: return@buildAst LetAst(getSequence(), errors, null, null, null)
    val type = consume<ColonToken>()?.let { consume<TypeAnnotationToken> { t, s -> CortecsError("Expected type annotation", s, Span.zero) } ?: return@buildAst LetAst(getSequence(), errors, name, null, null) }
    consume<EqualSignToken> { t, s -> CortecsError("Expected =", s, Span.zero) } ?: return@buildAst LetAst(getSequence(), errors, name, type, null)
    val expression = parseExpression(getBuilder())
    LetAst(getSequence(), errors, name, type, expression)
}

fun parseReturn(builder: AstBuilder) = buildAst(builder) {
    consume<ReturnToken>()
    val expression = parseExpression(getBuilder())
    ReturnAst(getSequence(), errors, expression)
}

fun parseGarbageBody(builder: AstBuilder): BodyGarbageAst = buildAst(builder) {
    while(iterator.hasNext()) {
        when(peekToken()) {
            is Keyword -> break
            is CloseCurlyToken -> break
            else -> consume<Token>()
        }
    }
    BodyGarbageAst(getSequence(), emptyList())
}

fun tryReuseExpression(node: Expression, minBindingPower: Int) =
    when(node) {
        is SingleExpression -> ReuseInstructions.dontProgress
        is BinaryExpression -> {
            //this covers cases where the iterator contains something like: "x", "*", BinaryExpression("y", "+", "z")
            //the "*" has higher binding power than "+", so the BinaryExpression can't be reused.
            //inject the nodes of the BinaryExpression into the iterator and parse them individually
            val (lhsBindingPower, _) = infixBindingPower(node.op)
            if(lhsBindingPower < minBindingPower) ReuseInstructions.inject
            else ReuseInstructions.reuse
        }
        else -> ReuseInstructions.reuse
    }

fun parseExpression(builder: AstBuilder, minBindingPower: Int = 0): Expression =
    buildAst(builder, { tryReuseExpression(it, minBindingPower) }) {
        var lhs: Expression = parsePrefix(getBuilder()) ?: return@buildAst EmptyExpression(errors)
        while(true) {
            lhs = when(val token = peekToken()) {
                is OperatorToken -> parseBinaryExpression(getBuilder(), lhs, token, minBindingPower) ?: break
                is OpenParenToken -> parseFunctionCallExpression(getBuilder(), lhs)
                else -> break
            }
        }

        lhs
    }

fun parsePrefix(builder: AstBuilder): SingleExpression? =
    when(builder.peekToken()) {
        is OperatorToken -> parseUnaryExpression(builder)
        is OpenParenToken -> parseGroupingExpression(builder)
        is AtomicExpressionToken -> parseAtomicExpression(builder)
        else -> {
            builder.emitError("Expected expression")
            null
        }
    }

fun parseBinaryExpression(builder: AstBuilder, lhs: Expression, operator: OperatorToken, minBindingPower: Int): BinaryExpression? {
    val (lhsBindingPower, rhsBindingPower) = infixBindingPower(operator)
    if(lhsBindingPower < minBindingPower) return null

    return buildAst(builder) {
        add(lhs, true, true)
        consume<OperatorToken>()
        val rhs = parseExpression(getBuilder(), rhsBindingPower)
        BinaryExpression(getSequence(), errors, lhs, operator, rhs)
    }
}

fun parseFunctionCallExpression(builder: AstBuilder, lhs: Expression) = buildAst(builder) {
    add(lhs, true, true)
    consume<OpenParenToken>()
    val arguments = parseArguments(getBuilder())
    consume<CloseParenToken>() ?: builder.emitError("Expected )")
    FnCallExpression(getSequence(), errors, lhs, arguments)
}

fun parseUnaryExpression(builder: AstBuilder) = buildAst(builder) {
    val operator = consume<OperatorToken>() ?: throw Exception("Programmer error")
    val expression = parseExpression(getBuilder(), Int.MAX_VALUE)
    UnaryExpression(getSequence(), errors, operator, expression)
}

fun parseGroupingExpression(builder: AstBuilder) = buildAst(builder) {
    consume<OpenParenToken>()
    val expression = parseExpression(getBuilder())
    consume<CloseParenToken>() ?: builder.emitError("Expected )")
    GroupingExpression(getSequence(), errors, expression)
}

fun parseAtomicExpression(builder: AstBuilder) = buildAst(builder) {
    val atom = consume<AtomicExpressionToken>() ?: throw Exception("programmer error")
    AtomicExpression(getSequence(), errors, atom)
}

fun parseArguments(builder: AstBuilder): StarAst<Argument> = buildStarAst(builder) {
    buildAst(getBuilder()) {
        val expression = parseExpression(getBuilder())
        if(expression is EmptyExpression) return@buildStarAst StopBuildingStar
        consume<CommaToken>() //TODO handle the case f(x y)
        Argument(getSequence(), errors, expression)
    }.let { KeepBuildingStar(it) }
}

fun infixBindingPower(op: OperatorToken) =
    when(op.value.first()) {
        '|' -> Pair(1, 2)
        '^' -> Pair(3, 4)
        '&' -> Pair(5, 6)
        '=', '!' -> Pair(7, 8)
        '>', '<' -> Pair(9, 10)
        '+', '-'  -> Pair(11, 12)
        '*', '/', '%' -> Pair(13, 14)
        else -> TODO()
    }
