package typechecker

import kotlinx.serialization.*
import parser.*

@Serializable
sealed class Type {
    abstract val offset: Span

    open val freeTypeVariables: Set<TypeVariable>
        get() = emptySet()

    abstract fun updateOffset(offset: Span): Type
}

sealed class ConcreteType: Type()

@Serializable
data class Invalid(override val offset: Span): Type() {
    override fun updateOffset(offset: Span) = Invalid(offset)
}

@Serializable
data class UnitType(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = UnitType(offset)
}

@Serializable
data class I8Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = I8Type(offset)
}

@Serializable
data class I16Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = I16Type(offset)
}

@Serializable
data class I32Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = I32Type(offset)
}

@Serializable
data class I64Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = I64Type(offset)
}

@Serializable
data class U8Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = U8Type(offset)
}

@Serializable
data class U16Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = U16Type(offset)
}

@Serializable
data class U32Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = U32Type(offset)
}

@Serializable
data class U64Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = U64Type(offset)
}

@Serializable
data class F32Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = F32Type(offset)
}

@Serializable
data class F64Type(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = F64Type(offset)
}

@Serializable
data class StringType(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = StringType(offset)
}

@Serializable
data class CharacterType(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = CharacterType(offset)
}

@Serializable
data class BooleanType(override val offset: Span): ConcreteType() {
    override fun updateOffset(offset: Span) = BooleanType(offset)
}


@Serializable
data class ProductType(override val offset: Span, val types: List<Type>): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = types.fold(emptySet()) {acc, type -> acc + type.freeTypeVariables}

    override fun updateOffset(offset: Span) = ProductType(offset, types)
}

@Serializable
data class ArrowType(override val offset: Span, val lhs: Type, val rhs: Type): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables

    override fun updateOffset(offset: Span) = ArrowType(offset, lhs, rhs)
}

@Serializable
data class TypeScheme(override val offset: Span, val boundVariables: List<TypeVariable>, val type: Type): Type() {
    override fun updateOffset(offset: Span) = TypeScheme(offset, boundVariables, type)
}

@Serializable
data class Placeholder(override val offset: Span, val id: Long): Type() {
    override fun updateOffset(offset: Span) = Placeholder(offset, id)
}

@Serializable
data class TypeVariable(override val offset: Span, val id: Long): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)

    override fun updateOffset(offset: Span) = TypeVariable(offset, id)
}