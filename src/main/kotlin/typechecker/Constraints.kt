package typechecker

import parser.*
import tokenizer.NameToken

class Environment private constructor(private val names: Map<String, Type>, private val types: Map<String, Type>) {
    constructor(): this(mapOf(), mapOf("Int" to IntType, "Float" to FloatType, "String" to StringType, "Char" to CharType))
    val freeTypeVariables: Set<TypeVariable>
        get() = names.values.fold(setOf()) { acc, type -> acc + type.freeTypeVariables }

    operator fun plus(rhs: Pair<NameToken, Type>) = Environment(names + Pair(rhs.first.value, rhs.second), types)
    operator fun plus(rhs: Environment) = Environment(names + rhs.names, types)
    operator fun plus(rhs: List<Pair<NameToken, Type>>) = Environment(names + rhs.map{ Pair(it.first.value, it.second) }, types)
    operator fun get(key: NameToken) = names[key.value] ?: throw Exception("Name not bound to value: $key")

    fun registerType(name: NameToken, type: Type) = Environment(names, types + Pair(name.value, type))
    fun lookupType(name: NameToken) = types[name.value] ?: throw Exception("Name not bound to type: $name")

    fun applySubstitutions(substitutions: Map<TypeVariable, Type>) =
        Environment(names.mapValues { applySubstitutions(it.value, substitutions) }, types)
}

data class Constraint(val lhs: Type, val rhs: Type)

var nextTypeIndex = 0
fun freshTypeVariable(kind: Kind) = TypeVariable(nextTypeIndex++, kind)

fun addComponentToEnvironment(environment: Environment, component: ComponentAst): Environment {
    if (component.valueDefs.isEmpty()) throw Exception()

    val labels = component.valueDefs.associate {
        it.name.value to environment.lookupType(it.type)
    }

    val componentType = ClosedRecordType(component.name.value, labels)

    val type =
        if(component.valueDefs.size == 1) FunctionType(environment.lookupType(component.valueDefs.first().type), ComponentType(component.name.value))
        else {
            val sum =
                component.valueDefs
                    .dropLast(1)
                    .foldRight(environment.lookupType(component.valueDefs.last().type)) { t, acc -> SumType(environment.lookupType(t.type), acc) }

            FunctionType(sum, componentType)
        }

    component._type = componentType
    printWithTypes(component)

    return environment.registerType(component.name, componentType) + Pair(component.name, type)
}

fun addFnClusterToEnvironment(environment: Environment, fns: Set<FnAst>): Environment {
    var env = environment
    val parameterTypesLookup = mutableMapOf<String, List<Pair<NameToken, TypeVariable>>>()
    val returnTypeLookup = mutableMapOf<String, TypeVariable>()
    val typeLookup = mutableMapOf<String, FunctionType>()
    val constraints = mutableListOf<Constraint>()

    for(fn in fns) {
        val parameterTypes = List(fn.parameters.size) { freshTypeVariable(UndeterminedKind) }
        val returnType = freshTypeVariable(UndeterminedKind)
        val type =
            when (parameterTypes.size) {
                0 -> FunctionType(UnitType, returnType)
                1 -> FunctionType(parameterTypes.first(), returnType)
                else -> {
                    val sum =
                        parameterTypes
                            .dropLast(1)
                            .foldRight(parameterTypes.last() as Type) { t, acc -> SumType(t, acc) }

                    FunctionType(sum, returnType)
                }
            }

        parameterTypesLookup[fn.name.value] = fn.parameters.zip(parameterTypes)
        returnTypeLookup[fn.name.value] = returnType
        typeLookup[fn.name.value] = type
        env += (fn.name to type)
    }

    for(fn in fns) {
        val parameterTypes = parameterTypesLookup[fn.name.value]!!
        val returnType = returnTypeLookup[fn.name.value]!!

        var envp = env + parameterTypes

        for (body in fn.body) {
            val (c, e) = generateFnBodyConstraints(envp, returnType, body)
            envp += e
            constraints.addAll(c)
        }
    }

    val substitutions = unify(constraints)
    env = env.applySubstitutions(substitutions)

    for(fn in fns) {
        val type = typeLookup[fn.name.value]!!

        val newType = applySubstitutions(type, substitutions)
        val variablesToGeneralize = newType.freeTypeVariables - env.freeTypeVariables
        val typeScheme = variablesToGeneralize.fold(newType) { acc, t -> TypeScheme(t, acc) }
        env += Pair(fn.name, typeScheme)
        fn._type = typeScheme

        for(body in fn.body) updateTypes(body, substitutions)
        printWithTypes(fn)
    }

    return env
}

