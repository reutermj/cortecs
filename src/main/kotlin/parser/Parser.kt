package parser

import errors.CortecsErrors

inline fun <reified T : Ast> reuse(iterator: ParserIterator): T? {
    val node = iterator.peekNode()
    if (node is T) {
        iterator.nextNode()
        return node
    }
    return null
}

fun consumeRemainingWhitespace(builder: AstBuilder) {
    while (true) {
        val newLine = builder.consume<NewLineToken>()
        val whitespace = builder.consume<WhitespaceToken>()
        if (newLine == -1 && whitespace == -1) break
    }
}

fun consumeWhitespace(builder: AstBuilder) {
    builder.consume<WhitespaceToken>()
    builder.markErrorLocation()
    consumeRemainingWhitespace(builder)
}

inline fun <S : Ast, T : StarAst<S>> bulkLoad(nodes: List<Ast>, ctor: (List<Ast>, Int) -> T): T {
    var front = nodes.toMutableList()
    var back = mutableListOf<Ast>()
    var height = 0
    while (front.size > STAR_MAX_NODES) {
        for (i in front.indices step STAR_MAX_NODES) back.add(ctor(front.drop(i).take(STAR_MAX_NODES), height))

        front.clear()
        val temp = back
        back = front
        front = temp
        height++
    }

    return ctor(front, height)
}

sealed interface StarBuildingInstruction<out T : Ast>
data class StarKeepBuilding<T : Ast>(val node: T) : StarBuildingInstruction<T>
data class StarLastNode<T : Ast>(val node: T) : StarBuildingInstruction<T>
data object StarStopBuilding : StarBuildingInstruction<Nothing>

inline fun <S : Ast, reified T : StarAst<S>> parseStar(
    iterator: ParserIterator,
    empty: T,
    ctor: (List<Ast>, Int) -> T,
    parse: (ParserIterator) -> StarBuildingInstruction<S>
): T {
    var star = empty
    val nodes = mutableListOf<Ast>()
    while (true) {
        val node = reuse<T>(iterator)
        if (node != null) {
            if (nodes.any()) {
                star += bulkLoad(nodes, ctor)
                nodes.clear()
            }
            star += node
            continue
        }

        when (val instruction = parse(iterator)) {
            is StarKeepBuilding -> nodes.add(instruction.node)
            is StarLastNode -> {
                nodes.add(instruction.node)
                break
            }

            is StarStopBuilding -> break
        }
    }

    if (nodes.any()) star += bulkLoad(nodes, ctor)
    return star
}

fun parseProgram(iterator: ParserIterator) = parseStar(iterator, ProgramAst.empty, ::ProgramAst, ::parseTopLevel)

fun parseTopLevel(iterator: ParserIterator): StarBuildingInstruction<TopLevelAst> {
    val reused = reuse<TopLevelAst>(iterator)
    if (reused != null) return StarKeepBuilding(reused)

    val topLevel = when (iterator.peekToken()) {
        null -> return StarStopBuilding
        is FunctionToken -> parseFunction(iterator)
        else -> parseGarbageTopLevels(iterator)
    }

    return StarKeepBuilding(topLevel)
}

fun parseGarbageTopLevel(iterator: ParserIterator): StarBuildingInstruction<Ast> =
    when(val token = iterator.peekToken()) {
        is TopLevelKeyword, null -> StarStopBuilding
        else -> {
            iterator.nextToken()
            StarKeepBuilding(token)
        }
    }

fun parseGarbageTopLevels(iterator: ParserIterator) =
    GarbageTopLevelAst(parseStar(iterator, GarbageAst.empty, ::GarbageAst, ::parseGarbageTopLevel))

