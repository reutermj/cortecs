package typechecker

import parser.*

var unificationVarNumber: Long = 0
fun freshUnificationVariable() = UnificationTypeVariable("${unificationVarNumber++}")

fun generalize(type: Type, substitution: Substitution): Pair<TypeScheme, Substitution> {
    val applied = substitution.apply(type)
    val typeVariables = applied.freeTypeVariables.toList().sortedBy { it.n }
    val outSubstitution = typeVariables.fold(substitution) { acc, typeVar ->
        when(typeVar) {
            is UnificationTypeVariable -> acc.makeCompatibility(typeVar)
            is UserDefinedTypeVariable -> acc
        }
    }
    return Pair(TypeScheme(typeVariables, applied), outSubstitution)
}

fun typesToType(types: List<Type>) = //todo find better name for this
    when(types.size) {
        0 -> UnitType  //should probably be empty type
        1 -> types.first()
        else -> ProductType(types)
    }

fun processParameters(parameters: StarAst<ParameterAst>, substitution: Substitution, requirements: MutableMap<BindableToken, List<Type>>, freeUserDefinedTypeVariables: MutableSet<UserDefinedTypeVariable>): Pair<Type, Substitution> {
    val parameterTypes = mutableListOf<Type>()
    val outSubstitution = parameters.fold(substitution) { sub, parameter ->
        val parameterType =
            if(parameter.typeAnnotationIndex == -1) freshUnificationVariable()
            else tokenToType(parameter.typeAnnotation())
        if(parameterType is UserDefinedTypeVariable) freeUserDefinedTypeVariables.remove(parameterType)
        parameterTypes.add(parameterType)
        requirements.remove(parameter.name())?.fold(sub) { acc, type -> acc.unify(parameterType, type) } ?: sub
    }
    return Pair(typesToType(parameterTypes), outSubstitution)
}

fun processReturn(fn: FunctionAst, substitution: Substitution, requirements: MutableMap<BindableToken, List<Type>>, freeUserDefinedTypeVariables: MutableSet<UserDefinedTypeVariable>): Pair<Type, Substitution> {
    val returnTypes = requirements.remove(ReturnTypeToken)
    val returnType =
        if(fn.returnTypeIndex == -1) {
            if(returnTypes == null) UnitType
            else freshUnificationVariable()
        } else tokenToType(fn.returnType())
    if(returnType is UserDefinedTypeVariable) freeUserDefinedTypeVariables.remove(returnType)
    val outSubstitution = returnTypes?.fold(substitution) { acc, type -> acc.unify(returnType, type) } ?: substitution
    return Pair(returnType, outSubstitution)
}

fun processRecursion(name: NameToken, type: Type, substitution: Substitution, requirements: MutableMap<BindableToken, List<Type>>) =
    requirements.remove(name)?.fold(substitution) { acc, requirement -> acc.unify(type, requirement) } ?: substitution

fun generateFunctionEnvironment(fn: FunctionAst): Environment {
    if(fn.nameIndex == -1) return EmptyEnvironment
    if(fn.parametersIndex == -1) return EmptyEnvironment
    if(fn.blockIndex == -1) return EmptyEnvironment
    val blockEnvironment = fn.block().environment as BlockEnvironment
    val requirements = blockEnvironment.requirements.toMutableMap()
    val freeUserDefinedTypeVariables = blockEnvironment.freeUserDefinedTypeVariables.toMutableSet()
    val (parameterType, parameterSubstitution) = processParameters(fn.parameters(), blockEnvironment.substitution, requirements, freeUserDefinedTypeVariables)
    val (returnType, returnSubstitution) = processReturn(fn, parameterSubstitution, requirements, freeUserDefinedTypeVariables)
    if(freeUserDefinedTypeVariables.any()) TODO()
    val arrow = ArrowType(parameterType, returnType)
    val substitution = processRecursion(fn.name(), arrow, returnSubstitution, requirements)
    val (generalized, outSub) = generalize(arrow, substitution)
    return TopLevelEnvironment(outSub, mapOf(fn.name() to generalized), requirements)
}

fun generateIfEnvironment(ifAst: IfAst): Environment {
    if(ifAst.conditionIndex == -1) return EmptyEnvironment
    if(ifAst.blockIndex == -1) return EmptyEnvironment
    val environment = ifAst.condition().environment + ifAst.block().environment.copy(bindings = emptyMap())
    return environment.copy(substitution = environment.substitution.unify(ifAst.condition().expressionType, BooleanType))
}

fun generateLetEnvironment(let: LetAst): Environment {
    if(let.nameIndex == -1) return EmptyEnvironment
    if(let.expressionIndex == -1) return EmptyEnvironment
    val environment = let.expression().environment
    val freeUserDefinedTypeVariable = mutableSetOf<UserDefinedTypeVariable>()
    val (type, substitution) =
        if(let.typeAnnotationIndex == -1) generalize(let.expression().expressionType, environment.substitution)
        else {
            val annotation = tokenToType(let.typeAnnotation())
            if(annotation is UserDefinedTypeVariable) freeUserDefinedTypeVariable.add(annotation)
            Pair(TypeScheme(emptyList(), annotation), environment.substitution.unify(annotation, let.expression().expressionType))
        }

    return BlockEnvironment(substitution, mapOf(let.name() to type), environment.requirements, freeUserDefinedTypeVariable)
}

