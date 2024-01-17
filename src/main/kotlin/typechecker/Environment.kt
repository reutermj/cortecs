package typechecker

import errors.*
import kotlinx.serialization.*
import parser.*

class Environment {
    val bindings = Bindings()
    val requirements = Requirements()
    val constraints = Constraints()
}

@Serializable
class FunctionEnvironment(val requirements: Requirements, val errors: CortecsErrors)

class Bindings {
    val stack = mutableListOf(mutableMapOf<NameToken, Type>())

    fun push() {
        stack.add(mutableMapOf())
    }

    fun pop() {
        stack.removeLast()
    }

    fun add(name: NameToken, type: Type) {
        stack.last()[name] = type
    }

    operator fun get(name: NameToken): Type? {
        for(bindings in stack.reversed()) {
            val type = bindings[name]
            if(type != null) {
                return type
            }
        }
        return null
    }
}

@Serializable
class Requirements {
    val placeholderLookup = mutableMapOf<BindableToken, MutableList<Placeholder>>()
    val requirementsLookup = mutableMapOf<Long, MutableList<Type>>()
    fun add(token: BindableToken, placeholder: Placeholder) {
        val placeholders = placeholderLookup.getOrPut(token) { mutableListOf() }
        placeholders.add(placeholder)
    }

    fun add(placeholder: Placeholder, type: Type) {
        val types = requirementsLookup.getOrPut(placeholder.id) { mutableListOf() }
        types.add(type)
    }

    fun apply(substitution: Substitution) {
        for((_, items) in requirementsLookup) {
            for(i in items.indices) {
                items[i] = substitution.apply(items[i])
            }
        }
    }
}

class Substitution {
    val substitution = mutableMapOf<Long, Type>()
    val fillings = mutableMapOf<Long, Type>()

    fun add(typeVariable: TypeVariable, type: Type) {
        substitution[typeVariable.id] = type
    }

    fun fillPlaceholder(placeholder: Placeholder, type: Type) {
        fillings[placeholder.id] = type
    }

    fun apply(type: Type, offset: Span? = null): Type =
        when(type) {
            is Placeholder -> fillings[type.id]?.let { apply(it, null) } ?: type
            is ConcreteType ->
                if(offset != null) type.updateOffset(offset)
                else type
            is TypeVariable -> {
                val out = substitution[type.id]
                if(out != null) apply(out, type.offset)
                else type
            }
            is ArrowType -> ArrowType(offset ?: type.offset, apply(type.lhs, offset), apply(type.rhs, offset))
            is ProductType -> ProductType(offset ?: type.offset, type.types.map { apply(type, offset) })
            else ->
                TODO()
        }
}

class Constraints {
    val constraints = mutableListOf<Pair<Type, Type>>()
    fun add(lhs: Type, rhs: Type) {
        constraints.add(Pair(lhs, rhs))
    }

    fun unify(requirements: Requirements, errors: MutableList<CortecsError>): Substitution {
        val substitution = Substitution()
        for(constraint in constraints) {
            val lhs = substitution.apply(constraint.first)
            val rhs = substitution.apply(constraint.second)
            unify(lhs, rhs, substitution, requirements, errors)
        }
        return substitution
    }

    fun pointAt(substitution: Substitution, typeVariable: TypeVariable, type: Type) {
        if(type is TypeVariable && type.id == typeVariable.id) {
            return
        }
        substitution.add(typeVariable, type)
    }

    fun unify(lhs: Type, rhs: Type, substitution: Substitution, requirements: Requirements, errors: MutableList<CortecsError>) {
        when {
            lhs is ConcreteType && rhs is ConcreteType && lhs::class == rhs::class -> {}
            lhs is TypeVariable -> pointAt(substitution, lhs, rhs)
            rhs is TypeVariable -> pointAt(substitution, rhs, lhs)
            lhs is Placeholder && rhs !is Placeholder -> requirements.add(lhs, rhs)
            lhs !is Placeholder && rhs is Placeholder -> requirements.add(rhs, lhs)
            lhs is ArrowType && rhs is ArrowType -> {
                unify(lhs.lhs, rhs.lhs, substitution, requirements, errors)
                unify(lhs.rhs, rhs.rhs, substitution, requirements, errors)
            }
            lhs is ProductType && rhs is ProductType -> {
                if(lhs.types.size != rhs.types.size) TODO()
                for(i in lhs.types.indices) {
                    unify(lhs.types[i], rhs.types[i], substitution, requirements, errors)
                }
            }
            else -> errors.add(CortecsError("Unification error", rhs.offset, Span.zero))
        }
    }
}