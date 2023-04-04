package tokenizer

val operators = setOf('=', '<', '>', '!', '|', '&', '+', '-', '*', '/', '%', '\\')

internal object OperatorState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in operators -> {
                state.addChar(c)
                Pair(OperatorState, true)
            }
            else -> {
                state.writeToken(::writeOperator)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::writeOperator)
    }
}
