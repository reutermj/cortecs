package tokenizer

val numberChars = ('0'..'9').toSet()

internal object IntState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in numberChars -> {
                state.addChar(c)
                Pair(IntState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(FloatState, true)
            }
            'l', 'L', 'b', 'B', 's', 'S' -> {
                state.addChar(c)
                Pair(IntEndState, true)
            }
            'f', 'F', 'd', 'D' -> {
                state.addChar(c)
                Pair(FloatEndState, true)
            }
            else -> {
                state.writeToken(::IntToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::IntToken)
    }
}

internal object IntEndState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in acceptableNameChars -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            else -> {
                state.writeToken(::IntToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::IntToken)
    }
}

internal object NumberErrorState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in acceptableNameChars -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            else -> {
                state.writeToken(::BadNumberToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::BadNumberToken)
    }
}

internal object FloatOrDotState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in numberChars -> {
                state.addChar(c)
                Pair(FloatState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            'f', 'F', 'd', 'D' -> {
                state.addChar(c)
                Pair(FloatEndState, true)
            }
            else -> {
                state.writeToken(::DotToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::DotToken)
    }
}

internal object FloatState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in numberChars -> {
                state.addChar(c)
                Pair(FloatState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            'f', 'F', 'd', 'D' -> {
                state.addChar(c)
                Pair(FloatEndState, true)
            }
            else -> {
                state.writeToken(::FloatToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::FloatToken)
    }
}

internal object FloatEndState: TokenizerStateMachine() {
    override fun process(c: Char, state: TokenizationState): Pair<TokenizerStateMachine, Boolean> {
        return when(c) {
            in acceptableNameChars -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            '.' -> {
                state.addChar(c)
                Pair(NumberErrorState, true)
            }
            else -> {
                state.writeToken(::FloatToken)
                Pair(TokenizerInitialState, false)
            }
        }
    }

    override fun finalize(state: TokenizationState) {
        state.writeToken(::FloatToken)
    }
}