package typechecker

import parser.*

var unificationVarNumber: Long = 0
fun freshUnificationVariable() = UnificationTypeVariable("${unificationVarNumber++}")

fun typesToType(types: List<Type>) = //todo find better name for this
    when(types.size) {
        0 -> UnitType  //should probably be empty type
        1 -> types.first()
        else -> ProductType(types)
    }

fun generateBlockEnvironment(block: BlockAst): BlockEnvironment {
    if(block.nodes.isEmpty()) return BlockEnvironment.empty

    val outSubordinates = mutableListOf<BlockSubordinates>()
    var substitution = Substitution.empty
    var outBindings = Bindings.empty
    var outRequirements = Requirements.empty
    var outCompatibilities = Compatibilities.empty
    val outFreeUserDefinedTypeVariable = mutableSetOf<UserDefinedTypeVariable>()

    for(node in block.nodes) {
        val environment =
            when(node) {
                is BodyAst -> node.environment
                is BlockAst -> node.environment
                else -> throw Exception()
            }
        outSubordinates.add(environment)
        substitution = environment.requirements.fold(substitution) { acc0, token, types ->
            val typeScheme = outBindings[token]
            if(typeScheme == null) {
                outCompatibilities += environment.compatibilities
                acc0
            } else types.fold(acc0) { acc1, type ->
                val (instantiated, compatibilities) = instantiate(typeScheme, environment.compatibilities)
                outCompatibilities += compatibilities
                acc1.unify(type, instantiated)
            }
        }
        outBindings += environment.bindings
        outRequirements += environment.requirements.filter { token, _ -> outBindings.contains(token) }
        outFreeUserDefinedTypeVariable.addAll(environment.freeUserDefinedTypeVariables)
    }

    outRequirements = outRequirements.map { substitution.apply(it) }

    return BlockEnvironment(outBindings, outRequirements, Compatibilities.empty, outFreeUserDefinedTypeVariable, outSubordinates)
}

fun generateIfEnvironment(ifAst: IfAst): BlockEnvironment {
    if(ifAst.conditionIndex == -1) return BlockEnvironment.empty
    if(ifAst.blockIndex == -1) return BlockEnvironment.empty

    val cEnvironment = ifAst.condition().environment
    val bEnvironment = generateBlockEnvironment(ifAst.block())

    val substitution = Substitution.empty.unify(cEnvironment.type, BooleanType)
    val requirements = cEnvironment.requirements.map { substitution.apply(it) } + bEnvironment.requirements

    return BlockEnvironment(Bindings.empty, requirements, bEnvironment.compatibilities, bEnvironment.freeUserDefinedTypeVariables, listOf(cEnvironment, bEnvironment))
}

fun generateLetEnvironment(let: LetAst): BlockEnvironment {
    if(let.nameIndex == -1) return BlockEnvironment.empty
    if(let.expressionIndex == -1) return BlockEnvironment.empty
    val environment = let.expression().environment
    val freeUserDefinedTypeVariable = mutableSetOf<UserDefinedTypeVariable>()
    val requirements: Requirements
    val type: TypeScheme
    val compatibilities: Compatibilities
    if(let.typeAnnotationIndex == -1) {
        requirements = environment.requirements
        val (t, c) = generalize(environment.type)
        type = t
        compatibilities = c
    } else {
        val annotation = tokenToType(let.typeAnnotation())
        if(annotation is UserDefinedTypeVariable) freeUserDefinedTypeVariable.add(annotation)
        val substitution = Substitution.empty.unify(annotation, environment.type)
        requirements = environment.requirements.map { substitution.apply(it) }
        type = TypeScheme(emptyList(), annotation)
        compatibilities = Compatibilities.empty
    }

    return BlockEnvironment(Bindings.empty.addBinding(let.name(), type), requirements, compatibilities, freeUserDefinedTypeVariable, listOf(environment))
}

fun generateReturnEnvironment(returnAst: ReturnAst): BlockEnvironment {
    if(returnAst.expressionIndex == -1) return BlockEnvironment.empty
    val environment = returnAst.expression().environment
    val requirements = environment.requirements.addRequirement(ReturnTypeToken, environment.type)
    return BlockEnvironment(Bindings.empty, requirements, Compatibilities.empty, emptySet(), listOf(environment))
}

