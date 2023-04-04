package tokenizer

val whiteSpace = setOf('\n', '\r', '\t', ' ')
internal object WhitespaceState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in whiteSpace -> {
                state.addChar(c)
                Pair(WhitespaceState, true)
            }
            else -> {
                state.writeToken(::WhitespaceToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::WhitespaceToken)
    }
}