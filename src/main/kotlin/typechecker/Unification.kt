package typechecker

fun applySubstitutions(t: Type, substitutions: Map<TypeVariable, Type>): Type {
    return when(t) {
        is TypeVariable -> {
            val substitution = substitutions[t]
            if(substitution == null) t
            else applySubstitutions(substitution, substitutions)
        }

        is TypeScheme -> TypeScheme(t.boundVariable, applySubstitutions(t.body, substitutions - t.boundVariable))

        is Arrow -> Arrow(applySubstitutions(t.lhs, substitutions), applySubstitutions(t.rhs, substitutions))
        is Sum -> Sum(applySubstitutions(t.lhs, substitutions), applySubstitutions(t.rhs, substitutions))

        is UnitType -> t
        is IntType -> t
        is FloatType -> t
        is StringType -> t
        is CharType -> t

        is OpenRecordType -> {
            val labels = t.labels.mapValues { applySubstitutions(it.value, substitutions) }
            when(val bla = applySubstitutions(t.row, substitutions)) {
                is OpenRecordType -> OpenRecordType(bla.labels + labels, bla.row) //bla.labels + labels is important because kotlin prioritizes rhs when creating the new map
                is ClosedRecordType -> ClosedRecordType(bla.labels + labels)
                is TypeVariable -> OpenRecordType(labels, bla)
                else -> throw Exception()
            }
        }

        is ClosedRecordType -> {
            val labels = t.labels.mapValues { applySubstitutions(it.value, substitutions) }
            ClosedRecordType(labels)
        }

        else -> throw Exception()
    }
}

fun instantiate(t: Type, substitutions: Map<TypeVariable, Type> = mapOf()): Type {
    return when(t) {
        is TypeScheme -> instantiate(t.body, substitutions + (t.boundVariable to freshTypeVariable()))
        else -> applySubstitutions(t, substitutions)
    }
}

fun unify(constraints: List<Constraint>, substitutions: Map<TypeVariable, Type> = mapOf()): Map<TypeVariable, Type> {
    return if(constraints.isEmpty()) substitutions
    else {
        val head = constraints.first()
        val tail = constraints.drop(1)
        val lhs = applySubstitutions(head.lhs, substitutions)
        val rhs = applySubstitutions(head.rhs, substitutions)

        if(lhs == rhs) unify(tail, substitutions)
        else if(lhs is TypeVariable && !rhs.freeTypeVariables.contains(lhs)) unify(tail, substitutions + Pair(lhs, rhs))
        else if(rhs is TypeVariable && !lhs.freeTypeVariables.contains(rhs)) unify(tail, substitutions + Pair(rhs, lhs))
        else if(lhs is Arrow && rhs is Arrow) unify(listOf(Constraint(lhs.lhs, rhs.lhs), Constraint(lhs.rhs, rhs.rhs)) + tail, substitutions)
        else if(lhs is OpenRecordType && rhs is OpenRecordType) unify(lhs, rhs, tail, substitutions)
        else if(lhs is ClosedRecordType && rhs is OpenRecordType) unify(rhs, lhs, tail, substitutions)
        else if(lhs is OpenRecordType && rhs is ClosedRecordType) unify(lhs, rhs, tail, substitutions)
        else if(lhs is ClosedRecordType && rhs is ClosedRecordType) unify(lhs, rhs, tail, substitutions)
        else throw Exception()
    }
}

fun unify(lhs: OpenRecordType, rhs: OpenRecordType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    val constraints = mutableListOf<Constraint>()
    for(label in lhs.labels.keys.intersect(rhs.labels.keys)) constraints.add(Constraint(lhs.labels[label]!!, rhs.labels[label]!!))

    val mlhs = (lhs.labels.keys - rhs.labels.keys).associateWith { lhs.labels[it]!! }
    constraints.add(Constraint(rhs.row, OpenRecordType(mlhs, freshTypeVariable())))

    val mrhs = (rhs.labels.keys - lhs.labels.keys).associateWith { rhs.labels[it]!! }
    constraints.add(Constraint(lhs.row, OpenRecordType(mrhs, freshTypeVariable())))

    return unify(constraints + tail, substitutions)
}

fun unify(lhs: OpenRecordType, rhs: ClosedRecordType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    //if((lhs.labels.keys - rhs.labels.keys).any()) throw Exception()

    val constraints = mutableListOf<Constraint>()
    for(label in lhs.labels.keys.intersect(rhs.labels.keys)) constraints.add(Constraint(lhs.labels[label]!!, rhs.labels[label]!!))

    val mrhs = (rhs.labels.keys - lhs.labels.keys).associateWith { rhs.labels[it]!! }
    constraints.add(Constraint(lhs.row, OpenRecordType(mrhs, freshTypeVariable())))

    return unify(constraints + tail, substitutions)
}

fun unify(lhs: ClosedRecordType, rhs: ClosedRecordType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    if(lhs.labels.keys != rhs.labels.keys) throw Exception()

    val constraints = mutableListOf<Constraint>()
    for(label in lhs.labels.keys) constraints.add(Constraint(lhs.labels[label]!!, rhs.labels[label]!!))

    return unify(constraints + tail, substitutions)
}

fun generalize(constraints: List<Constraint>, environment: Environment, type: Type): Pair<Environment, Type> {
    val substitutions = unify(constraints)
    val newType = applySubstitutions(type, substitutions)
    val newEnvironment = environment.applySubstitutions(substitutions)
    val variablesToGeneralize = newType.freeTypeVariables - newEnvironment.freeTypeVariables
    val typeScheme = variablesToGeneralize.fold(newType) { acc, t -> TypeScheme(t, acc) }
    return Pair(newEnvironment, typeScheme)
}
