package parser_v2

import kotlinx.serialization.*
import kotlin.test.*

val whitespaceCombos: List<String> = run {
    val whitespaceCombos = mutableListOf("")
    val whitespaceCharacters = listOf(" ", "\n")
    whitespaceCombos.addAll(whitespaceCharacters)

    for(i in whitespaceCharacters)
        for(j in whitespaceCharacters) {
            if(i == " " && j == " ") continue
            whitespaceCombos.add("$i$j")
        }

    for(i in whitespaceCharacters)
        for(j in whitespaceCharacters)
            for(k in whitespaceCharacters) {
                if(i == " " && j == " ") continue
                if(j == " " && k == " ") continue
                whitespaceCombos.add("$i$j$k")
            }

    for(i in whitespaceCharacters)
        for(j in whitespaceCharacters)
            for(k in whitespaceCharacters)
                for(l in whitespaceCharacters) {
                    if(i == " " && j == " ") continue
                    if(j == " " && k == " ") continue
                    if(k == " " && l == " ") continue
                    whitespaceCombos.add("$i$j$k$l")
                }

    whitespaceCombos
}

val whitespaceCombosStartingWithNewLine = whitespaceCombos.filter { it.firstOrNull() != ' ' }

fun generateGoldText(inString: String, change: Change): String {
    val lines = inString.lines()
    val withNewLines = lines.mapIndexed { i, s ->
        if(i == lines.size - 1) s
        else "$s\n"
    }

    val preStart = withNewLines.filterIndexed { i, _ -> i < change.start.line }.fold("") { acc, s -> acc + s }
    val startLine = withNewLines[change.start.line].substring(0, change.start.column)
    val endLine = withNewLines[change.end.line].substring(change.end.column)
    val postEnd = withNewLines.filterIndexed { i, _ -> i > change.end.line }.fold("") { acc, s -> acc + s }
    return preStart + startLine + change.text + endLine + postEnd
}

inline fun <reified T: Ast?>tryParse(inString: String, parse: (ParserIterator) -> T, asserts: (T) -> Unit) {
    val iterator = ParserIterator()
    iterator.add(inString)
    val node = parse(iterator)
    assertNotNull(node)
    assertFails { iterator.nextToken() }

    val builder = StringBuilder()
    node.stringify(builder)
    assertEquals(inString, builder.toString())

    val serialized = astJsonFormat.encodeToString(node)
    val deserialized = astJsonFormat.decodeFromString<T>(serialized)
    assertEquals(node, deserialized)

    asserts(node)
}

fun <T: Ast>testReparse(inString: String, change: Change, parse: (ParserIterator) -> T) {
    val inIterator = ParserIterator()
    inIterator.add(inString)
    val inExpression = parse(inIterator)

    val outIterator = inExpression.createChangeIterator(change)
    val outExpression = parse(outIterator)
    assertFails { outIterator.nextToken() }

    val goldText = generateGoldText(inString, change)
    val goldIterator = ParserIterator()
    goldIterator.add(goldText)
    val goldExpression = parse(goldIterator)
    assertEquals(goldExpression, outExpression)

    val builder = StringBuilder()
    outExpression.stringify(builder)
    assertEquals(goldText, builder.toString())
}