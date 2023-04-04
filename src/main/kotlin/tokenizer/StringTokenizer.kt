package tokenizer

internal object StringBaseState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '"' -> {
                state.addChar(c)
                state.writeToken(::StringToken)
                Pair(TokenizerInitialState, true)
            }
            '\\' -> {
                state.addChar(c)
                Pair(StringEscapeState, true)
            }
            '\r', '\n' -> {
                state.writeToken(::BadStringToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(StringBaseState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadStringToken)
    }
}

internal object StringEscapeState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\r', '\n' -> {
                state.writeToken(::BadStringToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(StringBaseState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadStringToken)
    }
}

internal object CharFirstState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\'' -> {
                state.addChar(c)
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, true)
            }
            '\\' -> {
                state.addChar(c)
                Pair(CharEscapeState, true)
            }
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharSecondState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

internal object CharSecondState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\'' -> {
                state.addChar(c)
                state.writeToken(::CharToken)
                Pair(TokenizerInitialState, true)
            }
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharErrorState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

internal object CharEscapeState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharSecondState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

internal object CharErrorState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\\' -> {
                state.addChar(c)
                Pair(CharErrorEscapeState, true)
            }
            '\'' -> {
                state.addChar(c)
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, true)
            }
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharErrorState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}

internal object CharErrorEscapeState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            '\r', '\n' -> {
                state.writeToken(::BadCharToken)
                Pair(TokenizerInitialState, false)
            }
            else -> {
                state.addChar(c)
                Pair(CharErrorState, true)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadCharToken)
    }
}