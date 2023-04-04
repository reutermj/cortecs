package parser

import tokenizer.*

internal object RecordOpenCurly: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is OpenCurlyToken -> {
                state.push(RecordValues(mutableListOf()))
                state.push(AddParser)
                state.push(MulParser)
                Pair(RecordTermParser, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class RecordValues(val values: MutableList<RecordLabel>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                when(expression.expression) {
                    is RecordLabel -> {
                        values.add(expression.expression)
                        when(token) {
                            is CloseCurlyToken -> Pair(RecordCloseCurly(values, null), false)
                            is CommaToken -> {
                                state.push(this)
                                state.push(AddParser)
                                state.push(MulParser)
                                Pair(RecordTermParser, true)
                            }
                            else -> throw Exception()
                        }
                    }
                    else -> Pair(RecordCloseCurly(values, expression.expression), false)
                }
            }
            else ->
                when(token) {
                    is CloseCurlyToken -> Pair(RecordCloseCurly(values, null), false)
                    else -> throw Exception()
                }
        }
    }
}

internal data class RecordCloseCurly(val values: List<RecordLabel>, val row: Expression?): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseCurlyToken -> {
                state.push(ExpressionValue(RecordDefinition(values, row)))
                Pair(TermFollowUp, true)
            }
            else -> throw Exception()
        }
    }

}

internal object RecordTermParser: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, AddParser/AddPParser, MulParser/MulPParser]

        return when(token) {
            is NameToken -> {
                state.push(ExpressionValue(Name(token)))
                Pair(RecordTermFollowUp, true)
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

internal object RecordTermFollowUp: ParserStateMachine() {
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
                    is EqualSignToken -> {
                        val label = expression.expression
                        if(label !is Name) throw Exception()
                        state.push(RecordLabelSet(label.name))
                        state.push(AddParser)
                        state.push(MulParser)
                        Pair(TermParser, true)
                    }
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

internal data class RecordLabelSet(val label: NameToken): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                val caller = state.pop()
                state.push(ExpressionValue(RecordLabel(label, expression.expression)))
                Pair(caller, false)
            }
            else -> throw Exception()
        }
    }
}