fun addFnToEnvironment(environment: Environment, fn: FnAst): Environment {
    val parameterTypes = List(fn.parameters.size) { freshTypeVariable(UndeterminedKind) }

    val constraints = mutableListOf<Constraint>()
    val returnType = freshTypeVariable(UndeterminedKind)

    val type =
        when(parameterTypes.size) {
            0 -> FunctionType(UnitType, returnType)
            1 -> FunctionType(parameterTypes.first(), returnType)
            else -> {
                val sum =
                    parameterTypes
                        .dropLast(1)
                        .foldRight(parameterTypes.last() as Type) { t, acc -> SumType(t, acc) }

                FunctionType(sum, returnType)
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
    printWithTypes(fn)

    return newEnvironment + Pair(fn.name, typeScheme)
}

data class FnBodyConstraints(val constraints: List<Constraint>, val envAdds: List<Pair<NameToken, Type>>)

fun generateFnBodyConstraints(environment: Environment, retType: Type, fnBody: FnBodyAst) =
    when(fnBody) {
        is LetAst -> generateLetConstraints(environment, fnBody)
        is ReturnAst -> generateReturnConstraints(environment, retType, fnBody)
    }

fun generateLetConstraints(environment: Environment, let: LetAst): FnBodyConstraints {
    val (c, t) = generateExpressionConstraints(environment, let.expression)
    let._type = t
    return FnBodyConstraints(c, listOf(let.name to t))
}

fun generateReturnConstraints(environment: Environment, retType: Type, ret: ReturnAst): FnBodyConstraints {
    val (c, t) = generateExpressionConstraints(environment, ret.expression)
    ret._type = t
    return FnBodyConstraints(c + Constraint(retType, t), listOf())
}

fun generateExpressionConstraints(environment: Environment, expression: Expression) =
    when(expression) {
        is FnCallAst -> generateFnCallConstraints(environment, expression)

        is ComponentSelectionAst -> generateComponentSelectionConstraints(environment, expression)

        is EntityDefinitionAst -> generateEntityDefinitionConstraints(environment, expression)
        is EntitySelectionAst -> generateEntitySelectionConstraints(environment, expression)
        is EntityRestrictionAst -> generateEntityRestritionConstraints(environment, expression)

        is NameAst -> generateNameConstraints(environment, expression)
        is IntConstantAst -> ExpressionConstraints(listOf(), IntType)
        is FloatConstantAst -> ExpressionConstraints(listOf(), FloatType)
        is StringConstantAst -> ExpressionConstraints(listOf(), StringType)
        is CharConstantAst -> ExpressionConstraints(listOf(), CharType)
    }

data class ExpressionConstraints(val constraints: List<Constraint>, val type: Type)

fun generateNameConstraints(environment: Environment, name: NameAst): ExpressionConstraints {
    val t = instantiate(environment[name.name])
    name._type = t
    return ExpressionConstraints(listOf(), t)
}

fun generateFnCallConstraints(environment: Environment, fnCall: FnCallAst): ExpressionConstraints {
    val retType = freshTypeVariable(UndeterminedKind)
    val (fc, ft) = generateExpressionConstraints(environment, fnCall.fn)
    val args = fnCall.arguments.map { generateExpressionConstraints(environment, it) }
    val argTypes =
        when(args.size) {
            0 -> UnitType
            1 -> args.first().type
            else -> args.dropLast(1).foldRight(args.first().type) { stuff, acc -> SumType(stuff.type, acc) }
        }
    val constraints = args.fold(fc) { acc, stuff -> acc + stuff.constraints } + Constraint(ft, FunctionType(argTypes, retType))
    fnCall._type = retType
    return ExpressionConstraints(constraints, retType)
}

fun generateEntityDefinitionConstraints(environment: Environment, definition: EntityDefinitionAst): ExpressionConstraints =
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
        val row = freshTypeVariable(EntityOrComponentKind)
        val entityType = OpenEntityType(components, row)

        definition._type = entityType
        constraints.addAll(c)
        constraints.add(Constraint(row, t))
        ExpressionConstraints(constraints, entityType)
    }

fun generateEntitySelectionConstraints(environment: Environment, selection: EntitySelectionAst): ExpressionConstraints {
    val (recordConstraints, recordType) = generateExpressionConstraints(environment, selection.entity)
    val outputType = environment.lookupType(selection.label)
    val inputType = OpenEntityType(setOf(environment.lookupType(selection.label)), freshTypeVariable(EntityOrComponentKind))
    selection._type = outputType
    return ExpressionConstraints(recordConstraints + Constraint(recordType, inputType), outputType)
}

fun generateEntityRestritionConstraints(environment: Environment, restriction: EntityRestrictionAst): ExpressionConstraints {
    val f = freshTypeVariable(EntityOrComponentKind)
    val inputType = OpenEntityType(setOf(environment.lookupType(restriction.label)), f)
    val (c, t) = generateExpressionConstraints(environment, restriction.entity)
    restriction._type = f
    return ExpressionConstraints(c + Constraint(inputType, t), f)
}

fun generateComponentSelectionConstraints(environment: Environment, selection: ComponentSelectionAst): ExpressionConstraints {
    val (c, t) = generateExpressionConstraints(environment, selection.component)
    val outputType = freshTypeVariable(UndeterminedKind)
    val inputType = OpenRecordType(mapOf(selection.label.value to outputType), freshTypeVariable(ComponentKind))
    selection._type = outputType
    return ExpressionConstraints(c + Constraint(t, inputType), outputType)
}