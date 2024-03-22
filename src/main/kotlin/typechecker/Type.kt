package typechecker

import kotlinx.serialization.*

@Serializable
sealed class Type {
    abstract val id: Long

    open val freeTypeVariables: Set<TypeVariable>
        get() = emptySet()

    fun isGround() = freeTypeVariables.isEmpty()

    abstract fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>): Boolean
}

@Serializable
sealed class ConcreteType: Type()

@Serializable
data class Invalid(override val id: Long): Type() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is Invalid
}

@Serializable
data class UnitType(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is UnitType
}

@Serializable
data class I8Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is I8Type
}

@Serializable
data class I16Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is I16Type
}

@Serializable
data class I32Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is I32Type
}

@Serializable
data class I64Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is I64Type
}

@Serializable
data class U8Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is U8Type
}

@Serializable
data class U16Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is U16Type
}

@Serializable
data class U32Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is U32Type
}

@Serializable
data class U64Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is U64Type
}

@Serializable
data class F32Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is F32Type
}

@Serializable
data class F64Type(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is F64Type
}

@Serializable
data class StringType(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is StringType
}

@Serializable
data class CharacterType(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is CharacterType
}

@Serializable
data class BooleanType(override val id: Long): ConcreteType() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>) = other is BooleanType
}

@Serializable
data class ProductType(override val id: Long, val types: List<Type>): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = types.fold(emptySet()) {acc, type -> acc + type.freeTypeVariables}

    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>): Boolean {
        if(other !is ProductType) return false
        if(types.size != other.types.size) return false
        for(i in types.indices) {
            if(!types[i].equalsUpToId(other.types[i], mapping)) return false
        }
        return true
    }
}

@Serializable
data class ArrowType(override val id: Long, val lhs: Type, val rhs: Type): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables

    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>): Boolean {
        if(other !is ArrowType) return false
        return lhs.equalsUpToId(other.lhs, mapping) && rhs.equalsUpToId(other.rhs, mapping)
    }
}

@Serializable
data class TypeScheme(override val id: Long, val boundVariables: List<TypeVariable>, val type: Type): Type() {
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>): Boolean {
        TODO()
    }
}

@Serializable
sealed class TypeVariable: Type()

@Serializable
data class UserDefinedTypeVariable(override val id: Long): TypeVariable() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>): Boolean {
        TODO()
    }
}

@Serializable
data class UnificationTypeVariable(override val id: Long): TypeVariable() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
    override fun equalsUpToId(other: Type, mapping: MutableMap<Long, Long>): Boolean {
        val mappedId = mapping[id]
        if(mappedId != null) return mappedId == other.id
        mapping[id] = other.id
        return true
    }
}