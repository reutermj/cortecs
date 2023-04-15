package parser

import tokenizer.*

//<fnBody> -> <let> | <return>
//<let> -> 'let' <name> '=' <expression>
//<return> -> 'return' <expression>

internal object FnBodyParser: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is LetToken -> Pair(LetParserKeyword, false)
            is ReturnToken -> Pair(ReturnParserKeyword, false)
            else -> throw Exception()
        }
    }
}

internal data class FnBodyValue(val body: FnBody): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        throw Exception()
    }
}

//region <let>

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
                state.push(LetParserExpression(name))
                Pair(ExpressionParserInitial, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class LetParserExpression(val name: Name): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., FnBodyParser, ExpressionValue]

        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                val caller = state.pop()
                state.push(FnBodyValue(Let(name, expression.expression)))
                Pair(caller, false)
            }
            else -> throw Exception()
        }
    }
}

//endregion <let>

//region <return>

internal object ReturnParserKeyword: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is ReturnToken -> {
                state.push(ReturnParserExpression)
                Pair(ExpressionParserInitial, true)
            }
            else -> throw Exception()
        }
    }
}

internal object ReturnParserExpression: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., FnBodyParser, ExpressionValue]

        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                val caller = state.pop()
                state.push(FnBodyValue(Return(expression.expression)))
                Pair(caller, false)
            }
            else -> throw Exception()
        }
    }
}

//endregion <return>