package parser

fun parseProgram(iterator: ParserIterator): StarAst<TopLevelAst> = buildStarAst(iterator) {
    when(iterator.peekToken()) {
        is FunctionToken -> parseFunction(getBuilder())
        else -> parseGarbageTopLevel(getBuilder())
    }.let { KeepBuildingStar(it) }
}

fun parseFunction(builder: AstBuilder): FunctionAst = buildAst(builder) {
    consume<FunctionToken>()
    val name = consume<NameToken>()

    consume<OpenParenToken>() ?: return@buildAst FunctionAst(getSequence(), name, null, null)
    val parameters = parseParameters(getBuilder())
    consume<CloseParenToken>() ?: return@buildAst FunctionAst(getSequence(), name, parameters, null)

    consume<OpenCurlyToken>() ?: return@buildAst FunctionAst(getSequence(), name, parameters, null)
    val block = parseBlock(getBuilder())
    consume<CloseCurlyToken>() ?: return@buildAst FunctionAst(getSequence(), name, parameters, block)

    FunctionAst(getSequence(), name, parameters, block)
}

fun parseGarbageTopLevel(builder: AstBuilder): GarbageAst = buildAst(builder) {
    while(iterator.hasNext()) {
        when(peekToken()) {
            is TopLevelKeyword -> break
            is BodyKeyword -> parseBlock(getBuilder())
            else -> consume<Token>()
        }
    }
    GarbageAst(getSequence())
}

fun parseParameters(builder: AstBuilder): StarAst<ParameterAst> = buildStarAst(builder) {
    buildAst(getBuilder()) {
        val name = consume<NameToken>() ?: return@buildStarAst StopBuildingStar
        val type = consume<ColonToken>()?.let { consume<TypeToken>() }
        consume<CommaToken>()
        ParameterAst(getSequence(), name, type)
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
    consume<OpenParenToken>() ?: return@buildAst IfAst(getSequence(), null, null)
    val condition = parseExpression(getBuilder()) ?: return@buildAst IfAst(getSequence(), null, null)
    consume<CloseParenToken>() ?: return@buildAst IfAst(getSequence(), condition, null)

    consume<OpenCurlyToken>() ?: return@buildAst IfAst(getSequence(), condition, null)
    val block = parseBlock(getBuilder())
    consume<CloseCurlyToken>() ?: return@buildAst IfAst(getSequence(), condition, block)

    IfAst(getSequence(), condition, block)
}

fun parseLet(builder: AstBuilder) = buildAst(builder) {
    consume<LetToken>()
    val name = consume<NameToken>() ?: return@buildAst LetAst(getSequence(), null, null)
    consume<EqualSignToken>() ?: return@buildAst LetAst(getSequence(), name, null)
    val expression = parseExpression(getBuilder()) ?: return@buildAst LetAst(getSequence(), name, null)
    LetAst(getSequence(), name, expression)
}

fun parseReturn(builder: AstBuilder) = buildAst(builder) {
    consume<ReturnToken>()
    val expression = parseExpression(getBuilder())
    ReturnAst(getSequence(), expression)
}

fun parseGarbageBody(builder: AstBuilder): GarbageAst = buildAst(builder) {
    while(iterator.hasNext()) {
        when(peekToken()) {
            is Keyword -> break
            is CloseCurlyToken -> break
            else -> consume<Token>()
        }
    }
    GarbageAst(getSequence())
}

fun tryReuseExpression(node: Expression, minBindingPower: Int) =
    when(node) {
        is SingleExpression -> ReuseInstructions.dontProgress
        is BinaryOpExpression -> {
            //this covers cases where the iterator contains something like: "x", "*", BinaryExpression("y", "+", "z")
            //the "*" has higher binding power than "+", so the BinaryExpression can't be reused.
            //inject the nodes of the BinaryExpression into the iterator and parse them individually
            val (lhsBindingPower, _) = infixBindingPower(node.op)
            if(lhsBindingPower < minBindingPower) ReuseInstructions.inject
            else ReuseInstructions.reuse
        }
        else -> ReuseInstructions.reuse
    }

fun parseExpression(builder: AstBuilder, minBindingPower: Int = 0): Expression? =
    buildAst(builder, { tryReuseExpression(it, minBindingPower) }) {
        var lhs: Expression = parsePrefix(getBuilder()) ?: return@parseExpression null
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
        else -> null
    }

fun parseBinaryExpression(builder: AstBuilder, lhs: Expression, operator: OperatorToken, minBindingPower: Int): BinaryOpExpression? {
    val (lhsBindingPower, rhsBindingPower) = infixBindingPower(operator)
    if(lhsBindingPower < minBindingPower) return null

    return buildAst(builder) {
        add(lhs)
        consume<OperatorToken>()
        val rhs = parseExpression(getBuilder(), rhsBindingPower)
        BinaryOpExpression(getSequence(), lhs, operator, rhs)
    }
}

fun parseFunctionCallExpression(builder: AstBuilder, lhs: Expression) = buildAst(builder) {
    add(lhs)
    consume<OpenParenToken>()
    val arguments = parseArguments(getBuilder())
    consume<CloseParenToken>()
    FunctionCallExpression(getSequence(), lhs, arguments)
}

fun parseUnaryExpression(builder: AstBuilder) = buildAst(builder) {
    val operator = consume<OperatorToken>() ?: throw Exception("Programmer error")
    val expression = parseExpression(getBuilder(), Int.MAX_VALUE)
    UnaryExpression(getSequence(), operator, expression)
}

fun parseGroupingExpression(builder: AstBuilder) = buildAst(builder) {
    consume<OpenParenToken>()
    val expression = parseExpression(getBuilder()) ?: return@buildAst GroupingExpression(getSequence(), null)
    consume<CloseParenToken>() ?: return@buildAst GroupingExpression(getSequence(), expression)
    GroupingExpression(getSequence(), expression)
}

fun parseAtomicExpression(builder: AstBuilder) = buildAst(builder) {
    val atom = consume<AtomicExpressionToken>() ?: throw Exception("programmer error")
    AtomicExpression(getSequence(), atom)
}

fun parseArguments(builder: AstBuilder): StarAst<Argument> = buildStarAst(builder) {
    buildAst(getBuilder()) {
        val expression = parseExpression(getBuilder()) ?: return@buildStarAst StopBuildingStar
        consume<CommaToken>()
        Argument(getSequence(), expression)
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
