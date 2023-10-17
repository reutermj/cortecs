package parser



fun parseCharaterByCharater() {
    val inIterator = ParserIterator()
    inIterator.add("")
    var program = parseProgram(inIterator)

    val s = "fn f(x) {}"
    var position = 1
    var line = 0
    var column = 0
    for(c in s) {
        val changeIterator = constructChangeIterator(program, "$c", Span(line, column), Span(line, column))
        program = parseProgram(changeIterator)

        if(c == '\n') {
            line++
            column = 0
        } else column++


        val goldString = s.substring(0, position)
        val goldIterator = ParserIterator()
        goldIterator.add(goldString)
        val goldProgram = parseProgram(goldIterator)

        position++
    }
}

fun tokenToLiteral(token: Token): String {
    return when(token) {
        is BadToken -> "BadToken(\"${token.value}\")"
        is CharToken -> "CharToken(\"${token.value}\")"
        is FloatToken -> "FloatToken(\"${token.value}\")"
        is IntToken -> "IntToken(\"${token.value}\")"
        is NameToken -> "NameToken(\"${token.value}\")"
        is StringToken -> "StringToken(\"\"\"${token.value}\"\"\")"
        BackSlashToken -> "BackSlashToken"
        is BadCharToken -> "BadCharToken(\"${token.value}\")"
        is BadNumberToken -> "BadNumberToken(\"${token.value}\")"
        is BadStringToken -> "BadStringToken(\"${token.value}\")"
        CloseCurlyToken -> "CloseCurlyToken"
        CloseParenToken -> "CloseParenToken"
        ColonToken ->"ColonToken"
        CommaToken -> "CommaToken"
        DotToken -> "DotToken"
        EqualSignToken -> "EqualSignToken"
        FnToken -> "FnToken"
        IfToken -> "IfToken"
        LetToken -> "LetToken"
        NewLineToken -> "NewLineToken"
        OpenCurlyToken -> "OpenCurlyToken"
        OpenParenToken -> "OpenParenToken"
        is OperatorToken -> "OperatorToken(\"${token.value}\")"
        ReturnToken -> "ReturnToken"
        is TypeToken -> "TypeToken(\"${token.value}\")"
        is WhitespaceToken -> "WhitespaceToken(\"${token.value}\")"
        ReturnTypeToken -> throw Exception("programmer error")
    }
}

fun main() {
    generateParseTest()
}

fun generateParseTest() {
    val inString = """fn f(x: s): t {}""".trimMargin()
    val inIterator = ParserIterator()
    inIterator.add(inString)
    val node = parseFn(SequenceBuilder(inIterator))

    val s = "Fn"

    printMultiLineString(inString, "inString")
    println("val inIterator = ParserIterator()")
    println("inIterator.add(inString)")
    println("val out$s = parse$s(SequenceBuilder(inIterator))")

    println()
    printFn(node, "gold$s")
    println()

    println("assertEquals(gold$s, out$s)")
}

fun printMultiLineString(s: String, name: String) {
    val spaces = name.length + 9
    val lines = s.lines()
    print("val $name = \"\"\"${lines.first()}")
    for(line in lines.drop(1)) {
        println()
        print(" ".repeat(spaces))
        print("|$line")
    }
    println("\"\"\".trimMargin()")
}

fun printAst(ast: Ast, prefix: String): List<String> {
    val names = ast.nodes.indices.map { i -> "${prefix}$i"}
    for(i in ast.nodes.indices) {
        val node = ast.nodes[i]
        val name = names[i]
        when(node) {
            is Token -> println("val $name = " + tokenToLiteral(node))
            is Expression -> printExpression(node, name)
            is ReturnAst -> printReturn(node, name)
            is LetAst -> printLet(node, name)
            is IfAst -> printIf(node, name)
            is FnAst -> printFn(node, name)
            is StarNode<*> -> {
                if(node.reifiedT == ParameterAst::class) printParameters(node as StarAst<ParameterAst>, name)
                else if(node.reifiedT == BodyAst::class) printBlock(node as StarAst<BodyAst>, name)
            }
            else -> TODO()
        }
    }

    println("val ${prefix}List = listOf(${names.joinToString { it }})")
    return names
}

fun printParameter(parameter: ParameterAst, prefix: String) {
    val prefixWithF = "${prefix}Pa"
    val names = printAst(parameter, prefixWithF)
    val nameName = names[parameter.nodes.indexOfFirst { it == parameter.name }]
    val typeName = if(parameter.typeAnnotation == null) "null" else names[parameter.nodes.indexOfFirst { it == parameter.typeAnnotation }]
    println("val $prefix = ParameterAst(${prefixWithF}List, $nameName, $typeName)")
}