fun generateReturnEnvironment(returnAst: ReturnAst): Environment {
    if(returnAst.expressionIndex == -1) return EmptyEnvironment
    return returnAst.expression().environment.addRequirement(ReturnTypeToken, returnAst.expression().expressionType)
}

fun generateFunctionCallExpressionEnvironment(fnCall: FunctionCallExpression): Pair<Type, Environment> {
    if(fnCall.argumentsIndex == -1) return Pair(Invalid, EmptyEnvironment)
    val argTypes = mutableListOf<Type>()
    val environment = fnCall.arguments().fold(fnCall.function().environment) { env, arg ->
        val environment = env + arg.expression().environment //need to access environment before type
        argTypes.add(arg.expression().expressionType)
        environment
    }
    val retType = freshUnificationVariable()
    val substitution = environment.substitution.unify(fnCall.function().expressionType, ArrowType(typesToType(argTypes), retType))
    return Pair(retType, BlockEnvironment(substitution, emptyMap(), environment.requirements, emptySet()))
}

fun generateBinaryExpressionEnvironment(binaryExpression: BinaryExpression): Pair<Type, Environment> {
    if(binaryExpression.rhsIndex == -1) return Pair(Invalid, EmptyEnvironment)
    val lEnvironment = binaryExpression.lhs().environment
    val lType = binaryExpression.lhs().expressionType

    val rEnvironment = binaryExpression.rhs().environment
    val rType = binaryExpression.rhs().expressionType

    val retType = freshUnificationVariable()
    val opType = ArrowType(ProductType(listOf(lType, rType)), retType)
    return Pair(retType, (lEnvironment + rEnvironment).addRequirement(binaryExpression.op(), opType))
}

fun generateUnaryExpressionEnvironment(unaryExpression: UnaryExpression): Pair<Type, Environment> {
    if(unaryExpression.expressionIndex == -1) return Pair(Invalid, EmptyEnvironment)
    val uEnvironment = unaryExpression.expression().environment
    val uType = unaryExpression.expression().expressionType

    val retType = freshUnificationVariable()
    return Pair(retType, uEnvironment.addRequirement(unaryExpression.op(), ArrowType(uType, retType)))
}

fun generateGroupingExpressionEnvironment(groupingExpression: GroupingExpression): Pair<Type, Environment> {
    if(groupingExpression.expressionIndex == -1) return Pair(Invalid, EmptyEnvironment)
    val environment = groupingExpression.expression().environment //type is lateinit and accessing environment initializes type
    return Pair(groupingExpression.expression().expressionType, environment)
}

fun generateAtomicExpressionEnvironment(a: AtomicExpression): Pair<Type, Environment> =
    when(val atom = a.atom()) {
        is NameToken -> {
            val type = freshUnificationVariable()
            Pair(type, EmptyEnvironment.addRequirement(atom, type))
        }
        is IntToken -> Pair(getIntType(atom), EmptyEnvironment)
        is FloatToken -> Pair(getFloatType(atom), EmptyEnvironment)
        is CharToken -> Pair(CharacterType, EmptyEnvironment)
        is StringToken -> Pair(StringType, EmptyEnvironment)
        is BadCharToken -> Pair(Invalid, EmptyEnvironment)
        is BadStringToken -> Pair(Invalid, EmptyEnvironment)
    }

fun getIntType(i: IntToken): Type {
    val isUnsigned = i.value.contains("u", true)
    return when(i.value.last()) {
        'b', 'B' -> if(isUnsigned) U8Type else I8Type
        's', 'S' -> if(isUnsigned) U16Type else I16Type
        'l', 'L' -> if(isUnsigned) U64Type else I64Type
        else -> if(isUnsigned) U32Type else I32Type
    }
}

fun getFloatType(i: FloatToken) =
    when(i.value.last()) {
        'd', 'D' -> F64Type
        else -> F32Type
    }

fun tokenToType(t: TypeAnnotationToken): Type =
    when(t) {
        is TypeToken ->
            when(t.value) {
                "U8" -> U8Type
                "U16" -> U16Type
                "U32" -> U32Type
                "U64" -> U64Type
                "I8" -> I8Type
                "I16" -> I16Type
                "I32" -> I32Type
                "I64" -> I64Type
                "F32" -> F32Type
                "F64" -> F64Type
                "String" -> StringType
                "Character" -> CharacterType
                "Boolean" -> BooleanType
                else -> TODO("User defined type")
            }
        is NameToken -> UserDefinedTypeVariable(t.value)
    }