package parser

import tokenizer.EofToken
import tokenizer.FnToken
import tokenizer.Token
import tokenizer.WhitespaceToken

internal class ParserState {
    private val stack = mutableListOf<ParserStateMachine>()
    fun push(stm: ParserStateMachine) {
        stack.add(stm)
    }
    fun pop() = stack.removeLast()
}

internal abstract class ParserStateMachine {
    abstract fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean>
}

internal object ProgramParserInitial: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        state.push(ProgramPaser(mutableListOf()))
        return when(token) {
            is FnToken -> Pair(FnParserKeyword, false)
            is EofToken -> Pair(this, true)
            else -> throw Exception()
        }
    }
}

internal data class ProgramPaser(val functions: MutableList<Fn>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        when(val fn = state.pop()) {
            is FnValue -> functions.add(fn.fn)
            else -> throw Exception()
        }
        state.push(this)
        return when(token) {
            is FnToken -> Pair(FnParserKeyword, false)
            is EofToken -> Pair(this, true)
            else -> throw Exception()
        }
    }
}

fun parse(tokens: List<Token>): List<Fn> {
    val state = ParserState()
    var stm: ParserStateMachine = ProgramParserInitial
    var i = 0
    while(i < tokens.size) {
        if(tokens[i] is WhitespaceToken) i++
        else {
            val (nstm, shouldProgress) = stm.process(tokens[i], state)
            stm = nstm
            if (shouldProgress) i++
        }
    }

    return when(val s = state.pop()) {
        is ProgramPaser -> s.functions
        else -> throw Exception()
    }
}