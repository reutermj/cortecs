package typechecker

import kotlinx.serialization.*

@Serializable
sealed class Type {
    abstract val id: String
    open val freeTypeVariables: Set<TypeVariable>
        get() = emptySet()
    abstract fun getIds(): Set<String>

    fun isGround() = freeTypeVariables.isEmpty()
}
@Serializable
sealed class ConcreteType: Type() {
    override fun getIds() = setOf(id)
}
@Serializable
data class Invalid(override val id: String): Type() {
    override fun getIds() = setOf(id)
}
@Serializable
data class UnitType(override val id: String): ConcreteType()
@Serializable
data class I8Type(override val id: String): ConcreteType()
@Serializable
data class I16Type(override val id: String): ConcreteType()
@Serializable
data class I32Type(override val id: String): ConcreteType()
@Serializable
data class I64Type(override val id: String): ConcreteType()
@Serializable
data class U8Type(override val id: String): ConcreteType()
@Serializable
data class U16Type(override val id: String): ConcreteType()
@Serializable
data class U32Type(override val id: String): ConcreteType()
@Serializable
data class U64Type(override val id: String): ConcreteType()
@Serializable
data class F32Type(override val id: String): ConcreteType()
@Serializable
data class F64Type(override val id: String): ConcreteType()
@Serializable
data class StringType(override val id: String): ConcreteType()
@Serializable
data class CharacterType(override val id: String): ConcreteType()
@Serializable
data class BooleanType(override val id: String): ConcreteType()
@Serializable
data class ProductType(override val id: String, val types: List<Type>): Type() {
    override fun getIds(): Set<String> =
        types.fold(setOf(id)) { acc, type -> acc + type.getIds() }

    override val freeTypeVariables: Set<TypeVariable>
        get() = types.fold(emptySet()) { acc, type -> acc + type.freeTypeVariables }
}
@Serializable
data class ArrowType(override val id: String, val lhs: Type, val rhs: Type): Type() {
    override fun getIds(): Set<String> =
        lhs.getIds() + rhs.getIds() + id
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables
}
@Serializable
data class TypeScheme(override val id: String, val boundVariables: List<TypeVariable>, val type: Type): Type() {
    override fun getIds(): Set<String> =
        boundVariables.fold(type.getIds() + id) { acc, typeVar -> acc + typeVar.getIds() }
}
@Serializable
sealed class TypeVariable: Type() {
    override fun getIds() = setOf(id)
}
@Serializable
data class UserDefinedTypeVariable(override val id: String): TypeVariable() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
}
@Serializable
data class UnificationTypeVariable(override val id: String): TypeVariable() {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
}