fun parseFunction(iterator: ParserIterator): FunctionAst {
    val builder = AstBuilder(iterator)
    builder.consume<FunctionToken>()
    consumeWhitespace(builder)

    val nameIndex = builder.consume<NameToken>()
    if (nameIndex == -1) {
        builder.emitError("Expected name", Span.zero)
        consumeRemainingWhitespace(builder)
        return FunctionAst(builder.nodes(), builder.errors(), -1, -1, -1, -1)
    }

    if (builder.consume<OpenParenToken>() == -1) {
        builder.emitError("Expected (", Span.zero)
        consumeRemainingWhitespace(builder)
        return FunctionAst(builder.nodes(), builder.errors(), nameIndex, -1, -1, -1)
    }

    consumeWhitespace(builder)
    val parametersIndex = builder.addSubnode(parseParameters(iterator))

    if (builder.consume<CloseParenToken>() == -1) {
        builder.emitError("Expected )", Span.zero)
        consumeRemainingWhitespace(builder)
        return FunctionAst(builder.nodes(), builder.errors(), nameIndex, parametersIndex, -1, -1)
    }
    consumeWhitespace(builder)

    val returnTypeIndex =
        if (builder.consume<ColonToken>() == -1) -1
        else {
            consumeWhitespace(builder)
            val returnTypeIndex = builder.consume<TypeAnnotationToken>()
            if(returnTypeIndex == -1) {
                builder.emitError("Expected type annotation", Span.zero)
                return FunctionAst(builder.nodes(), builder.errors(), nameIndex, parametersIndex, -1, -1)
            }
            returnTypeIndex
        }
    consumeWhitespace(builder)

    if (builder.consume<OpenCurlyToken>() == -1) {
        builder.emitError("Expected {", Span.zero)
        consumeRemainingWhitespace(builder)
        return FunctionAst(builder.nodes(), builder.errors(), nameIndex, parametersIndex, returnTypeIndex, -1)
    }
    consumeWhitespace(builder)

    val blockIndex = builder.addSubnode(parseBlock(iterator))

    if (builder.consume<CloseCurlyToken>() == -1) {
        builder.emitError("Expected }", Span.zero)
    }
    consumeRemainingWhitespace(builder)

    return FunctionAst(builder.nodes(), builder.errors(), nameIndex, parametersIndex, returnTypeIndex, blockIndex)
}

fun parseParameter(iterator: ParserIterator): StarBuildingInstruction<ParameterAst> {
    val builder = AstBuilder(iterator)
    val nameIndex = builder.consume<NameToken>()
    if (nameIndex == -1) return StarStopBuilding

    consumeWhitespace(builder)
    val typeAnnotationTokenIndex =
        if (builder.consume<ColonToken>() == -1) -1
        else {
            consumeWhitespace(builder)
            val typeAnnotationTokenIndex = builder.consume<TypeAnnotationToken>()
            if (typeAnnotationTokenIndex == -1) {
                builder.emitError("Expected type annotation", Span.zero)
                consumeRemainingWhitespace(builder)
                return StarLastNode(ParameterAst(builder.nodes(), builder.errors(), nameIndex, -1))
            }
            typeAnnotationTokenIndex
        }

    val commaIndex = builder.consume<CommaToken>()
    consumeRemainingWhitespace(builder)

    return if (commaIndex == -1) StarLastNode(
        ParameterAst(
            builder.nodes(),
            builder.errors(),
            nameIndex,
            typeAnnotationTokenIndex
        )
    )
    else StarKeepBuilding(ParameterAst(builder.nodes(), builder.errors(), nameIndex, typeAnnotationTokenIndex))
}

fun parseParameters(iterator: ParserIterator) =
    parseStar(iterator, ParametersAst.empty, ::ParametersAst, ::parseParameter)

fun parseBlock(iterator: ParserIterator) = parseStar(iterator, BlockAst.empty, ::BlockAst, ::parseBody)

fun parseBody(iterator: ParserIterator): StarBuildingInstruction<BodyAst> {
    val reused = reuse<BodyAst>(iterator)
    if (reused != null) return StarKeepBuilding(reused)

    val body = when (iterator.peekToken()) {
        is LetToken -> parseLet(iterator)
        is ReturnToken -> parseReturn(iterator)
        is IfToken -> parseIf(iterator)
        is FunctionToken, is CloseCurlyToken, null -> return StarStopBuilding
        else -> parseGarbageBodies(iterator)
    }

    return StarKeepBuilding(body)
}

