package parser

import tokenizer.*

//<program> -> <fn> | <component>
//<fn> -> 'fn' <name> '(' (<name> (',' <name>)*) | <lambda>  ')' '{' <fnBody>* '}'
//<component> -> 'component' <name> '(' (<name> ':' <name>) (',' (<name> ':' <name>))* ')'

internal object ProgramParserInitial: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        state.push(ProgramPaser(mutableListOf()))
        return when(token) {
            is FnToken -> Pair(FnParserKeyword, false)
            is ComponentToken -> Pair(ComponentParserKeyword, false)
            is EofToken -> Pair(this, true)
            else -> throw Exception()
        }
    }
}

internal data class ProgramPaser(val program: MutableList<ProgramAst>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        when(val fn = state.pop()) {
            is ProgramValue -> program.add(fn.fn)
            else -> throw Exception()
        }
        state.push(this)
        return when(token) {
            is FnToken -> Pair(FnParserKeyword, false)
            is ComponentToken -> Pair(ComponentParserKeyword, false)
            is EofToken -> Pair(this, true)
            else -> throw Exception()
        }
    }
}

internal data class ProgramValue(val fn: ProgramAst): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        throw Exception()
    }
}

//region <fn>

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
            is NameToken -> Pair(FnParserOpenParen(token), true)
            else -> throw Exception()
        }
    }
}

internal data class FnParserOpenParen(val name: NameToken): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is OpenParenToken -> Pair(FnParserArgsOrLambda(name), true)
            else -> throw Exception()
        }
    }
}

internal data class FnParserArgsOrLambda(val name: NameToken): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> Pair(FnParserArgs(name, mutableListOf()), false)
            is CloseParenToken -> Pair(FnParserCloseParen(name, listOf()), false)
            else -> throw Exception()
        }
    }
}

internal data class FnParserArgs(val name: NameToken, val args: MutableList<NameToken>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> {
                args.add(token)
                Pair(FnParserCommaOrLambda(this), true)
            }
            else -> throw Exception()
        }
    }
}

internal data class FnParserCommaOrLambda(val args: FnParserArgs): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CommaToken -> Pair(args, true)
            is CloseParenToken -> Pair(FnParserCloseParen(args.name, args.args.toList()), false)
            else -> throw Exception()
        }
    }
}

internal data class FnParserCloseParen(val name: NameToken, val args: List<NameToken>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseParenToken -> Pair(FnParserOpenCurly(name, args), true)
            else -> throw Exception()
        }
    }
}

internal data class FnParserOpenCurly(val name: NameToken, val args: List<NameToken>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is OpenCurlyToken -> Pair(FnParserBodyInit(name, args), true)
            else -> throw Exception()
        }
    }
}

internal data class FnParserBodyInit(val name: NameToken, val args: List<NameToken>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseCurlyToken -> Pair(FnParserCloseCurly(name, args, mutableListOf()), false)
            else -> {
                state.push(FnParserBody(name, args, mutableListOf()))
                Pair(FnBodyParser, false)
            }
        }
    }
}

internal data class FnParserBody(val name: NameToken, val args: List<NameToken>, val body: MutableList<FnBodyAst>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        val fnBody = state.pop()
        when(fnBody) {
            is FnBodyValue -> body.add(fnBody.body)
            else -> throw Exception()
        }

        return when(token) {
            is CloseCurlyToken -> Pair(FnParserCloseCurly(name, args, body), false)
            else -> {
                state.push(this)
                Pair(FnBodyParser, false)
            }
        }
    }
}

internal data class FnParserCloseCurly(val name: NameToken, val args: List<NameToken>, val body: List<FnBodyAst>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseCurlyToken -> {
                val caller = state.pop()
                state.push(ProgramValue(FnAst(name, args, body)))
                Pair(caller, true)
            }
            else -> throw Exception()
        }
    }
}

//endregion <fn>

//region <component>

internal object ComponentParserKeyword: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is ComponentToken -> Pair(ComponentParserName, true)
            else -> throw Exception()
        }
    }
}

internal object ComponentParserName: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> Pair(ComponentParserOpenParen(token), true)
            else -> throw Exception()
        }
    }
}

internal data class ComponentParserOpenParen(val name: NameToken): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is OpenParenToken -> Pair(ComponentParserArgs(name, mutableListOf()), true)
            else -> throw Exception()
        }
    }
}

internal data class ComponentParserArgs(val name: NameToken, val args: MutableList<ComponentValue>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> Pair(ComponentParserColon(token, this), true)
            else -> throw Exception()
        }
    }
}

internal data class ComponentParserColon(val name: NameToken, val args: ComponentParserArgs): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is ColonToken -> Pair(ComponentParserType(name, args), true)
            else -> throw Exception()
        }
    }
}

internal data class ComponentParserType(val name: NameToken, val args: ComponentParserArgs): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> {
                args.args.add(ComponentValue(name, token))
                Pair(ComponentParserCommaOrLambda(args), true)
            }
            else -> throw Exception()
        }
    }
}

internal data class ComponentParserCommaOrLambda(val args: ComponentParserArgs): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CommaToken -> Pair(args, true)
            is CloseParenToken -> Pair(ComponentParserCloseParen(args.name, args.args.toList()), false)
            else -> throw Exception()
        }
    }
}

internal data class ComponentParserCloseParen(val name: NameToken, val args: List<ComponentValue>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseParenToken -> {
                val caller = state.pop()
                state.push(ProgramValue(ComponentAst(name, args)))
                Pair(caller, true)
            }
            else -> throw Exception()
        }
    }
}

//endregion <component>