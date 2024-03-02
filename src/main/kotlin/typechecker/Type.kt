package typechecker

import kotlinx.serialization.*

@Serializable
sealed class Type {
    abstract val id: Long
    
    open val freeTypeVariables: Set<TypeVariable>
        get() = emptySet()

    fun isGround() = freeTypeVariables.isEmpty()
}
@Serializable
sealed class ConcreteType: Type()
@Serializable
data class Invalid(override val id: Long): Type()
@Serializable
data class UnitType(override val id: Long): ConcreteType()
@Serializable
data class I8Type(override val id: Long): ConcreteType()
@Serializable
data class I16Type(override val id: Long): ConcreteType()
@Serializable
data class I32Type(override val id: Long): ConcreteType()
@Serializable
data class I64Type(override val id: Long): ConcreteType()
@Serializable
data class U8Type(override val id: Long): ConcreteType()
@Serializable
data class U16Type(override val id: Long): ConcreteType()
@Serializable
data class U32Type(override val id: Long): ConcreteType()
@Serializable
data class U64Type(override val id: Long): ConcreteType()
@Serializable
data class F32Type(override val id: Long): ConcreteType()
@Serializable
data class F64Type(override val id: Long): ConcreteType()
@Serializable
data class StringType(override val id: Long): ConcreteType()
@Serializable
data class CharacterType(override val id: Long): ConcreteType()
@Serializable
data class BooleanType(override val id: Long): ConcreteType()
@Serializable
data class ProductType(override val id: Long, val types: List<Type>): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = types.fold(emptySet()) { acc, type -> acc + type.freeTypeVariables }
}
@Serializable
data class ArrowType(override val id: Long, val lhs: Type, val rhs: Type): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables
}
@Serializable
data class TypeScheme(override val id: Long, val boundVariables: List<TypeVariable>, val type: Type): Type()
@Serializable
sealed class TypeVariable: Type()
@Serializable
data class UserDefinedTypeVariable(override val id: Long): TypeVariable() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
}
@Serializable
data class UnificationTypeVariable(override val id: Long): TypeVariable() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
}