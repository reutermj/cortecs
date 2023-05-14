package typechecker

import parser.*
import tokenizer.NameToken

class Environment private constructor(private val names: MutableList<MutableMap<String, Type>>, private val types: MutableMap<String, Type>) {
    constructor(): this(mutableListOf(mutableMapOf()), mutableMapOf("Int" to IntType, "Float" to FloatType, "String" to StringType, "Char" to CharType))
    fun registerType(name: NameToken, type: Type) { types[name.value] = type }
    fun lookupType(name: NameToken) = types[name.value] ?: throw Exception("Name not bound to type: $name")
    fun push() { names.add(mutableMapOf()) }
    fun pop() = names.removeLast()
    operator fun set(key: NameToken, value: Type) { names.last()[key.value] = value }
    operator fun get(key: NameToken): Type {
        for(name in names.asReversed()) return name[key.value] ?: continue
        throw Exception("Name not found: ${key.value}")
    }
}

data class Constraint(val lhs: Type, val rhs: Type)

var nextTypeIndex = 0
fun freshTypeVariable(kind: Kind) = TypeVariable(nextTypeIndex++, kind)

fun addComponentToEnvironment(environment: Environment, component: ComponentAst) {
    if (component.valueDefs.isEmpty()) throw Exception()

    val labels = component.valueDefs.associate {
        it.name.value to environment.lookupType(it.typeName)
    }

    val componentType = ClosedComponentType(component.name.value, labels)

    val type =
        if(component.valueDefs.size == 1) FunctionType(environment.lookupType(component.valueDefs.first().typeName), componentType)
        else FunctionType(SumType(component.valueDefs.map {environment.lookupType(it.typeName) }), componentType)

    component._type = componentType
    component._fnType = type

    environment.registerType(component.name, componentType)
    environment[component.name] = type
}

fun addFnClusterToEnvironment(environment: Environment, fns: Set<FnAst>) {
    val constraints = mutableListOf<Constraint>()

    environment.push()
    for(fn in fns) {
        val parameterTypes = List(fn.parameters.size) { freshTypeVariable(UndeterminedKind) }
        val returnType = freshTypeVariable(UndeterminedKind)
        val type =
            when (parameterTypes.size) {
                0 -> FunctionType(UnitType, returnType)
                1 -> FunctionType(parameterTypes.first(), returnType)
                else -> FunctionType(SumType(parameterTypes), returnType)
            }
        environment[fn.name] = type
    }

    for(fn in fns) {
        environment.push()
        val (parameterTypes, returnType) = environment[fn.name] as FunctionType
        when(parameterTypes) {
            is UnitType -> {}
            is SumType -> for(i in fn.parameters.indices) environment[fn.parameters[i]] = parameterTypes.types[i]
            else -> environment[fn.parameters.first()] = parameterTypes
        }
        for (body in fn.body) generateFnBodyConstraints(environment, constraints, returnType, body)
        environment.pop()
    }
    val fnTypes = environment.pop()

    val substitutions = unify(constraints)

    for(fn in fns) {
        val type = fnTypes[fn.name.value]!!
        val newType = applySubstitutions(type, substitutions)
        val variablesToGeneralize = newType.freeTypeVariables
        val fnType =
            if(variablesToGeneralize.any()) TypeScheme(variablesToGeneralize, newType)
            else newType
        environment[fn.name] = fnType
        fn._type = fnType

        for(body in fn.body) updateTypes(body, substitutions)
        printWithTypes(fn)
    }
}

fun addFnToEnvironment(environment: Environment, fn: FnAst) {
    val parameterTypes = List(fn.parameters.size) { freshTypeVariable(UndeterminedKind) }
    val returnType = freshTypeVariable(UndeterminedKind)
    val type =
        when(parameterTypes.size) {
            0 -> FunctionType(UnitType, returnType)
            1 -> FunctionType(parameterTypes.first(), returnType)
            else -> FunctionType(SumType(parameterTypes), returnType)
        }

    environment.push()
    for(i in fn.parameters.indices) environment[fn.parameters[i]] = parameterTypes[i]
    environment[fn.name] = type
    val constraints = mutableListOf<Constraint>()
    for(body in fn.body) generateFnBodyConstraints(environment, constraints, returnType, body)
    environment.pop()

    val substitutions = unify(constraints)
    val newType = applySubstitutions(type, substitutions)
    val variablesToGeneralize = newType.freeTypeVariables
    val fnType =
        if(variablesToGeneralize.any()) TypeScheme(variablesToGeneralize, newType)
        else newType
    fn._type = fnType

    for(body in fn.body) updateTypes(body, substitutions)
    printWithTypes(fn)

    environment[fn.name] = fnType
}

