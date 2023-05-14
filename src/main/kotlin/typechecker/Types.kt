package typechecker

sealed interface Kind
object TypeKind: Kind
object EntityKind: Kind
object ComponentKind: Kind
object EntityOrComponentKind: Kind
object UndeterminedKind: Kind
sealed interface Type {
    val freeTypeVariables: Set<TypeVariable>
    val kind: Kind
}
sealed interface MonomorphicType: Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = emptySet()
}
object IntType: MonomorphicType {
    override val kind: Kind
        get() = TypeKind

    override fun toString() = "Int"
}
object FloatType: MonomorphicType {
    override val kind: Kind
        get() = TypeKind

    override fun toString() = "Float"
}
object StringType: MonomorphicType {
    override val kind: Kind
        get() = TypeKind

    override fun toString() = "String"
}
object CharType: MonomorphicType {
    override val kind: Kind
        get() = TypeKind

    override fun toString() = "Char"
}
object UnitType: MonomorphicType {
    override val kind: Kind
        get() = TypeKind

    override fun toString() = "Unit"
}
/*data class MonomorphicFunctionType(val lhs: MonomorphicType, val rhs: MonomorphicType): MonomorphicType {
    override val kind: Kind
        get() = TypeKind

    override fun toString() = "($lhs -> $rhs)"
}*/
data class FunctionType(val lhs: Type, val rhs: Type): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables
    override val kind: Kind
        get() = TypeKind

    override fun toString() = "($lhs -> $rhs)"
}
/*data class MonomorphicSumType(val types: List<MonomorphicType>): MonomorphicType {
    override val kind: Kind
        get() = TypeKind

    override fun toString() = types.joinToString(" x ", "(", ")") { it.toString() }
}*/
data class SumType(val types: List<Type>): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = types.fold(emptySet()) { acc, type -> acc + type.freeTypeVariables }
    override val kind: Kind
        get() = TypeKind

    override fun toString() = types.joinToString(" x ", "(", ")") { it.toString() }
}
data class TypeVariable(val n: Int, override val kind: Kind): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)

    override fun toString() =
        when(kind) {
            is UndeterminedKind -> "u$n"
            is TypeKind -> "t$n"
            is EntityOrComponentKind -> "ec$n"
            is EntityKind -> "e$n"
            is ComponentKind -> "c$n"
        }
}
data class TypeScheme(val boundVariables: Set<TypeVariable>, val body: Type): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = body.freeTypeVariables - boundVariables

    override val kind: Kind
        get() = TypeKind

    override fun toString() = "${boundVariables.joinToString(",")}.$body"
}
data class OpenEntityType(val components: Set<Type>, val row: TypeVariable): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = components.fold(row.freeTypeVariables) { acc, type -> acc + type.freeTypeVariables }

    override val kind: Kind
        get() = EntityKind

    override fun toString() =
        when(components.size) {
            0 -> "{row=$row}"
            1 -> {
                val type = components.first()
                "{$type, row=$row}"
            }
            else -> components.fold("{") { acc, type -> "$acc$type, " } + "row=$row}"
        }
}
data class ClosedEntityType(val components: Set<Type>): MonomorphicType { //todo hmmmm
    override val freeTypeVariables: Set<TypeVariable>
        get() = components.fold(setOf()) { acc, type -> acc + type.freeTypeVariables }

    override val kind: Kind
        get() = EntityKind

    override fun toString() =
        when(components.size) {
            0 -> "{}"
            1 -> {
                val type = components.first()
                "{$type}"
            }
            else -> components.fold("{") { acc, type -> "$acc$type, " }.dropLast(2) + "}"
        }
}
data class OpenComponentType(val labels: Map<String, Type>, val row: TypeVariable): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = labels.values.fold(setOf(row)) { acc, type -> acc + type.freeTypeVariables }

    override val kind: Kind
        get() = ComponentKind

    override fun toString() =
        when(labels.size) {
            0 -> "{row=$row}"
            1 -> {
                val (name, type) = labels.toList().first()
                "{$name: $type, row=$row}"
            }
            else -> labels.toList().fold("{") { acc, pair -> acc + "${pair.first}: ${pair.second}, " }.dropLast(2) + ", row=$row}"
        }
}
data class ClosedComponentType(val name: String, val labels: Map<String, Type>): MonomorphicType { //todo hmmm
    override val freeTypeVariables: Set<TypeVariable>
        get() = labels.values.fold(setOf()) { acc, type -> acc + type.freeTypeVariables }

    override val kind: Kind
        get() = ComponentKind

    override fun toString() = name
}

data class EntityOrComponentType(val component: Type): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = component.freeTypeVariables

    override val kind: Kind
        get() = EntityOrComponentKind
}