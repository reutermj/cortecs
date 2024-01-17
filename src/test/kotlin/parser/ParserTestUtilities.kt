package parser

import kotlinx.serialization.*
import kotlin.test.*

val nonEmptyWhitespaceCombos: List<String> = run {
    val whitespaceCombos = mutableListOf<String>()
    val whitespaceCharacters = listOf(" ", "\n")
    whitespaceCombos.addAll(whitespaceCharacters)

    for(i in whitespaceCharacters) for(j in whitespaceCharacters) {
        if(i == " " && j == " ") continue
        whitespaceCombos.add("$i$j")
    }

    for(i in whitespaceCharacters) for(j in whitespaceCharacters) for(k in whitespaceCharacters) {
        if(i == " " && j == " ") continue
        if(j == " " && k == " ") continue
        whitespaceCombos.add("$i$j$k")
    }

    for(i in whitespaceCharacters) for(j in whitespaceCharacters) for(k in whitespaceCharacters) for(l in whitespaceCharacters) {
        if(i == " " && j == " ") continue
        if(j == " " && k == " ") continue
        if(k == " " && l == " ") continue
        whitespaceCombos.add("$i$j$k$l")
    }

    whitespaceCombos
}

val whitespaceCombos = nonEmptyWhitespaceCombos + ""
val operators = listOf("|", "^", "&", "==", "!", ">", "<", "+", "-", "~", "*", "/", "%")

fun generateGoldText(inString: String, change: Change): String {
    val lines = inString.lines()
    val withNewLines = lines.mapIndexed {i, s ->
        if(i == lines.size - 1) s
        else "$s\n"
    }

    //todo there's a bug in here where it's not caught that the newline character is getting deleted.
    //inString = "(\na\n)" change=Change("bc", Span(1, 1), Span(1, 2))
    val preStart = withNewLines.filterIndexed {i, _ -> i < change.start.line}.fold("") {acc, s -> acc + s}
    val startLine = withNewLines[change.start.line].substring(0, change.start.column)
    val endLine = withNewLines[change.end.line].substring(change.end.column)
    val postEnd = withNewLines.filterIndexed {i, _ -> i > change.end.line}.fold("") {acc, s -> acc + s}
    return preStart + startLine + change.text + endLine + postEnd
}

inline fun <reified T: Ast?> testParse(inString: String, parse: (ParserIterator) -> T, asserts: (T) -> Unit) {
    val iterator = ParserIterator()
    iterator.add(inString)
    val node = parse(iterator)
    assertNotNull(node)
    assertFails {
        try {
            iterator.nextToken()
            println()
        } catch(e: Exception) {
            throw e
        }
    }

    val builder = StringBuilder()
    node.stringify(builder)
    assertEquals(inString, builder.toString())

    val serialized = astJsonFormat.encodeToString(node)
    val deserialized = astJsonFormat.decodeFromString<T>(serialized)
    assertEquals(node, deserialized)

    asserts(node)
}

fun <T: Ast> testReparse(inString: String, change: Change, parse: (ParserIterator) -> T) {
    val inIterator = ParserIterator()
    inIterator.add(inString)
    val inExpression = parse(inIterator)

    val outIterator = inExpression.createChangeIterator(change)
    val outExpression = parse(outIterator)
    assertFails {
        try {
            outIterator.nextToken()
            println()
        } catch(e: Exception) {
            throw e
        }
    }

    val goldText = generateGoldText(inString, change)
    val goldIterator = ParserIterator()
    goldIterator.add(goldText)
    val goldExpression = parse(goldIterator)
    assertEquals(goldExpression, outExpression)


    val builder = StringBuilder()
    outExpression.stringify(builder)
    assertEquals(goldText, builder.toString())
}

fun testAppendToBeginning(inText: String, beginningText: String) {
    val change = Change(beginningText, Span.zero, Span.zero)
    testReparse(inText, change) {parseExpression(it)!!}
}

fun <T: Ast> testAppendToEnd(inText: String, endText: String, parse: (ParserIterator) -> T) {
    val span = getSpan(inText)
    val change = Change(endText, span, span)
    testReparse(inText, change, parse)
}

fun <T: Ast> testReplaceMiddle(
    left: String, middle: String, right: String, replace: String, parse: (ParserIterator) -> T) {
    val inText = "$left$middle$right"
    val start = getSpan(left)
    val end = start + getSpan(middle)
    val change = Change(replace, start, end)
    testReparse(inText, change, parse)
}

fun getSpan(text: String): Span {
    val lines = text.lines()
    val lastLine = lines.last()
    return Span(lines.size - 1, lastLine.length)
}

fun randomExpression(fuel: Int = 5): String {
    if(fuel == 0) return randomAtomicExpression()
    return when((0..2).random()) {
        0 -> randomBinaryExpression(fuel)
        else -> randomBaseExpression(fuel)
    }
}

fun randomBinaryExpression(fuel: Int) = randomExpression(fuel - 1) + whitespaceCombos.random() + operators.random() + whitespaceCombos.random() + randomExpression(fuel - 1)

fun randomBaseExpression(fuel: Int): String {
    if(fuel == 0) return randomAtomicExpression()
    return when((0..2).random()) {
        0 -> randomAtomicExpression()
        1 -> randomUnaryExpression(fuel)
        2 -> randomGroupingExpression(fuel)
        else -> throw Exception()
    }
}

fun randomGroupingExpression(fuel: Int) = "(" + whitespaceCombos.random() + randomExpression(fuel - 1) + ")"
fun randomUnaryExpression(fuel: Int) = operators.random() + whitespaceCombos.random() + randomBaseExpression(fuel - 1)

val atomicExpressions = listOf("a", "\"hello world\"", "'a'", "1", "1.1")
fun randomAtomicExpression() = atomicExpressions.random() + whitespaceCombos.random()