fun parseLet(iterator: ParserIterator): LetAst {
    val builder = AstBuilder(iterator)
    builder.consume<LetToken>()
    consumeWhitespace(builder)

    val nameIndex = builder.consume<NameToken>()
    if (nameIndex == -1) {
        builder.emitError("Expected name", Span.zero)
        return LetAst(builder.nodes(), builder.errors(), -1, -1, -1)
    }
    consumeWhitespace(builder)

    val typeAnnotationIndex =
        if (builder.consume<ColonToken>() == -1) -1
        else {
            consumeWhitespace(builder)
            val typeAnnotationIndex = builder.consume<TypeAnnotationToken>()
            if(typeAnnotationIndex == -1) {
                builder.emitError("Expected type annotation", Span.zero)
                return LetAst(builder.nodes(), builder.errors(), nameIndex, -1, -1)
            }
            typeAnnotationIndex
        }
    consumeWhitespace(builder)

    if (builder.consume<EqualSignToken>() == -1) {
        builder.emitError("Expected =", Span.zero)
        consumeRemainingWhitespace(builder)
        return LetAst(builder.nodes(), builder.errors(), nameIndex, typeAnnotationIndex, -1)
    }
    consumeWhitespace(builder)


    val expressionIndex = builder.addSubnode(parseExpression(iterator))
    if (expressionIndex == -1) {
        builder.emitError("Expected expression", Span.zero)
        consumeRemainingWhitespace(builder)
    }

    return LetAst(builder.nodes(), builder.errors(), nameIndex, typeAnnotationIndex, expressionIndex)
}

fun parseReturn(iterator: ParserIterator): ReturnAst {
    val builder = AstBuilder(iterator)
    builder.consume<ReturnToken>()
    consumeWhitespace(builder)

    val expressionIndex = builder.addSubnode(parseExpression(iterator))
    if (expressionIndex == -1) {
        builder.emitError("Expected expression", Span.zero)
        consumeRemainingWhitespace(builder)
    }

    return ReturnAst(builder.nodes(), builder.errors(), expressionIndex)
}

fun parseIf(iterator: ParserIterator): IfAst {
    val builder = AstBuilder(iterator)
    builder.consume<IfToken>()
    consumeWhitespace(builder)

    if (builder.consume<OpenParenToken>() == -1) {
        builder.emitError("Expected (", Span.zero)
        consumeRemainingWhitespace(builder)
        return IfAst(builder.nodes(), builder.errors(), -1, -1)
    }
    consumeWhitespace(builder)

    val conditionIndex = builder.addSubnode(parseExpression(iterator))
    if (conditionIndex == -1) {
        builder.emitError("Expected expression", Span.zero)
        consumeRemainingWhitespace(builder)
        return IfAst(builder.nodes(), builder.errors(), -1, -1)
    }

    if (builder.consume<CloseParenToken>() == -1) {
        builder.emitError("Expected )", Span.zero)
        consumeRemainingWhitespace(builder)
        return IfAst(builder.nodes(), builder.errors(), conditionIndex, -1)
    }
    consumeWhitespace(builder)

    if (builder.consume<OpenCurlyToken>() == -1) {
        builder.emitError("Expected {", Span.zero)
        consumeRemainingWhitespace(builder)
        return IfAst(builder.nodes(), builder.errors(), conditionIndex, -1)
    }
    consumeWhitespace(builder)

    val blockIndex = builder.addSubnode(parseBlock(iterator))

    if (builder.consume<CloseCurlyToken>() == -1) {
        builder.emitError("Expected }", Span.zero)
        consumeRemainingWhitespace(builder)
    }
    consumeWhitespace(builder)

    return IfAst(builder.nodes(), builder.errors(), conditionIndex, blockIndex)
}

fun parseGarbageBody(iterator: ParserIterator): StarBuildingInstruction<Ast> =
    when(val token = iterator.peekToken()) {
        is Keyword, is CloseCurlyToken, null -> StarStopBuilding
        else -> {
            iterator.nextToken()
            StarKeepBuilding(token)
        }
    }

fun parseGarbageBodies(iterator: ParserIterator) =
    GarbageBodyAst(parseStar(iterator, GarbageAst.empty, ::GarbageAst, ::parseGarbageBody))

