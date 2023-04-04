package typechecker

import parser.*

class Environment private constructor(private val backingMap: Map<String, Type>/*, val freeTypeVariables: Set<TypeVariable>*/) {
    constructor(): this(mapOf()/*, setOf()*/)
    val freeTypeVariables: Set<TypeVariable>
        get() = backingMap.values.fold(setOf()) { acc, type -> acc + type.freeTypeVariables }

    operator fun plus(rhs: Pair<Name, Type>) = Environment(backingMap + Pair(rhs.first.name.value, rhs.second)/*, freeTypeVariables + rhs.second.freeTypeVariables*/)
    operator fun plus(rhs: Environment) = Environment(backingMap + rhs.backingMap/*, freeTypeVariables + rhs.second.freeTypeVariables*/)
    operator fun plus(rhs: List<Pair<Name, Type>>) = Environment(backingMap + rhs.map{ Pair(it.first.name.value, it.second) }/*, freeTypeVariables + rhs.second.freeTypeVariables*/)
    operator fun get(key: Name) = backingMap[key.name.value] ?: throw Exception("Name not bound $key")

    fun applySubstitutions(substitutions: Map<TypeVariable, Type>) =
        Environment(backingMap.mapValues { applySubstitutions(it.value, substitutions) })
}

data class Constraint(val lhs: Type, val rhs: Type)

var nextTypeIndex = 0
fun freshTypeVariable() = TypeVariable(nextTypeIndex++)

data class FnConstraints(val constraints: List<Constraint>, val environment: Environment)

fun generateFnConstraints(environment: Environment, fn: Fn): FnConstraints {
    val parameterTypes = List(fn.parameters.size) { freshTypeVariable() }
    var env = environment + fn.parameters.zip(parameterTypes)
    val constraints = mutableListOf<Constraint>()
    val returnType = freshTypeVariable()

    for(body in fn.body) {
        val (c, e) = generateFnBodyConstraints(env, returnType, body)
        env += e
        constraints.addAll(c)
    }

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



    val substitutions = unify(constraints)
    val newType = applySubstitutions(type, substitutions)
    val newEnvironment = environment.applySubstitutions(substitutions)
    val variablesToGeneralize = newType.freeTypeVariables - newEnvironment.freeTypeVariables
    val typeScheme = variablesToGeneralize.fold(newType) { acc, t -> TypeScheme(t, acc) }
    fn._type = typeScheme

    for(body in fn.body) updateTypes(body, substitutions)

    return FnConstraints(constraints, newEnvironment + Pair(fn.name, typeScheme))
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
        is RecordDefinition -> generateRecordDefinitionConstraints(environment, expression)
        is RecordLabel -> generateRecordLabelConstraints(environment, expression)
        is RecordSelection -> generateRecordSelectionConstraints(environment, expression)
        is RecordRestriction -> generateRecordRestritionConstraints(environment, expression)
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
    val retType = freshTypeVariable()
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

fun generateRecordDefinitionConstraints(environment: Environment, definition: RecordDefinition): ExpressionConstraints {
    val labels = mutableMapOf<String, Type>()
    val constraints = mutableListOf<Constraint>()
    for(l in definition.labels) {
        val (c, t) = generateExpressionConstraints(environment, l.expression)
        constraints.addAll(c)
        labels[l.label.value] = t
    }


    val outputType =
        if(definition.row != null) {
            val record = OpenRecordType(labels, freshTypeVariable())
            val (c, t) = generateExpressionConstraints(environment, definition.row)
            constraints.addAll(c)
            constraints.add(Constraint(t, record.row))
            record
        } else ClosedRecordType(labels)



    //{a=1, b=2, x}
    //{x, y}
    //

    /*val substitutions = unify(constraints)
    val r = applySubstitutions(outputType, substitutions) as OpenRecordType
    val newEnv = environment.applySubstitutions(substitutions)
    return if(newEnv.freeTypeVariables.contains(r.row)) ExpressionConstraints(listOf(), r)
    else ExpressionConstraints(listOf(), ClosedRecordType(r.labels))*/
    //when closing, run unification, check if the row type is free or in the environment, if it's free, return open, else return closed

    definition._type = outputType

    return ExpressionConstraints(constraints, outputType)
}

fun generateRecordLabelConstraints(environment: Environment, label: RecordLabel): ExpressionConstraints {
    val (c, t) = generateExpressionConstraints(environment, label.expression)
    val type = ClosedRecordType(mapOf(label.label.value to t))
    label._type = type
    return ExpressionConstraints(c, type)
}

fun generateRecordSelectionConstraints(environment: Environment, selection: RecordSelection): ExpressionConstraints {
    val (recordConstraints, recordType) = generateExpressionConstraints(environment, selection.record)
    val outputType = freshTypeVariable()
    val otherRecordType = OpenRecordType(mapOf(selection.label.value to outputType), freshTypeVariable())
    selection._type = outputType
    return ExpressionConstraints(recordConstraints + Constraint(recordType, otherRecordType), outputType)
}

fun generateRecordRestritionConstraints(environment: Environment, restriction: RecordRestriction): ExpressionConstraints {
    val f = freshTypeVariable()
    val lt = freshTypeVariable()
    val inputType = OpenRecordType(mapOf(restriction.label.value to lt), f)
    val (recordConstraints, recordType) = generateExpressionConstraints(environment, restriction.record)
    restriction._type = f
    return ExpressionConstraints(recordConstraints + Constraint(inputType, recordType), f)
}