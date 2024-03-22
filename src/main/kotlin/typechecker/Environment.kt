package typechecker

import errors.*
import kotlinx.serialization.*
import parser.*

@Serializable
sealed class Environment {
    abstract fun getSpansForType(type: Type): List<Span>
    abstract fun applySubstitution(type: Type): Type
    abstract fun equalsUpToId(other: Environment): Boolean
}

@Serializable
data class Subordinate<T: Environment>(val offset: Span, val environment: T) {
    fun equalsUpToId(other: Subordinate<T>): Boolean {
        return offset == other.offset && environment.equalsUpToId(other.environment)
    }
}

@Serializable
data class Bindings(val bindings: Map<BindableToken, Type>) {
    companion object {
        val empty = Bindings(emptyMap())
    }

    //todo handle multiple bindings
    operator fun plus(other: Bindings) = Bindings(bindings + other.bindings)

    operator fun get(token: BindableToken) = bindings[token]

    fun contains(token: BindableToken) = bindings.containsKey(token)

    fun addBinding(token: BindableToken, type: Type) =
        Bindings(bindings + (token to type))

    fun <T>fold(init: T, f: (T, BindableToken, Type) -> T): T {
        var acc = init
        for((key, value) in bindings) acc = f(acc, key, value)
        return acc
    }

    //todo figure out if these are right. do we need to map passed in?
    fun applySubstitution(substitution: Substitution, mappings: MutableMap<Long, Type>) = Bindings(bindings.mapValues {
        val outType = substitution.apply(it.value, mappings)
        if(outType is TypeScheme) outType
        else TODO()
    })
}

@Serializable
data class Requirements(val requirements: Map<BindableToken, List<Type>>) {
    companion object {
        val empty = Requirements(emptyMap())
    }

    operator fun plus(other: Requirements): Requirements {
        val outRequirements = requirements.toMutableMap()
        for((token, types) in other.requirements) outRequirements[token] = (requirements[token] ?: emptyList()) + types

        return Requirements(outRequirements)
    }

    operator fun get(token: BindableToken) = requirements[token]

    fun addRequirement(token: BindableToken, type: Type): Requirements {
        val requirement = (requirements[token] ?: emptyList()) + type
        return copy(requirements = requirements + (token to requirement))
    }

    fun applySubstitution(substitution: Substitution, mapping: MutableMap<Long, Type>) = Requirements(requirements.mapValues {kv -> kv.value.map {substitution.apply(it, mapping)}})

    fun equalsUpToId(other: Requirements, mapping: MutableMap<Long, Long>): Boolean {
        for((name, types) in requirements) {
            val otherTypes = other[name] ?: return false
            if(types.size != otherTypes.size) return false
            for(i in types.indices) {
                if(!types[i].equalsUpToId(otherTypes[i], mapping)) return false
            }
        }
        return true
    }
}

@Serializable
sealed class BlockEnvironment: Environment() {
    abstract val bindings: Bindings
    abstract val requirements: Requirements
    abstract val errors: CortecsErrors
}

@Serializable
data class LetEnvironment(val annotation: Type?, val annotationOffset: Span, val subordinate: Subordinate<ExpressionEnvironment>, val substitution: Substitution, override val bindings: Bindings, override val requirements: Requirements, override val errors: CortecsErrors): BlockEnvironment() {
    override fun getSpansForType(type: Type) =
        if(annotation?.id == type.id) listOf(annotationOffset)
        else subordinate.environment.getSpansForType(type).map {subordinate.offset + it }

    //todo why do I need this mutable map here?
    override fun applySubstitution(type: Type) = substitution.apply(type, mutableMapOf())
    override fun equalsUpToId(other: Environment): Boolean {
        TODO("Not yet implemented")
    }
}

@Serializable
data class ReturnEnvironment(val subordinate: Subordinate<ExpressionEnvironment>, override val requirements: Requirements, override val errors: CortecsErrors): BlockEnvironment() {
    override val bindings: Bindings
        get() = Bindings.empty

    override fun getSpansForType(type: Type) = subordinate.environment.getSpansForType(type).map {subordinate.offset + it }
    override fun applySubstitution(type: Type) = type
    override fun equalsUpToId(other: Environment): Boolean {
        TODO("Not yet implemented")
    }
}

@Serializable
sealed class ExpressionEnvironment: Environment() {
    abstract val expressionType: Type
    abstract val requirements: Requirements
    abstract val errors: CortecsErrors
}