fun generateFnBodyConstraints(environment: Environment, constraints: MutableList<Constraint>, retType: Type, fnBody: FnBodyAst) {
    fnBody._type =
        when(fnBody) {
            is LetAst -> generateLetConstraints(environment, constraints, fnBody)
            is ReturnAst -> generateReturnConstraints(environment, constraints, retType, fnBody)
        }
}


fun generateLetConstraints(environment: Environment, constraints: MutableList<Constraint>, let: LetAst): Type {
    val t = generateExpressionConstraints(environment, constraints, let.expression)
    environment[let.name] = t
    return t
}

fun generateReturnConstraints(environment: Environment, constraints: MutableList<Constraint>, retType: Type, ret: ReturnAst): Type {
    val t = generateExpressionConstraints(environment, constraints, ret.expression)
    constraints.add(Constraint(retType, t))
    return t
}

fun generateExpressionConstraints(environment: Environment, constraints: MutableList<Constraint>, expression: Expression): Type {
    val t =
        when(expression) {
            is FnCallAst -> generateFnCallConstraints(environment, constraints, expression)

            is ComponentSelectionAst -> generateComponentSelectionConstraints(environment, constraints, expression)

            is EntityDefinitionAst -> generateEntityDefinitionConstraints(environment, constraints, expression)
            is EntitySelectionAst -> generateEntitySelectionConstraints(environment, constraints, expression)
            is EntityRestrictionAst -> generateEntityRestritionConstraints(environment, constraints, expression)

            is NameAst -> generateNameConstraints(environment, expression)
            is IntConstantAst -> IntType
            is FloatConstantAst -> FloatType
            is StringConstantAst -> StringType
            is CharConstantAst -> CharType
        }
    expression._type = t
    return t
}

fun generateNameConstraints(environment: Environment, name: NameAst) = instantiate(environment[name.name])

fun generateFnCallConstraints(environment: Environment, constraints: MutableList<Constraint>, fnCall: FnCallAst): Type {
    val retType = freshTypeVariable(UndeterminedKind)
    val ft = generateExpressionConstraints(environment, constraints, fnCall.fn)
    val args = fnCall.arguments.map { generateExpressionConstraints(environment, constraints, it) }
    val argTypes =
        when(args.size) {
            0 -> UnitType
            1 -> args.first()
            else -> SumType(args)
        }
    constraints.add(Constraint(ft, FunctionType(argTypes, retType)))
    return retType
}

fun generateEntityDefinitionConstraints(environment: Environment, constraints: MutableList<Constraint>, definition: EntityDefinitionAst): Type =
    if(definition.expressions.isEmpty()) ClosedEntityType(setOf())
    else {
        val components = mutableSetOf<Type>()

        for(e in definition.expressions.dropLast(1)) {
            val t = generateExpressionConstraints(environment, constraints, e)
            val componentType = OpenComponentType(mapOf(), freshTypeVariable(ComponentKind))
            constraints.add(Constraint(componentType, t))
            components.add(componentType)
        }

        val l = definition.expressions.last()
        val t = generateExpressionConstraints(environment, constraints, l)
        val row = freshTypeVariable(EntityOrComponentKind)
        val entityType = OpenEntityType(components, row)

        constraints.add(Constraint(row, t))
        entityType
    }

fun generateEntitySelectionConstraints(environment: Environment, constraints: MutableList<Constraint>, selection: EntitySelectionAst): Type {
    val entityType = generateExpressionConstraints(environment, constraints, selection.entity)
    val outputType = environment.lookupType(selection.label)
    val inputType = OpenEntityType(setOf(outputType), freshTypeVariable(EntityOrComponentKind))
    constraints.add(Constraint(entityType, inputType))
    return outputType
}

fun generateEntityRestritionConstraints(environment: Environment, constraints: MutableList<Constraint>, restriction: EntityRestrictionAst): Type {
    val f = freshTypeVariable(EntityOrComponentKind)
    val inputType = OpenEntityType(setOf(environment.lookupType(restriction.label)), f)
    val t = generateExpressionConstraints(environment, constraints, restriction.entity)
    constraints.add(Constraint(inputType, t))
    return f
}

fun generateComponentSelectionConstraints(environment: Environment, constraints: MutableList<Constraint>, selection: ComponentSelectionAst): Type {
    val t = generateExpressionConstraints(environment, constraints, selection.component)
    val outputType = freshTypeVariable(UndeterminedKind)
    val inputType = OpenComponentType(mapOf(selection.label.value to outputType), freshTypeVariable(ComponentKind))
    constraints.add(Constraint(t, inputType))
    return outputType
}