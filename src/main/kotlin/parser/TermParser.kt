package parser

import tokenizer.*

internal object TermParser: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, AddParser/AddPParser, MulParser/MulPParser]

        return when(token) {
            is NameToken -> {
                state.push(ExpressionValue(Name(token)))
                Pair(TermFollowUp, true)
            }
            is IntToken -> {
                val caller = state.pop()
                state.push(ExpressionValue(IntConstant(token)))
                Pair(caller, true)
            }
            is FloatToken -> {
                val caller = state.pop()
                state.push(ExpressionValue(FloatConstant(token)))
                Pair(caller, true)
            }
            is StringToken -> {
                val caller = state.pop()
                state.push(ExpressionValue(StringConstant(token)))
                Pair(caller, true)
            }
            is CharToken -> {
                val caller = state.pop()
                state.push(ExpressionValue(CharConstant(token)))
                Pair(caller, true)
            }
            is OpenParenToken -> {
                state.push(TermCloseParenParser)
                state.push(AddParser)
                state.push(MulParser)
                Pair(TermParser, true)
            }
            is OpenCurlyToken -> Pair(RecordOpenCurly, false)
            else -> Pair(state.pop(), false)
        }
    }
}

internal object TermFollowUp: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                when(token) {
                    is OpenParenToken -> {
                        state.push(FnCallArgs(expression.expression, mutableListOf()))
                        state.push(AddParser)
                        state.push(MulParser)
                        Pair(TermParser, true)
                    }
                    is DotToken -> Pair(RecordSelectionParser(expression.expression), true)
                    is BackSlashToken -> Pair(RecordRestrictionParser(expression.expression), true)
                    else -> {
                        val caller = state.pop()
                        state.push(expression)
                        Pair(caller, false)
                    }
                }
            }
            else -> throw Exception()
        }
    }
}

internal data class RecordSelectionParser(val record: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> {
                val caller = state.pop()
                state.push(ExpressionValue(RecordSelection(record, token)))
                Pair(caller, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class RecordRestrictionParser(val record: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> {
                val caller = state.pop()
                state.push(ExpressionValue(RecordRestriction(record, token)))
                Pair(caller, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class FnCallArgs(val target: Expression, val args: MutableList<Expression>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                args.add(expression.expression)
                when(token) {
                    is CloseParenToken -> Pair(FnCallCloseParen(target, args), false)
                    is CommaToken -> {
                        state.push(this)
                        state.push(AddParser)
                        state.push(MulParser)
                        Pair(TermParser, true)
                    }
                    else -> throw Exception()
                }
            }
            else ->
                when(token) {
                    is CloseParenToken -> Pair(FnCallCloseParen(target, args), false)
                    else -> throw Exception()
                }
        }
    }
}

internal data class FnCallCloseParen(val target: Expression, val args: List<Expression>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseParenToken -> {
                state.push(ExpressionValue(FnCall(target, args)))
                Pair(TermFollowUp, true)
            }
            else -> throw Exception()
        }
    }
}

internal object TermCloseParenParser: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseParenToken -> Pair(TermFollowUp, true)
            else -> throw Exception()
        }
    }
}

internal object AddParser: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, ExpressionValue]
        val expression = state.pop()

        return when(token) {
            is PlusToken -> {
                when(expression) {
                    is ExpressionValue -> {
                        state.push(AddPParser(token, expression.expression))
                        state.push(MulParser)
                        Pair(TermParser, true)
                    }
                    else -> throw Exception()
                }
            }
            else -> {
                val caller = state.pop()
                state.push(expression)
                Pair(caller, false)
            }
        }
    }
}

internal data class AddPParser(val token: PlusToken, val lhs: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, ExpressionValue]
        return when(val rhs = state.pop()) {
            is ExpressionValue -> {
                state.push(AddParser)//Arithmetic(ArithmeticKind.add, lhs, rhs.expression)
                state.push(ExpressionValue(FnCall(Name(NameToken(token.value, token.line, token.column)), listOf(lhs, rhs.expression))))
                Pair(MulParser, false)
            }
            else -> throw Exception()
        }
    }

}

internal object MulParser: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, AddParser/AddPParser, ExpressionValue]
        val expression = state.pop()

        return when(token) {
            is MulToken -> {
                when(expression) {
                    is ExpressionValue -> {
                        state.push(MulPParser(expression.expression))
                        Pair(TermParser, true)
                    }
                    else -> throw Exception()
                }
            }
            else -> {
                val caller = state.pop()
                state.push(expression)
                Pair(caller, false)
            }
        }
    }
}

internal data class MulPParser(val lhs: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, AddParser/AddPParser, ExpressionValue]
        return when(val rhs = state.pop()) {
            is ExpressionValue -> {
                state.push(ExpressionValue(FnCall(Name(NameToken(token.value, token.line, token.column)), listOf(lhs, rhs.expression))))
                Pair(MulParser, false)
            }
            else -> throw Exception()
        }
    }
}

internal data class ExpressionValue(val expression: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        throw Exception()
    }
}