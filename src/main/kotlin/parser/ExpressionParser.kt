package parser

import tokenizer.*

//<expression> -> <term> <expression'>
//<expression'> -> '+' <term> <expression'> | '-' <term> <expression'> | <lambda>
//<term> -> <factor> <term'>
//<term'> -> '*' <factor> <term'> | '/' <factor> <term'> | <lambda>
//<factor> -> '(' <expression> ')' | ('+' | '-')* (<name> | <entity>) (<fnCall> | '.' <name> | '\' <name>)* |
//            <int> | <float> | <string> | <char> | <bool>
//<fnCall> -> '(' (<expression> (',' <expression>)*) | <lambda> ')'
//<entity> -> '{' (<expression> (',' <expression>)*) | <lambda> '}'

internal data class ExpressionValue(val expression: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        throw Exception()
    }
}

//region <expression>

internal object ExpressionParserInitial: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        state.push(ExpressionParserOperator)
        state.push(TermParserOperator)
        return Pair(FactorParser, false)
    }
}

internal object ExpressionParserOperator: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, ExpressionValue]
        val expression = state.pop()

        return when(token) {
            is PlusToken -> {
                when(expression) {
                    is ExpressionValue -> {
                        state.push(ExpressionParserAccumulate(token, expression.expression))
                        state.push(TermParserOperator)
                        Pair(FactorParser, true)
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

internal data class ExpressionParserAccumulate(val token: PlusToken, val lhs: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, ExpressionValue]
        return when(val rhs = state.pop()) {
            is ExpressionValue -> {
                state.push(ExpressionParserOperator)//Arithmetic(ArithmeticKind.add, lhs, rhs.expression)
                state.push(ExpressionValue(FnCall(Name(NameToken(token.value, token.line, token.column)), listOf(lhs, rhs.expression))))
                Pair(TermParserOperator, false)
            }
            else -> throw Exception()
        }
    }

}

//endregion <expression>

//region <term>

internal object TermParserOperator: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, AddParser/AddPParser, ExpressionValue]
        val expression = state.pop()

        return when(token) {
            is MulToken -> {
                when(expression) {
                    is ExpressionValue -> {
                        state.push(TermParserAccumulate(expression.expression))
                        Pair(FactorParser, true)
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

internal data class TermParserAccumulate(val lhs: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, AddParser/AddPParser, ExpressionValue]
        return when(val rhs = state.pop()) {
            is ExpressionValue -> {
                state.push(ExpressionValue(FnCall(Name(NameToken(token.value, token.line, token.column)), listOf(lhs, rhs.expression))))
                Pair(TermParserOperator, false)
            }
            else -> throw Exception()
        }
    }
}

//endregion <term>

//region <factor>

internal object FactorParser: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        //this function should be called with a stack:
        //[..., Caller, AddParser/AddPParser, MulParser/MulPParser]

        return when(token) {
            is NameToken -> {
                state.push(ExpressionValue(Name(token)))
                Pair(FactorParserFollowUp, true)
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
                state.push(FactorParserCloseParen)
                Pair(ExpressionParserInitial, true)
            }
            is OpenCurlyToken -> Pair(EntityParserOpenCurly, false)
            else -> Pair(state.pop(), false)
        }
    }
}

internal object FactorParserFollowUp: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                when(token) {
                    is OpenParenToken -> {
                        state.push(FnCallArgs(expression.expression, mutableListOf()))
                        Pair(ExpressionParserInitial, true)
                    }
                    is DotToken -> Pair(SelectionParserName(expression.expression), true)
                    is BackSlashToken -> Pair(EntityRestrictionParserName(expression.expression), true)
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

internal object FactorParserCloseParen: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseParenToken -> Pair(FactorParserFollowUp, true)
            else -> throw Exception()
        }
    }
}

//endregion <factor>

//region <fnCall>

internal data class FnCallArgs(val target: Expression, val args: MutableList<Expression>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(val expression = state.pop()) {
            is ExpressionValue -> {
                args.add(expression.expression)
                when(token) {
                    is CloseParenToken -> Pair(FnCallCloseParen(target, args), false)
                    is CommaToken -> {
                        state.push(this)
                        Pair(ExpressionParserInitial, true)
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
                Pair(FactorParserFollowUp, true)
            }
            else -> throw Exception()
        }
    }
}

//endregion <fnCall>

//region <entity>

internal object EntityParserOpenCurly: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is OpenCurlyToken -> Pair(EntityParserExpressionsOrLambda, true)
            else -> throw Exception()
        }
    }
}

internal object EntityParserExpressionsOrLambda: ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseCurlyToken -> Pair(EntityParserCloseCurly(listOf()), false)
            else -> {
                state.push(EntityParserExpressions(mutableListOf()))
                Pair(ExpressionParserInitial, false)
            }
        }
    }
}

internal data class EntityParserExpressions(val expressions: MutableList<Expression>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        when(val expression = state.pop()) {
            is ExpressionValue -> expressions.add(expression.expression)
            else -> throw Exception()
        }

        return Pair(EntityParserCommaOrLambda(this), false)
    }
}

internal data class EntityParserCommaOrLambda(val expressions: EntityParserExpressions): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseCurlyToken -> Pair(EntityParserCloseCurly(expressions.expressions.toList()), false)
            is CommaToken -> {
                state.push(expressions)
                Pair(ExpressionParserInitial, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class EntityParserCloseCurly(val expressions: List<Expression>): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is CloseCurlyToken -> {
                state.push(ExpressionValue(EntityDefinition(expressions)))
                Pair(FactorParserFollowUp, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class EntityRestrictionParserName(val record: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> {
                state.push(ExpressionValue(EntityRestriction(record, Name(token))))
                Pair(FactorParserFollowUp, true)
            }
            else -> throw Exception()
        }
    }
}

internal data class SelectionParserName(val record: Expression): ParserStateMachine() {
    override fun process(token: Token, state: ParserState): Pair<ParserStateMachine, Boolean> {
        return when(token) {
            is NameToken -> {
                if(token.value[0].isUpperCase()) state.push(ExpressionValue(EntitySelection(record, Name(token))))
                else state.push(ExpressionValue(ComponentSelection(record, Name(token))))
                Pair(FactorParserFollowUp, true)
            }
            else -> throw Exception()
        }
    }
}

//endregion <entity>
