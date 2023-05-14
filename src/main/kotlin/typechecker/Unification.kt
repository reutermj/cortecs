package typechecker

fun applySubstitutions(t: Type, substitutions: Map<Type, Type>): Type {
    return if(substitutions.isEmpty()) t
    else when(t) {
        is TypeVariable -> applySubstitutions(substitutions[t] ?: return t, substitutions)
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

            when(val type = applySubstitutions(t.row, substitutions)) {
                is OpenEntityType -> OpenEntityType(type.components + components, type.row) //order of type.components + components is important because kotlin prioritizes rhs when creating the new map
                is ClosedEntityType -> ClosedEntityType(type.components + components)
                is TypeVariable -> OpenEntityType(components, type)
                is OpenComponentType -> ClosedEntityType(components + type)
                is ClosedComponentType -> ClosedEntityType(components + type)
                is EntityOrComponentType -> ClosedEntityType(components + type.component)
                else -> throw Exception()
            }
        }

        is ClosedEntityType -> {
            val components = t.components.map { applySubstitutions(it, substitutions) }.toSet()
            ClosedEntityType(components)
        }

        is OpenComponentType -> {
            val labels = t.labels.mapValues { applySubstitutions(it.value, substitutions) }
            when(val type = applySubstitutions(t.row, substitutions)) {
                is OpenComponentType -> OpenComponentType(type.labels + labels, type.row) //type.labels + labels is important because kotlin prioritizes rhs when creating the new map
                is ClosedComponentType -> ClosedComponentType(type.name, type.labels + labels)
                is TypeVariable -> OpenComponentType(labels, type)
                else -> throw Exception()
            }
        }

        is ClosedComponentType -> {
            val labels = t.labels.mapValues { applySubstitutions(it.value, substitutions) }
            ClosedComponentType(t.name, labels)
        }

        is EntityOrComponentType -> applySubstitutions(substitutions[t] ?: return EntityOrComponentType(applySubstitutions(t.component, substitutions)), substitutions)
    }
}

fun instantiate(t: Type, substitutions: Map<Type, Type> = mapOf()): Type {
    return when(t) {
        is TypeScheme -> applySubstitutions(t.body, substitutions + t.boundVariables.map { it to freshTypeVariable(it.kind) })
        else -> applySubstitutions(t, substitutions)
    }
}

fun unify(constraints: MutableList<Constraint>): Map<Type, Type> {
    val substitutions = mutableMapOf<Type, Type>()
    while(constraints.any()) {
        val constraint = constraints.removeLast()
        val lhs = applySubstitutions(constraint.lhs, substitutions)
        val rhs = applySubstitutions(constraint.rhs, substitutions)
        when {
            lhs == rhs -> continue
            lhs is TypeVariable && !rhs.freeTypeVariables.contains(lhs) -> unify(lhs, rhs, substitutions)
            rhs is TypeVariable && !lhs.freeTypeVariables.contains(rhs) -> unify(rhs, lhs, substitutions)
            lhs is FunctionType && rhs is FunctionType -> unify(lhs, rhs, constraints)
            lhs is SumType && rhs is SumType -> unify(lhs, rhs, constraints)
            lhs is OpenComponentType && rhs is OpenComponentType -> unify(lhs, rhs, constraints)
            lhs is OpenComponentType && rhs is ClosedComponentType -> unify(lhs, rhs, constraints)
            lhs is ClosedComponentType && rhs is OpenComponentType -> unify(rhs, lhs, constraints)
            lhs is ClosedComponentType && rhs is ClosedComponentType -> unify(rhs, lhs, constraints)
            lhs is OpenEntityType && rhs is OpenEntityType -> unify(lhs, rhs, constraints)
            lhs is OpenEntityType && rhs is ClosedEntityType -> unify(lhs, rhs, constraints)
            lhs is ClosedEntityType && rhs is OpenEntityType -> unify(rhs, lhs, constraints)
            lhs is ClosedEntityType && rhs is ClosedEntityType -> unify(lhs, rhs)
            lhs is EntityOrComponentType && rhs is EntityOrComponentType -> unify(rhs, lhs, constraints)
            lhs is EntityOrComponentType && rhs is ClosedComponentType -> unify(lhs, rhs, constraints, substitutions)
            lhs is ClosedComponentType && rhs is EntityOrComponentType -> unify(rhs, lhs, constraints, substitutions)
            lhs is EntityOrComponentType && rhs is ClosedEntityType -> unify(lhs, rhs, constraints, substitutions)
            lhs is ClosedEntityType && rhs is EntityOrComponentType -> unify(rhs, lhs, constraints, substitutions)
            else -> throw Exception()
        }
    }

    return substitutions
}

