package typechecker

sealed interface Type {
    val freeTypeVariables: Set<TypeVariable>
}

object IntType: Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf()

    override fun toString() = "Int"
}
object FloatType: Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf()

    override fun toString() = "Float"
}
object StringType: Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf()

    override fun toString() = "String"
}
object CharType: Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf()

    override fun toString() = "Char"
}
object UnitType: Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf()

    override fun toString() = "Unit"
}

data class Arrow(val lhs: Type, val rhs: Type): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables

    override fun toString() = "($lhs -> $rhs)"
}
data class Sum(val lhs: Type, val rhs: Type): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = lhs.freeTypeVariables + rhs.freeTypeVariables

    override fun toString() = "($lhs x $rhs)"
}


data class TypeVariable(val n: Int): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = setOf(this)

    override fun toString() = "t$n"
}
data class TypeScheme(val boundVariable: TypeVariable, val body: Type): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = body.freeTypeVariables - boundVariable

    override fun toString() = "$boundVariable.$body"
}

data class OpenRecordType(val labels: Map<String, Type>, val row: TypeVariable): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = labels.values.fold(setOf(row)) { acc, type -> acc + type.freeTypeVariables }

    override fun toString() =
        when(labels.size) {
            0 -> "{row=$row}"
            1 -> {
                val (name, type) = labels.toList().first()
                "{$name: $type, row=$row}"
            }
            else -> labels.toList().fold("{") { acc, pair -> acc + "${pair.first}: ${pair.second}, " }.dropLast(2) + "row=\$}"
        }
}
data class ClosedRecordType(val labels: Map<String, Type>): Type {
    override val freeTypeVariables: Set<TypeVariable>
        get() = labels.values.fold(setOf()) { acc, type -> acc + type.freeTypeVariables }

    override fun toString() =
        when(labels.size) {
            0 -> "{}"
            1 -> {
                val (name, type) = labels.toList().first()
                "{$name: $type}"
            }
            else -> labels.toList().fold("{") { acc, pair -> acc + "${pair.first}: ${pair.second}, " }.dropLast(2) + "}"
        }
}