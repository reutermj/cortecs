package typechecker

import parser.*

class Environment private constructor(private val names: Map<String, Type>, private val types: Map<String, Type>) {
    constructor(): this(mapOf(), mapOf("Int" to IntType, "Float" to FloatType, "String" to StringType, "Char" to CharType))
    val freeTypeVariables: Set<TypeVariable>
        get() = names.values.fold(setOf()) { acc, type -> acc + type.freeTypeVariables }

    operator fun plus(rhs: Pair<Name, Type>) = Environment(names + Pair(rhs.first.name.value, rhs.second), types)
    operator fun plus(rhs: Environment) = Environment(names + rhs.names, types)
    operator fun plus(rhs: List<Pair<Name, Type>>) = Environment(names + rhs.map{ Pair(it.first.name.value, it.second) }, types)
    operator fun get(key: Name) = names[key.name.value] ?: throw Exception("Name not bound to value: $key")

    fun registerType(name: Name, type: Type) = Environment(names, types + Pair(name.name.value, type))
    fun lookupType(name: Name) = types[name.name.value] ?: throw Exception("Name not bound to type: $name")
    fun containsType(name: Name) = types.containsKey(name.name.value)

    fun applySubstitutions(substitutions: Map<TypeVariable, Type>) =
        Environment(names.mapValues { applySubstitutions(it.value, substitutions) }, types)
}

data class Constraint(val lhs: Type, val rhs: Type)

var nextTypeIndex = 0
fun freshTypeVariable(kind: Kind) = TypeVariable(nextTypeIndex++, kind)

data class ProgramConstraints(val constraints: List<Constraint>, val environment: Environment)

fun generateProgramConstraints(environment: Environment, program: Program) =
    when(program) {
        is Component -> generateComponentConstraints(environment, program)
        is Fn -> generateFnConstraints(environment, program)
    }

fun generateComponentConstraints(environment: Environment, component: Component): ProgramConstraints {
    if (component.valueDefs.isEmpty()) throw Exception()

    val labels = component.valueDefs.associate {
        it.name.name.value to environment.lookupType(it.type)
    }

    val componentType = ClosedRecordType(component.name.name.value, labels)

    val type =
        if(component.valueDefs.size == 1) Arrow(environment.lookupType(component.valueDefs.first().type), ComponentType(component.name.name.value))
        else {
            val sum =
                component.valueDefs
                    .dropLast(1)
                    .foldRight(environment.lookupType(component.valueDefs.last().type)) { t, acc -> Sum(environment.lookupType(t.type), acc) }

            Arrow(sum, componentType)
        }

    return ProgramConstraints(listOf(), environment.registerType(component.name, componentType) + Pair(component.name, type))
}

fun generateFnType(fn: Fn): Type {
    val parameterTypes = List(fn.parameters.size) { freshTypeVariable(Undetermined) }
    val returnType = freshTypeVariable(Undetermined)

    val type =
        when(parameterTypes.size) {
            0 -> Arrow(UnitType, returnType)
            1 -> Arrow(parameterTypes.first(), returnType)
            else -> {
                val sum =
                    parameterTypes
                        .dropLast(1)
                        .foldRight(parameterTypes.last() as Type) { t, acc -> Sum(t, acc) }

                Arrow(sum, returnType)
            }
        }

    return type
}

fun generateFnConstraints(environment: Environment, fn: Fn): ProgramConstraints {
    val parameterTypes = List(fn.parameters.size) { freshTypeVariable(Undetermined) }

    val constraints = mutableListOf<Constraint>()
    val returnType = freshTypeVariable(Undetermined)

    val type =
        when(parameterTypes.size) {
            0 -> Arrow(UnitType, returnType)
            1 -> Arrow(parameterTypes.first(), returnType)
            else -> {
                val sum =
                    parameterTypes
                        .dropLast(1)
                        .foldRight(parameterTypes.last() as Type) { t, acc -> Sum(t, acc) }

                Arrow(sum, returnType)
            }
        }

    var env = environment + fn.parameters.zip(parameterTypes) + (fn.name to type)

    for(body in fn.body) {
        val (c, e) = generateFnBodyConstraints(env, returnType, body)
        env += e
        constraints.addAll(c)
    }

    val substitutions = unify(constraints)
    val newType = applySubstitutions(type, substitutions)
    val newEnvironment = environment.applySubstitutions(substitutions)
    val variablesToGeneralize = newType.freeTypeVariables - newEnvironment.freeTypeVariables
    val typeScheme = variablesToGeneralize.fold(newType) { acc, t -> TypeScheme(t, acc) }
    fn._type = typeScheme

    for(body in fn.body) updateTypes(body, substitutions)

    return ProgramConstraints(constraints, newEnvironment + Pair(fn.name, typeScheme))
}

data class FnBodyConstraints(val constraints: List<Constraint>, val envAdds: List<Pair<Name, Type>>)

fun generateFnBodyConstraints(environment: Environment, retType: Type, fnBody: FnBody) =
    when(fnBody) {
        is Let -> generateLetConstraints(environment, fnBody)
        is Return -> generateReturnConstraints(environment, retType, fnBody)
    }

