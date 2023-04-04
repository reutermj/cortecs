package parser

import tokenizer.*

internal object ReturnParserKeyword: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is ReturnToken -> {
                state.push(ReturnParserValue)
                Pair(TermParser, true)
            }
            else -> throw Exception()
        }
    }
}

internal object ReturnParserValue: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., FnBodyParser, ExpressionValue]

        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                val caller = state.pop()
                state.push(ReturnValue(Return(expression.expression)))
                Pair(caller, false)
            }
            else -> throw Exception()
        }
    }
}

internal data class ReturnValue(val ret: Return): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        throw Exception()
    }
}