fun printBlock(block: StarAst<BodyAst>, prefix: String) {
    val prefixWithB = "${prefix}B"

    val bodyNames = mutableListOf<String>()

    var i = 0
    block.inOrder {
        val name = "${prefixWithB}$i"
        when(it) {
            is LetAst -> printLet(it, name)
            is ReturnAst -> printReturn(it, name)
            is IfAst -> printIf(it, name)
            else -> TODO()
        }
        bodyNames.add(name)
        i++
    }

    println("val $prefix = starOf<BodyAst>(listOf(${bodyNames.joinToString { it }}))")
}

fun printParameters(parameters: StarAst<ParameterAst>, prefix: String) {
    val prefixWithPs = "${prefix}Ps"

    val parameterNames = mutableListOf<String>()

    var i = 0
    parameters.inOrder {
        val name = "${prefixWithPs}$i"
        printParameter(it, name)
        parameterNames.add(name)
        i++
    }

    println("val $prefix = starOf<ParameterAst>(listOf(${parameterNames.joinToString { it }}))")
}

fun printFn(fn: FnAst, prefix: String) {
    val prefixWithF = "${prefix}F"
    val names = printAst(fn, prefixWithF)
    val nameName = if(fn.name == null) "null" else names[fn.nodes.indexOfFirst { it == fn.name }]
    val parameterName = if(fn.parameters == null) "null" else names[fn.nodes.indexOfFirst { it == fn.parameters }]
    val returnTypeName = if(fn.returnType == null) "null" else names[fn.nodes.indexOfFirst { it == fn.returnType }]
    val blockName = if(fn.block == null) "null" else names[fn.nodes.indexOfFirst { it == fn.block }]
    println("val $prefix = FnAst(${prefixWithF}List, $nameName, $parameterName, $returnTypeName, $blockName)")
}

fun printIf(i: IfAst, prefix: String) {
    val prefixWithI = "${prefix}I"
    val names = printAst(i, prefixWithI)
    val conditionName = if(i.condition == null) "null" else names[i.nodes.indexOfFirst { it == i.condition }]
    val blockName = if(i.block == null) "null" else names[i.nodes.indexOfFirst { it == i.block }]
    println("val $prefix = IfAst(${prefixWithI}List, $conditionName, $blockName)")
}

fun printReturn(ret: ReturnAst, prefix: String) {
    val prefixWithR = "${prefix}R"
    val names = printAst(ret, prefixWithR)
    val expressionName = if(ret.expression == null) "null" else names[ret.nodes.indexOfFirst { it == ret.expression }]
    println("val $prefix = ReturnAst(${prefixWithR}List, $expressionName)")
}

fun printLet(let: LetAst, prefix: String) {
    val prefixWithL = "${prefix}L"
    val names = printAst(let, prefixWithL)
    val nameName = if(let.name == null) "null" else names[let.nodes.indexOfFirst { it == let.name }]
    val typeName = if(let.typeAnnotation == null) "null" else names[let.nodes.indexOfFirst { it == let.typeAnnotation }]
    val expressionName = if(let.expression == null) "null" else names[let.nodes.indexOfFirst { it == let.expression }]
    println("val $prefix = LetAst(${prefixWithL}List, $nameName, $typeName, $expressionName)")
}

fun printArgument(argument: Argument, prefix: String) {
    val prefixWithA = "${prefix}A"
    val names = printAst(argument, prefixWithA)
    val argumentName = names[argument.nodes.indexOfFirst { it == argument.argument }]
    println("val $prefix = Argument(${prefixWithA}List, $argumentName)")
}

fun printExpression(expression: Expression, prefix: String) {
    val prefixWithE = "${prefix}E"
    val names = printAst(expression, prefixWithE)
    when(expression) {
        is UnaryExpression -> {
            val opName = names[expression.nodes.indexOfFirst { it == expression.op }]
            val expressionName =
                if(expression.expression == null) "null"
                else names[expression.nodes.indexOfFirst { it == expression.expression }]
            println("val $prefix = UnaryExpression(${prefixWithE}List, $opName, $expressionName)")
        }
        is AtomicExpression -> {
            val atomName = names[expression.nodes.indexOfFirst { it == expression.atom }]
            println("val $prefix = AtomicExpression(${prefixWithE}List, $atomName)")
        }
        is BinaryExpression -> {
            val lhsName = names[expression.nodes.indexOfFirst { it == expression.lhs }]
            val opName = names[expression.nodes.indexOfFirst { it == expression.op }]
            val rhsName =
                if(expression.rhs == null) "null"
                else names[expression.nodes.indexOfFirst { it == expression.rhs }]
            println("val $prefix = BinaryOpExpression(${prefixWithE}List, $lhsName, $opName, $rhsName)")
        }
        is GroupingExpression -> {
            val expressionName =
                if(expression.expression == null) "null"
                else names[expression.nodes.indexOfFirst { it == expression.expression }]
            println("val $prefix = GroupingExpression(${prefixWithE}List, $expressionName)")
        }
        is FnCallExpression -> {
            val fnName = names[expression.nodes.indexOfFirst { it == expression.function }]
            val argumentsName = names[expression.nodes.indexOfFirst { it == expression.arguments }]
            println("val $prefix = FnCallExpression(${prefixWithE}List, $fnName, $argumentsName)")
        }
    }
}