fun unify(typeVar: TypeVariable, type: Type, substitutions: MutableMap<Type, Type>) {
    when {
        typeVar.kind == type.kind -> substitutions[typeVar] = type
        typeVar.kind == UndeterminedKind -> substitutions[typeVar] = type
        type is TypeVariable && type.kind == UndeterminedKind -> substitutions[type] = typeVar
        typeVar.kind == EntityOrComponentKind && (type.kind == EntityKind || type.kind == ComponentKind) -> substitutions[typeVar] = type
        type is TypeVariable && type.kind == EntityOrComponentKind && (typeVar.kind == EntityKind || typeVar.kind == ComponentKind) -> substitutions[type] = typeVar
        else -> throw Exception()
    }
}

fun unify(lhs: FunctionType, rhs: FunctionType, constraints: MutableList<Constraint>) {
    constraints.add(Constraint(lhs.lhs, rhs.lhs))
    constraints.add(Constraint(lhs.rhs, rhs.rhs))
}

fun unify(lhs: SumType, rhs: SumType, constraints: MutableList<Constraint>) {
    if(lhs.types.size != rhs.types.size) throw Exception()
    for(i in lhs.types.indices) constraints.add(Constraint(lhs.types[i], rhs.types[i]))
}
fun unify(lhs: OpenComponentType, rhs: OpenComponentType, constraints: MutableList<Constraint>) {
    for((k, v) in lhs.labels) constraints.add(Constraint(v, rhs.labels[k] ?: continue))
    constraints.add(Constraint(rhs.row, OpenComponentType(lhs.labels - rhs.labels.keys, freshTypeVariable(EntityKind))))
    constraints.add(Constraint(lhs.row, OpenComponentType(rhs.labels - lhs.labels.keys, freshTypeVariable(EntityKind))))
}

fun unify(lhs: OpenComponentType, rhs: ClosedComponentType, constraints: MutableList<Constraint>) {
    if((lhs.labels.keys - rhs.labels.keys).any()) throw Exception()
    for((k, v) in lhs.labels) constraints.add(Constraint(v, rhs.labels[k] ?: continue))
    constraints.add(Constraint(lhs.row, ClosedComponentType(rhs.name, rhs.labels - lhs.labels.keys)))
}

fun unify(lhs: ClosedComponentType, rhs: ClosedComponentType, constraints: MutableList<Constraint>) {
    if(lhs.labels.keys != rhs.labels.keys) throw Exception()
    for((k, v) in lhs.labels) constraints.add(Constraint(v, rhs.labels[k] ?: continue))
}

fun unify(lhs: OpenEntityType, rhs: OpenEntityType, constraints: MutableList<Constraint>) {
    constraints.add(Constraint(rhs.row, OpenEntityType(lhs.components - rhs.components, freshTypeVariable(EntityOrComponentKind))))
    constraints.add(Constraint(lhs.row, OpenEntityType(rhs.components - lhs.components, freshTypeVariable(EntityOrComponentKind))))
}

fun unify(lhs: OpenEntityType, rhs: ClosedEntityType, constraints: MutableList<Constraint>) {
    if(!rhs.components.containsAll(lhs.components)) throw Exception()
    val components = rhs.components - lhs.components
    if(components.size == 1) constraints.add(Constraint(lhs.row, EntityOrComponentType(components.first())))
    else constraints.add(Constraint(lhs.row, ClosedEntityType(components)))
}

fun unify(lhs: ClosedEntityType, rhs: ClosedEntityType) {
    if(lhs.components != rhs.components) throw Exception()
}

fun unify(lhs: EntityOrComponentType, rhs: EntityOrComponentType, constraints: MutableList<Constraint>) {
    constraints.add(Constraint(lhs.component, rhs.component))
}

fun unify(lhs: EntityOrComponentType, rhs: ClosedComponentType, constraints: MutableList<Constraint>, substitutions: MutableMap<Type, Type>) {
    constraints.add(Constraint(lhs.component, rhs))
    substitutions[lhs] = rhs
}

fun unify(lhs: EntityOrComponentType, rhs: ClosedEntityType, constraints: MutableList<Constraint>, substitutions: MutableMap<Type, Type>) {
    if(rhs.components.size != 1) throw Exception()
    constraints.add(Constraint(lhs.component, rhs.components.first()))
    substitutions[lhs] = rhs
}