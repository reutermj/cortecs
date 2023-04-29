package typechecker

fun applySubstitutions(t: Type, substitutions: Map<TypeVariable, Type>): Type {
    return if(substitutions.isEmpty()) t
    else when(t) {
        is TypeVariable -> {
            val substitution = substitutions[t]
            if(substitution == null) t
            else applySubstitutions(substitution, substitutions)
        }

        is TypeScheme -> TypeScheme(t.boundVariables, applySubstitutions(t.body, substitutions - t.boundVariables))

        is FunctionType -> FunctionType(applySubstitutions(t.lhs, substitutions), applySubstitutions(t.rhs, substitutions))
        is SumType -> SumType(t.types.map { applySubstitutions(it, substitutions) })

        is UnitType -> t
        is IntType -> t
        is FloatType -> t
        is StringType -> t
        is CharType -> t

        is OpenEntityType -> {
            val components = t.components.map { applySubstitutions(it, substitutions) }.toSet()
            when(val bla = applySubstitutions(t.row, substitutions)) {
                is OpenEntityType -> OpenEntityType(bla.components + components, bla.row) //bla.labels + labels is important because kotlin prioritizes rhs when creating the new map
                is ClosedEntityType -> ClosedEntityType(bla.components + components)
                is TypeVariable -> OpenEntityType(components, bla)
                is OpenRecordType -> ClosedEntityType(components + bla)
                is ClosedRecordType -> ClosedEntityType(components + bla)
                else -> throw Exception()
            }
        }

        is ClosedEntityType -> {
            val components = t.components.map { applySubstitutions(it, substitutions) }.toSet()
            ClosedEntityType(components)
        }

        is OpenRecordType -> {
            val labels = t.labels.mapValues { applySubstitutions(it.value, substitutions) }
            when(val bla = applySubstitutions(t.row, substitutions)) {
                is OpenRecordType -> OpenRecordType(bla.labels + labels, bla.row) //bla.labels + labels is important because kotlin prioritizes rhs when creating the new map
                is ClosedRecordType -> ClosedRecordType(bla.name, bla.labels + labels)
                is TypeVariable -> OpenRecordType(labels, bla)
                else -> throw Exception()
            }
        }

        is ClosedRecordType -> {
            val labels = t.labels.mapValues { applySubstitutions(it.value, substitutions) }
            ClosedRecordType(t.name, labels)
        }
    }
}

fun instantiate(t: Type, substitutions: Map<TypeVariable, Type> = mapOf()): Type {
    return when(t) {
        is TypeScheme -> applySubstitutions(t.body, substitutions + t.boundVariables.map { it to freshTypeVariable(it.kind) })
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
        else if(lhs is TypeVariable && !rhs.freeTypeVariables.contains(lhs)) {
            if (lhs.kind == rhs.kind) unify(tail, substitutions + Pair(lhs, rhs))
            else if(lhs.kind == UndeterminedKind) unify(tail, substitutions + Pair(lhs, rhs))
            else if(rhs is TypeVariable && rhs.kind == UndeterminedKind) unify(tail, substitutions + Pair(rhs, lhs))
            else if (lhs.kind == EntityOrComponentKind && (rhs.kind == EntityKind || rhs.kind == ComponentKind)) unify(tail, substitutions + Pair(lhs, rhs))
            else if (rhs is TypeVariable && rhs.kind == EntityOrComponentKind && (lhs.kind == EntityKind || lhs.kind == ComponentKind)) unify(tail, substitutions + Pair(rhs, lhs))
            else throw Exception()
        } else if(rhs is TypeVariable && !lhs.freeTypeVariables.contains(rhs)) {
            if (lhs.kind == rhs.kind) unify(tail, substitutions + Pair(rhs, lhs))
            else if(rhs.kind == UndeterminedKind) unify(tail, substitutions + Pair(rhs, lhs))
            else if(lhs is TypeVariable && lhs.kind == UndeterminedKind) unify(tail, substitutions + Pair(lhs, rhs))
            else if (rhs.kind == EntityOrComponentKind && (lhs.kind == EntityKind || lhs.kind == ComponentKind)) unify(tail, substitutions + Pair(rhs, lhs))
            else if (lhs is TypeVariable && lhs.kind == EntityOrComponentKind && (rhs.kind == EntityKind || rhs.kind == ComponentKind)) unify(tail, substitutions + Pair(lhs, rhs))
            else throw Exception()
        } else if(lhs is FunctionType && rhs is FunctionType) unify(listOf(Constraint(lhs.lhs, rhs.lhs), Constraint(lhs.rhs, rhs.rhs)) + tail, substitutions)
        else if(lhs is SumType && rhs is SumType) {
            if(lhs.types.size != rhs.types.size) throw Exception()
            unify(lhs.types.zip(rhs.types) { l, r -> Constraint(l, r) } + tail, substitutions)
        } else if(lhs is OpenRecordType && rhs is OpenRecordType) unify(lhs, rhs, tail, substitutions)
        else if(lhs is ClosedRecordType && rhs is OpenRecordType) unify(rhs, lhs, tail, substitutions)
        else if(lhs is OpenRecordType && rhs is ClosedRecordType) unify(lhs, rhs, tail, substitutions)
        else if(lhs is ClosedRecordType && rhs is ClosedRecordType) unify(lhs, rhs, tail, substitutions)
        else if(lhs is OpenEntityType && rhs is OpenEntityType) unify(lhs, rhs, tail, substitutions)
        else if(lhs is ClosedEntityType && rhs is OpenEntityType) unify(rhs, lhs, tail, substitutions)
        else if(lhs is OpenEntityType && rhs is ClosedEntityType) unify(lhs, rhs, tail, substitutions)
        else if(lhs is ClosedEntityType && rhs is ClosedEntityType) unify(lhs, rhs, tail, substitutions)
        else throw Exception()
    }
}

