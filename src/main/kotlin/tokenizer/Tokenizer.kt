package tokenizer

internal class TokenizationState {
    private val buf = StringBuilder()
    private val tokens = mutableListOf<Token>()
    private var line = 0
    private var column = 0
    private var tokenLine = 0
    private var tokenColumn = 0

    fun <T : Token>writeToken(f: (String, Int, Int) -> T) {
        tokens.add(f(buf.toString(), tokenLine, tokenColumn))
        buf.clear()
        tokenLine = line
        tokenColumn = column
    }

    fun addChar(c: Char) {
        if(c == '\n') {
            line++
            column = 0
        } else column++

        buf.append(c)
    }

    fun getTokens() = tokens.toList()
}

internal abstract class TokenizerStateMachine {
    abstract fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean>
    abstract fun finalize(state: TokenizationState)
}

internal object TokenizerInitialState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        state.addChar(c)
        return when(c) {
            in initialNameChars -> Pair(NameState, true)
            in whiteSpace -> Pair(WhitespaceState, true)
            in numberChars -> Pair(IntState, true)
            in operators -> Pair(OperatorState, true)
            ':' -> {
                state.writeToken(::ColonToken)
                Pair(TokenizerInitialState, true)
            }
            '.' -> Pair(FloatOrDotState, true)
            '"' -> Pair(StringBaseState, true)
            '\'' -> Pair(CharFirstState, true)
            ',' -> {
                state.writeToken(::CommaToken)
                Pair(TokenizerInitialState, true)
            }
            '(', ')', '{', '}' -> {
                state.writeToken(::writeBracket)
                Pair(TokenizerInitialState, true)
            }
            else -> throw Exception()
        }
    }

    override fun finalize(state: TokenizationState) {
        //Noop
    }
}

fun tokenize(s: String): List<Token> {
    val state = TokenizationState()
    var stm: TokenizerStateMachine = TokenizerInitialState
    var i = 0
    while (i < s.length) {
        val (nstm, shouldProgress) = stm.process(s[i], state)
        stm = nstm
        if (shouldProgress) i++
    }
    stm.finalize(state)
    state.writeToken(::EofToken)
    return state.getTokens()
}

//region Name

val initialNameChars = ('a'..'z').toSet() + ('A'..'Z').toSet()
val acceptableNameChars = initialNameChars + ('0'..'9').toSet()

internal object NameState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in acceptableNameChars -> {
                state.addChar(c)
                Pair(NameState, true)
            }
            else -> {
                state.writeToken(::writeName)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::writeName)
    }
}

//endregion Name

//region Number

val numberChars = ('0'..'9').toSet()

internal object IntState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in numberChars -> {
                state.addChar(c)
                Pair(IntState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(FloatState, true)
            }
            'l', 'L', 'b', 'B', 's', 'S' -> {
                state.addChar(c)
                Pair(IntEndState, true)
            }
            'f', 'F', 'd', 'D' -> {
                state.addChar(c)
                Pair(FloatEndState, true)
            }
            else -> {
                state.writeToken(::IntToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::IntToken)
    }
}

internal object IntEndState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in acceptableNameChars -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            else -> {
                state.writeToken(::IntToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::IntToken)
    }
}

internal object NumberErrorState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in acceptableNameChars -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            else -> {
                state.writeToken(::BadNumberToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadNumberToken)
    }
}

internal object FloatOrDotState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in numberChars -> {
                state.addChar(c)
                Pair(FloatState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            'f', 'F', 'd', 'D' -> {
                state.addChar(c)
                Pair(FloatEndState, true)
            }
            else -> {
                state.writeToken(::DotToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::DotToken)
    }
}

internal object FloatState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in numberChars -> {
                state.addChar(c)
                Pair(FloatState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            'f', 'F', 'd', 'D' -> {
                state.addChar(c)
                Pair(FloatEndState, true)
            }
            else -> {
                state.writeToken(::FloatToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::FloatToken)
    }
}

internal object FloatEndState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in acceptableNameChars -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            else -> {
                state.writeToken(::FloatToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::FloatToken)
    }
}

//endregion Number

//region String

internal object StringBaseState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '"' -> {
                state.addChar(c)
                state.writeToken(::StringToken)
                Pair(TokenizerInitialState, true)
            }
            '\\' -> {
                state.addChar(c)
                Pair(StringEscapeState, true)
            }
            '\r', '\n' -> {
                state.writeToken(::BadStringToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(StringBaseState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadStringToken)
    }
}

internal object StringEscapeState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\r', '\n' -> {
                state.writeToken(::BadStringToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(StringBaseState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadStringToken)
    }
}

//endregion String

//region Char

internal object CharFirstState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\'' -> {
                state.addChar(c)
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, true)
            }
            '\\' -> {
                state.addChar(c)
                Pair(CharEscapeState, true)
            }
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharSecondState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

internal object CharSecondState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\'' -> {
                state.addChar(c)
                state.writeToken(::CharToken)
                Pair(TokenizerInitialState, true)
            }
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharErrorState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

internal object CharEscapeState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharSecondState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

internal object CharErrorState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\\' -> {
                state.addChar(c)
                Pair(CharErrorEscapeState, true)
            }
            '\'' -> {
                state.addChar(c)
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, true)
            }
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharErrorState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

internal object CharErrorEscapeState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharErrorState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

//endregion Char

//region Operator

val operators = setOf('=', '<', '>', '!', '|', '&', '+', '-', '*', '/', '%', '\\')

internal object OperatorState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in operators -> {
                state.addChar(c)
                Pair(OperatorState, true)
            }
            else -> {
                state.writeToken(::writeOperator)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::writeOperator)
    }
}

//endregion Operator

//region Whitespace

val whiteSpace = setOf('\n', '\r', '\t', ' ')

internal object WhitespaceState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in whiteSpace -> {
                state.addChar(c)
                Pair(WhitespaceState, true)
            }
            else -> {
                state.writeToken(::WhitespaceToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::WhitespaceToken)
    }
}

//endregion Whitespace
