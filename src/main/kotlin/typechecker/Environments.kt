package typechecker

import parser.*

data class Subordinate<out T: Environment>(val offset: Span, val environment: T)

interface Environment

data class Bindings(val bindings: Map<BindableToken, TypeScheme>) {
    companion object {
        val empty = Bindings(emptyMap())
    }

    //todo handle multiple bindings
    operator fun plus(other: Bindings) = Bindings(bindings + other.bindings)

    operator fun get(token: BindableToken) = bindings[token]

    fun contains(token: BindableToken) = bindings.containsKey(token)

    fun addBinding(token: BindableToken, typeScheme: TypeScheme) =
        Bindings(bindings + (token to typeScheme))

    fun <T>fold(init: T, f: (T, BindableToken, TypeScheme) -> T): T {
        var acc = init
        for((key, value) in bindings) acc = f(acc, key, value)
        return acc
    }

    //todo figure out if these are right. do we need to map passed in?
    fun applySubstitution(substitution: Substitution, mappings: MutableMap<String, Type>) = Bindings(bindings.mapValues { substitution.apply(it.value, mappings) as TypeScheme })
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

    fun filter(f: (BindableToken, List<Type>) -> Boolean): Requirements = Requirements(requirements.filter { f(it.key, it.value) })

    fun map(f: (Type) -> Type) = Requirements(requirements.mapValues { it.value.map(f) })
    //todo figure out if these are right. do we need to map passed in?
    fun applySubstitution(substitution: Substitution, mapping: MutableMap<String, Type>) = Requirements(requirements.mapValues { it.value.map { type -> substitution.apply(type, mapping) } })
}

data class Compatibilities(val compatibilities: Map<UnificationTypeVariable, List<Type>>) {
    companion object {
        val empty = Compatibilities(emptyMap())
    }

    operator fun get(typeVar: UnificationTypeVariable) = compatibilities[typeVar]

    operator fun plus(other: Compatibilities): Compatibilities {
        val outCompatibilities = compatibilities.toMutableMap()
        for((typeVar, types) in other.compatibilities)
            outCompatibilities[typeVar] = (outCompatibilities[typeVar] ?: emptyList()) + types
        return Compatibilities(outCompatibilities)
    }

    fun <T>fold(init: T, f: (T, UnificationTypeVariable, List<Type>) -> T): T {
        var acc = init
        for((key, value) in compatibilities) acc = f(acc, key, value)
        return acc
    }

    //todo figure out if these are right. do we need to map passed in?
    fun applySubstitution(substitution: Substitution, mappings: MutableMap<String, Type>) = Compatibilities(compatibilities.mapValues { it.value.map { type -> substitution.apply(type, mappings) } })

    fun addCompatibilities(typeVars: List<UnificationTypeVariable>) = Compatibilities(compatibilities + typeVars.associateWith { emptyList() })
    fun makeCompatible(typeVar: UnificationTypeVariable, type: Type): Compatibilities {
        val types = (compatibilities[typeVar] ?: emptyList()) + type
        return Compatibilities(compatibilities + (typeVar to types))
    }
}

sealed interface BlockSubordinates: Environment {
    fun getSpansForType(type: Type): List<Span>
}

data class BlockEnvironment(val bindings: Bindings, val requirements: Requirements, val compatibilities: Compatibilities, val freeUserDefinedTypeVariables: Set<UserDefinedTypeVariable>, val mappings: Map<String, Type>, val subordinates: List<Subordinate<BlockSubordinates>>): BlockSubordinates {
    val ids = run {
        val bindingIds = bindings.fold(emptySet<String>()) { acc, _, typeScheme -> acc + typeScheme.getIds()}
        val requirementIds = requirements.fold(bindingIds) { acc, _, types -> types.fold(acc) { acc, type -> acc + type.getIds() } }
        compatibilities.fold(requirementIds) { acc, _, types -> types.fold(acc) { acc, type -> acc + type.getIds() } }
    }

    companion object {
        val empty = BlockEnvironment(Bindings.empty, Requirements.empty, Compatibilities.empty, emptySet(), emptyMap(), emptyList())
    }

    override fun getSpansForType(type: Type): List<Span> {
        if(!ids.contains(type.id)) return emptyList()
        val spans = mutableListOf<Span>()
        val type1 = mappings[type.id] ?: type
        for(subordinate in subordinates)
            spans.addAll(subordinate.environment.getSpansForType(type1).map { subordinate.offset + it })
        return spans
    }
}

data class AnnotationEnvironment(val type: Type): BlockSubordinates {
    override fun getSpansForType(type: Type) =
        if(type.id == this.type.id) listOf(Span.zero)
        else emptyList()
}

data class ExpressionEnvironment(val type: Type, val requirements: Requirements, val mappings: Map<String, Type>, val subordinates: List<Subordinate<ExpressionEnvironment>>): BlockSubordinates {
    val ids = requirements.fold(type.getIds()) { acc, _, types -> types.fold(acc) { acc, type -> acc + type.getIds() } }
    companion object {
        //TODO, probably not right????
        val empty = ExpressionEnvironment(Invalid(nextId()), Requirements.empty, emptyMap(), emptyList())
    }

    override fun getSpansForType(type: Type): List<Span> {
        if(!ids.contains(type.id)) return emptyList()
        if(subordinates.isEmpty()) return listOf(Span.zero)
        val spans = mutableListOf<Span>()
        val type1 = mappings[type.id] ?: type
        for(subordinate in subordinates)
            spans.addAll(subordinate.environment.getSpansForType(type1).map { subordinate.offset + it })
        return spans
    }
}