fun generateGoldText(inString: String, change: String, start: Span, end: Span): String {
    val lines = inString.lines()
    val withNewLines = lines.mapIndexed { i, s ->
        if(i == lines.size - 1) s
        else "$s\n"
    }

    val preStart = withNewLines.filterIndexed { i, _ -> i < start.line }.fold("") { acc, s -> acc + s }
    val startLine = withNewLines[start.line].substring(0, start.column)
    val endLine = withNewLines[end.line].substring(end.column)
    val postEnd = withNewLines.filterIndexed { i, _ -> i > end.line }.fold("") { acc, s -> acc + s }
    return preStart + startLine + change + endLine + postEnd
}

fun testTokenReparse() {
    val inToken1 = NameToken("a123456789")
    val inToken2 = WhitespaceToken(" ")
    val inToken3 = NameToken("b123456789")
    val outIterator = ParserIterator()

    val change = "c"

    val start1 = Span(0, 5)
    val end1 = Span(0, 16)
    inToken1.addToIterator(change, start1, end1, outIterator, inToken2)

    val start2 = start1 - inToken1.span
    val end2 = end1 - inToken1.span
    inToken2.addToIterator(change, start2, end2, outIterator, inToken3)

    val start3 = start2 - inToken2.span
    val end3 = end2 - inToken2.span
    inToken3.addToIterator(change, start3, end3, outIterator, null)

    var goldString = ""
    while(outIterator.hasNext()) {
        goldString +=
            if(outIterator.isNextToken()) outIterator.peekToken().value.replace("\n", "\\n")
            else (outIterator.peekNode() as Token).value.replace("\n", "\\n")
        outIterator.next()
    }

    println("val inToken1 = ${tokenToLiteral(inToken1)}")
    println("val inToken2 = ${tokenToLiteral(inToken2)}")
    println("val inToken3 = ${tokenToLiteral(inToken3)}")
    println("val change = \"$change\"")
    println("val start1 = Offset(${start1.line}, ${start1.column})")
    println("val end1 = Offset(${end1.line}, ${end1.column})")
    println("val outIterator = ParserIterator()")
    println("inToken1.addToIterator(change, start1, end1, outIterator, inToken2)")
    println("val start2 = start1 - inToken1.offset")
    println("val end2 = end1 - inToken1.offset")
    println("inToken2.addToIterator(change, start2, end2, outIterator, inToken3)")
    println("val start3 = start2 - inToken2.offset")
    println("val end3 = end2 - inToken2.offset")
    println("inToken3.addToIterator(change, start3, end3, outIterator, null)")
    println("val outToken = outIterator.peekToken()")
    println("val goldToken = NameToken(\"$goldString\")")
    println("assertEquals(goldToken, outToken)")
}

fun testReturn() {
    val inString = "return f(x)"
    val inIterator = ParserIterator()
    inIterator.add(inString)
    val returnNode = parseReturn(SequenceBuilder(inIterator))
    println(returnNode)
}

/*fun expressionStringTest() {
    val inString = "a(b, c)(d, e)(f)(g)"
    val inIterator = ParserIterator()
    inIterator.add(inString)
    val expression = parseExpression(inIterator)!!
    val outString = expressionToString(expression)

    println("val inString = \"$inString\"")
    println("val inIterator = ParserIterator()")
    println("inIterator.add(inString)")
    println("val expression = parseExpression(inIterator)")
    println("val outString = expressionToString(expression)")
    println("val goldString = \"$outString\"")
    println("assertEquals(goldString, outString)")
}*/

fun numberToken() {
    val iter = ParserIterator()
    val r = (0..100000).random().toString()
    val r1 = (0..100000).random().toString()
    val r2 = (0..100000).random().toString()
    val text = "${r}.${r1}t"
    iter.add(text)
    val tokens = mutableListOf<Token>()
    while(iter.hasNext()) {
        tokens.add(iter.peekToken())
        iter.next()
    }

    println("val text = \"$text\"")
    println("val tokens = getTokens(text)")
    print("val goldTokens = listOf(")
    print(tokens.joinToString(", ") { tokenToLiteral(it) })
    println(")")
    println("assertEquals(tokens, goldTokens)")
}