fun generateFunctionCallExpressionEnvironment(fnCall: FunctionCallExpression): ExpressionEnvironment {
    if(fnCall.argumentsIndex == -1) return ExpressionEnvironment.empty
    val fEnvironment = fnCall.function().environment

    val argTypes = mutableListOf<Type>()
    var requirements = Requirements.empty
    val subordinates = mutableListOf<ExpressionEnvironment>()

    fnCall.arguments().inOrder {
        val environment = it.expression().environment
        requirements += environment.requirements
        argTypes.add(environment.type)
        subordinates.add(environment)
    }

    val retType = freshUnificationVariable()
    //todo, start handling unification errors
    val substitution = Substitution.empty.unify(fEnvironment.type, ArrowType(typesToType(argTypes), retType))
    requirements += fEnvironment.requirements.map { substitution.apply(it) }
    return ExpressionEnvironment(retType, requirements, subordinates)
}

fun generateBinaryExpressionEnvironment(binaryExpression: BinaryExpression): ExpressionEnvironment {
    if(binaryExpression.rhsIndex == -1) return ExpressionEnvironment.empty
    val lEnvironment = binaryExpression.lhs().environment
    val rEnvironment = binaryExpression.rhs().environment

    val retType = freshUnificationVariable()
    val opType = ArrowType(ProductType(listOf(lEnvironment.type, rEnvironment.type)), retType)
    val requirements = (lEnvironment.requirements + rEnvironment.requirements).addRequirement(binaryExpression.op(), opType)
    return ExpressionEnvironment(retType, requirements, listOf(lEnvironment, rEnvironment))
}

fun generateUnaryExpressionEnvironment(unaryExpression: UnaryExpression): ExpressionEnvironment {
    if(unaryExpression.expressionIndex == -1) return ExpressionEnvironment.empty
    val environment = unaryExpression.expression().environment

    val retType = freshUnificationVariable()
    val requirements = environment.requirements.addRequirement(unaryExpression.op(), ArrowType(environment.type, retType))
    return ExpressionEnvironment(retType, requirements, listOf(environment))
}

fun generateGroupingExpressionEnvironment(groupingExpression: GroupingExpression) =
    if(groupingExpression.expressionIndex == -1) ExpressionEnvironment.empty
    else {
        val environment = groupingExpression.expression().environment
        environment.copy(subordinates = listOf(environment))
    }

fun generateAtomicExpressionEnvironment(a: AtomicExpression) =
    when(val atom = a.atom()) {
        is NameToken -> {
            val type = freshUnificationVariable()
            val requirements = Requirements.empty.addRequirement(atom, type)
            ExpressionEnvironment.empty.copy(type = type, requirements = requirements)
        }
        is IntToken -> ExpressionEnvironment.empty.copy(type = getIntType(atom))
        is FloatToken -> ExpressionEnvironment.empty.copy(type = getFloatType(atom))
        is CharToken -> ExpressionEnvironment.empty.copy(type = CharacterType)
        is StringToken -> ExpressionEnvironment.empty.copy(type = StringType)
        is BadCharToken -> ExpressionEnvironment.empty
        is BadStringToken -> ExpressionEnvironment.empty
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

fun generalize(type: Type): Pair<TypeScheme, Compatibilities> {
    val typeVariables = type.freeTypeVariables.toList().sortedBy { it.n }
    val compatibilities = Compatibilities.empty.addCompatibilities(typeVariables.filterIsInstance<UnificationTypeVariable>())
    return Pair(TypeScheme(typeVariables, type), compatibilities)
}

fun instantiate(typeScheme: TypeScheme, compatibilities: Compatibilities): Pair<Type, Compatibilities> {
    //todo really test compatibilities
    var outCompatibilities = compatibilities
    val mapping =
        typeScheme.boundVariables.associateWith {
            val fresh = freshUnificationVariable()
            if(it is UnificationTypeVariable) outCompatibilities = outCompatibilities.makeCompatible(it, fresh)
            fresh
        }

    fun inst(type: Type): Type =
        when(type) {
            is TypeVariable -> mapping[type] ?: throw Exception()
            is ArrowType -> ArrowType(inst(type.lhs), inst(type.rhs))
            is ProductType -> ProductType(type.types.map { inst(it) })
            else -> type
        }

    return Pair(inst(typeScheme.type), outCompatibilities)
}