fun unify(lhs: OpenEntityType, rhs: OpenEntityType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    val constraints = mutableListOf<Constraint>()

    val mlhs = lhs.components - rhs.components
    constraints.add(Constraint(rhs.row, OpenEntityType(mlhs, freshTypeVariable(EntityOrComponentKind))))

    val mrhs = (rhs.components - lhs.components)
    constraints.add(Constraint(lhs.row, OpenEntityType(mrhs, freshTypeVariable(EntityOrComponentKind))))

    return unify(constraints + tail, substitutions)
}

fun unify(lhs: OpenEntityType, rhs: ClosedEntityType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    if((lhs.components - rhs.components).any()) throw Exception()

    val constraints = mutableListOf<Constraint>()

    val mrhs = rhs.components - lhs.components
    constraints.add(Constraint(lhs.row, ClosedEntityType(mrhs)))

    return unify(constraints + tail, substitutions)
}

fun unify(lhs: ClosedEntityType, rhs: ClosedEntityType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    if(lhs.components != rhs.components) throw Exception()
    return unify(tail, substitutions)
}

fun unify(lhs: OpenRecordType, rhs: OpenRecordType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    val constraints = mutableListOf<Constraint>()
    for(label in lhs.labels.keys.intersect(rhs.labels.keys)) constraints.add(Constraint(lhs.labels[label]!!, rhs.labels[label]!!))

    val mlhs = (lhs.labels.keys - rhs.labels.keys).associateWith { lhs.labels[it]!! }
    constraints.add(Constraint(rhs.row, OpenRecordType(mlhs, freshTypeVariable(EntityKind))))

    val mrhs = (rhs.labels.keys - lhs.labels.keys).associateWith { rhs.labels[it]!! }
    constraints.add(Constraint(lhs.row, OpenRecordType(mrhs, freshTypeVariable(EntityKind))))

    return unify(constraints + tail, substitutions)
}

fun unify(lhs: OpenRecordType, rhs: ClosedRecordType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    if((lhs.labels.keys - rhs.labels.keys).any()) throw Exception()

    val constraints = mutableListOf<Constraint>()
    for(label in lhs.labels.keys.intersect(rhs.labels.keys)) constraints.add(Constraint(lhs.labels[label]!!, rhs.labels[label]!!))

    val mrhs = (rhs.labels.keys - lhs.labels.keys).associateWith { rhs.labels[it]!! }
    constraints.add(Constraint(lhs.row, ClosedRecordType(rhs.name, mrhs)))

    return unify(constraints + tail, substitutions)
}

fun unify(lhs: ClosedRecordType, rhs: ClosedRecordType, tail: List<Constraint>, substitutions: Map<TypeVariable, Type>): Map<TypeVariable, Type> {
    if(lhs.labels.keys != rhs.labels.keys) throw Exception()

    val constraints = mutableListOf<Constraint>()
    for(label in lhs.labels.keys) constraints.add(Constraint(lhs.labels[label]!!, rhs.labels[label]!!))

    return unify(constraints + tail, substitutions)
}
