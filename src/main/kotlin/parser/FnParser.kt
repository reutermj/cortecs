package parser

import tokenizer.*

internal object FnParserKeyword: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is FnToken -> Pair(FnParserName, true)
            else -> throw Exception()
        }
    }
}

internal object FnParserName: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> Pair(FnParserOpenParen(Name(token)), true)
            else -> throw Exception()
        }
    }
}

internal data class FnParserOpenParen(val name: Name): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is OpenParenToken -> Pair(FnParserArgs(name, mutableListOf()), true)
            else -> throw Exception()
        }
    }
}

internal data class FnParserArgs(val name: Name, val args: MutableList<Name>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> {
                args.add(Name(token))
                state.push(this)
                Pair(FnParserArgsComma, true)
            }
            is CloseParenToken -> Pair(FnParserCloseParen(name, args.toList()), false)
            else -> throw Exception()
        }
    }
}

internal object FnParserArgsComma: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(val args = state.pop()) {
            is FnParserArgs -> {
                when(token) {
                    is CommaToken -> Pair(args, true)
                    is CloseParenToken -> Pair(FnParserCloseParen(args.name, args.args.toList()), false)
                    else -> throw Exception()
                }
            }
            else -> throw Exception()
        }
    }

}

internal data class FnParserCloseParen(val name: Name, val args: List<Name>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseParenToken -> Pair(FnParserOpenCurly(name, args), true)
            else -> throw Exception()
        }
    }
}

internal data class FnParserOpenCurly(val name: Name, val args: List<Name>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is OpenCurlyToken -> Pair(FnParserBodyInit(name, args), true)
            else -> throw Exception()
        }
    }
}

internal data class FnParserBodyInit(val name: Name, val args: List<Name>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseCurlyToken -> Pair(FnParserCloseCurly(name, args, mutableListOf()), false)
            is LetToken -> {
                state.push(FnParserBody(name, args, mutableListOf()))
                Pair(LetParserKeyword, false)
            }
            is ReturnToken -> {
                state.push(FnParserBody(name, args, mutableListOf()))
                Pair(ReturnParserKeyword, false)
            }
            else -> throw Exception()
        }
    }
}

internal data class FnParserBody(val name: Name, val args: List<Name>, val body: MutableList<FnBody>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        val fnBody = state.pop()
        when(fnBody) {
            is LetValue -> body.add(fnBody.let)
            is ReturnValue -> body.add(fnBody.ret)
            else -> throw Exception()
        }

        return when(token) {
            is CloseCurlyToken -> Pair(FnParserCloseCurly(name, args, body), false)
            is LetToken -> {
                state.push(this)
                Pair(LetParserKeyword, false)
            }
            is ReturnToken -> {
                state.push(this)
                Pair(ReturnParserKeyword, false)
            }
            else -> throw Exception()
        }
    }
}

internal data class FnParserCloseCurly(val name: Name, val args: List<Name>, val body: List<FnBody>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseCurlyToken -> {
                val caller = state.pop()
                state.push(FnValue(Fn(name, args, body)))
                Pair(caller, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class FnValue(val fn: Fn): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        throw Exception()
    }
}