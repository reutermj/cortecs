package typechecker

import kotlinx.serialization.*

@Serializable
sealed class Type {
    open val freeTypeVariables: Set<TypeVariable>
        get() = emptySet()

    fun isGround() = freeTypeVariables.isEmpty()
}
@Serializable
sealed class ConcreteType: Type()
@Serializable
data object Invalid: Type()
@Serializable
data object UnitType: ConcreteType()
@Serializable
data object I8Type: ConcreteType()
@Serializable
data object I16Type: ConcreteType()
@Serializable
data object I32Type: ConcreteType()
@Serializable
data object I64Type: ConcreteType()
@Serializable
data object U8Type: ConcreteType()
@Serializable
data object U16Type: ConcreteType()
@Serializable
data object U32Type: ConcreteType()
@Serializable
data object U64Type: ConcreteType()
@Serializable
data object F32Type: ConcreteType()
@Serializable
data object F64Type: ConcreteType()
@Serializable
data object StringType: ConcreteType()
@Serializable
data object CharacterType: ConcreteType()
@Serializable
data object BooleanType: ConcreteType()
@Serializable
data class ProductType(val types: List<Type>): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = types.fold(emptySet()) { acc, type -> acc + type.freeTypeVariables }
}
@Serializable
data class ArrowType(val lhs: Type, val rhs: Type): Type() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables
}
@Serializable
data class TypeScheme(val boundVariables: List<TypeVariable>, val type: Type): Type()
@Serializable
sealed class TypeVariable: Type() {
    abstract val n: String
}
@Serializable
data class UserDefinedTypeVariable(override val n: String): TypeVariable() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
}
@Serializable
data class UnificationTypeVariable(override val n: String): TypeVariable() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
}