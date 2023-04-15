package parser

import tokenizer.*

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

fun parse(tokens: List<Token>): List<Program> {
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
        is ProgramPaser -> s.program
        else -> throw Exception()
    }
}