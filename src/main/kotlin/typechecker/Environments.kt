package typechecker

import parser.*

data class Bindings(val bindings: Map<BindableToken, TypeScheme>) {
    companion object {
        val empty = Bindings(emptyMap())
    }

    //todo handle multiple bindings
    operator fun plus(other: Bindings) = Bindings(bindings + other.bindings)

    operator fun get(token: BindableToken) = bindings[token]

    fun addBinding(token: BindableToken, typeScheme: TypeScheme) =
        Bindings(bindings + (token to typeScheme))
}

data class Requirements(val requirements: Map<BindableToken, List<Type>>) {
    companion object {
        val empty = Requirements(emptyMap())
    }

    operator fun plus(other: Requirements): Requirements {
        val outRequirements = requirements.toMutableMap()
        for((token, types) in other.requirements)
            outRequirements[token] = (requirements[token] ?: emptyList()) + types

        return Requirements(outRequirements)
    }

    operator fun get(token: BindableToken) = requirements[token]

    fun addRequirement(token: BindableToken, type: Type): Requirements {
        val requirement = (requirements[token] ?: emptyList()) + type
        return copy(requirements = requirements + (token to requirement))
    }

    fun <T>fold(init: T, f: (T, BindableToken, List<Type>) -> T): T {
        var acc = init
        for((key, value) in requirements) acc = f(acc, key, value)
        return acc
    }
}

sealed interface BlockSubordinates

data class BlockEnvironment(val bindings: Bindings, val substitution: Substitution, val requirements: Requirements, val freeUserDefinedTypeVariables: Set<UserDefinedTypeVariable>, val subordinates: List<BlockSubordinates>): BlockSubordinates {
    companion object {
        val empty = BlockEnvironment(Bindings.empty, Substitution.empty, Requirements.empty, emptySet(), emptyList())
    }
}

data class ExpressionEnvironment(val type: Type, val substitution: Substitution, val requirements: Requirements, val subordinates: List<ExpressionEnvironment>): BlockSubordinates {
    companion object {
        val empty = ExpressionEnvironment(Invalid, Substitution.empty, Requirements.empty, emptyList())
    }
}
