package parser_v2

fun nextToken(text: String, start: Int): TokenImpl =
    when(text[start]) {
        in initialNameOrType -> nextToken(text, start, allNameOrType, ::toKeywordOrNameOrTypeToken)
        in whiteSpace -> nextToken(text, start, whiteSpace, ::WhitespaceToken)
        in operators -> nextToken(text, start, operators, ::toOperatorToken)

        in numbers -> nextIntOrFloatToken(text, start)
        '.' -> nextDotOrFloatToken(text, start)

        '"' -> nextString(text, start)
        '\'' -> nextChar(text, start)

        '\n' -> NewLineToken
        ',' -> CommaToken
        ':' -> ColonToken
        '(' -> OpenParenToken
        ')' -> CloseParenToken
        '{' -> OpenCurlyToken
        '}' -> CloseCurlyToken
        else -> nextBadToken(text, start)
    }

val operators = setOf('=', '<', '>', '!', '|', '&', '+', '-', '*', '/', '%', '~')
val whiteSpace = setOf('\r', '\t', ' ')
val initialType = ('A'..'Z').toSet()
val initialName = ('a'..'z').toSet() + setOf('_')
val initialNameOrType = initialType + initialName
val allNameOrType = initialNameOrType + ('0'..'9').toSet()
val numbers = ('0'..'9').toSet()
val valid = operators + whiteSpace + allNameOrType + numbers + setOf('.', '"', '\'', '\n', ',', ':', '(', ')', '{', '}')

private fun nextToken(s: String, start: Int, acceptableChars: Set<Char>, toToken: (String) -> TokenImpl): TokenImpl {
    var end = start + 1
    while(end < s.length && s[end] in acceptableChars) end++
    return toToken(s.substring(start, end))
}

private fun nextBadToken(s: String, start: Int): TokenImpl {
    var end = start + 1
    while(end < s.length && s[end] !in valid) end++
    return BadToken(s.substring(start, end))
}

private fun nextString(text: String, start: Int): TokenImpl {
    var end = start + 1
    var isEscaped = false
    while(end < text.length) {
        val c = text[end]
        if(c == '\n') return BadStringToken(text.substring(start, end))
        end++

        if(isEscaped) isEscaped = false // todo maybe validate escape sequences
        else when(c) {
            '"' -> break
            '\\' -> isEscaped = true
        }
    }

    return if(isEscaped) BadStringToken(text.substring(start, end))
    else StringToken(text.substring(start, end))
}

private fun nextChar(text: String, start: Int): TokenImpl {
    var end = start + 1
    var numChars = 0
    var isEscaped = false
    while(end < text.length) {
        val c = text[end]
        if(c == '\n') return BadCharToken(text.substring(start, end))
        end++

        if(isEscaped) { // todo maybe validate escape sequences
            isEscaped = false
            numChars++
        } else when(c) {
            '\'' -> break
            '\\' -> isEscaped = true
            else -> numChars++
        }
    }

    return if(numChars != 1 || isEscaped) BadCharToken(text.substring(start, end))
    else CharToken(text.substring(start, end))
}

private fun nextIntOrFloatToken(text: String, start: Int): TokenImpl {
    var end = start + 1
    while(end < text.length) {
        when(text[end]) {
            in numbers -> end++
            '.' -> return nextFloatToken(text, start, end + 1)
            'u', 'U' -> return nextUintToken(text, start, end + 1)
            'l', 'L', 'b', 'B', 's', 'S' -> {
                end++
                break
            }
            'f', 'F', 'd', 'D' -> {
                end++
                return FloatToken(text.substring(start, end))
            }
            else -> break
        }
    }
    return IntToken(text.substring(start, end))
}

private fun nextUintToken(text: String, start: Int, end: Int): TokenImpl {
    if(end == text.length) return IntToken(text.substring(start, end))
    return when(text[end]) {
        'l', 'L', 'b', 'B', 's', 'S' -> IntToken(text.substring(start, end + 1))
        else -> IntToken(text.substring(start, end))
    }
}

private fun nextFloatToken(text: String, start: Int, end: Int): TokenImpl {
    var end = end
    while(end < text.length) {
        when(text[end]) {
            in numbers -> end++
            'f', 'F', 'd', 'D' -> {
                end++
                break
            }
            else -> break
        }
    }
    return FloatToken(text.substring(start, end))
}

private fun nextDotOrFloatToken(text: String, start: Int): TokenImpl {
    val end = start + 1
    return if(end < text.length && text[end] in numbers) nextFloatToken(text, start, end + 1)
    else DotToken
}

private fun toKeywordOrNameOrTypeToken(value: String) =
    when(value) {
        "let" -> LetToken
        "if" -> IfToken
        "function" -> FunctionToken
        "return" -> ReturnToken
        else ->
            if(value[0].isUpperCase()) TypeToken(value)
            else NameToken(value)
    }

private fun toOperatorToken(value: String): TokenImpl =
    when(value) {
        "\\" ->  BackSlashToken
        "=" ->  EqualSignToken
        else -> OperatorToken(value)
    }
