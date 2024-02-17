package typechecker

import parser.*
import utilities.*

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

    fun plus(other: BlockEnvironment): BlockEnvironment {
        val outBindings = bindings + other.bindings
        val outRequirements = mutableMapOf<BindableToken, List<Type>>()
        val outSubstitution = merge(substitution + other.substitution, requirements, bindings, other.requirements, outRequirements)
        val outFreeUserDefinedTypeVariable = freeUserDefinedTypeVariables + other.freeUserDefinedTypeVariables
        val outSubordinates = subordinates + other.subordinates //todo this isnt right
        for((token, typeVars) in requirements.requirements) if(!outRequirements.containsKey(token)) outRequirements[token] = typeVars
        return BlockEnvironment(outBindings, outSubstitution, Requirements(outRequirements), outFreeUserDefinedTypeVariable, outSubordinates)

        //BlockEnvironment(outSubstitution, outBindings, outRequirements, )
    }
}

data class ExpressionEnvironment(val type: Type, val substitution: Substitution, val requirements: Requirements, val subordinates: List<ExpressionEnvironment>): BlockSubordinates {
    companion object {
        val empty = ExpressionEnvironment(Invalid, Substitution.empty, Requirements.empty, emptyList())
    }
}

fun merge(substitution: Substitution, requirements: Requirements, bindings: Bindings, otherRequirements: Requirements, outRequirements: MutableMap<BindableToken, List<Type>>): Substitution =
    otherRequirements.fold(substitution) { acc, token, otherTypeVars ->
        val typeScheme = bindings[token]
        if(typeScheme != null)
            otherTypeVars.fold(acc) { acc, typeVar ->
                val (instantiated, instSubstitution) = instantiate(typeScheme, acc)
                instSubstitution.unify(typeVar, instantiated)
            }
        else if(outRequirements.containsKey(token)) acc
        else {
            val typeVars = requirements[token]
            if(typeVars == null) outRequirements[token] = otherTypeVars
            else outRequirements[token] = typeVars + otherTypeVars
            acc
        }
    }

fun instantiate(typeScheme: TypeScheme, substitution: Substitution): Pair<Type, Substitution> {
    val outSubstitution = substitution.mapping.toMutableMap()
    val mapping =
        typeScheme.boundVariables.associateWith {
            when(it) {
                is UserDefinedTypeVariable -> freshUnificationVariable()
                is UnificationTypeVariable ->
                    when(val lookup = substitution.find(it)) {
                        is Representative -> throw Exception("error")
                        is TypeMapping -> lookup.type//todo does this need to be recusive?
                        is Compatibility -> {
                            val fresh = freshUnificationVariable()
                            outSubstitution[lookup.typeVar] = lookup.copy(typeVars = lookup.typeVars + fresh)
                            fresh
                        }
                    }
            }
        }

    fun inst(type: Type): Type =
        when(type) {
            is TypeVariable -> mapping[type] ?: throw Exception()
            is ArrowType -> ArrowType(inst(type.lhs), inst(type.rhs))
            is ProductType -> ProductType(type.types.map { inst(it) })
            else -> type
        }

    return Pair(inst(typeScheme.type), Substitution(outSubstitution))
}