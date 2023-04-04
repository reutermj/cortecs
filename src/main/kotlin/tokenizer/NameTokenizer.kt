package tokenizer

val initialNameChars = ('a'..'z').toSet() + ('A'..'Z').toSet()
val acceptableNameChars = initialNameChars + ('0'..'9').toSet()

internal object NameState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in acceptableNameChars -> {
                state.addChar(c)
                Pair(NameState, true)
            }
            else -> {
                state.writeToken(::writeName)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::writeName)
    }
}
