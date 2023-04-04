package parser

import tokenizer.*

internal object LetParserKeyword: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is LetToken -> Pair(LetParserName, true)
            else -> throw Exception()
        }
    }
}

internal object LetParserName: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> Pair(LetParserEqualSign(Name(token)), true)
            else -> throw Exception()
        }
    }
}

internal data class LetParserEqualSign(val name: Name): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is EqualSignToken -> {
                state.push(LetParserValue(name))
                state.push(AddParser)
                state.push(MulParser)
                Pair(TermParser, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class LetParserValue(val name: Name): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., FnBodyParser, ExpressionValue]

        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                val caller = state.pop()
                state.push(LetValue(Let(name, expression.expression)))
                Pair(caller, false)
            }
            else -> throw Exception()
        }
    }
}

internal data class LetValue(val let: Let): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        throw Exception()
    }
}