inline fun <reified T : BinaryExpression> parseBinaryExpressionGen(
    iterator: ParserIterator,
    acceptedTokens: Set<Char>,
    nextPrecedenceLevel: (ParserIterator) -> Expression?,
    ctor: (List<Ast>, CortecsErrors, Int, Int, Int) -> T
): Expression? {
    var lhs: Expression? = reuse<T>(iterator) ?: nextPrecedenceLevel(iterator) ?: return null
    while (true) {
        val token = iterator.peekToken()
        if (token !is OperatorToken) return lhs
        if (token.value[0] in acceptedTokens) {
            val builder = AstBuilder(iterator)
            val lhsIndex = builder.addSubnode(lhs)
            val opIndex = builder.consume<OperatorToken>()
            consumeWhitespace(builder)
            val rhsIndex = builder.addSubnode(nextPrecedenceLevel(iterator))
            if (rhsIndex == -1) {
                builder.emitError("Expected expression", Span.zero)
                return ctor(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
            }
            lhs = ctor(builder.nodes(), builder.errors(), lhsIndex, opIndex, rhsIndex)
        } else return lhs
    }
}

fun parseExpression(iterator: ParserIterator) =
    parseBinaryExpressionGen(iterator, setOf('|'), ::parseP2Expression, ::BinaryExpressionP1)

fun parseP2Expression(iterator: ParserIterator) =
    parseBinaryExpressionGen(iterator, setOf('^'), ::parseP3Expression, ::BinaryExpressionP2)

fun parseP3Expression(iterator: ParserIterator) =
    parseBinaryExpressionGen(iterator, setOf('&'), ::parseP4Expression, ::BinaryExpressionP3)

fun parseP4Expression(iterator: ParserIterator) =
    parseBinaryExpressionGen(iterator, setOf('=', '!'), ::parseP5Expression, ::BinaryExpressionP4)

fun parseP5Expression(iterator: ParserIterator) =
    parseBinaryExpressionGen(iterator, setOf('>', '<'), ::parseP6Expression, ::BinaryExpressionP5)

fun parseP6Expression(iterator: ParserIterator) =
    parseBinaryExpressionGen(iterator, setOf('+', '-', '~'), ::parseP7Expression, ::BinaryExpressionP6)

fun parseP7Expression(iterator: ParserIterator) =
    parseBinaryExpressionGen(iterator, setOf('*', '/', '%'), ::parseBaseExpression, ::BinaryExpressionP7)

fun parseBaseExpression(iterator: ParserIterator): Expression? {
    val expression = reuse<BaseExpression>(iterator) ?: when (iterator.peekToken()) {
        is OpenParenToken -> parseGroupingExpression(iterator)
        is AtomicExpressionToken -> parseAtomicExpression(iterator)
        is OperatorToken -> parseUnaryExpression(iterator)
        else -> null
    }
    if(expression == null) return null
    return parsePostfix(iterator, expression)
}

fun parsePostfix(iterator: ParserIterator, expression: Expression): Expression {
    var acc = expression
    while (true) {
        when (iterator.peekToken()) {
            is OpenParenToken -> acc = parseFunctionCallExpression(iterator, acc)
            else -> return acc
        }
    }
}

fun parseArgument(iterator: ParserIterator): StarBuildingInstruction<ArgumentAst> {
    val builder = AstBuilder(iterator)
    val expressionIndex = builder.addSubnode(parseExpression(iterator))
    if (expressionIndex == -1) return StarStopBuilding

    val commaIndex = builder.consume<CommaToken>()
    consumeRemainingWhitespace(builder)

    return if (commaIndex == -1) StarLastNode(ArgumentAst(builder.nodes(), builder.errors(), expressionIndex))
    else StarKeepBuilding(ArgumentAst(builder.nodes(), builder.errors(), expressionIndex))
}

fun parseArguments(iterator: ParserIterator): ArgumentsAst =
    parseStar(iterator, ArgumentsAst.empty, ::ArgumentsAst, ::parseArgument)

fun parseFunctionCallExpression(iterator: ParserIterator, baseExpression: Expression): Expression {
    val builder = AstBuilder(iterator)
    val functionIndex = builder.addSubnode(baseExpression)
    builder.consume<OpenParenToken>()
    consumeWhitespace(builder)

    val argumentsIndex = builder.addSubnode(parseArguments(iterator))

    if (builder.consume<CloseParenToken>() == -1) builder.emitError("Expected )", Span.zero)
    consumeRemainingWhitespace(builder)

    return FunctionCallExpression(builder.nodes(), builder.errors(), functionIndex, argumentsIndex)
}

fun parseUnaryExpression(iterator: ParserIterator): Expression {
    val builder = AstBuilder(iterator)
    val opIndex = builder.consume<OperatorToken>()
    consumeWhitespace(builder)
    val expressionIndex = builder.addSubnode(parseBaseExpression(iterator))
    if (expressionIndex == -1) {
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
    if (expressionIndex == -1) {
        builder.emitError("Expected expression", Span.zero)
        return GroupingExpression(builder.nodes(), builder.errors(), expressionIndex)
    }
    if (builder.consume<CloseParenToken>() == -1) builder.emitError("Expected )", Span.zero)
    consumeRemainingWhitespace(builder)

    return GroupingExpression(builder.nodes(), builder.errors(), expressionIndex)
}