@Serializable
data class FunctionCallExpressionEnvironment(
    override val expressionType: Type,
    val functionType: Type,
    override val requirements: Requirements,
    val substitution: Substitution,
    val functionSubordinate: Subordinate<ExpressionEnvironment>,
    val argumentSubordinates: List<Subordinate<ExpressionEnvironment>>,
    override val errors: CortecsErrors): ExpressionEnvironment() {
    override fun getSpansForType(type: Type) = when(type.id) {
        expressionType.id, functionType.id -> {
            val environment = functionSubordinate.environment
            environment.getSpansForType(environment.expressionType).map {functionSubordinate.offset + it}
        }

        else -> functionSubordinate.environment.getSpansForType(type).map {functionSubordinate.offset + it} + argumentSubordinates.flatMap {subordinate ->
            subordinate.environment.getSpansForType(type).map {subordinate.offset + it}
        }
    }

    override fun applySubstitution(type: Type) = substitution.apply(type, mutableMapOf())
    override fun equalsUpToId(other: Environment): Boolean {
        TODO("Not yet implemented")
    }
}

@Serializable
data class BinaryExpressionEnvironment(
    override val expressionType: Type,
    val opType: Type,
    val opSpan: Span,
    override val requirements: Requirements,
    val lhsSubordinate: Subordinate<ExpressionEnvironment>,
    val rhsSubordinate: Subordinate<ExpressionEnvironment>?,
    override val errors: CortecsErrors): ExpressionEnvironment() {
    override fun getSpansForType(type: Type) = when(type.id) { //todo do these ids need to be different?
        expressionType.id, opType.id -> listOf(opSpan)
        else -> {
            val lhsSpans = lhsSubordinate.environment.getSpansForType(type).map {lhsSubordinate.offset + it}
            if(rhsSubordinate == null) lhsSpans
            else {
                val rhsSpans = rhsSubordinate.environment.getSpansForType(type).map {rhsSubordinate.offset + it}
                lhsSpans + rhsSpans
            }
        }
    }

    override fun applySubstitution(type: Type) = type
    override fun equalsUpToId(other: Environment): Boolean {
        TODO("Not yet implemented")
    }
}

@Serializable
data class UnaryExpressionEnvironment(
    override val expressionType: Type, val opType: Type, override val requirements: Requirements, val subordinate: Subordinate<ExpressionEnvironment>, override val errors: CortecsErrors): ExpressionEnvironment() {
    override fun getSpansForType(type: Type) = when(type.id) { //todo do these ids need to be different?
        expressionType.id, opType.id -> listOf(Span.zero)
        else -> subordinate.environment.getSpansForType(type).map {subordinate.offset + it}
    }

    override fun applySubstitution(type: Type) = type
    override fun equalsUpToId(other: Environment): Boolean {
        TODO("Not yet implemented")
    }
}

@Serializable
data class GroupingExpressionEnvironment(
    override val expressionType: Type, override val requirements: Requirements, val subordinate: Subordinate<ExpressionEnvironment>, override val errors: CortecsErrors): ExpressionEnvironment() {
    override fun getSpansForType(type: Type) = subordinate.environment.getSpansForType(type).map {subordinate.offset + it}
    override fun applySubstitution(type: Type) = type

    override fun equalsUpToId(other: Environment): Boolean {
        if(other !is GroupingExpressionEnvironment) return false
        val mapping = mutableMapOf<Long, Long>()

        return expressionType.equalsUpToId(other.expressionType, mapping) && requirements.equalsUpToId(other.requirements, mapping) && subordinate.equalsUpToId(other.subordinate) && errors == other.errors
    }
}

@Serializable
data class AtomicExpressionEnvironment(override val expressionType: Type, override val requirements: Requirements): ExpressionEnvironment() {
    override fun getSpansForType(type: Type) = if(type.id == expressionType.id) listOf(Span.zero)
    else emptyList()

    override fun applySubstitution(type: Type) = type
    override val errors: CortecsErrors
        get() = CortecsErrors.empty
    override fun equalsUpToId(other: Environment): Boolean {
        if(other !is AtomicExpressionEnvironment) return false
        val mapping = mutableMapOf<Long, Long>()
        return expressionType.equalsUpToId(other.expressionType, mapping) && requirements.equalsUpToId(other.requirements, mapping)
    }
}

@Serializable
data object EmptyExpressionEnvironment: ExpressionEnvironment() {
    override val expressionType = Invalid(getNextId())
    override val requirements = Requirements.empty
    override val errors: CortecsErrors = CortecsErrors.empty
    override fun getSpansForType(type: Type) = emptyList<Span>()
    override fun applySubstitution(type: Type) = type
    override fun equalsUpToId(other: Environment) = other is EmptyExpressionEnvironment
}