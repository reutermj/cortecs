package tokenizer

import parser.parse
import parser.printWithTypes
import typechecker.*
import java.io.File

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

fun main() {
    val x = mapOf("a" to 1, "b" to 2)
    val y = mapOf("a" to 3)
    println(x + y)

    val parent = File(".").absolutePath.dropLast(1)
    val file = File(parent + File.separator + "program.upl")
    val s = file.readText()
    val fns = parse(tokenize(s))
    var env = Environment()
    for (fn in fns) {
        val (c, e) = generateFnConstraints(env, fn)
        val substitutions = unify(c)
        println(printWithTypes(fn))
        env = e
    }

}