fun generateLetConstraints(environment: Environment, let: Let): FnBodyConstraints {
    val (c, t) = generateExpressionConstraints(environment, let.expression)
    let._type = t
    return FnBodyConstraints(c, listOf(let.name to t))
}

fun generateReturnConstraints(environment: Environment, retType: Type, ret: Return): FnBodyConstraints {
    val (c, t) = generateExpressionConstraints(environment, ret.expression)
    ret._type = t
    return FnBodyConstraints(c + Constraint(retType, t), listOf())
}

fun generateExpressionConstraints(environment: Environment, expression: Expression) =
    when(expression) {
        is FnCall -> generateFnCallConstraints(environment, expression)

        is ComponentSelection -> generateComponentSelectionConstraints(environment, expression)

        is EntityDefinition -> generateEntityDefinitionConstraints(environment, expression)
        is EntitySelection -> generateEntitySelectionConstraints(environment, expression)
        is EntityRestriction -> generateEntityRestritionConstraints(environment, expression)

        is Name -> generateNameConstraints(environment, expression)
        is IntConstant -> ExpressionConstraints(listOf(), IntType)
        is FloatConstant -> ExpressionConstraints(listOf(), FloatType)
        is StringConstant -> ExpressionConstraints(listOf(), StringType)
        is CharConstant -> ExpressionConstraints(listOf(), CharType)
        else -> throw Exception()
    }

data class ExpressionConstraints(val constraints: List<Constraint>, val type: Type)

fun generateNameConstraints(environment: Environment, name: Name): ExpressionConstraints {
    val t = instantiate(environment[name])
    name._type = t
    return ExpressionConstraints(listOf(), t)
}

fun generateFnCallConstraints(environment: Environment, fnCall: FnCall): ExpressionConstraints {
    val retType = freshTypeVariable(Undetermined)
    val (fc, ft) = generateExpressionConstraints(environment, fnCall.fn)
    val args = fnCall.arguments.map { generateExpressionConstraints(environment, it) }
    val argTypes =
        when(args.size) {
            0 -> UnitType
            1 -> args.first().type
            else -> args.dropLast(1).foldRight(args.first().type) { stuff, acc -> Sum(stuff.type, acc) }
        }
    val constraints = args.fold(fc) { acc, stuff -> acc + stuff.constraints } + Constraint(ft, Arrow(argTypes, retType))
    fnCall._type = retType
    return ExpressionConstraints(constraints, retType)
}

fun generateEntityDefinitionConstraints(environment: Environment, definition: EntityDefinition): ExpressionConstraints =
    if(definition.expressions.isEmpty()) {
        val entityType = ClosedEntityType(setOf())
        definition._type = entityType
        ExpressionConstraints(listOf(), entityType)
    } else {
        val constraints = mutableListOf<Constraint>()
        val components = mutableSetOf<Type>()

        for(e in definition.expressions.dropLast(1)) {
            val (c, t) = generateExpressionConstraints(environment, e)
            val componentType = OpenRecordType(mapOf(), freshTypeVariable(ComponentKind))
            constraints.addAll(c)
            constraints.add(Constraint(componentType, t))
            components.add(componentType)
        }
        val l = definition.expressions.last()

        val (c, t) = generateExpressionConstraints(environment, l)
        val row = freshTypeVariable(RowOrComponent)
        val entityType = OpenEntityType(components, row)
        definition._type = entityType
        constraints.addAll(c)
        constraints.add(Constraint(row, t))
        ExpressionConstraints(constraints, entityType)
    }

fun generateEntitySelectionConstraints(environment: Environment, selection: EntitySelection): ExpressionConstraints {
    val (recordConstraints, recordType) = generateExpressionConstraints(environment, selection.entity)
    val outputType = environment.lookupType(selection.label)
    val inputType = OpenEntityType(setOf(environment.lookupType(selection.label)), freshTypeVariable(RowOrComponent))
    selection._type = outputType
    return ExpressionConstraints(recordConstraints + Constraint(recordType, inputType), outputType)
}

fun generateEntityRestritionConstraints(environment: Environment, restriction: EntityRestriction): ExpressionConstraints {
    val f = freshTypeVariable(RowOrComponent)
    val inputType = OpenEntityType(setOf(environment.lookupType(restriction.label)), f)
    val (c, t) = generateExpressionConstraints(environment, restriction.entity)
    restriction._type = f
    return ExpressionConstraints(c + Constraint(inputType, t), f)
}

fun generateComponentSelectionConstraints(environment: Environment, selection: ComponentSelection): ExpressionConstraints {
    val (c, t) = generateExpressionConstraints(environment, selection.component)
    val outputType = freshTypeVariable(Undetermined)
    val inputType = OpenRecordType(mapOf(selection.label.name.value to outputType), freshTypeVariable(ComponentKind))
    selection._type = outputType
    return ExpressionConstraints(c + Constraint(t, inputType), outputType)
}