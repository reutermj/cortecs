package typechecker

sealed interface Type {
    val freeTypeVariables: Set<TypeVariable>
        get() = emptySet()

    fun isGround() = freeTypeVariables.isEmpty()
}

sealed interface ConcreteType: Type

object Invalid: Type

object UnitType: ConcreteType

object I8Type: ConcreteType
object I16Type: ConcreteType
object I32Type: ConcreteType
object I64Type: ConcreteType

object U8Type: ConcreteType
object U16Type: ConcreteType
object U32Type: ConcreteType
object U64Type: ConcreteType

object F32Type: ConcreteType
object F64Type: ConcreteType

object StringType: ConcreteType
object CharacterType: ConcreteType

object BooleanType: ConcreteType

data class ProductType(val types: List<Type>): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = types.fold(emptySet()) { acc, type -> acc + type.freeTypeVariables }
}

data class ArrowType(val lhs: Type, val rhs: Type): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables
}

data class TypeScheme(val boundVariables: List<TypeVariable>, val type: Type): Type

sealed interface TypeVariable: Type {
    val n: String
}
data class UserDefinedTypeVariable(override val n: String): TypeVariable {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
}
data class UnificationTypeVariable(override val n: String): TypeVariable {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)
}